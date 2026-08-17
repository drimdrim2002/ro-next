package com.ronext.rpdptw.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class TravelMatrix {

    /**
     * self arc: 같은 장소면 이동이 없다 (Domain §4, 2026-08-17 원복). 입력 self 값은 읽지 않는다.
     * 같은 좌표라 GreatCircle 보정을 태워도 0이 나오지만, 분기로 채워 좌표를 아예 보지 않는다 —
     * "입력 self 값을 절대 쓰지 않는다"는 규칙을 코드에 남기려는 것이다 (Stage 2 §6 E4).
     */
    static final int SELF_DISTANCE_M = 0;
    static final int SELF_TIME_SEC = 0;

    private final Map<LocationId, Integer> indexById;
    private final Set<LocationId> locationIds;
    private final int n;
    private final int[] distanceMeter;
    /** 준비된 speed 집합은 이 맵의 키가 곧 답이라 따로 들지 않는다. */
    private final Map<Integer, int[]> timeSecBySpeed;

    private TravelMatrix(
            Map<LocationId, Integer> indexById,
            Set<LocationId> locationIds,
            int n,
            int[] distanceMeter,
            Map<Integer, int[]> timeSecBySpeed) {
        this.indexById = indexById;
        this.locationIds = locationIds;
        this.n = n;
        this.distanceMeter = distanceMeter;
        this.timeSecBySpeed = timeSecBySpeed;
    }

    /**
     * 준비 = 검증 + 누락 보정 + 동결. locations의 모든 장소 쌍에 대해 D·U를 확정한다.
     * 실패(중복 arc, overflow, speed ≤ 0 등)는 IllegalArgumentException — Problem.freeze가
     * ProblemCreationException으로 번역한다.
     */
    public static TravelMatrix prepare(
            Map<LocationId, Location> locations,
            List<TravelEntry> entries,
            Set<Integer> resolvedSpeedsKmH) {
        Objects.requireNonNull(locations, "locations");
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(resolvedSpeedsKmH, "resolvedSpeedsKmH");

        Set<Integer> speeds = new LinkedHashSet<>();
        for (Integer speed : resolvedSpeedsKmH) {
            Objects.requireNonNull(speed, "resolvedSpeedsKmH");
            if (speed <= 0) {
                throw new IllegalArgumentException("resolved speed <= 0: " + speed);
            }
            speeds.add(speed);
        }
        speeds = Set.copyOf(speeds);

        Map<LocationId, Integer> indexById = new LinkedHashMap<>();
        Location[] byIndex = new Location[locations.size()];
        int n = 0;
        for (Map.Entry<LocationId, Location> e : locations.entrySet()) {
            LocationId id = Objects.requireNonNull(e.getKey(), "locations key");
            Location loc = Objects.requireNonNull(e.getValue(), "locations value");
            if (indexById.put(id, n) != null) {
                throw new IllegalArgumentException("duplicate location: " + id);
            }
            byIndex[n] = loc;
            n++;
        }
        indexById = Collections.unmodifiableMap(indexById);

        int[] distance = new int[n * n];
        int[] givenTime = new int[n * n];
        boolean[] given = new boolean[n * n];

        for (TravelEntry entry : entries) {
            Objects.requireNonNull(entry, "entries");
            Integer from = indexById.get(entry.from());
            Integer to = indexById.get(entry.to());
            if (from == null || to == null) {
                continue;
            }
            if (from.intValue() == to.intValue()) {
                continue;
            }
            int slot = from * n + to;
            if (given[slot]) {
                throw new IllegalArgumentException("duplicate arc: " + entry.from() + " -> " + entry.to());
            }
            given[slot] = true;
            distance[slot] = entry.distanceMeter();
            givenTime[slot] = entry.timeSec();
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distance[i * n + j] = SELF_DISTANCE_M;
                    continue;
                }
                if (!given[i * n + j]) {
                    distance[i * n + j] = GreatCircle.distanceMeter(byIndex[i], byIndex[j]);
                }
            }
        }

        Map<Integer, int[]> timeBySpeed = new LinkedHashMap<>();
        for (int speed : speeds) {
            int[] times = new int[n * n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    int slot = i * n + j;
                    if (i == j) {
                        times[slot] = SELF_TIME_SEC;
                    } else if (given[slot]) {
                        times[slot] = givenTime[slot];
                    } else {
                        times[slot] = correctedTimeSec(distance[slot], speed);
                    }
                }
            }
            timeBySpeed.put(speed, times);
        }

        return new TravelMatrix(
                indexById,
                Collections.unmodifiableSet(new LinkedHashSet<>(indexById.keySet())),
                n,
                distance,
                Collections.unmodifiableMap(timeBySpeed));
    }

    public int distanceMeter(LocationId from, LocationId to) {
        return distanceMeter[slot(from, to)];
    }

    public int timeSec(LocationId from, LocationId to, int resolvedSpeedKmH) {
        int[] times = timeSecBySpeed.get(resolvedSpeedKmH);
        if (times == null) {
            throw new IllegalArgumentException("unprepared speed: " + resolvedSpeedKmH);
        }
        return times[slot(from, to)];
    }

    public Set<LocationId> locationIds() {
        return locationIds;
    }

    private int slot(LocationId from, LocationId to) {
        Integer i = indexById.get(Objects.requireNonNull(from, "from"));
        Integer j = indexById.get(Objects.requireNonNull(to, "to"));
        if (i == null || j == null) {
            throw new IllegalArgumentException("unprepared location: " + from + " -> " + to);
        }
        return i * n + j;
    }

    /** U := ceil(D × 3.6 / speed) = ceilDiv(18L·D, 5L·speed). double 경유 없음. */
    static int correctedTimeSec(int distanceMeter, int speedKmH) {
        long time = Math.ceilDiv(18L * distanceMeter, 5L * speedKmH);
        if (time > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("corrected time overflow");
        }
        return (int) time;
    }
}
