package com.ronext.rpdptw.architecture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RouteSelectionAbsenceTest {

    private Path getRootDir() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path current = cwd;
        while (current != null) {
            Path candidate = current.resolve("rpdptw");
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find root project directory starting from " + cwd);
    }

    @Test
    void backendsDirectoryDoesNotExist() {
        Path rootDir = getRootDir();
        Path backendsDir = rootDir.resolve("backends");
        Path rpdptwBackendsDir = rootDir.resolve("rpdptw").resolve("backends");

        Assertions.assertFalse(Files.exists(backendsDir), "backends directory should not exist at root: " + backendsDir);
        Assertions.assertFalse(Files.exists(rpdptwBackendsDir), "backends directory should not exist under rpdptw: " + rpdptwBackendsDir);
    }

    @Test
    void routeSelectionAndOrToolsNotReferencedInTargetModulesOrPoms() throws IOException {
        Path rootDir = getRootDir();
        List<String> violations = new ArrayList<>();

        List<Path> pathsToScan = List.of(
                rootDir.resolve("rpdptw"),
                rootDir.resolve("pom.xml"),
                rootDir.resolve("build").resolve("architecture-rules").resolve("pom.xml")
        );

        List<String> forbiddenTokens = List.of(
                "route-selection-ortools-cpsat",
                "com.google.ortools"
        );

        for (Path target : pathsToScan) {
            if (!Files.exists(target)) {
                continue;
            }

            if (Files.isRegularFile(target)) {
                scanFileForTokens(target, forbiddenTokens, violations);
            } else {
                try (Stream<Path> stream = Files.walk(target)) {
                    stream.filter(Files::isRegularFile)
                            .filter(p -> {
                                String name = p.getFileName().toString();
                                return name.endsWith(".java") || name.equals("pom.xml") || name.endsWith(".xml");
                            })
                            .forEach(p -> scanFileForTokens(p, forbiddenTokens, violations));
                }
            }
        }

        if (!violations.isEmpty()) {
            Assertions.fail("Forbidden route-selection or OR-Tools references found:\n" + String.join("\n", violations));
        }
    }

    private String stripExclusionsFromXml(String xml) {
        String[] patterns = new String[] {
            "(?s)<plugin>\\s*(?:<groupId>[^<]*</groupId>\\s*)?<artifactId>maven-enforcer-plugin</artifactId>.*?</plugin>",
            "(?s)<bannedDependencies>.*?</bannedDependencies>",
            "(?s)<enforcer>.*?</enforcer>",
            "(?s)<excludes>.*?</excludes>",
            "(?s)<exclude>.*?</exclude>",
            "(?s)<exclusions>.*?</exclusions>",
            "(?s)<exclusion>.*?</exclusion>"
        };
        for (String ptn : patterns) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(ptn).matcher(xml);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String match = m.group();
                StringBuilder spaces = new StringBuilder();
                for (int i = 0; i < match.length(); i++) {
                    char c = match.charAt(i);
                    if (c == '\n') spaces.append('\n');
                    else spaces.append(' ');
                }
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(spaces.toString()));
            }
            m.appendTail(sb);
            xml = sb.toString();
        }
        return xml;
    }

    private void scanFileForTokens(Path file, List<String> forbiddenTokens, List<String> violations) {
        try {
            boolean isPom = file.getFileName().toString().endsWith(".xml");
            String rawContent = Files.readString(file, StandardCharsets.UTF_8);
            String contentToScan = isPom ? stripExclusionsFromXml(rawContent) : rawContent;
            String[] lines = contentToScan.split("\r?\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                for (String token : forbiddenTokens) {
                    if (line.contains(token)) {
                        violations.add("Found forbidden token '" + token + "' in " + file + ":" + (i + 1));
                    }
                }
            }
        } catch (IOException e) {
            // Ignore unreadable binary files
        }
    }
}
