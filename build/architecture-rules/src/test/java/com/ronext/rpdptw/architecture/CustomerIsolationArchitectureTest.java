package com.ronext.rpdptw.architecture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CustomerIsolationArchitectureTest {

    private Set<String> loadAllowlist() throws IOException {
        Set<String> tokens = new HashSet<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("customer-identity-allowlist.txt")) {
            if (is == null) {
                throw new IllegalStateException("customer-identity-allowlist.txt not found on classpath");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        tokens.add(line.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        return tokens;
    }

    private List<Path> getGenericModuleSourceDirs() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path current = cwd;
        Path rpdptwDir = null;
        while (current != null) {
            Path candidate = current.resolve("rpdptw");
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                rpdptwDir = candidate;
                break;
            }
            current = current.getParent();
        }
        if (rpdptwDir == null) {
            throw new IllegalStateException("Could not find rpdptw directory starting from " + cwd);
        }

        List<String> modules = List.of("core", "solver", "verification", "application");
        List<Path> sourceDirs = new ArrayList<>();
        for (String module : modules) {
            Path srcMainJava = rpdptwDir.resolve(module).resolve("src").resolve("main").resolve("java");
            if (Files.exists(srcMainJava)) {
                sourceDirs.add(srcMainJava);
            }
        }
        return sourceDirs;
    }

    @Test
    void genericModulesContainNoCustomerSpecificPackage() throws IOException {
        Set<String> allowlist = loadAllowlist();
        List<Path> sourceDirs = getGenericModuleSourceDirs();
        List<String> violations = new ArrayList<>();

        for (Path srcDir : sourceDirs) {
            try (Stream<Path> stream = Files.walk(srcDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            Path relPath = srcDir.relativize(p);
                            int nameCount = relPath.getNameCount();
                            // Examine directory segments (excluding filename)
                            for (int i = 0; i < nameCount - 1; i++) {
                                String segment = relPath.getName(i).toString().toLowerCase(Locale.ROOT);
                                if (segment.startsWith("customer_") || segment.startsWith("client_")) {
                                    String token = segment.substring(segment.indexOf('_') + 1);
                                    if (!allowlist.contains(token)) {
                                        violations.add("Disallowed customer package segment '" + segment + "' in " + p);
                                    }
                                } else if (segment.equals("customer") || segment.equals("customers")) {
                                    // Next segment might be customer name
                                    if (i + 1 < nameCount - 1) {
                                        String nextSeg = relPath.getName(i + 1).toString().toLowerCase(Locale.ROOT);
                                        if (!allowlist.contains(nextSeg)) {
                                            violations.add("Disallowed customer package identity '" + nextSeg + "' in " + p);
                                        }
                                    }
                                }
                            }
                        });
            }
        }

        if (!violations.isEmpty()) {
            Assertions.fail("Customer package violations found:\n" + String.join("\n", violations));
        }
    }

    @Test
    void genericSourceContainsNoDeclaredCustomerIdentityConditional() throws IOException {
        Set<String> allowlist = loadAllowlist();
        List<Path> sourceDirs = getGenericModuleSourceDirs();
        List<String> violations = new ArrayList<>();

        // Match patterns like "customer_xyz" or "client_xyz" or customer conditionals
        Pattern customerPrefixPattern = Pattern.compile("\"(?:customer|client)_([a-zA-Z0-9_-]+)\"", Pattern.CASE_INSENSITIVE);
        Pattern customerEqualsPattern = Pattern.compile("(?:[a-zA-Z0-9_$\\.]|\\(\\))*\\b[a-zA-Z0-9_$]*(?:customer|client)[a-zA-Z0-9_$]*\\b(?:[a-zA-Z0-9_$\\.]|\\(\\))*\\s*\\.\\s*equals(?:IgnoreCase)?\\s*\\(\\s*\"([^\"]+)\"\\s*\\)", Pattern.CASE_INSENSITIVE);
        Pattern yodaEqualsPattern = Pattern.compile("\"([^\"]+)\"\\s*\\.\\s*equals(?:IgnoreCase)?\\s*\\(\\s*(?:[a-zA-Z0-9_$\\.]|\\(\\))*\\b[a-zA-Z0-9_$]*(?:customer|client)[a-zA-Z0-9_$]*\\b[^\"]*\\)", Pattern.CASE_INSENSITIVE);
        Pattern objectsEquals1Pattern = Pattern.compile("Objects\\s*\\.\\s*equals\\s*\\(\\s*(?:[a-zA-Z0-9_$\\.]|\\(\\))*\\b[a-zA-Z0-9_$]*(?:customer|client)[a-zA-Z0-9_$]*\\b[^\"]*,\\s*\"([^\"]+)\"\\s*\\)", Pattern.CASE_INSENSITIVE);
        Pattern objectsEquals2Pattern = Pattern.compile("Objects\\s*\\.\\s*equals\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*(?:[a-zA-Z0-9_$\\.]|\\(\\))*\\b[a-zA-Z0-9_$]*(?:customer|client)[a-zA-Z0-9_$]*\\b[^\"]*\\)", Pattern.CASE_INSENSITIVE);

        List<Pattern> equalsPatterns = List.of(customerEqualsPattern, yodaEqualsPattern, objectsEquals1Pattern, objectsEquals2Pattern);

        for (Path srcDir : sourceDirs) {
            try (Stream<Path> stream = Files.walk(srcDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            try {
                                List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                                for (int i = 0; i < lines.size(); i++) {
                                    String line = lines.get(i);
                                    if (line.trim().startsWith("//") || line.trim().startsWith("*") || line.trim().startsWith("/*")) {
                                        continue;
                                    }

                                    Matcher m1 = customerPrefixPattern.matcher(line);
                                    while (m1.find()) {
                                        String token = m1.group(1).toLowerCase(Locale.ROOT);
                                        if (!allowlist.contains(token)) {
                                            violations.add("Disallowed customer token '" + token + "' in " + p + ":" + (i + 1));
                                        }
                                    }

                                    for (Pattern ptn : equalsPatterns) {
                                        Matcher m2 = ptn.matcher(line);
                                        while (m2.find()) {
                                            String token = m2.group(1).toLowerCase(Locale.ROOT);
                                            if (!allowlist.contains(token)) {
                                                violations.add("Disallowed customer identity conditional for token '" + token + "' in " + p + ":" + (i + 1));
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        if (!violations.isEmpty()) {
            Assertions.fail("Customer identity source scan violations found:\n" + String.join("\n", violations));
        }
    }
}
