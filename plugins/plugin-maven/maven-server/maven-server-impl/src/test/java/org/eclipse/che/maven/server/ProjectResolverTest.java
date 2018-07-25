/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.maven.server;

import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.che.maven.data.MavenModel;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Evgen Vidolob */
public class ProjectResolverTest {

  private MavenServerImpl mavenServer;
  protected static final String wsPath = "target/workspace";

  @BeforeMethod
  public void setUp() throws Exception {
    MavenServerContext.setLoggerAndListener(
        new MavenServerLogger() {

          @Override
          public void warning(Throwable t) throws RemoteException {
            t.printStackTrace();
          }

          @Override
          public void info(Throwable t) throws RemoteException {
            t.printStackTrace();
          }

          @Override
          public void error(Throwable t) throws RemoteException {
            t.printStackTrace();
          }
        },
        (file, relativePath) -> System.out.println(file.getAbsolutePath()));
    MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
    mavenSettings.setMavenHome(new File(System.getenv("M2_HOME")));
    mavenSettings.setGlobalSettings(new File(System.getProperty("user.home"), ".m2/settings.xml"));
    File localRepository = new File("target/localrepo");
    localRepository.mkdirs();
    mavenSettings.setLocalRepository(localRepository);
    mavenServer = new MavenServerImpl(mavenSettings);
    mavenServer.setComponents(
        null,
        true,
        (level, message, throwable) -> {
          System.out.println(message);
        },
        new MavenServerProgressNotifier() {
          @Override
          public void setText(String text) throws RemoteException {}

          @Override
          public void setPercent(double percent) throws RemoteException {}

          @Override
          public void setPercentUndefined(boolean undefined) throws RemoteException {}

          @Override
          public boolean isCanceled() throws RemoteException {
            return false;
          }
        },
        true);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    File localRepository = new File("target/localrepo");
    localRepository.mkdirs();
  }

  @Test
  public void testResolveProject() throws Exception {
    MavenServerResult resolveProject =
        mavenServer.resolveProject(
            new File(MavenServerTest.class.getResource("/FirstProject/pom.xml").getFile()),
            emptyList(),
            emptyList());
    assertNotNull(resolveProject);
    assertNotNull(resolveProject.getProjectInfo());
  }

  @Test
  public void multimoduleProjectWithProfilesShouldBeResolved() throws Exception {
    final File pomFile =
        new File(
            MavenServerTest.class.getResource("/multi-module-with-profiles/pom.xml").getFile());
    MavenServerResult resolveProject =
        mavenServer.resolveProject(pomFile, emptyList(), emptyList());
    assertNotNull(resolveProject);
    MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
    assertNotNull(resolveProject.getProjectInfo());
    MavenModel mavenModel = projectInfo.getMavenModel();
    assertNotNull(mavenModel);
    List<String> modules = mavenModel.getModules();
    assertEquals(3, modules.size());
  }

  @Test
  public void propertiesFromProfileShouldBeExpanded() throws Exception {
    final String content =
        "<artifactId>project-with-properties</artifactId>\n"
            + "    <groupId>com.aw.ad</groupId>\n"
            + "    <packaging>${prop2}</packaging>\n"
            + "    <name>${prop1}</name>\n"
            + "    <version>1.0</version>\n"
            + "    <profiles>\n"
            + "        <profile>\n"
            + "            <id>prop1</id>\n"
            + "            <activation>\n"
            + "                <activeByDefault>true</activeByDefault>\n"
            + "            </activation>\n"
            + "            <properties>\n"
            + "                <prop1>value1</prop1>\n"
            + "            </properties>\n"
            + "        </profile>\n"
            + "        <profile>\n"
            + "            <id>prop2</id>\n"
            + "            <activation>\n"
            + "                <activeByDefault>true</activeByDefault>\n"
            + "            </activation>\n"
            + "            <properties>\n"
            + "                <prop2>jar</prop2>\n"
            + "            </properties>\n"
            + "        </profile>\n"
            + "    </profiles>";
    final File pom = createTestPom("propertiesFromProfile", content);
    MavenServerResult resolveProject = mavenServer.resolveProject(pom, emptyList(), emptyList());
    final MavenModel mavenModel = resolveProject.getProjectInfo().getMavenModel();
    assertEquals("jar", mavenModel.getPackaging());
    assertEquals("value1", mavenModel.getName());
  }

