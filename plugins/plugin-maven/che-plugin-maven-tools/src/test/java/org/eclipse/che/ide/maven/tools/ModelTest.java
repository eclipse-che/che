/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.maven.tools;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class ModelTest {

  @BeforeMethod
  private void resetLineSeparator() {
    // needed for xml tree to write content with \n line separator
    System.setProperty("line.separator", "\n");
  }

  @Test
  public void shouldBeAbleToCreateModel() throws Exception {
    final Model model = Model.createModel();
    model
        .setModelVersion("4.0.0")
        .setArtifactId("artifact-id")
        .setGroupId("group-id")
        .setVersion("x.x.x")
        .setName("name")
        .setDescription("description")
        .dependencies()
        .set(asList(new Dependency("junit", "org.junit", "x.x.x")))
        .add(new Dependency("testng", "org.testng", "x.x.x"));
    final File pom = getTestPomFile();

    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
            + "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <name>name</name>\n"
            + "    <description>description</description>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <groupId>junit</groupId>\n"
            + "            <artifactId>org.junit</artifactId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "        <dependency>\n"
            + "            <groupId>testng</groupId>\n"
            + "            <artifactId>org.testng</artifactId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToReadModelFromFile() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
            + "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <name>name</name>\n"
            + "    <packaging>jar</packaging>"
            + "    <description>description</description>\n"
            + "    <modules>\n"
            + "        <module>first</module>\n"
            + "        <module>second</module>\n"
            + "    </modules>\n"
            + "    <dependencyManagement>\n"
            + "        <dependencies>\n"
            + "            <dependency>\n"
            + "                <groupId>junit</groupId>\n"
            + "                <artifactId>junit</artifactId>\n"
            + "                <version>3.8</version>\n"
            + "            </dependency>\n"
            + "        </dependencies>\n"
            + "    </dependencyManagement>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "        <dependency>\n"
            + "            <artifactId>testng</artifactId>\n"
            + "            <groupId>org.testng</groupId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");

    final Model model = Model.readFrom(pom);

    assertEquals(model.getModelVersion(), "4.0.0");
    assertEquals(model.getArtifactId(), "artifact-id");
    assertEquals(model.getGroupId(), "group-id");
    assertEquals(model.getVersion(), "x.x.x");
    assertEquals(model.getName(), "name");
    assertEquals(model.getDescription(), "description");
    assertEquals(model.getArtifactId(), "artifact-id");
    assertEquals(model.getPackaging(), "jar");
    assertEquals(model.getModules(), asList("first", "second"));
    assertEquals(model.getDependencies().size(), 2);
    assertEquals(model.getDependencyManagement().getDependencies().size(), 1);
  }

  @Test
  public void shouldRemoveModulesIfLastModuleWasRemoved() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <modules>\n"
            + "        <module>first</module>\n"
            + "        <module>second</module>\n"
            + "    </modules>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.removeModule("first");
    model.removeModule("second");

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    assertTrue(model.getModules().isEmpty());
  }

  @Test
  public void shouldRemoveModule() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <modules>\n"
            + "        <module>firstModule</module>\n"
            + "        <module>secondModule</module>\n"
            + "    </modules>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.removeModule("firstModule");

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <modules>\n"
            + "        <module>secondModule</module>\n"
            + "    </modules>\n"
            + "</project>");
    assertEquals(model.getModules().size(), 1);
  }

  @Test
  public void shouldBeAbleToRemoveModelMembers() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <packaging>war</packaging>\n"
            + "    <name>name</name>\n"
            + "    <description>description</description>\n"
            + "    <modules>\n"
            + "        <module>first</module>\n"
            + "        <module>second</module>\n"
            + "    </modules>\n"
            + "    <properties>\n"
            + "        <childKey>child</childKey>\n"
            + "        <parentKey>parent</parentKey>\n"
            + "    </properties>\n"
            + "    <dependencyManagement>\n"
            + "        <dependencies>\n"
            + "            <dependency>\n"
            + "                <groupId>artifact-id</groupId>\n"
            + "                <artifactId>group-id</artifactId>\n"
            + "                <version>version</version>\n"
            + "            </dependency>\n"
            + "        </dependencies>\n"
            + "    </dependencyManagement>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <groupId>junit</groupId>\n"
            + "            <artifactId>org.junit</artifactId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .setModelVersion(null)
        .setGroupId(null)
        .setArtifactId(null)
        .setVersion(null)
        .setPackaging(null)
        .setName(null)
        .setDescription(null)
        .setDependencyManagement(null)
        .setModules(null)
        .setProperties(null)
        .dependencies()
        .set(null);

    model.writeTo(pom);

    assertEquals(
        read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
  }

  @Test
  public void shouldAddCreateModuleParentElementWhenAddingNewModule() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.addModule("new-module");

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <modules>\n"
            + "        <module>new-module</module>\n"
            + "    </modules>\n"
            + "</project>");
    assertEquals(model.getModules(), asList("new-module"));
  }

  @Test
  public void shouldBeAbleToUpdateProperties() throws Exception {
    final File pomFile = getTestPomFile();
    write(
        pomFile,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <properties>\n"
            + "        <childKey>child</childKey>\n"
            + "        <parentKey>parent</parentKey>\n"
            + "    </properties>\n"
            + "</project>");
    final Model pom = Model.readFrom(pomFile);

    pom.addProperty("childKey", "new-child");
    pom.addProperty("newProperty", "new-property");

    pom.writeTo(pomFile);
    assertEquals(
        read(pomFile),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <properties>\n"
            + "        <childKey>new-child</childKey>\n"
            + "        <parentKey>parent</parentKey>\n"
            + "        <newProperty>new-property</newProperty>\n"
            + "    </properties>\n"
            + "</project>");
  }

  @Test
  public void shouldRemovePropertiesWhenLastPropertyWasRemoved() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <properties>\n"
            + "        <childKey>child</childKey>\n"
            + "    </properties>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.removeProperty("childKey");

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetDependencyManagement() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final DependencyManagement dm = new DependencyManagement();
    dm.dependencies().add(new Dependency("artifact-id", "group-id", "version"));
    model.setDependencyManagement(dm);

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <dependencyManagement>\n"
            + "        <dependencies>\n"
            + "            <dependency>\n"
            + "                <groupId>artifact-id</groupId>\n"
            + "                <artifactId>group-id</artifactId>\n"
            + "                <version>version</version>\n"
            + "            </dependency>\n"
            + "        </dependencies>\n"
            + "    </dependencyManagement>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetBuildToModelWhichDoesNotHaveIt() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .setBuild(
            new Build().setSourceDirectory("src/main/java").setTestSourceDirectory("src/main/test"))
        .writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/java</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "    </build>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetBuildToModelWhichAlreadyHasIt() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/java</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .setBuild(
            new Build()
                .setSourceDirectory("src/main/groovy")
                .setTestSourceDirectory("src/main/test"))
        .writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/groovy</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "    </build>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToUpdateBuild() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/java</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "        <scriptSourceDirectory>src/main/scripts</scriptSourceDirectory>\n"
            + "        <testOutputDirectory>src/main/testOutput</testOutputDirectory>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.getBuild().setSourceDirectory("src/main/groovy").setOutputDirectory("output/path");

    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/groovy</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "        <scriptSourceDirectory>src/main/scripts</scriptSourceDirectory>\n"
            + "        <testOutputDirectory>src/main/testOutput</testOutputDirectory>\n"
            + "        <outputDirectory>output/path</outputDirectory>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(model.getBuild().getSourceDirectory(), "src/main/groovy");
    assertEquals(model.getBuild().getTestSourceDirectory(), "src/main/test");
    assertEquals(model.getBuild().getOutputDirectory(), "output/path");
    assertEquals(model.getBuild().getTestOutputDirectory(), "src/main/testOutput");
    assertEquals(model.getBuild().getScriptSourceDirectory(), "src/main/scripts");
  }

  @Test
  public void shouldBeAbleToRemoveBuild() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/java</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.setBuild(null).writeTo(pom);

    assertNull(model.getBuild());
    assertEquals(
        read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
  }

  @Test
  public void shouldBeAbleToRemoveBuildMembers() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <version>x.x.x</version>\n"
            + "    <build>\n"
            + "        <sourceDirectory>src/main/java</sourceDirectory>\n"
            + "        <testSourceDirectory>src/main/test</testSourceDirectory>\n"
            + "        <outputDirectory>output/path</outputDirectory>\n"
            + "        <testOutputDirectory>test/output/path</testOutputDirectory>\n"
            + "        <scriptSourceDirectory>script/source/path</scriptSourceDirectory>\n"
            + "        <resources>\n"
            + "            <resource>\n"
            + "                 <directory>${basedir}/src/main/temp</directory>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                    <item2>value2</item2>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getBuild()
        .setOutputDirectory(null)
        .setSourceDirectory(null)
        .setScriptSourceDirectory(null)
        .setTestOutputDirectory(null)
        .setTestSourceDirectory(null)
        .setResources(null)
        .setPlugins(null);

    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <version>x.x.x</version>\n"
            + "    <build>\n"
            + "    </build>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToGetBuildResources() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "             <resource>\n"
            + "                 <targetPath>META-INF/temp</targetPath>\n"
            + "                 <filtering>true</filtering>\n"
            + "                 <directory>${basedir}/src/main/temp</directory>\n"
            + "                 <includes>\n"
            + "                     <include>configuration.xml</include>\n"
            + "                 </includes>\n"
            + "                 <excludes>\n"
            + "                     <exclude>**/*.properties</exclude>\n"
            + "                 </excludes>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final List<Resource> resources = model.getBuild().getResources();

    assertEquals(resources.size(), 1);
    final Resource resource = resources.get(0);
    assertEquals(resource.getTargetPath(), "META-INF/temp");
    assertTrue(resource.isFiltering());
    assertEquals(resource.getDirectory(), "${basedir}/src/main/temp");
    assertEquals(resource.getIncludes(), asList("configuration.xml"));
    assertEquals(resource.getExcludes(), asList("**/*.properties"));
  }

  @Test
  public void shouldBeAbleToUpdateBuildResource() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "             <resource>\n"
            + "                 <targetPath>META-INF/temp</targetPath>\n"
            + "                 <filtering>true</filtering>\n"
            + "                 <!-- DIRECTORY -->\n"
            + "                 <directory>${basedir}/src/main/temp</directory>\n"
            + "                 <includes>\n"
            + "                     <include>configuration.xml</include>\n"
            + "                 </includes>\n"
            + "                 <excludes>\n"
            + "                     <exclude>**/*.properties</exclude>\n"
            + "                 </excludes>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Resource resource = model.getBuild().getResources().get(0);
    resource
        .setDirectory("new-directory")
        .setFiltering(false)
        .setTargetPath("target-path")
        .setIncludes(asList("include1", "include2", "include3"))
        .setExcludes(asList("exclude1", "exclude2", "exclude3"));
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "             <resource>\n"
            + "                 <targetPath>target-path</targetPath>\n"
            + "                 <filtering>false</filtering>\n"
            + "                 <!-- DIRECTORY -->\n"
            + "                 <directory>new-directory</directory>\n"
            + "                 <includes>\n"
            + "                    <include>include1</include>\n"
            + "                    <include>include2</include>\n"
            + "                    <include>include3</include>\n"
            + "                 </includes>\n"
            + "                 <excludes>\n"
            + "                    <exclude>exclude1</exclude>\n"
            + "                    <exclude>exclude2</exclude>\n"
            + "                    <exclude>exclude3</exclude>\n"
            + "                 </excludes>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(resource.getDirectory(), "new-directory");
    assertEquals(resource.getTargetPath(), "target-path");
    assertEquals(resource.getIncludes(), asList("include1", "include2", "include3"));
    assertEquals(resource.getExcludes(), asList("exclude1", "exclude2", "exclude3"));
    assertFalse(resource.isFiltering());
  }

  @Test
  public void shouldBeAbleToAddResourcesToBuild() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "            <resource>\n"
            + "                 <directory>${basedir}/src/main/temp</directory>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final List<Resource> resources = new ArrayList<>(model.getBuild().getResources());

    resources.add(new Resource().setDirectory("${basedir}/src/main/fake"));

    model.getBuild().setResources(resources);
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "            <resource>\n"
            + "                <directory>${basedir}/src/main/temp</directory>\n"
            + "            </resource>\n"
            + "            <resource>\n"
            + "                <directory>${basedir}/src/main/fake</directory>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(model.getBuild().getResources(), resources);
  }

  @Test
  public void shouldBeAbleToSetResources() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "            <resource>\n"
            + "                 <directory>${basedir}/src/main/temp</directory>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getBuild()
        .setResources(
            asList(
                new Resource().setDirectory("directory1"),
                new Resource().setDirectory("directory2")));
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <resources>\n"
            + "            <resource>\n"
            + "                <directory>directory1</directory>\n"
            + "            </resource>\n"
            + "            <resource>\n"
            + "                <directory>directory2</directory>\n"
            + "            </resource>\n"
            + "         </resources>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(model.getBuild().getResources().size(), 2);
  }

  @Test
  public void shouldCreateDependenciesParentElementWhenAddingFirstDependency() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.dependencies().add(new Dependency("group-id", "artifact-id", "version"));

    model.writeTo(pom);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <groupId>group-id</groupId>\n"
            + "            <artifactId>artifact-id</artifactId>\n"
            + "            <version>version</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    assertEquals(model.getDependencies().size(), 1);
  }

  @Test
  public void shouldReplaceExistingDependenciesWithNew() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <artifactId>artifact-id</artifactId>\n"
            + "            <groupId>group-id</groupId>\n"
            + "            <version>version</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.dependencies().set(asList(new Dependency("a", "b", "c")));

    model.writeTo(pom);

    assertEquals(model.getDependencies().size(), 1);
    final Dependency inserted = model.dependencies().first();
    assertEquals(inserted.getGroupId(), "a");
    assertEquals(inserted.getArtifactId(), "b");
    assertEquals(inserted.getVersion(), "c");
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <groupId>a</groupId>\n"
            + "            <artifactId>b</artifactId>\n"
            + "            <version>c</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
  }

  @Test
  public void shouldRemoveDependenciesWhenLastExistedDependencyWasRemoved() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <version>x.x.x</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.dependencies().remove(model.dependencies().first());

    model.writeTo(pom);

    assertTrue(model.getDependencies().isEmpty());
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToUpdateExistingDependency() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <version>x.x.x</version>\n"
            + "            <scope>test</scope>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .dependencies()
        .first()
        .setVersion("new-version")
        .setScope(null)
        .setOptional(true)
        .setType("type")
        .setClassifier("classifier")
        .setExclusions(asList(new Exclusion("artifact1", "group1")))
        .addExclusion(new Exclusion("artifact2", "group2"));

    model.writeTo(pom);

    final Dependency junit = model.dependencies().first();
    assertEquals(junit.getGroupId(), "org.junit");
    assertEquals(junit.getArtifactId(), "junit");
    assertEquals(junit.getVersion(), "new-version");
    assertEquals(junit.getScope(), "compile");
    assertEquals(junit.getType(), "type");
    assertEquals(junit.getClassifier(), "classifier");
    assertEquals(junit.getExclusions().size(), 2);
    assertTrue(junit.isOptional());
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <optional>true</optional>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <version>new-version</version>\n"
            + "            <type>type</type>\n"
            + "            <classifier>classifier</classifier>\n"
            + "            <exclusions>\n"
            + "                <exclusion>\n"
            + "                    <groupId>group1</groupId>\n"
            + "                    <artifactId>artifact1</artifactId>\n"
            + "                </exclusion>\n"
            + "                <exclusion>\n"
            + "                    <groupId>group2</groupId>\n"
            + "                    <artifactId>artifact2</artifactId>\n"
            + "                </exclusion>\n"
            + "            </exclusions>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
  }

  @Test
  public void shouldRemoveExclusionsIfLastExclusionWasRemoved() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <version>new-version</version>\n"
            + "            <exclusions>\n"
            + "                <exclusion>\n"
            + "                    <artifactId>artifact-id</artifactId>\n"
            + "                    <groupId>group-id</groupId>\n"
            + "                </exclusion>\n"
            + "            </exclusions>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Dependency test = model.dependencies().first();

    test.removeExclusion(test.getExclusions().get(0));

    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <dependencies>\n"
            + "        <dependency>\n"
            + "            <artifactId>junit</artifactId>\n"
            + "            <groupId>org.junit</groupId>\n"
            + "            <version>new-version</version>\n"
            + "        </dependency>\n"
            + "    </dependencies>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetParentToModelWhichDoesNotHaveIt() throws Exception {
    final File pom = targetDir().resolve("test-pom.xml").toFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.setParent(new Parent("parent-group", "parent-artifact", "parent-version"));

    model.writeTo(pom);

    assertNotNull(model.getParent());
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <groupId>parent-group</groupId>\n"
            + "        <artifactId>parent-artifact</artifactId>\n"
            + "        <version>parent-version</version>\n"
            + "    </parent>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetParentToModelWhichAlreadyHasIt() throws Exception {
    final File pom = targetDir().resolve("test-pom.xml").toFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <groupId>new-parent-artifact</groupId>\n"
            + "        <artifactId>new-parent-group</artifactId>\n"
            + "        <version>new-parent-version</version>\n"
            + "    </parent>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.setParent(new Parent("new-parent-artifact", "new-parent-group", "new-parent-version"));

    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <groupId>new-parent-artifact</groupId>\n"
            + "        <artifactId>new-parent-group</artifactId>\n"
            + "        <version>new-parent-version</version>\n"
            + "    </parent>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToUpdateParent() throws Exception {
    final File pomFile = getTestPomFile();
    write(
        pomFile,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <artifactId>parent-artifact</artifactId>\n"
            + "        <groupId>parent-group</groupId>\n"
            + "        <version>parent-version</version>\n"
            + "    </parent>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model pom = Model.readFrom(pomFile);

    final Parent parent = pom.getParent();

    parent
        .setArtifactId("new-parent-artifact-id")
        .setGroupId("new-parent-group-id")
        .setVersion("new-parent-version");

    pom.writeTo(pomFile);

    assertEquals(parent.getArtifactId(), "new-parent-artifact-id");
    assertEquals(parent.getGroupId(), "new-parent-group-id");
    assertEquals(parent.getVersion(), "new-parent-version");
    assertEquals(
        read(pomFile),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <artifactId>new-parent-artifact-id</artifactId>\n"
            + "        <groupId>new-parent-group-id</groupId>\n"
            + "        <version>new-parent-version</version>\n"
            + "    </parent>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToRemoveParent() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <parent>\n"
            + "        <artifactId>parent-artifact</artifactId>\n"
            + "        <groupId>parent-group</groupId>\n"
            + "        <version>parent-version</version>\n"
            + "    </parent>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.setParent(null);

    model.writeTo(pom);

    assertNull(model.getParent());
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <artifactId>artifact-id</artifactId>\n"
            + "    <groupId>group-id</groupId>\n"
            + "    <version>x.x.x</version>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToGetPluginsFromBuild() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                    <item2>value2</item2>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Build build = model.getBuild();

    assertEquals(build.getPlugins().size(), 1);
    assertTrue(build.getPluginsAsMap().containsKey("groupId:artifactId"));
    final Plugin plugin = build.getPluginsAsMap().get("groupId:artifactId");
    assertEquals(plugin.getArtifactId(), "artifactId");
    assertEquals(plugin.getGroupId(), "groupId");
    assertEquals(plugin.getConfiguration().get("item1"), "value1");
    assertEquals(plugin.getConfiguration().get("item2"), "value2");
    assertNull(plugin.getConfiguration().get("properties"));
  }

  @Test
  public void shouldBeAbleToSetPluginConfiguration() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                    <item2>value2</item2>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Plugin plugin = model.getBuild().getPlugins().get(0);
    plugin.setConfiguration(singletonMap("newItem", "value"));
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <newItem>value</newItem>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(plugin.getConfiguration(), singletonMap("newItem", "value"));
  }

  @Test
  public void shouldBeAbleToChangePluginConfigurationProperty() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                    <item2>value2</item2>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Plugin plugin = model.getBuild().getPlugins().get(0);
    plugin.setConfigProperty("item1", "other-value");
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>other-value</item1>\n"
            + "                    <item2>value2</item2>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Map<String, String> expectedConfig = new HashMap<>();
    expectedConfig.put("item1", "other-value");
    expectedConfig.put("item2", "value2");
    assertEquals(plugin.getConfiguration(), expectedConfig);
  }

  @Test
  public void configurationShouldBeRemovedWhenLastPluginConfigurationPropertyWasRemoved()
      throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model.getBuild().getPlugins().get(0).removeConfigProperty("item1");
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    assertTrue(model.getBuild().getPlugins().get(0).getConfiguration().isEmpty());
  }

  @Test
  public void shouldAddConfigurationWhenFirstConfigurationPropertyAdded() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Plugin plugin = model.getBuild().getPlugins().get(0);
    plugin.setConfigProperty("item1", "value1");
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "                <configuration>\n"
            + "                    <item1>value1</item1>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    assertEquals(plugin.getConfiguration(), singletonMap("item1", "value1"));
  }

  @Test
  public void shouldBeAbleToSetBuildPlugins() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <!-- BUILD PLUGINS --> \n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <artifactId>artifactId</artifactId>\n"
            + "                <groupId>groupId</groupId>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getBuild()
        .setPlugins(
            asList(
                new Plugin()
                    .setArtifactId("plugin1-artifactId")
                    .setGroupId("plugin1-groupId")
                    .setConfigProperty("p1-config-property", "value"),
                new Plugin()
                    .setArtifactId("plugin2-artifactId")
                    .setGroupId("plugin2-groupId")
                    .setConfigProperty("p2-config-property", "value")));
    model.save();

    assertEquals(model.getBuild().getPlugins().size(), 2);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <build>\n"
            + "        <!-- BUILD PLUGINS --> \n"
            + "        <plugins>\n"
            + "            <plugin>\n"
            + "                <groupId>plugin1-groupId</groupId>\n"
            + "                <artifactId>plugin1-artifactId</artifactId>\n"
            + "                <configuration>\n"
            + "                    <p1-config-property>value</p1-config-property>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "            <plugin>\n"
            + "                <groupId>plugin2-groupId</groupId>\n"
            + "                <artifactId>plugin2-artifactId</artifactId>\n"
            + "                <configuration>\n"
            + "                    <p2-config-property>value</p2-config-property>\n"
            + "                </configuration>\n"
            + "            </plugin>\n"
            + "        </plugins>\n"
            + "    </build>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToGetRepositorySnapshotsReleases() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <snapshots>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </snapshots>"
            + "            <releases>"
            + "                <enabled>false</enabled>\n"
            + "                <updatePolicy>monthly</updatePolicy>\n"
            + "                <checksumPolicy>fail</checksumPolicy>\n"
            + "            </releases>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");

    final Model model = Model.readFrom(pom);
    final List<Repository> repositories = model.getRepositories();

    assertEquals(repositories.size(), 1);
    final Repository first = repositories.get(0);
    assertTrue(first.getSnapshots().isEnabled());
    assertEquals(first.getSnapshots().getUpdatePolicy(), "daily");
    assertEquals(first.getSnapshots().getChecksumPolicy(), "warn");
    assertFalse(first.getReleases().isEnabled());
    assertEquals(first.getReleases().getUpdatePolicy(), "monthly");
    assertEquals(first.getReleases().getChecksumPolicy(), "fail");
  }

  @Test
  public void shouldBeAbleToChangeRepositorySnapshots() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <snapshots>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </snapshots>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getRepositories()
        .get(0)
        .getSnapshots()
        .setEnabled(false)
        .setUpdatePolicy("monthly")
        .setChecksumPolicy(null);
    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <snapshots>\n"
            + "                <enabled>false</enabled>\n"
            + "                <updatePolicy>monthly</updatePolicy>\n"
            + "            </snapshots>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToChangeRepositoryReleases() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getRepositories()
        .get(0)
        .getReleases()
        .setEnabled(null)
        .setUpdatePolicy("monthly")
        .setChecksumPolicy("fail");
    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <releases>\n"
            + "                <updatePolicy>monthly</updatePolicy>\n"
            + "                <checksumPolicy>fail</checksumPolicy>\n"
            + "            </releases>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToGetRepository() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>"
            + "        </repository>"
            + "    </repositories>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Repository repository = model.getRepositories().get(0);

    assertEquals(repository.getId(), "central");
    assertEquals(repository.getName(), "Central Repository");
    assertEquals(repository.getUrl(), "http://repo.maven.apache.org/maven2");
    assertEquals(repository.getLayout(), "default");
  }

  @Test
  public void shouldBeAbleToModifyRepository() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    model
        .getRepositories()
        .get(0)
        .setId("other-id")
        .setName("other-name")
        .setUrl("http://other-url.com")
        .setLayout("other layout")
        .setReleases(new RepositoryPolicy(false, "fail", "monthly"))
        .setSnapshots(new RepositoryPolicy(false, "fail", "monthly"));
    model.writeTo(pom);

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>other-id</id>\n"
            + "            <name>other-name</name>\n"
            + "            <url>http://other-url.com</url>\n"
            + "            <layout>other layout</layout>\n"
            + "            <snapshots>\n"
            + "                <enabled>false</enabled>\n"
            + "                <checksumPolicy>fail</checksumPolicy>\n"
            + "                <updatePolicy>monthly</updatePolicy>\n"
            + "            </snapshots>\n"
            + "            <releases>\n"
            + "                <enabled>false</enabled>\n"
            + "                <checksumPolicy>fail</checksumPolicy>\n"
            + "                <updatePolicy>monthly</updatePolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToAddRepository() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");

    final Model model =
        Model.readFrom(pom)
            .addRepository(
                new Repository().setId("id").setLayout("default").setUrl("url").setName("name"));
    model.save();
    assertEquals(model.getRepositories().size(), 2);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetRepositories() throws Exception {
    final File pom = getTestPomFile();
    write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
    final Model model =
        Model.readFrom(pom)
            .setRepositories(
                singletonList(
                    new Repository()
                        .setId("id")
                        .setLayout("default")
                        .setUrl("url")
                        .setName("name")));
    model.save();

    assertEquals(model.getRepositories().size(), 1);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToRemoveRepositories() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <repositories>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </repositories>\n"
            + "</project>");

    final Model model = Model.readFrom(pom).setRepositories(null);
    model.save();

    assertTrue(model.getRepositories().isEmpty());
    assertEquals(
        read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
  }

  @Test
  public void shouldBeAbleToGetPluginRepository() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Plugin repositories --> \n"
            + "    <pluginRepositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>"
            + "    </pluginRepositories>\n"
            + "</project>");
    final Model model = Model.readFrom(pom);

    final Repository repository = model.getPluginRepositories().get(0);

    assertEquals(repository.getId(), "central");
    assertEquals(repository.getName(), "Central Repository");
    assertEquals(repository.getUrl(), "http://repo.maven.apache.org/maven2");
    assertEquals(repository.getLayout(), "default");
  }

  @Test
  public void shouldBeAbleToAddPluginRepository() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <pluginRepositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "    </pluginRepositories>\n"
            + "</project>");

    final Model model =
        Model.readFrom(pom)
            .addPluginRepository(
                new Repository().setId("id").setLayout("default").setUrl("url").setName("name"));
    model.save();
    assertEquals(model.getPluginRepositories().size(), 2);
    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <!-- Repositories --> \n"
            + "    <pluginRepositories>\n"
            + "        <repository>\n"
            + "            <id>central</id>\n"
            + "            <name>Central Repository</name>\n"
            + "            <url>http://repo.maven.apache.org/maven2</url>\n"
            + "            <layout>default</layout>\n"
            + "            <releases>\n"
            + "                <enabled>true</enabled>\n"
            + "                <updatePolicy>daily</updatePolicy>\n"
            + "                <checksumPolicy>warn</checksumPolicy>\n"
            + "            </releases>\n"
            + "        </repository>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </pluginRepositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToSetPluginRepositories() throws Exception {
    final File pom = getTestPomFile();
    write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
    final Model model =
        Model.readFrom(pom)
            .setPluginRepositories(
                singletonList(
                    new Repository()
                        .setId("id")
                        .setLayout("default")
                        .setUrl("url")
                        .setName("name")));
    model.save();

    assertEquals(
        read(pom),
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <pluginRepositories>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </pluginRepositories>\n"
            + "</project>");
  }

  @Test
  public void shouldBeAbleToRemovePluginRepositories() throws Exception {
    final File pom = getTestPomFile();
    write(
        pom,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<project>\n"
            + "    <pluginRepositories>\n"
            + "        <repository>\n"
            + "            <id>id</id>\n"
            + "            <name>name</name>\n"
            + "            <url>url</url>\n"
            + "            <layout>default</layout>\n"
            + "        </repository>\n"
            + "    </pluginRepositories>\n"
            + "</project>");

    final Model model = Model.readFrom(pom).setPluginRepositories(null);
    model.save();

    assertTrue(model.getRepositories().isEmpty());
    assertEquals(
        read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" + "</project>");
  }

  private File getTestPomFile() throws URISyntaxException {
    return targetDir().resolve("test-pom.xml").toFile();
  }

  private String read(File file) throws IOException {
    return new String(Files.readAllBytes(file.toPath()));
  }

  private void write(File file, String content) throws IOException {
    Files.write(file.toPath(), content.getBytes());
  }

  private Path targetDir() throws URISyntaxException {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
    assertNotNull(url);
    return Paths.get(url.toURI()).getParent();
  }
}
