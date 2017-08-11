/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.core.project;

import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.plugin.maven.server.BaseTest;
import org.eclipse.che.plugin.maven.server.rmi.MavenServerManagerTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MavenModelReaderTest extends BaseTest {
    private MavenModelReader mavenModelReader;

    @BeforeMethod
    public void setUp() throws Exception {
        mavenModelReader = new MavenModelReader();
    }

    @Test
    public void multimoduleProjectWithProfilesShouldBeResolved() throws Exception {
        final File file = new File(MavenServerManagerTest.class.getResource("/multi-module-with-profiles/pom.xml").getFile());
        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(file, mavenServerManager);
        final MavenModel mavenModel = mavenModelReaderResult.getMavenModel();
        assertNotNull(mavenModel);
        List<String> modules = mavenModel.getModules();
        assertEquals(3, modules.size());
    }

    @Test
    public void propertiesFromProfileShouldBeExpanded() throws Exception {
        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>${prop2}</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>\n" +
                               "        <profile>\n" +
                               "            <id>prop1</id>\n" +
                               "            <activation>\n" +
                               "                <activeByDefault>true</activeByDefault>\n" +
                               "            </activation>\n" +
                               "            <properties>\n" +
                               "                <prop1>value1</prop1>\n" +
                               "            </properties>\n" +
                               "        </profile>\n" +
                               "        <profile>\n" +
                               "            <id>prop2</id>\n" +
                               "            <activation>\n" +
                               "                <activeByDefault>true</activeByDefault>\n" +
                               "            </activation>\n" +
                               "            <properties>\n" +
                               "                <prop2>jar</prop2>\n" +
                               "            </properties>\n" +
                               "        </profile>\n" +
                               "    </profiles>";
        final File pom = createTestPom("propertiesFromProfile", content);
        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        final MavenModel mavenModel = mavenModelReaderResult.getMavenModel();

        assertEquals("value1", mavenModel.getName());
        assertEquals("jar", mavenModel.getPackaging());
    }

    @Test
    public void propertiesOnlyFromActiveProfileShouldBeExpanded() throws Exception {
        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>${prop2}</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>\n" +
                               "        <profile>\n" +
                               "            <id>prop1</id>\n" +
                               "            <activation>\n" +
                               "                <activeByDefault>true</activeByDefault>\n" +
                               "            </activation>\n" +
                               "            <properties>\n" +
                               "                <prop1>value1</prop1>\n" +
                               "            </properties>\n" +
                               "        </profile>\n" +
                               "        <profile>\n" +
                               "            <id>prop2</id>\n" +
                               "            <properties>\n" +
                               "                <prop2>jar</prop2>\n" +
                               "            </properties>\n" +
                               "        </profile>\n" +
                               "    </profiles>";
        final File pom = createTestPom("propertiesFromProfile", content);
        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        final MavenModel mavenModel = mavenModelReaderResult.getMavenModel();

        assertEquals("value1", mavenModel.getName());
        assertEquals("${prop2}", mavenModel.getPackaging());
    }

    @Test
    public void profileShouldBeActivatedByDefault() throws Exception {
        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>${prop2}</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>\n" +
                               "        <profile>\n" +
                               "            <id>prof1</id>\n" +
                               "            <activation>\n" +
                               "                <activeByDefault>true</activeByDefault>\n" +
                               "            </activation>\n" +
                               "            <properties>\n" +
                               "                <prop1>value1</prop1>\n" +
                               "            </properties>\n" +
                               "        </profile>\n" +
                               "        <profile>\n" +
                               "            <id>prof2</id>\n" +
                               "        </profile>\n" +
                               "    </profiles>";

        final File pom = createTestPom("propertiesFromProfile", content);

        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        assertEquals(1, mavenModelReaderResult.getActiveProfiles().size());
        assertEquals("prof1", mavenModelReaderResult.getActiveProfiles().get(0));
    }

    @Test
    public void profileShouldBeActivatedByOs() throws Exception {
        OSValidator osValidator = new OSValidator();
        String os = osValidator.isWindows() ? "windows" : osValidator.isMac() ? "mac" : "unix";

        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>${prop2}</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>" +
                               "        <profile>" +
                               "            <id>one</id>" +
                               "            <activation>" +
                               "                <os><family>" + os + "</family></os>" +
                               "            </activation>" +
                               "        </profile>" +
                               "        <profile>" +
                               "            <id>two</id>" +
                               "            <activation>" +
                               "                <os><family>xxx</family></os>" +
                               "            </activation>" +
                               "        </profile>" +
                               "    </profiles>";

        final File pom = createTestPom("propertiesFromProfile", content);

        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        assertEquals(1, mavenModelReaderResult.getActiveProfiles().size());
        assertEquals("one", mavenModelReaderResult.getActiveProfiles().get(0));
    }

    @Test
    public void profileShouldBeActivatedByJDK() throws Exception {
        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>${prop2}</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>" +
                               "        <profile>" +
                               "            <id>one</id>" +
                               "            <activation>" +
                               "                <jdk>[1.5,)</jdk>" +
                               "            </activation>" +
                               "        </profile>" +
                               "        <profile>" +
                               "            <id>two</id>" +
                               "            <activation>" +
                               "                <jdk>1.4</jdk>" +
                               "            </activation>" +
                               "        </profile>" +
                               "    </profiles>";

        final File pom = createTestPom("propertiesFromProfile", content);

        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        assertEquals(1, mavenModelReaderResult.getActiveProfiles().size());
        assertEquals("one", mavenModelReaderResult.getActiveProfiles().get(0));
    }

    @Test
    public void profileShouldBeActivatedByProperty() throws Exception {
        final String osProperty = System.getProperty("os.name");
        final String content = "<artifactId>project-with-properties</artifactId>\n" +
                               "    <groupId>com.aw.ad</groupId>\n" +
                               "    <name>${prop1}</name>\n" +
                               "    <packaging>jar</packaging>\n" +
                               "    <version>1.0</version>\n" +
                               "    <profiles>" +
                               "        <profile>" +
                               "            <id>one</id>" +
                               "            <activation>" +
                               "                <property>" +
                               "                    <name>os.name</name>" +
                               "                    <value>" + osProperty + "</value>" +
                               "                </property>" +
                               "            </activation>" +
                               "        </profile>" +
                               "        <profile>" +
                               "            <id>two</id>" +
                               "            <activation>" +
                               "                <property>" +
                               "                    <name>os.name</name>" +
                               "                    <value>xxx</value>" +
                               "                </property>" +
                               "            </activation>" +
                               "        </profile>" +
                               "    </profiles>";

        final File pom = createTestPom("propertiesFromProfile", content);

        final MavenModelReaderResult mavenModelReaderResult = mavenModelReader.readMavenProject(pom, mavenServerManager);

        assertEquals(1, mavenModelReaderResult.getActiveProfiles().size());
        assertEquals("one", mavenModelReaderResult.getActiveProfiles().get(0));
    }

    private class OSValidator {
        private String OS = System.getProperty("os.name").toLowerCase();

        boolean isWindows() {
            return (OS.contains("win"));
        }

        boolean isMac() {
            return (OS.contains("mac"));
        }

        public boolean isUnix() {
            return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
        }

        public boolean isSolaris() {
            return (OS.contains("sunos"));
        }
    }

}
