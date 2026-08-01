package com.ronext.rpdptw.architecture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestScopeLeakageArchitectureTest {

    private Path getRpdptwDir() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path current = cwd;
        while (current != null) {
            Path candidate = current.resolve("rpdptw");
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find rpdptw directory starting from " + cwd);
    }

    private boolean isNegativeArchActive() {
        return getClass().getClassLoader().getResource("com/ronext/rpdptw/architecture/negative/NegativeTestScopeLeakageTestFixture.class") != null;
    }

    @Test
    void productionModulesDoNotDependOnTestFixturesInCompileOrRuntimeScope() throws Exception {
        Path rpdptwDir = getRpdptwDir();
        List<String> modules = List.of("core", "solver", "verification", "application");
        List<Path> pomFiles = new ArrayList<>();
        for (String module : modules) {
            Path pomFile = rpdptwDir.resolve(module).resolve("pom.xml");
            if (Files.exists(pomFile)) {
                pomFiles.add(pomFile);
            }
        }

        if (isNegativeArchActive()) {
            Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
            Path current = cwd;
            while (current != null) {
                Path negativePom = current.resolve("build").resolve("architecture-rules").resolve("src").resolve("negative-fixtures").resolve("java").resolve("com").resolve("ronext").resolve("rpdptw").resolve("architecture").resolve("negative").resolve("pom-leakage-fixture.xml");
                if (Files.exists(negativePom)) {
                    pomFiles.add(negativePom);
                    break;
                }
                current = current.getParent();
            }
        }

        List<String> violations = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);

        for (Path pomFile : pomFiles) {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pomFile.toFile());

            NodeList depList = doc.getElementsByTagName("dependency");
            for (int i = 0; i < depList.getLength(); i++) {
                Node node = depList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element depElement = (Element) node;

                if (isInsideDependencyManagement(depElement)) {
                    continue;
                }

                String artifactId = getDirectChildText(depElement, "artifactId");
                if ("rpdptw-test-fixtures".equals(artifactId)) {
                    String scope = getDirectChildText(depElement, "scope");
                    if (!"test".equals(scope) && !"provided".equals(scope)) {
                        violations.add("POM file '" + pomFile.getFileName() + "' includes rpdptw-test-fixtures in non-test scope ('" + (scope.isEmpty() ? "compile" : scope) + "'): " + pomFile);
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            Assertions.fail("Test fixture leakage violations found:\n" + String.join("\n", violations));
        }
    }

    private boolean isInsideDependencyManagement(Node node) {
        Node parent = node.getParentNode();
        while (parent != null) {
            if ("dependencyManagement".equals(parent.getNodeName())) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    private String getDirectChildText(Element element, String tagName) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return "";
    }
}
