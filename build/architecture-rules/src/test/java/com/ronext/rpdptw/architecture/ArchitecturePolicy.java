package com.ronext.rpdptw.architecture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class ArchitecturePolicy {
    static final List<String> STABLE_MODULE_PATHS = List.of(
            "rpdptw/core",
            "rpdptw/solver",
            "rpdptw/verification",
            "rpdptw/application",
            "rpdptw/capabilities",
            "rpdptw/profile-catalog");

    static final Set<String> FORBIDDEN_STABLE_REFERENCES = Set.of(
            "com.google.",
            "software.amazon.awssdk",
            "com.amazonaws.",
            "com.azure.",
            "io.kubernetes.",
            "com.google.ortools",
            "gurobi",
            "com.ronext.optimizer");

    static final Set<String> FORBIDDEN_CORE_AMBIENT_APIS = Set.of(
            "System.getenv(",
            "System.currentTimeMillis(",
            "System.nanoTime(",
            "Instant.now(",
            "Clock.system",
            "Math.random(",
            "new Random(",
            "new SplittableRandom(",
            "RandomGenerator.getDefault(");

    private static final Pattern STATIC_MUTABLE = Pattern.compile(
            "\\bstatic\\s+(?:(?!final\\b)[\\w.<>, ?\\[\\]]+"
                    + "|final\\s+[\\w.]*?(?:Map|List|Set|Collection)<[^;]+>)"
                    + "\\s+\\w+\\s*(?:=|;)");
    private static final Pattern CUSTOMER_CONDITIONAL = Pattern.compile(
            "(?:if\\s*\\([^)]*\\b(?:customerId|customerName|presetName)\\b[^)]*\\)"
                    + "|switch\\s*\\([^)]*\\b(?:customerId|customerName|presetName)\\b[^)]*\\))");

    private ArchitecturePolicy() {
    }

    static Path repository() {
        String configured = System.getProperty("phase00.repoRoot");
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("phase00.repoRoot is required");
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    static Map<String, ModuleModel> targetModels() throws Exception {
        Map<String, ModuleModel> result = new LinkedHashMap<>();
        for (String modulePath : STABLE_MODULE_PATHS) {
            ModuleModel model = readModule(repository().resolve(modulePath).resolve("pom.xml"));
            result.put(model.artifactId(), model);
        }
        return result;
    }

    static ModuleModel readModule(Path pom) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Element project = factory.newDocumentBuilder().parse(pom.toFile()).getDocumentElement();
        String artifactId = directChildText(project, "artifactId");
        String packaging = directChildText(project, "packaging");
        if (packaging == null) {
            packaging = "jar";
        }
        List<Dependency> dependencies = new ArrayList<>();
        Element dependenciesElement = directChild(project, "dependencies");
        if (dependenciesElement != null) {
            for (Element dependency : directChildren(dependenciesElement, "dependency")) {
                dependencies.add(new Dependency(
                        directChildText(dependency, "groupId"),
                        directChildText(dependency, "artifactId"),
                        defaultValue(directChildText(dependency, "scope"), "compile"),
                        defaultValue(directChildText(dependency, "type"), "jar"),
                        directChildText(dependency, "classifier")));
            }
        }
        return new ModuleModel(pom, artifactId, packaging, List.copyOf(dependencies));
    }

    static List<Path> javaSources(Path module) throws IOException {
        Path sourceRoot = module.resolve("src/main/java");
        if (!Files.isDirectory(sourceRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    static List<Path> classFiles(Path module) throws IOException {
        Path classRoot = module.resolve("target/classes");
        if (!Files.isDirectory(classRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(classRoot)) {
            return paths.filter(path -> path.toString().endsWith(".class"))
                    .sorted()
                    .toList();
        }
    }

    static List<Violation> forbiddenStableSourceReferences(Path root) throws IOException {
        List<Violation> violations = new ArrayList<>();
        for (String modulePath : STABLE_MODULE_PATHS) {
            for (Path source : javaSources(root.resolve(modulePath))) {
                String executable = stripCommentsAndStrings(Files.readString(source));
                for (String forbidden : FORBIDDEN_STABLE_REFERENCES) {
                    if (executable.contains(forbidden)) {
                        violations.add(new Violation("P00-ARCH-PROVIDER", source, forbidden));
                    }
                }
            }
        }
        return violations;
    }

    static List<Violation> forbiddenStableBytecodeReferences(Path root) throws IOException {
        List<Violation> violations = new ArrayList<>();
        for (String modulePath : STABLE_MODULE_PATHS) {
            for (Path classFile : classFiles(root.resolve(modulePath))) {
                String constantPool = new String(Files.readAllBytes(classFile), StandardCharsets.ISO_8859_1);
                for (String forbidden : FORBIDDEN_STABLE_REFERENCES) {
                    String internalName = forbidden.replace('.', '/');
                    if (constantPool.contains(internalName)) {
                        violations.add(new Violation("P00-ARCH-BYTECODE", classFile, internalName));
                    }
                }
            }
        }
        return violations;
    }

    static List<Violation> coreAmbientLeaks(Path root) throws IOException {
        List<Violation> violations = new ArrayList<>();
        for (Path source : javaSources(root.resolve("rpdptw/core"))) {
            String executable = stripCommentsAndStrings(Files.readString(source));
            for (String forbidden : FORBIDDEN_CORE_AMBIENT_APIS) {
                if (executable.contains(forbidden)) {
                    violations.add(new Violation("P00-ARCH-CORE-AMBIENT", source, forbidden));
                }
            }
            if (STATIC_MUTABLE.matcher(executable).find()) {
                violations.add(new Violation("P00-ARCH-CORE-STATIC-MUTABLE", source, "static mutable field"));
            }
        }
        return violations;
    }

    static List<Violation> crossModuleInternalAccess(Path root) throws IOException {
        Map<String, String> internalOwners = new HashMap<>();
        for (String modulePath : STABLE_MODULE_PATHS) {
            for (Path source : javaSources(root.resolve(modulePath))) {
                String packageName = declaredPackage(Files.readString(source));
                if (packageName != null && packageName.contains(".internal")) {
                    internalOwners.put(packageName, modulePath);
                }
            }
        }

        List<Violation> violations = new ArrayList<>();
        for (String modulePath : STABLE_MODULE_PATHS) {
            for (Path source : javaSources(root.resolve(modulePath))) {
                String executable = stripCommentsAndStrings(Files.readString(source));
                for (Map.Entry<String, String> owner : internalOwners.entrySet()) {
                    if (!owner.getValue().equals(modulePath)
                            && executable.contains(owner.getKey())) {
                        violations.add(new Violation("P00-ARCH-INTERNAL", source, owner.getKey()));
                    }
                }
            }
        }
        return violations;
    }

    static List<Violation> customerConditionals(Path root) throws IOException {
        List<Violation> violations = new ArrayList<>();
        for (String modulePath : List.of("rpdptw/core", "rpdptw/solver", "rpdptw/verification")) {
            for (Path source : javaSources(root.resolve(modulePath))) {
                String executable = stripComments(Files.readString(source));
                if (CUSTOMER_CONDITIONAL.matcher(executable).find()) {
                    violations.add(new Violation("P00-ARCH-CUSTOMER-CONDITIONAL", source, "customer identity conditional"));
                }
            }
        }
        return violations;
    }

    static boolean detectsProviderReference(String source) {
        String executable = stripCommentsAndStrings(source);
        return FORBIDDEN_STABLE_REFERENCES.stream().anyMatch(executable::contains);
    }

    static boolean detectsCoreAmbientLeak(String source) {
        String executable = stripCommentsAndStrings(source);
        return FORBIDDEN_CORE_AMBIENT_APIS.stream().anyMatch(executable::contains)
                || STATIC_MUTABLE.matcher(executable).find();
    }

    static boolean detectsCustomerConditional(String source) {
        return CUSTOMER_CONDITIONAL.matcher(stripComments(source)).find();
    }

    static boolean detectsCapabilityAdvertisement(String path, String source) {
        String normalizedPath = path.replace('\\', '/');
        String executable = stripCommentsAndStrings(source);
        return normalizedPath.contains("META-INF/services")
                || executable.contains("RouteSelectionCapability")
                || executable.contains("RouteSelectionProvider")
                || executable.contains("advertiseRouteSelection");
    }

    static boolean detectsCrossModuleInternal(String originSource, String targetInternalPackage) {
        return stripCommentsAndStrings(originSource).contains(targetInternalPackage);
    }

    static boolean detectsTestFixtureProductionLeak(Dependency dependency) {
        return "rpdptw-test-fixtures".equals(dependency.artifactId())
                && (!"test".equals(dependency.scope())
                || !"test-jar".equals(dependency.type())
                || !"tests".equals(dependency.classifier()));
    }

    static Set<String> directRonextDependencies(ModuleModel module) {
        Set<String> result = new LinkedHashSet<>();
        for (Dependency dependency : module.dependencies()) {
            if ("com.ronext".equals(dependency.groupId())
                    && !"test".equals(dependency.scope())) {
                result.add(dependency.artifactId());
            }
        }
        return Set.copyOf(result);
    }

    static boolean hasCycle(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> active = new HashSet<>();
        for (String node : graph.keySet()) {
            if (visit(node, graph, visited, active)) {
                return true;
            }
        }
        return false;
    }

    private static boolean visit(
            String node,
            Map<String, Set<String>> graph,
            Set<String> visited,
            Set<String> active) {
        if (active.contains(node)) {
            return true;
        }
        if (!visited.add(node)) {
            return false;
        }
        active.add(node);
        for (String next : graph.getOrDefault(node, Set.of())) {
            if (graph.containsKey(next) && visit(next, graph, visited, active)) {
                return true;
            }
        }
        active.remove(node);
        return false;
    }

    static List<Path> findFiles(Path root, Predicate<Path> predicate) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(predicate)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    static String stripCommentsAndStrings(String source) {
        String withoutComments = stripComments(source);
        return withoutComments
                .replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"")
                .replaceAll("'(?:\\\\.|[^'\\\\])*'", "''");
    }

    static String stripComments(String source) {
        return source
                .replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("(?m)//.*$", " ");
    }

    private static String declaredPackage(String source) {
        var matcher = Pattern.compile("\\bpackage\\s+([\\w.]+)\\s*;").matcher(stripComments(source));
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String directChildText(Element parent, String name) {
        Element child = directChild(parent, name);
        return child == null ? null : child.getTextContent().trim();
    }

    private static Element directChild(Element parent, String name) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && name.equals(element.getLocalName())) {
                return element;
            }
        }
        return null;
    }

    private static List<Element> directChildren(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && name.equals(element.getLocalName())) {
                result.add(element);
            }
        }
        return result;
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    record ModuleModel(Path pom, String artifactId, String packaging, List<Dependency> dependencies) {
    }

    record Dependency(String groupId, String artifactId, String scope, String type, String classifier) {
    }

    record Violation(String ruleId, Path file, String evidence) {
    }
}
