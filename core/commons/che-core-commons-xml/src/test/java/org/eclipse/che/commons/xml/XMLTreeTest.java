/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.xml;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.io.Files.toByteArray;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class XMLTreeTest {

    private static final String XML_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                              "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                              "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                              "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                              "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                              "    <modelVersion>4.0.0</modelVersion>\n" +
                                              "    <parent>\n" +
                                              "        <artifactId>test-parent</artifactId>\n" +
                                              "        <groupId>test-parent-group-id</groupId>\n" +
                                              "        <version>test-parent-version</version>\n" +
                                              "    </parent>\n" +
                                              "    <artifactId>test-artifact</artifactId>\n" +
                                              "    <packaging>jar</packaging>\n" +
                                              "    <name>Test</name>\n" +
                                              "    <configuration>\n" +
                                              "        <items combine.children=\"append\">\n" +
                                              "            <item>parent-1</item>\n" +
                                              "            <item>parent-2</item>\n" +
                                              "            <item>child-1</item>\n" +
                                              "        </items>\n" +
                                              "        <properties combine.self=\"override\">\n" +
                                              "            <childKey>child</childKey>\n" +
                                              "        </properties>\n" +
                                              "    </configuration>\n" +
                                              "    <dependencies>\n" +
                                              "        <dependency>\n" +
                                              "            <groupId>com.google.guava</groupId>\n" +
                                              "            <artifactId>guava</artifactId>\n" +
                                              "            <version>18.0</version>\n" +
                                              "        </dependency>\n" +
                                              "        <!-- Test dependencies -->\n" +
                                              "        <dependency>\n" +
                                              "            <groupId>org.testng</groupId>\n" +
                                              "            <artifactId>testng</artifactId>\n" +
                                              "            <version>6.8</version>\n" +
                                              "            <scope>test</scope>\n" +
                                              "        </dependency>\n" +
                                              "        <dependency>\n" +
                                              "            <groupId>org.mockito</groupId>\n" +
                                              "            <artifactId>mockito-core</artifactId>\n" +
                                              "            <version>1.10.0</version>\n" +
                                              "            <scope>test</scope>\n" +
                                              "        </dependency>\n" +
                                              "    </dependencies>\n" +
                                              "</project>\n";

    @BeforeMethod
    private void resetLineSeparator() {
        System.setProperty("line.separator", "\n");
    }

    @Test(expectedExceptions = XMLTreeException.class)
    public void shouldThrowExceptionWhenXMLContentContainsDoctypeDeclaration() throws Exception {
        String xml = "<?xml version=\"1.0\"?>\n" +
                     "<!DOCTYPE lolz [\n" +
                     "<!ENTITY lol \"lol\">\n" +
                     "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                     "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                     "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                     "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                     "<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                     "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                     "<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                     "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                     "]>\n" +
                     "<lolz>&lol9;</lolz>";

        XMLTree.from(xml);
    }

    @Test
    public void shouldFindSingleText() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final String version = tree.getSingleText("/project/dependencies/dependency[groupId='org.testng']/version");

        assertEquals(version, "6.8");
    }

    @Test
    public void shouldFindEachElementText() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final List<String> artifacts = tree.getText("/project/dependencies/dependency[scope='test']/artifactId");

        assertEquals(artifacts, asList("testng", "mockito-core"));
    }

    @Test
    public void shouldFindElements() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final List<Element> artifacts = tree.getElements("/project/dependencies/dependency[scope='test']/artifactId");

        assertEquals(artifacts.size(), 2);
        assertEquals(asList(artifacts.get(0).getText(), artifacts.get(1).getText()), asList("testng", "mockito-core"));
    }

    @Test
    public void shouldFindAttributeValues() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final List<String> attributes = tree.getText("/project/configuration/properties/@combine.self");

        assertEquals(attributes, asList("override"));
    }

    @Test
    public void shouldBeAbleToGetAttributesUsingModel() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element properties = tree.getSingleElement("/project/configuration/properties");

        assertEquals(properties.getAttributes().size(), 1);
        final Attribute attribute = properties.getAttributes().get(0);
        assertEquals(attribute.getName(), "combine.self");
        assertEquals(attribute.getValue(), "override");
    }

    @Test
    public void shouldBeAbleToGetElementParent() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element artifactId = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/artifactId");

        assertEquals(artifactId.getParent().getName(), "dependency");
    }

    @Test
    public void shouldBeAbleToGetSingleSibling() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element name = tree.getSingleElement("/project/name");

        assertEquals(name.getSingleSibling("packaging").getText(), "jar");
    }

    @Test
    public void shouldReturnNullIfSiblingWithRequestedNameDoesNotExist() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element name = tree.getSingleElement("/project/name");

        assertNull(name.getSingleSibling("developers"));
    }

    @Test
    public void shouldBeAbleToCheckThatElementHasAttribute() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        assertTrue(tree.getSingleElement("//properties").hasAttribute("combine.self"));
    }

    @Test(expectedExceptions = XMLTreeException.class)
    public void shouldThrowExceptionIfMoreThenOnlySiblingWereFound() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependency = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        dependency.getSingleSibling("dependency");
    }

    @Test
    public void shouldBeAbleToGetOnlyChild() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        assertEquals(tree.getRoot().getSingleChild("packaging").getText(), "jar");
    }

    @Test
    public void shouldReturnNullIfChildDoesNotExist() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        assertNull(tree.getRoot().getSingleChild("developers"));
    }

    @Test(expectedExceptions = XMLTreeException.class)
    public void shouldThrowExceptionIfMoreThenOnlyChildWereFound() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependencies = tree.getSingleElement("/project/dependencies");

        dependencies.getSingleChild("dependency");
    }

    @Test
    public void shouldBeAbleToCheckElementHasSibling() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element artifactID = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/artifactId");

        assertTrue(artifactID.hasSibling("groupId"));
        assertTrue(artifactID.hasSibling("version"));
        assertTrue(artifactID.hasSibling("scope"));
        assertFalse(artifactID.hasSibling("artifactId"));
    }

    @Test
    public void shouldBeAbleToGetSibling() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element artifactId = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/artifactId");

        assertEquals(artifactId.getSingleSibling("groupId").getText(), "org.testng");
        assertEquals(artifactId.getSingleSibling("version").getText(), "6.8");
        assertEquals(artifactId.getSingleSibling("scope").getText(), "test");
        assertNull(artifactId.getSingleSibling("other"));
    }

    @Test
    public void shouldBeAbleToGetPreviousSibling() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element scope = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/scope");

        assertEquals(scope.getPreviousSibling().getName(), "version");
    }

    @Test
    public void shouldBeAbleToGetChildText() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final String artifactId = tree.getRoot().getChildText("artifactId");

        assertEquals(artifactId, "test-artifact");
    }

    @Test
    public void shouldReturnNullWhenGettingTextIfChildDoesNotExist() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependency = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        assertNull(dependency.getChildText("scope"));
    }

    @Test
    public void shouldBeAbleToGetChildTextOrDefaultValue() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependency = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        assertEquals(dependency.getChildTextOrDefault("scope", "compile"), "compile");
    }

    @Test
    public void shouldBeAbleToGetNextSibling() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element scope = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/version");

        assertEquals(scope.getNextSibling().getName(), "scope");
    }

    @Test
    public void shouldBeAbleToGetRootElement() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element project = tree.getRoot();

        assertEquals(project.getName(), "project");
        assertFalse(project.hasParent());
    }

    @Test
    public void shouldBeAbleToGetSiblings() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element artifactId = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']/artifactId");

        assertEquals(artifactId.getSiblings().size(), 3);
    }

    @Test
    public void shouldBeAbleToCheckElementHasChild() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element guavaDep = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        assertTrue(guavaDep.hasChild("groupId"));
        assertTrue(guavaDep.hasChild("version"));
        assertTrue(guavaDep.hasChild("artifactId"));
        assertFalse(guavaDep.hasChild("scope"));
    }

    @Test
    public void shouldBeAbleToGetFirstChild() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element guavaDep = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        assertEquals(guavaDep.getSingleChild("groupId").getText(), "com.google.guava");
        assertEquals(guavaDep.getSingleChild("version").getText(), "18.0");
        assertEquals(guavaDep.getSingleChild("artifactId").getText(), "guava");
        assertNull(guavaDep.getSingleChild("scope"));
    }

    @Test
    public void shouldBeAbleToGetChildren() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element guavaDep = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");

        assertEquals(guavaDep.getChildren().size(), 3);
    }

    @Test
    public void shouldBeAbleToChangeElementTextByModel() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element name = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']/version");
        name.setText("new version");

        assertEquals(tree.getSingleText("/project/dependencies/dependency[artifactId='guava']/version"), "new version");
    }

    @Test
    public void shouldBeAbleToChangeElementTextByTree() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.updateText("/project/name", "new name");

        assertEquals(tree.getSingleText("/project/name"), "new name");
    }

    @Test
    public void shouldBeAbleToAppendChildByModel() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element guavaDep = tree.getSingleElement("/project/dependencies/dependency[artifactId='guava']");
        guavaDep.appendChild(NewElement.createElement("scope", "compile"));

        assertTrue(guavaDep.hasChild("scope"));
        assertEquals(guavaDep.getSingleChild("scope").getText(), "compile");
        assertEquals(tree.getSingleText("/project/dependencies/dependency[artifactId='guava']/scope"), "compile");
    }

    @Test
    public void shouldBeAbleToAppendComplexChild() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.getSingleElement("//dependencies")
            .appendChild(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "test-artifact"),
                                                  NewElement.createElement("groupId", "test-group"),
                                                  NewElement.createElement("version", "test-version")));

        final Element dependency = tree.getSingleElement("//dependency[artifactId='test-artifact']");
        assertTrue(dependency.hasChild("artifactId"));
        assertTrue(dependency.hasChild("groupId"));
        assertTrue(dependency.hasChild("version"));
    }

    @Test
    public void shouldBeAbleToInsertElementAfterExisting() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element name = tree.getSingleElement("/project/name");
        name.insertAfter(NewElement.createElement("description", "This is test pom.xml"));

        assertTrue(name.hasSibling("description"));
        assertEquals(name.getNextSibling().getText(), "This is test pom.xml");
        assertEquals(tree.getSingleText("/project/description"), "This is test pom.xml");
    }

    @Test
    public void shouldBeAbleToInsertElementBeforeExisting() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element name = tree.getSingleElement("/project/name");
        name.insertBefore(NewElement.createElement("description", "This is test pom.xml"));

        assertTrue(name.hasSibling("description"));
        assertEquals(name.getPreviousSibling().getText(), "This is test pom.xml");
        assertEquals(tree.getSingleText("/project/description"), "This is test pom.xml");
    }

    @Test
    public void shouldBeAbleToInsertChildAfterSpecifiedElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .insertChild(NewElement.createElement("packaging", "jar"), after("artifactId"));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToInsertChildBeforeSpecifiedElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .insertChild(NewElement.createElement("modelVersion", "4.0.0"), before("artifactId"));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToInsertChildInTheStartOfChildrenList() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .insertChild(NewElement.createElement("modelVersion", "4.0.0"), before("artifactId").or(inTheBegin()));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToInsertChildInTheEndOfChildrenList() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .insertChild(NewElement.createElement("groupId", "test-group-id"), after("artifactId").or(inTheEnd()));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <name>Test</name>\n" +
                                      "    <groupId>test-group-id</groupId>\n" +
                                      "</project>");
    }

    @Test(expectedExceptions = XMLTreeException.class)
    public void shouldThrowExceptionIfNotPossibleToInsertElementInSpecifiedPlace() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .insertChild(NewElement.createElement("groupId", "test-group-id"), after("artifactId").or(after("version"))
                                                                                                  .or(after("parent"))
                                                                                                  .or(after("build")));
    }

    @Test
    public void shouldBeAbleToReplaceElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <dependencies>\n" +
                                          "        <!-- Test dependencies -->\n" +
                                          "        <dependency>\n" +
                                          "            <groupId>org.testng</groupId>\n" +
                                          "            <artifactId>testng</artifactId>\n" +
                                          "            <version>6.8</version>\n" +
                                          "            <scope>test</scope>\n" +
                                          "        </dependency>\n" +
                                          "    </dependencies>\n" +
                                          "</project>\n");

        tree.getSingleElement("//dependency")
            .replaceWith(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "junit"),
                                                  NewElement.createElement("groupId", "org.junit"),
                                                  NewElement.createElement("version", "4.0")));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <dependencies>\n" +
                                      "        <!-- Test dependencies -->\n" +
                                      "        <dependency>\n" +
                                      "            <artifactId>junit</artifactId>\n" +
                                      "            <groupId>org.junit</groupId>\n" +
                                      "            <version>4.0</version>\n" +
                                      "        </dependency>\n" +
                                      "    </dependencies>\n" +
                                      "</project>\n");
        assertEquals(tree.getElements("//dependency").size(), 1);
        assertEquals(tree.getSingleElement("//dependencies").getChildren().size(), 1);
    }

    @Test
    public void shouldBeAbleToInsertElementBeforeFirstExisting() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element modelVersion = tree.getSingleElement("/project/modelVersion");
        modelVersion.insertBefore(NewElement.createElement("description", "This is test pom.xml"));

        assertTrue(modelVersion.hasSibling("description"));
        assertEquals(modelVersion.getPreviousSibling().getText(), "This is test pom.xml");
        assertEquals(tree.getSingleText("/project/description"), "This is test pom.xml");
    }

    @Test
    public void shouldBeAbleToRemoveElementByTree() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);
        assertTrue(tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']").hasChild("scope"));

        tree.removeElement("/project/dependencies/dependency[artifactId='testng']/scope");

        assertFalse(tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']").hasChild("scope"));
    }

    @Test
    public void shouldBeAbleToRemoveElementChild() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependency = tree.getSingleElement("/project/dependencies/dependency[artifactId='testng']");
        assertTrue(dependency.hasChild("version"));

        dependency.removeChild("version");
        assertFalse(dependency.hasChild("version"));
    }

    @Test
    public void shouldBeAbleToRemoveChildren() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element dependencies = tree.getSingleElement("/project/dependencies");
        dependencies.removeChildren("dependency");

        assertTrue(dependencies.getChildren().isEmpty());
    }

    @Test
    public void newElementWithPostAddedChildrenAndNewElementConstructedWithChildrenShouldProduceSameTreeBytes() {
        final XMLTree tree1 = XMLTree.from(XML_CONTENT);
        final XMLTree tree2 = XMLTree.from(XML_CONTENT);

        //first tree
        tree1.getSingleElement("//dependencies")
             .appendChild(NewElement.createElement("dependency",
                                                   NewElement.createElement("artifactId", "test-artifact"),
                                                   NewElement.createElement("groupId", "test-group"),
                                                   NewElement.createElement("version", "test-version")));
        //second tree
        final NewElement dependency = NewElement
                .createElement("dependency").appendChild(NewElement.createElement("artifactId", "test-artifact"))
                .appendChild(NewElement.createElement("groupId", "test-group"))
                .appendChild(NewElement.createElement("version", "test-version"));
        tree2.getSingleElement("//dependencies")
             .appendChild(dependency);

        assertEquals(tree2.toString(), tree1.toString());
    }

    @Test
    public void chainRemovingAndBatchRemovingShouldProduceSameTreeBytes() {
        final XMLTree tree1 = XMLTree.from(XML_CONTENT);
        final XMLTree tree2 = XMLTree.from(XML_CONTENT);

        //removing dependencies from first tree
        tree1.removeElement("/project/dependencies/dependency[3]");
        tree1.removeElement("/project/dependencies/dependency[2]");
        tree1.removeElement("/project/dependencies/dependency[1]");
        //removing dependencies from second tree
        tree2.getSingleElement("//dependencies").removeChildren("dependency");

        //use strings for assertion to quick review difference if assertion failed
        assertEquals(tree1.toString(), tree2.toString());
    }

    @Test
    public void removeInsertedElementShouldProduceSameTreeBytes() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element description = tree.getRoot()
                                        .getLastChild()
                                        .insertAfter(NewElement.createElement("description", "description"))
                                        .getSingleSibling("description");
        description.remove();

        assertEquals(tree.toString(), XML_CONTENT);
    }

    @Test
    public void removeInsertedAfterElementWithChildrenShouldProduceSameTreeBytes() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.getSingleElement("//dependencies")
            .getFirstChild()
            .insertAfter(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "test-artifact"),
                                                  NewElement.createElement("groupId", "test-group"),
                                                  NewElement.createElement("version", "test-version")))
            .getNextSibling()
            .remove();

        assertEquals(tree.toString(), XML_CONTENT);
    }

    @Test
    public void removeAppendedElementWithChildrenShouldProduceSameTreeBytes() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.getSingleElement("//dependencies")
            .appendChild(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "test-artifact"),
                                                  NewElement.createElement("groupId", "test-group"),
                                                  NewElement.createElement("version", "test-version")))
            .getLastChild()
            .remove();

        //use strings for assertion to quick review difference if assertion failed
        assertEquals(tree.toString(), XML_CONTENT);
    }

    @Test
    public void removeInsertedBeforeElementWithChildrenShouldProduceSameTreeBytes() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.getSingleElement("//dependencies")
            .getFirstChild()
            .insertBefore(NewElement.createElement("dependency",
                                                   NewElement.createElement("artifactId", "test-artifact"),
                                                   NewElement.createElement("groupId", "test-group"),
                                                   NewElement.createElement("version", "test-version")))
            .getPreviousSibling()
            .remove();

        //use strings for assertion to quick review difference if assertion failed
        assertEquals(tree.toString(), XML_CONTENT);
    }

    @Test
    public void shouldBeAbleToChangeTextOfNewlyInsertedElement() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        tree.getSingleElement("//dependencies")
            .getFirstChild()
            .insertBefore(NewElement.createElement("dependency",
                                                   NewElement.createElement("artifactId", "test-artifact"),
                                                   NewElement.createElement("groupId", "test-group"),
                                                   NewElement.createElement("version", "test-version")));
        tree.updateText("//dependencies/dependency[artifactId='test-artifact']/version", "test-version");

        assertEquals(tree.getSingleText("//dependencies/dependency[artifactId='test-artifact']/version"), "test-version");
    }

    @Test
    public void shouldBeAbleToChangeInsertedElementText() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getRoot()
            .getLastChild()
            .insertBefore(NewElement.createElement("description", "description"))
            .getPreviousSibling()
            .setText("other description");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <description>other description</description>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToRemoveAttribute() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<root>\n" +
                                          "    <level1 attribute=\"value\">text</level1>\n" +
                                          "</root>");

        tree.getSingleElement("//level1")
            .removeAttribute("attribute");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<root>\n" +
                                      "    <level1>text</level1>\n" +
                                      "</root>");
    }

    @Test
    public void shouldBeAbleToChangeAttributeValue() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<root>\n" +
                                          "    <level1 longer=\"value\" long=\"value\">text</level1>\n" +
                                          "</root>");

        tree.getSingleElement("//level1")
            .getAttribute("long")
            .setValue("new value");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<root>\n" +
                                      "    <level1 longer=\"value\" long=\"new value\">text</level1>\n" +
                                      "</root>");
    }

    @Test
    public void shouldBeAbleToAddAttributeToExistingElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project></project>");

        tree.getRoot().setAttribute("xlmns", "http://maven.apache.org/POM/4.0.0");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xlmns=\"http://maven.apache.org/POM/4.0.0\"></project>");
    }

    @Test
    public void shouldBeAbleToAddAttributesWithPrefix() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project></project>");

        tree.getRoot()
            .setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0")
            .setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .setAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");

        tree.getRoot().getAttributes();

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\"></project>");
        assertEquals(tree.getRoot()
                         .getAttributes()
                         .size(), 3);

        //xmlns
        assertTrue(tree.getRoot().hasAttribute("xmlns"));
        final Attribute xmlns = tree.getRoot().getAttribute("xmlns");
        assertEquals(xmlns.getValue(), "http://maven.apache.org/POM/4.0.0");

        //xmlns:xsi
        assertTrue(tree.getRoot().hasAttribute("xmlns:xsi"));
        final Attribute xmlnsXsi = tree.getRoot().getAttribute("xmlns:xsi");
        assertEquals(xmlnsXsi.getValue(), "http://www.w3.org/2001/XMLSchema-instance");
        assertEquals(xmlnsXsi.getPrefix(), "xmlns");

        //xsi:schemaLocation
        assertTrue(tree.getRoot().hasAttribute("xsi:schemaLocation"));
        final Attribute xsiSchemaLocation = tree.getRoot().getAttribute("xsi:schemaLocation");
        assertEquals(xsiSchemaLocation.getValue(), "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
        assertEquals(xsiSchemaLocation.getPrefix(), "xsi");
    }

    @Test
    public void shouldBeAbleToAddAttributeToNewElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "    <dependencies>\n" +
                                          "    </dependencies>\n" +
                                          "</project>");

        tree.getSingleElement("//dependencies")
            .appendChild(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "test-artifact"),
                                                  NewElement.createElement("groupId", "test-group"),
                                                  NewElement.createElement("version", "test-version").setAttribute("attribute1", "value1"))
                                   .setAttribute("attribute1", "value1")
                                   .setAttribute("attribute2", "value2")
                                   .setAttribute("attribute3", "value3"));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "    <dependencies>\n" +
                                      "        <dependency attribute1=\"value1\" attribute2=\"value2\" attribute3=\"value3\">\n" +
                                      "            <artifactId>test-artifact</artifactId>\n" +
                                      "            <groupId>test-group</groupId>\n" +
                                      "            <version attribute1=\"value1\">test-version</version>\n" +
                                      "        </dependency>\n" +
                                      "    </dependencies>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToRemoveAttributes() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        for (Attribute attribute : tree.getRoot().getAttributes()) {
            attribute.remove();
        }

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
        assertTrue(tree.getRoot().getAttributes().isEmpty());
    }

    @Test
    public void shouldNotDestroyFormattingAfterSimpleElementInsertion() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        final Element name = tree.getSingleElement("/project/name");
        name.insertAfter(NewElement.createElement("description", "This is test pom.xml"));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "    <description>This is test pom.xml</description>\n" +
                                      "</project>");
    }

    @Test
    public void shouldNotDestroyFormattingAfterComplexElementInsertion() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.getSingleElement("//name")
            .insertAfter(NewElement.createElement("dependencies", NewElement.createElement("dependency",
                                                                                           NewElement.createElement("artifactId",
                                                                                                                    "test-artifact"),
                                                                                           NewElement
                                                                                                   .createElement("groupId", "test-group"),
                                                                                           NewElement.createElement("version",
                                                                                                                    "test-version"))));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "    <dependencies>\n" +
                                      "        <dependency>\n" +
                                      "            <artifactId>test-artifact</artifactId>\n" +
                                      "            <groupId>test-group</groupId>\n" +
                                      "            <version>test-version</version>\n" +
                                      "        </dependency>\n" +
                                      "    </dependencies>\n" +
                                      "</project>");
        assertEquals(tree.getSingleText("/project/dependencies/dependency/artifactId"), "test-artifact");
    }

    @Test
    public void shouldNotDestroyFormattingAfterRemovingElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "    <dependencies>\n" +
                                          "        <dependency>\n" +
                                          "            <artifactId>test-artifact</artifactId>\n" +
                                          "            <groupId>test-group</groupId>\n" +
                                          "            <version>test-version</version>\n" +
                                          "            <scope>compile</scope>\n" +
                                          "        </dependency>\n" +
                                          "    </dependencies>\n" +
                                          "</project>");

        tree.removeElement("/project/dependencies/dependency[1]/scope");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "    <dependencies>\n" +
                                      "        <dependency>\n" +
                                      "            <artifactId>test-artifact</artifactId>\n" +
                                      "            <groupId>test-group</groupId>\n" +
                                      "            <version>test-version</version>\n" +
                                      "        </dependency>\n" +
                                      "    </dependencies>\n" +
                                      "</project>");
    }

    @Test
    public void shouldNotDestroyFormattingAfterRemovingElementWithChildren() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "    <dependencies>\n" +
                                          "        <dependency>\n" +
                                          "            <artifactId>test-artifact</artifactId>\n" +
                                          "            <groupId>test-group</groupId>\n" +
                                          "            <version>test-version</version>\n" +
                                          "            <scope>compile</scope>\n" +
                                          "        </dependency>\n" +
                                          "    </dependencies>\n" +
                                          "</project>");

        tree.removeElement("/project/dependencies");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "</project>");
    }

    @Test
    public void batchUpdateShouldProduceExpectedContent() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        //removing parent
        tree.removeElement("//parent");
        //removing configuration
        tree.getSingleElement("//configuration").remove();
        //adding groupId before artifactId and version after
        tree.getSingleElement("/project/artifactId")
            .insertBefore(NewElement.createElement("groupId", "test-group"))
            .insertAfter(NewElement.createElement("version", "test-version"));
        //delete all test dependencies
        for (Element element : tree.getElements("//dependency[scope='test']")) {
            element.remove();
        }
        //adding junit dependency to the end of dependencies list
        tree.getSingleElement("//dependencies")
            .appendChild(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "junit"),
                                                  NewElement.createElement("groupId", "junit"),
                                                  NewElement.createElement("version", "4.0")));
        //change junit version
        tree.updateText("//dependency[artifactId='junit']/version", "4.1");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <groupId>test-group</groupId>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <version>test-version</version>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <name>Test</name>\n" +
                                      "    <dependencies>\n" +
                                      "        <dependency>\n" +
                                      "            <groupId>com.google.guava</groupId>\n" +
                                      "            <artifactId>guava</artifactId>\n" +
                                      "            <version>18.0</version>\n" +
                                      "        </dependency>\n" +
                                      "        <!-- Test dependencies -->\n" +
                                      "        <dependency>\n" +
                                      "            <artifactId>junit</artifactId>\n" +
                                      "            <groupId>junit</groupId>\n" +
                                      "            <version>4.1</version>\n" +
                                      "        </dependency>\n" +
                                      "    </dependencies>\n" +
                                      "</project>\n");
    }

    @Test
    public void shouldBeAbleToAppendChildToEmptyElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                          "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                          "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "    <dependencies></dependencies>\n" +
                                          "</project>");

        tree.getSingleElement("//dependencies")
            .appendChild(NewElement.createElement("dependency",
                                                  NewElement.createElement("artifactId", "test-artifact"),
                                                  NewElement.createElement("groupId", "test-group"),
                                                  NewElement.createElement("version", "test-version")));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>Test</name>\n" +
                                      "    <dependencies>\n" +
                                      "        <dependency>\n" +
                                      "            <artifactId>test-artifact</artifactId>\n" +
                                      "            <groupId>test-group</groupId>\n" +
                                      "            <version>test-version</version>\n" +
                                      "        </dependency></dependencies>\n" +
                                      "</project>");
    }

    @Test
    public void shouldRemoveCommentIfCommentContainerRemoved() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <dependencies>\n" +
                                          "        <!-- test dependencies -->\n" +
                                          "        <dependency>\n" +
                                          "            <artifactId>test-artifact</artifactId>\n" +
                                          "            <groupId>test-group</groupId>\n" +
                                          "            <version>test-version</version>\n" +
                                          "        </dependency>\n" +
                                          "    </dependencies>\n" +
                                          "</project>");

        tree.removeElement("//dependencies");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "</project>");
    }

    @Test
    public void elementsShouldBeShiftedRightAfterElementTextUpdate() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>test-artifact</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.updateText("//artifactId", "longer artifact identifier");
        //all elements which are right from artifact id
        //should be shifted right on "new artifact id length" minus "old artifact id length"
        //to check it lets modify elements which are right
        tree.removeElement("//packaging");
        tree.updateText("//name", "new name");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>longer artifact identifier</artifactId>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>new name</name>\n" +
                                      "</project>");
    }

    @Test
    public void elementsShouldBeShiftedLeftAfterElementTextUpdate() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <modelVersion>4.0.0</modelVersion>\n" +
                                          "    <artifactId>long-artifact-identifier</artifactId>\n" +
                                          "    <packaging>jar</packaging>\n" +
                                          "    <!-- project name -->\n" +
                                          "    <name>Test</name>\n" +
                                          "</project>");

        tree.updateText("//artifactId", "smaller-art-id");
        //all elements which are right from artifact id
        //should be shifted left on "old artifact id length" minus "new artifact id length"
        //to check it lets modify elements which are right
        tree.removeElement("//packaging");
        tree.updateText("//name", "new name");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <artifactId>smaller-art-id</artifactId>\n" +
                                      "    <!-- project name -->\n" +
                                      "    <name>new name</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToChangeElementEmptyText() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <name></name>\n" +
                                          "</project>");

        tree.updateText("//name", "name");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <name>name</name>\n" +
                                      "</project>");
    }

    @Test
    public void textBeforeElementShouldBeRemovedWithElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<root>text-before<test>text-inside</test>text-after</root>");

        tree.removeElement("//test");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<root>text-after</root>");
    }

    @Test
    public void commentBeforeElementShouldNotBeRemovedWithElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<root><!--comment--><test>text-inside</test>text-after</root>");

        tree.removeElement("//test");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<root><!--comment-->text-after</root>");
    }

    @Test
    public void commentsShouldNotBeRemovedAfterElementTextUpdate() {
        final XMLTree tree = XMLTree.from("<root>" +
                                          "<!--comment1-->" +
                                          "<!--comment2-->" +
                                          "<!--long \n" +
                                          "comment3-->" +
                                          "text" +
                                          "</root>");

        tree.getRoot().setText("new text");

        assertEquals(tree.toString(), "<root>" +
                                      "<!--comment1-->" +
                                      "<!--comment2-->" +
                                      "<!--long \n" +
                                      "comment3-->" +
                                      "new text" +
                                      "</root>");
    }

    @Test
    public void textUpdateShouldNotRemoveElements() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<root>" +
                                          "text-before" +
                                          "<!--comment-->" +
                                          "<test>text-inside</test>" +
                                          "text-after" +
                                          "</root>");

        tree.getRoot().setText("new text");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<root>" +
                                      "new text" +
                                      "<!--comment-->" +
                                      "<test>text-inside</test>" +
                                      "</root>");
    }

    @Test
    public void childrenShouldNotBeRemovedAfterParentTextUpdate() {
        final XMLTree tree = XMLTree.from("<root><inner></inner><inner></inner></root>");

        tree.getRoot().setText("root text");

        assertEquals(tree.toString(), "<root>root text<inner></inner><inner></inner></root>");
        assertEquals(tree.getRoot().getText(), "root text");
    }

    @Test
    public void shouldBeAbleToRemoveVoidElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project name=\"MyProject\" default=\"dist\" basedir=\".\">\n" +
                                          "    <description>\n" +
                                          "        simple example build file\n" +
                                          "    </description>\n" +
                                          "  <!-- set global properties for this build -->\n" +
                                          "  <property name=\"src\" location=\"src\"/>\n" +
                                          "  <property name=\"build\" location=\"build\"/>\n" +
                                          "  <property name=\"dist\"  location=\"dist\"/>\n" +
                                          "  <target name=\"init\">\n" +
                                          "    <!-- Create the time stamp -->\n" +
                                          "    <tstamp/>\n" +
                                          "    <!-- Create the build directory structure used by compile -->\n" +
                                          "    <mkdir dir=\"${build}\"/>\n" +
                                          "  </target>\n" +
                                          "</project>");

        tree.getSingleElement("//property[@name='build']").remove();
        tree.getSingleElement("//property[@name='src']").remove();

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project name=\"MyProject\" default=\"dist\" basedir=\".\">\n" +
                                      "    <description>\n" +
                                      "        simple example build file\n" +
                                      "    </description>\n" +
                                      "  <!-- set global properties for this build -->\n" +
                                      "  <property name=\"dist\"  location=\"dist\"/>\n" +
                                      "  <target name=\"init\">\n" +
                                      "    <!-- Create the time stamp -->\n" +
                                      "    <tstamp/>\n" +
                                      "    <!-- Create the build directory structure used by compile -->\n" +
                                      "    <mkdir dir=\"${build}\"/>\n" +
                                      "  </target>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToChangeAttributeValueOfVoidElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project name=\"MyProject\" default=\"dist\" basedir=\".\">\n" +
                                          "    <description>\n" +
                                          "        simple example build file\n" +
                                          "    </description>\n" +
                                          "  <!-- set global properties for this build -->\n" +
                                          "  <property name=\"src\" location=\"src\"/>\n" +
                                          "  <property name=\"build\" location=\"build\"/>\n" +
                                          "  <property name=\"dist\"  location=\"dist\"/>\n" +
                                          "  <target name=\"init\">\n" +
                                          "    <!-- Create the time stamp -->\n" +
                                          "    <tstamp/>\n" +
                                          "    <!-- Create the build directory structure used by compile -->\n" +
                                          "    <mkdir dir=\"${build}\"/>\n" +
                                          "  </target>\n" +
                                          "</project>");

        tree.getSingleElement("//property[@name='build']")
            .getAttribute("location")
            .setValue("other-build");

        //to check that segments were shifted
        tree.removeElement("//tstamp");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project name=\"MyProject\" default=\"dist\" basedir=\".\">\n" +
                                      "    <description>\n" +
                                      "        simple example build file\n" +
                                      "    </description>\n" +
                                      "  <!-- set global properties for this build -->\n" +
                                      "  <property name=\"src\" location=\"src\"/>\n" +
                                      "  <property name=\"build\" location=\"other-build\"/>\n" +
                                      "  <property name=\"dist\"  location=\"dist\"/>\n" +
                                      "  <target name=\"init\">\n" +
                                      "    <!-- Create the time stamp -->\n" +
                                      "    <!-- Create the build directory structure used by compile -->\n" +
                                      "    <mkdir dir=\"${build}\"/>\n" +
                                      "  </target>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToGetCDATATextContent() {
        final String CDATA = "Maven's model for the old archetype descriptor (ie for Archetype 1.0.x).";
        final XMLTree tree = XMLTree.from("<model>\n" +
                                          "    <id>archetype</id>\n" +
                                          "    <name>Archetype</name>\n" +
                                          "    <description><![CDATA[" + CDATA + "]]></description>\n" +
                                          "</model>");
        //AS tree#getSingleText uses XPath for selecting text content
        //we can easily select CDATA from element
        assertEquals(tree.getSingleText("/model/description"), CDATA);
        assertTrue(tree.getSingleElement("/model/description").getText().isEmpty());
    }

    @Test
    public void shouldBeAbleToCreateTreeFromRootName() {
        final XMLTree tree = XMLTree.create("project");

        tree.getRoot()
            .setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0")
            .setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .setAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd")
            .appendChild(NewElement.createElement("modelVersion", "4.0.0"))
            .appendChild(NewElement.createElement("parent",
                                                  NewElement.createElement("artifactId", "test-parent"),
                                                  NewElement.createElement("groupId", "test-parent-group-id"),
                                                  NewElement.createElement("version", "test-parent-version")))
            .appendChild(NewElement.createElement("artifactId", "test-artifact"))
            .appendChild(NewElement.createElement("packaging", "jar"))
            .appendChild(NewElement.createElement("name", "test"));

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                      "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                      "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                      "    <modelVersion>4.0.0</modelVersion>\n" +
                                      "    <parent>\n" +
                                      "        <artifactId>test-parent</artifactId>\n" +
                                      "        <groupId>test-parent-group-id</groupId>\n" +
                                      "        <version>test-parent-version</version>\n" +
                                      "    </parent>\n" +
                                      "    <artifactId>test-artifact</artifactId>\n" +
                                      "    <packaging>jar</packaging>\n" +
                                      "    <name>test</name>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToAddElementWithPrefix() {
        final XMLTree tree = XMLTree.from("<examples:tests xmlns:examples=\"http://whatever.com/\">\n" +
                                          "    <examples:test>first</examples:test>\n" +
                                          "</examples:tests>");

        tree.getRoot()
            .appendChild(NewElement.createElement("examples:test", "second"));

        assertEquals(tree.toString(), "<examples:tests xmlns:examples=\"http://whatever.com/\">\n" +
                                      "    <examples:test>first</examples:test>\n" +
                                      "    <examples:test>second</examples:test>\n" +
                                      "</examples:tests>");
        final Element appended = tree.getRoot().getLastChild();
        assertEquals(appended.getLocalName(), "test");
        assertEquals(appended.getPrefix(), "examples");
        assertEquals(appended.getName(), "examples:test");
    }

    @Test
    public void shouldBeAbleToCreateTreeFromXMLWhichContainsInstructionElement() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "    <configuration>\n" +
                                          "        <tasks>\n" +
                                          "            <?SORTPOM IGNORE?>\n" +
                                          "            <echo append=\"false\" file=\"${project.build.directory}/classes/com/codenvy/ide/BuildInfo.properties\">\n" +
                                          "                  revision= ${revision}\n" +
                                          "                  buildTime = ${timestamp}\n" +
                                          "                  version = ${codenvy.cloud-ide.version}\n" +
                                          "            </echo>\n" +
                                          "            <?SORTPOM RESUME?>\n" +
                                          "        </tasks>\n" +
                                          "     </configuration>\n" +
                                          "</project>");

        tree.updateText("//echo", "new text content");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "    <configuration>\n" +
                                      "        <tasks>\n" +
                                      "            <?SORTPOM IGNORE?>\n" +
                                      "            <echo append=\"false\" file=\"${project.build.directory}/classes/com/codenvy/ide/BuildInfo.properties\">new text content</echo>\n" +
                                      "            <?SORTPOM RESUME?>\n" +
                                      "        </tasks>\n" +
                                      "     </configuration>\n" +
                                      "</project>");
    }

    @Test
    public void shouldBeAbleToCreateTreeFromXMLWhichContainsCoupleOfCDATAElements() {
        final XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<project>\n" +
                                          "<!-- Spring security authen/authorize query string -->\n" +
                                          "<security.query.authen>\n" +
                                          "<![CDATA[\n" +
                                          "select u.login as user, u.password as password, case when u.status = 'ok' " +
                                          "then 1 else 0 end as enabled \n" +
                                          "from ${jdbc.default.schema}.USER u \n" +
                                          "where u.login = ? and u.status = 'ok'" +
                                          "]]>\n\n\n\n" +
                                          "</security.query.authen>\n" +
                                          "<security.query.authorize>\n" +
                                          "<![CDATA[\n" +
                                          "select u.login as user, u.password as password, case when u.status = 'ok' " +
                                          "then 1 else 0 end as enabled \n" +
                                          "from ${jdbc.default.schema}.USER u \n" +
                                          "where u.login = ? and u.status = 'ok'" +
                                          "]]>\na" +
                                          "</security.query.authorize>\n" +
                                          "</project>");

        tree.removeElement("//security.query.authorize");
        tree.updateText("//security.query.authen", "new-text");

        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<project>\n" +
                                      "<!-- Spring security authen/authorize query string -->\n" +
                                      "<security.query.authen>new-text</security.query.authen>\n" +
                                      "</project>");
    }

    @Test(expectedExceptions = XMLTreeException.class,
            expectedExceptionsMessageRegExp = "Operation not permitted for element which has been removed from XMLTree")
    public void shouldNotBeAbleToUseElementWhenParentWasRemovedFromTree() {
        final XMLTree tree = XMLTree.from(XML_CONTENT);

        final Element firstDep = tree.getSingleElement("/project/dependencies/dependency[1]");
        assertNotNull(firstDep.getText());

        tree.removeElement("/project/dependencies");

        firstDep.getText();
    }

    @Test
    public void shouldBeAbleToWriteTreeBytesToPath() throws Exception {
        final XMLTree tree = XMLTree.from(XML_CONTENT);
        final Path path = targetDir().resolve("test-xml.xml");

        tree.writeTo(path);

        assertTrue(exists(path));
        assertEquals(readAllBytes(path), tree.getBytes());

        delete(path);
    }

    @Test
    public void shouldBeABleToWriteTreeBytesToFile() throws Exception {
        final XMLTree tree = XMLTree.from(XML_CONTENT);
        final java.io.File file = targetDir().resolve("test-xml.xml").toFile();

        tree.writeTo(file);

        assertTrue(file.exists());
        assertEquals(toByteArray(file), tree.getBytes());
        assertTrue(file.delete());
    }

    @Test
    public void shouldBeAbleToWriteTreeBytesToOutputStream() throws Exception {
        final XMLTree tree = XMLTree.from(XML_CONTENT);
        final Path path = targetDir().resolve("test-xml.xml");

        try (OutputStream os = newOutputStream(path)) {
            tree.writeTo(os);
        }

        assertTrue(exists(path));
        assertEquals(readAllBytes(path), tree.getBytes());

        delete(path);
    }

    @Test
    public void shouldBeAbleToCreateTreeFromPath() throws Exception {
        final byte[] bytes = XML_CONTENT.getBytes();
        final Path path = targetDir().resolve("test-xml.xml");
        write(path, bytes);

        final XMLTree tree = XMLTree.from(path);

        assertEquals(tree.getBytes(), bytes);

        delete(path);
    }

    @Test
    public void shouldBeAbleToCreateTreeFromFile() throws Exception {
        final byte[] bytes = XML_CONTENT.getBytes();
        final Path path = targetDir().resolve("test-xml.xml");
        write(path, bytes);

        final XMLTree tree = XMLTree.from(path.toFile());

        assertEquals(tree.getBytes(), bytes);

        delete(path);
    }

    @Test
    public void shouldBeAbleToCreateTreeFromInputStream() throws Exception {
        final byte[] bytes = XML_CONTENT.getBytes();

        final XMLTree tree = XMLTree.from(new ByteArrayInputStream(bytes));

        assertEquals(tree.getBytes(), bytes);
    }

    @Test
    public void shouldBeAbleToCreateTreeFromXMLWhichContainsCDATAAndTextUnderSameParent() {
        final XMLTree tree = XMLTree.from("<parent>\n" +
                                          "TEXT" +
                                          "<![CDATA[ CDATA CONTENT ]]>\n" +
                                          "TEXT AGAIN" +
                                          "<child></child>" +
                                          "</parent>");

        assertEquals(tree.getRoot().getText(), "\n" +
                                               "TEXT\n" +
                                               "TEXT AGAIN");
        tree.updateText("/parent", "new text");
        assertEquals(tree.toString(), "<parent>new text<child></child></parent>");
    }

    @Test
    public void shouldIncludeCarriageReturnCharacterOffsetWhileParsingXMLContent() {
        System.setProperty("line.separator", "\n");
        final XMLTree tree = XMLTree.from("<parent>\n" +
                                          "    <child1>\r\r\r\r\rchild1 text\r</child1>\n" +
                                          "\r\r<child2>child 2 text</child2>\n" +
                                          "</parent>");

        tree.updateText("/parent/child1", "new text");
        tree.updateText("/parent/child2", "new text");

        assertEquals(tree.toString(), "<parent>\n" +
                                      "    <child1>new text</child1>\n" +
                                      "\n\n<child2>new text</child2>\n" +
                                      "</parent>");
    }

    @Test
    public void shouldIncludeCarriageReturnCharacterOffsetWhileParsingXMLContent2() {
        System.setProperty("line.separator", "\n");
        final XMLTree tree = XMLTree.from("<parent>\n" +
                                          "    <child1>\rchild1 text\r</child1>\n" +
                                          "\r\r<child2>child 2 text</child2>\n" +
                                          "</parent>");

        tree.insertAfter("/parent/child1", NewElement.createElement("newTag"));
        assertEquals(tree.toString(), "<parent>\n" +
                                      "    <child1>\nchild1 text\n</child1>\n" +
                                      "    <newTag/>\n" +
                                      "\n\n<child2>child 2 text</child2>\n" +
                                      "</parent>");
    }

    @Test
    public void shouldRespectContentPositionsWhenUpdatingTextWithCarriageReturnCharacter() {
        System.setProperty("line.separator", "\r\n");
        final String XML = "<parent><child>\r\nchild text\r\n</child></parent>";

        XMLTree tree = XMLTree.from(XML);

        tree.updateText("/parent/child", "new text");

        assertEquals(tree.toString(), "<parent><child>new text</child></parent>");
    }

    @Test
    public void shouldParseWithCarriageReturnCharacterInDocumentPrologue() {
        System.setProperty("line.separator", "\r");
        XMLTree tree = XMLTree.from("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<!-- <<<<< COMMENT >>>>> -->\n" +
                                    "\r\r\r\r\r\r\r\r\r\r\r\r\r\r" +
                                    "<project>\r\r\r\r\n" +
                                    "   <name>\r\n\r\nname\r\n\r\n</name>" +
                                    "   <packaging>\r\r\r\n\n\nwar</packaging>" +
                                    "</project>");

        tree.updateText("/project/packaging", "jar");

        assertEquals(tree.getSingleText("/project/name"), "\n\n\n\nname\n\n\n\n");
        assertEquals(tree.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r" +
                                      "<!-- <<<<< COMMENT >>>>> -->\r" +
                                      "\r\r\r\r\r\r\r\r\r\r\r\r\r\r" +
                                      "<project>\r\r\r\r\r" +
                                      "   <name>\r\r\r\rname\r\r\r\r</name>" +
                                      "   <packaging>jar</packaging>" +
                                      "</project>");
    }

    @Test
    public void shouldParseContentWithCarriageReturnCharacterBetweenTagAttributes() {
        System.setProperty("line.separator", "\r\n");
        final String XML = "<parent \r\n\r\n\r\n attr1=\"v\"><child>\r\nchild text\r\n</child></parent>";

        XMLTree tree = XMLTree.from(XML);

        tree.updateText("/parent/child", "new text");

        assertEquals(tree.toString(), "<parent \r\n\r\n\r\n attr1=\"v\"><child>new text</child></parent>");
    }

    @Test(dataProvider = "custom-xml-files")
    public void shouldBeAbleToCreateTreeFromCustomXML(File xml) throws IOException {
        //should be able to parse file
        try {
            XMLTree.from(xml);
        } catch (XMLTreeException ex) {
            throw new XMLTreeException(ex.getMessage() + " file: " + xml.getAbsolutePath());
        }
    }

    @DataProvider(name = "custom-xml-files")
    public Object[][] getCustomXMLFiles() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final File testFilesRoot = Paths.get(url.toURI()).resolve("test-xml-files").toFile();
        final File[] files = testFilesRoot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().endsWith(".xml");
            }
        });
        final Object[][] data = new Object[files.length][];
        for (int i = 0; i < files.length; i++) {
            data[i] = new Object[]{files[i]};
        }
        return data;
    }

    private Path targetDir() throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }
}