  @Test
  public void profileShouldBeActivatedByProperty() throws Exception {
    final String osProperty = System.getProperty("os.name");
    final String content =
        "<artifactId>project-with-properties</artifactId>\n"
            + "    <groupId>com.aw.ad</groupId>\n"
            + "    <name>${prop1}</name>\n"
            + "    <packaging>jar</packaging>\n"
            + "    <version>1.0</version>\n"
            + "    <profiles>"
            + "        <profile>"
            + "            <id>one</id>"
            + "            <activation>"
            + "                <property>"
            + "                    <name>os.name</name>"
            + "                    <value>"
            + osProperty
            + "</value>"
            + "                </property>"
            + "            </activation>"
            + "        </profile>"
            + "        <profile>"
            + "            <id>two</id>"
            + "            <activation>"
            + "                <property>"
            + "                    <name>os.name</name>"
            + "                    <value>xxx</value>"
            + "                </property>"
            + "            </activation>"
            + "        </profile>"
            + "    </profiles>";

    final File pom = createTestPom("propertiesFromProfile", content);

    MavenServerResult resolveProject = mavenServer.resolveProject(pom, emptyList(), emptyList());
    final MavenModel mavenModel = resolveProject.getProjectInfo().getMavenModel();
    assertNotNull(mavenModel);
  }

  @Test
  public void testProjectHasDependencies() throws Exception {
    MavenServerResult resolveProject =
        mavenServer.resolveProject(
            new File(MavenServerTest.class.getResource("/FirstProject/pom.xml").getFile()),
            emptyList(),
            emptyList());
    assertNotNull(resolveProject);
    MavenProjectInfo projectInfo = resolveProject.getProjectInfo();
    assertNotNull(projectInfo);
    MavenModel mavenModel = projectInfo.getMavenModel();
    assertNotNull(mavenModel);
    List<MavenArtifact> dependencies = mavenModel.getDependencies();
    assertFalse(dependencies.isEmpty());
    assertEquals(26, dependencies.size());
  }

  @Test
  public void testResolveBadProject() throws Exception {
    MavenServerResult resolveProject =
        mavenServer.resolveProject(
            new File(MavenServerTest.class.getResource("/BadProject/pom.xml").getFile()),
            emptyList(),
            emptyList());
    assertNotNull(resolveProject);
    assertNotNull(resolveProject.getProblems());
    assertEquals(1, resolveProject.getProblems().size());
  }

  @Test
  public void testResolveArtifact() throws Exception {
    MavenArtifactKey artifactKey = new MavenArtifactKey("junit", "junit", "3.7", "jar", "");
    MavenArtifact artifact = mavenServer.resolveArtifact(artifactKey, emptyList());
    assertNotNull(artifact);
    assertTrue(artifact.isResolved());
  }

  @Test
  public void testResolveProjectWithRepository() throws Exception {
    MavenServerResult resolveProject =
        mavenServer.resolveProject(
            new File(
                MavenServerTest.class.getResource("/with-remote-repository/pom.xml").getFile()),
            emptyList(),
            emptyList());
    assertNotNull(resolveProject);
    assertEquals(resolveProject.getProjectInfo().getMavenModel().getRemoteRepositories().size(), 3);
  }

  @Test
  public void testResolveArtifactSource() throws Exception {
    MavenArtifactKey artifactKey = new MavenArtifactKey("junit", "junit", "4.11", "jar", "sources");
    MavenArtifact artifact = mavenServer.resolveArtifact(artifactKey, emptyList());
    assertNotNull(artifact);
    assertTrue(artifact.isResolved());
  }

  @Test
  public void testResolveLog4jSource() throws Exception {
    MavenArtifactKey artifactKey =
        new MavenArtifactKey("log4j", "log4j", "1.2.12", "jar", "sources");
    MavenArtifact artifact = mavenServer.resolveArtifact(artifactKey, emptyList());
    assertNotNull(artifact);
    assertFalse(artifact.isResolved());
  }

  @Test
  public void testResolveNotExistingArtifact() throws Exception {
    MavenArtifactKey artifactKey = new MavenArtifactKey("junit", "junit", "3.56", "jar", "");
    MavenArtifact artifact = mavenServer.resolveArtifact(artifactKey, emptyList());
    assertNotNull(artifact);
    assertFalse(artifact.isResolved());
  }

  @Test
  public void testResolveNotExistingArtifactSource() throws Exception {
    MavenArtifactKey artifactKey = new MavenArtifactKey("junit", "junit", "3.56", "jar", "sources");
    MavenArtifact artifact = mavenServer.resolveArtifact(artifactKey, emptyList());
    assertNotNull(artifact);
    assertFalse(artifact.isResolved());
  }

  private File createTestPom(String folderName, String pomContent) throws IOException {
    File file = new File(wsPath, folderName);
    file.mkdirs();
    File pomFile = new File(file, "pom.xml");

    FileOutputStream outputStream = new FileOutputStream(pomFile);
    outputStream.write(getPomContent(pomContent).getBytes());
    outputStream.flush();
    outputStream.close();
    return pomFile;
  }

  private String getPomContent(String content) {
    return "<?xml version=\"1.0\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + content
        + "</project>";
  }
}
