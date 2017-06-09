/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.XMLTree;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.util.GwtXmlGenerator.GwtXmlGeneratorConfig;
import org.eclipse.che.util.GwtXmlGenerator.GwtXmlModuleSearcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.util.GwtXmlGenerator.DEFAULT_GWT_ETNRY_POINT;
import static org.eclipse.che.util.GwtXmlGenerator.DEFAULT_GWT_XML_PATH;
import static org.eclipse.che.util.GwtXmlGenerator.DEFAULT_STYLE_SHEET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GwtXmlGeneratorTest {

    File testRoot;

    @BeforeMethod
    public void setUp() {
        testRoot = Files.createTempDir();
    }

    @AfterMethod
    public void cleanup() {
        IoUtil.deleteRecursive(testRoot);
    }

    @Test
    public void shouldFindGwtXmlModules() {
        //given
        Set<String> excludePackages = Collections.emptySet();
        Set<String> includePackages = Collections.emptySet();
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages, ImmutableSet
                .of(GwtXmlGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation()));

        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
    }

    @Test
    public void shouldBeAbleToIncludeOnlyGwtModuleFrom() {
        //given
        Set<String> excludePackages = Collections.emptySet();
        Set<String> includePackages = ImmutableSet.of("elemental");
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages, ImmutableSet
                .of(GwtXmlGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation()));

        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
        assertEquals(actual.size(), 3);

        assertTrue(actual.contains("elemental/Json.gwt.xml"));
        assertTrue(actual.contains("elemental/Collections.gwt.xml"));
        assertTrue(actual.contains("elemental/Elemental.gwt.xml"));
    }


    @Test
    public void shouldBeAbleToExcludeGwtModuleFrom() {
        //given
        Set<String> excludePackages = ImmutableSet.of("elemental");
        Set<String> includePackages = Collections.emptySet();
        GwtXmlModuleSearcher searcher = new GwtXmlModuleSearcher(excludePackages, includePackages, ImmutableSet
                .of(GwtXmlGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation()));
        //when
        Set<String> actual = searcher.getGwtModulesFromClassPath();
        //then
        assertFalse(actual.isEmpty());
        assertFalse(actual.contains("elemental/Json.gwt.xml"));
        assertFalse(actual.contains("elemental/Collections.gwt.xml"));
        assertFalse(actual.contains("elemental/Elemental.gwt.xml"));
    }

    @Test
    public void shouldGenerateGwtXml() throws IOException {
        //given
        Set<String> gwtModule = ImmutableSet.of("org/mydomain/Printer.gwt.xml");
        GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                new GwtXmlGeneratorConfig(gwtModule, testRoot);
        GwtXmlGenerator gwtXmlGenerator = new GwtXmlGenerator(gwtXmlGeneratorConfig);
        //when
        File actual = gwtXmlGenerator.generateGwtXml();
        //then
        XMLTree tree = XMLTree.from(actual);


        List<Element> inherits = tree.getElements("/module/inherits");
        assertEquals(inherits.size(), 1);
        assertEquals(inherits.get(0).getAttribute("name").getValue(), "org.mydomain.Printer");
        assertEquals(tree.getSingleElement("/module/stylesheet").getAttribute("src").getValue(),
                     DEFAULT_STYLE_SHEET);
        assertEquals(tree.getSingleElement("/module/entry-point").getAttribute("class").getValue(),
                     DEFAULT_GWT_ETNRY_POINT);
    }

    @Test
    public void shouldBeAbleToSetStylesheet() throws IOException {
        //given
        Set<String> gwtModule = ImmutableSet.of("org/mydomain/Printer.gwt.xml");
        GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                new GwtXmlGeneratorConfig(gwtModule,
                                          testRoot,
                                          DEFAULT_GWT_XML_PATH,
                                          DEFAULT_GWT_ETNRY_POINT,
                                          "MyStylesheet.css",
                                          false);
        GwtXmlGenerator gwtXmlGenerator = new GwtXmlGenerator(gwtXmlGeneratorConfig);
        //when
        File actual = gwtXmlGenerator.generateGwtXml();
        //then
        XMLTree tree = XMLTree.from(actual);


        assertEquals(tree.getSingleElement("/module/stylesheet").getAttribute("src").getValue(),
                     "MyStylesheet.css");
    }

    @Test
    public void shouldBeAbleToDisableLogging() throws IOException {
        //given
        Set<String> gwtModule = ImmutableSet.of("org/mydomain/Printer.gwt.xml");
        GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                new GwtXmlGeneratorConfig(gwtModule,
                                          testRoot,
                                          DEFAULT_GWT_XML_PATH,
                                          DEFAULT_GWT_ETNRY_POINT,
                                          DEFAULT_STYLE_SHEET,
                                          false);
        GwtXmlGenerator gwtXmlGenerator = new GwtXmlGenerator(gwtXmlGeneratorConfig);
        //when
        File actual = gwtXmlGenerator.generateGwtXml();
        //then
        XMLTree tree = XMLTree.from(actual);
        assertNotExists(tree, "/module/inherits[@name='com.google.gwt.logging.Logging']");
        assertNotExists(tree, "/module/set-property[@name='gwt.logging.consoleHandler']");
        assertNotExists(tree, "/module/set-property[@name='gwt.logging.developmentModeHandler']");
        assertNotExists(tree, "/module/set-property[@name='gwt.logging.simpleRemoteHandler']");

    }

    @Test
    public void shouldBeAbleToEnableLogging() throws IOException {
        //given
        Set<String> gwtModule = ImmutableSet.of("org/mydomain/Printer.gwt.xml");
        GwtXmlGeneratorConfig gwtXmlGeneratorConfig =
                new GwtXmlGeneratorConfig(gwtModule,
                                          testRoot,
                                          DEFAULT_GWT_XML_PATH,
                                          DEFAULT_GWT_ETNRY_POINT,
                                          DEFAULT_STYLE_SHEET,
                                          true);
        GwtXmlGenerator gwtXmlGenerator = new GwtXmlGenerator(gwtXmlGeneratorConfig);
        //when
        File actual = gwtXmlGenerator.generateGwtXml();
        //then
        XMLTree tree = XMLTree.from(actual);
        assertExists(tree, "/module/inherits[@name='com.google.gwt.logging.Logging']");

        assertEquals(tree.getSingleElement("/module/set-property[@name='gwt.logging.consoleHandler']")
                         .getAttribute("value").getValue(), "ENABLED");
        assertEquals(tree.getSingleElement("/module/set-property[@name='gwt.logging.developmentModeHandler']")
                         .getAttribute("value").getValue(), "ENABLED");
        assertEquals(tree.getSingleElement("/module/set-property[@name='gwt.logging.simpleRemoteHandler']")
                         .getAttribute("value").getValue(), "DISABLED");

    }

    @Test
    public void shouldFindModulesAndGenerate() throws IOException {
        //given
        String[] args = new String[]{"--rootDir=" + testRoot,
                                     "--gwtFileName=com/myorg/My.gwt.xml",
                                     "--includePackages=org.eclipse.che.api.testing",
                                     "--includePackages=org.eclipse.che.api.core"
        };
        GwtXmlGenerator.main(args);
        //when
        File actual = new File(testRoot, "com/myorg/My.gwt.xml");
        //then
        XMLTree tree = XMLTree.from(actual);
        List<Element> inherits = tree.getElements("/module/inherits");
        assertEquals(inherits.size(), 3);
        assertEquals(inherits.get(0).getAttribute("name").getValue(), "org.eclipse.che.api.core.Core");
        assertEquals(inherits.get(1).getAttribute("name").getValue(), "org.eclipse.che.api.core.model.Model");
        assertEquals(inherits.get(2).getAttribute("name").getValue(), "org.eclipse.che.api.testing.Testing");

    }

    public static void assertNotExists(XMLTree tree, String xpath) {
        try {
            tree.getSingleElement(xpath);
        } catch (XMLTreeException e) {
            assertEquals(e.getLocalizedMessage(), "Required list with one element");
        }
    }

    public static void assertExists(XMLTree tree, String xpath) {
        assertNotNull(tree.getSingleElement(xpath));
    }


}
