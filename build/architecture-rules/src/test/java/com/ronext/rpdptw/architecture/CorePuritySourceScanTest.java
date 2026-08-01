package com.ronext.rpdptw.architecture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CorePuritySourceScanTest {

    private boolean isNegativeArchActive() {
        return getClass().getClassLoader().getResource("com/ronext/rpdptw/core/negative/NegativeCorePurityTestFixture.class") != null;
    }

    private List<Path> getCoreSourceDirs() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path current = cwd;
        Path rpdptwCoreSrc = null;
        Path buildArchRulesDir = null;
        while (current != null) {
            Path candidate = current.resolve("rpdptw").resolve("core").resolve("src").resolve("main").resolve("java");
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                rpdptwCoreSrc = candidate;
                buildArchRulesDir = current.resolve("build").resolve("architecture-rules");
                break;
            }
            current = current.getParent();
        }
        if (rpdptwCoreSrc == null) {
            throw new IllegalStateException("Could not find rpdptw/core/src/main/java starting from " + cwd);
        }
        List<Path> dirs = new ArrayList<>();
        dirs.add(rpdptwCoreSrc);
        if (isNegativeArchActive() && buildArchRulesDir != null) {
            Path negativeFixturesSrc = buildArchRulesDir.resolve("src").resolve("negative-fixtures").resolve("java");
            if (Files.exists(negativeFixturesSrc)) {
                dirs.add(negativeFixturesSrc);
            }
        }
        return dirs;
    }

    private String stripCommentsAndStrings(String code) {
        StringBuilder sb = new StringBuilder();
        int len = code.length();
        boolean inSingleComment = false;
        boolean inMultiComment = false;
        boolean inString = false;
        boolean inChar = false;

        for (int i = 0; i < len; i++) {
            char c = code.charAt(i);
            char next = (i + 1 < len) ? code.charAt(i + 1) : '\0';

            if (inSingleComment) {
                if (c == '\n') {
                    inSingleComment = false;
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            } else if (inMultiComment) {
                if (c == '*' && next == '/') {
                    inMultiComment = false;
                    sb.append("  ");
                    i++;
                } else if (c == '\n') {
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            } else if (inString) {
                if (c == '\\') {
                    sb.append("  ");
                    i++;
                } else if (c == '"') {
                    inString = false;
                    sb.append(' ');
                } else if (c == '\n') {
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            } else if (inChar) {
                if (c == '\\') {
                    sb.append("  ");
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                    sb.append(' ');
                } else if (c == '\n') {
                    sb.append(c);
                } else {
                    sb.append(' ');
                }
            } else {
                if (c == '/' && next == '/') {
                    inSingleComment = true;
                    sb.append("  ");
                    i++;
                } else if (c == '/' && next == '*') {
                    inMultiComment = true;
                    sb.append("  ");
                    i++;
                } else if (c == '"') {
                    inString = true;
                    sb.append(' ');
                } else if (c == '\'') {
                    inChar = true;
                    sb.append(' ');
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Test
    void coreContainsNoProhibitedImpurePatterns() throws IOException {
        List<Path> coreSrcDirs = getCoreSourceDirs();
        List<String> violations = new ArrayList<>();

        Pattern nonFinalStaticPattern = Pattern.compile("^\\s*(?:@[A-Za-z0-9_]+(?:\\([^)]*\\))?\\s+)*(?:public|protected|private|package)?\\s*static\\s+(?!final\\b|class\\b|interface\\b|enum\\b|record\\b)[A-Za-z0-9_<>,\\[\\]\\s]+\\s+[A-Za-z0-9_]+\\s*(=|;)");
        Pattern staticMutableCollectionPattern = Pattern.compile("^\\s*(?:@[A-Za-z0-9_]+(?:\\([^)]*\\))?\\s+)*(?:public|protected|private)?\\s*static\\s+final\\s+.*(?:List|Set|Map|Collection|ArrayList|HashMap|HashSet|LinkedList|TreeMap|TreeSet)\\b.*=\\s*new\\s+");
        Pattern unparameterizedNowPattern = Pattern.compile("\\b(?:Instant|LocalDate|LocalDateTime|ZonedDateTime|LocalTime|OffsetDateTime|OffsetTime)\\s*\\.\\s*now\\s*\\(\\s*\\)");
        Pattern clockSystemPattern = Pattern.compile("\\bClock\\s*\\.\\s*system[A-Za-z0-9_]*\\s*\\(");
        Pattern unseededRandomPattern = Pattern.compile("\\bnew\\s+(?:java\\.util\\.)?Random\\s*\\(\\s*\\)");

        for (Path coreSrcDir : coreSrcDirs) {
            try (Stream<Path> stream = Files.walk(coreSrcDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            String rawContent = Files.readString(p, StandardCharsets.UTF_8);
                            String strippedContent = stripCommentsAndStrings(rawContent);
                            String[] lines = strippedContent.split("\r?\n", -1);

                            for (int i = 0; i < lines.length; i++) {
                                String line = lines[i];
                                String trimmed = line.trim();

                                if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                                    continue;
                                }

                                if (line.contains("System.getenv")) {
                                    violations.add("Prohibited System.getenv access in " + p + ":" + (i + 1));
                                }
                                if (line.contains("System.getProperty")) {
                                    violations.add("Prohibited System.getProperty access in " + p + ":" + (i + 1));
                                }
                                if (line.contains("System.currentTimeMillis")) {
                                    violations.add("Prohibited System.currentTimeMillis access in " + p + ":" + (i + 1));
                                }
                                if (unparameterizedNowPattern.matcher(line).find()) {
                                    violations.add("Prohibited un-parameterized .now() access in " + p + ":" + (i + 1));
                                }
                                if (clockSystemPattern.matcher(line).find()) {
                                    violations.add("Prohibited Clock.system access in " + p + ":" + (i + 1));
                                }
                                if (unseededRandomPattern.matcher(line).find()) {
                                    violations.add("Prohibited unseeded Random instantiation in " + p + ":" + (i + 1));
                                }
                                if (line.contains("ThreadLocalRandom")) {
                                    violations.add("Prohibited ThreadLocalRandom access in " + p + ":" + (i + 1));
                                }

                                if (nonFinalStaticPattern.matcher(line).find()) {
                                    violations.add("Prohibited non-final static field in " + p + ":" + (i + 1) + ": " + trimmed);
                                }
                                if (staticMutableCollectionPattern.matcher(line).find()) {
                                    violations.add("Prohibited static mutable collection in " + p + ":" + (i + 1) + ": " + trimmed);
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            }
        }

        if (!violations.isEmpty()) {
            Assertions.fail("Core purity violations found:\n" + String.join("\n", violations));
        }
    }
}
