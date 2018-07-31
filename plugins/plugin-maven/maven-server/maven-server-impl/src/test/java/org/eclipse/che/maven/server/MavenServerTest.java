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
import static java.util.Collections.singletonList;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.eclipse.che.maven.data.MavenActivation;
import org.eclipse.che.maven.data.MavenActivationFile;
import org.eclipse.che.maven.data.MavenBuild;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenProfile;
import org.eclipse.che.maven.data.MavenResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/** @author Evgen Vidolob */
public class MavenServerTest {

  @Test
  public void testServerCreation() throws RemoteException {
    MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
    mavenSettings.setGlobalSettings(new File(System.getProperty("user.home"), ".m2/settings.xml"));
    new MavenServerImpl(mavenSettings);
  }

  @Test
  public void testGetEffectivePom() throws Exception {
    MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
    String effectivePom =
        new MavenServerImpl(mavenSettings)
            .getEffectivePom(
                new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()),
                emptyList(),
                emptyList());
    Assert.assertNotNull(effectivePom);
  }

  @Test
  public void testCustomComponents() throws Exception {
    MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_DEBUG);
    MavenServerImpl mavenServerImpl = new MavenServerImpl(mavenSettings);
    boolean[] isPrintCalled = new boolean[] {false};
    mavenServerImpl.setComponents(
        null,
        false,
        new MavenTerminal() {

          @Override
          public void print(int level, String message, Throwable throwable) throws RemoteException {
            isPrintCalled[0] = true;
          }
        },
        null,
        false);
    mavenServerImpl.getEffectivePom(
        new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()),
        emptyList(),
        emptyList());
    Assert.assertTrue(isPrintCalled[0]);
  }

  @Test
  public void testInterpolateModel() throws Exception {
    MavenModel model = new MavenModel();
    model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
    model.getBuild().setSources(Collections.singletonList("src/main/java"));
    model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

    MavenModel interpolateModel =
        MavenServerImpl.interpolateModel(
            model, new File(MavenServerTest.class.getResource("/EffectivePom/pom.xml").getFile()));
    Assert.assertNotNull(interpolateModel);
  }

  @Test
  public void profilesShouldBeAnalyzed() throws Exception {
    MavenModel model = new MavenModel();
    model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
    model.getBuild().setSources(Collections.singletonList("src/main/java"));
    model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

    MavenProfile profile = new MavenProfile("id", "pom");
    final MavenActivation activation = new MavenActivation();
    activation.setActiveByDefault(true);
    profile.setActivation(activation);
    final Properties properties = new Properties();
    properties.setProperty("key", "value");
    profile.setProperties(properties);
    model.setProfiles(singletonList(profile));

    final ProfileApplicationResult profileApplicationResult =
        MavenServerImpl.applyProfiles(
            model,
            new File(
                MavenServerTest.class.getResource("/multi-module-with-profiles/pom.xml").getFile()),
            MavenExplicitProfiles.NONE,
            emptyList());
    Assert.assertNotNull(profileApplicationResult);
    Assert.assertEquals(
        1, profileApplicationResult.getActivatedProfiles().getEnabledProfiles().size());
    Assert.assertEquals(1, profileApplicationResult.getModel().getProperties().size());
  }

  @Test
  public void profilesShouldBeActivatedBeExistingFile() throws Exception {
    MavenModel model = new MavenModel();
    model.setMavenKey(new MavenKey("aaa", "bbb", "ccc"));
    model.getBuild().setSources(Collections.singletonList("src/main/java"));
    model.getBuild().setTestSources(Collections.singletonList("src/test/java"));

    MavenProfile profile = new MavenProfile("id", "pom");
    final MavenActivation activation = new MavenActivation();
    activation.setFile(new MavenActivationFile("${basedir}/dir/file.txt", ""));
    profile.setActivation(activation);
    final Properties properties = new Properties();
    properties.setProperty("key", "value");
    profile.setProperties(properties);
    model.setProfiles(singletonList(profile));

    final ProfileApplicationResult profileApplicationResult =
        MavenServerImpl.applyProfiles(
            model,
            new File(MavenServerTest.class.getResource("/multi-module-with-profiles").getFile()),
            MavenExplicitProfiles.NONE,
            emptyList());
    Assert.assertNotNull(profileApplicationResult);
    Assert.assertEquals(
        1, profileApplicationResult.getActivatedProfiles().getEnabledProfiles().size());
    Assert.assertEquals(1, profileApplicationResult.getModel().getProperties().size());
  }

  @Test
  public void testShouldProvideRelativePathsInsteadAbsoluteForSimplePom() throws Exception {
    final MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);

    final MavenServer server = new MavenServerImpl(mavenSettings);
    final MavenServerResult mavenServerResult =
        server.resolveProject(
            new File(MavenServerTest.class.getResource("/SimplePom/pom.xml").getFile()),
            emptyList(),
            emptyList());

    final MavenBuild mavenBuild = mavenServerResult.getProjectInfo().getMavenModel().getBuild();

    final MavenResource expectedMavenResource =
        new MavenResource("src/main/resources", false, null, emptyList(), emptyList());
    final MavenResource expectedMavenTestResource =
        new MavenResource("src/test/resources", false, null, emptyList(), emptyList());

    Assert.assertEquals(mavenBuild.getOutputDirectory(), "target/classes");
    Assert.assertEquals(mavenBuild.getTestOutputDirectory(), "target/test-classes");
    Assert.assertEquals(mavenBuild.getSources(), singletonList("src/main/java"));
    Assert.assertEquals(mavenBuild.getTestSources(), singletonList("src/test/java"));
    Assert.assertEquals(mavenBuild.getResources(), singletonList(expectedMavenResource));
    Assert.assertEquals(mavenBuild.getTestResources(), singletonList(expectedMavenTestResource));
  }

  @Test
  public void testShouldProvideRelativePathsInsteadAbsoluteForComplexPom() throws Exception {
    final MavenSettings mavenSettings = new MavenSettings();
    mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);

    final MavenServer server = new MavenServerImpl(mavenSettings);
    final MavenServerResult mavenServerResult =
        server.resolveProject(
            new File(MavenServerTest.class.getResource("/ComplexPom/pom.xml").getFile()),
            emptyList(),
            emptyList());

    final MavenBuild mavenBuild = mavenServerResult.getProjectInfo().getMavenModel().getBuild();

    final MavenResource expectedMavenResource =
        new MavenResource(
            "resDir1",
            true,
            "targetPath",
            Arrays.asList("include1", "include2"),
            Arrays.asList("exclude1", "exclude2"));
    final MavenResource expectedMavenTestResource =
        new MavenResource(
            "testResDir1",
            true,
            "targetPath",
            Arrays.asList("include1", "include2"),
            Arrays.asList("exclude1", "exclude2"));

    Assert.assertEquals(mavenBuild.getOutputDirectory(), "outputDir");
    Assert.assertEquals(mavenBuild.getTestOutputDirectory(), "testOutputDir");
    Assert.assertEquals(mavenBuild.getSources(), singletonList("srcDir"));
    Assert.assertEquals(mavenBuild.getTestSources(), singletonList("testSrcDir"));
    Assert.assertEquals(mavenBuild.getResources(), singletonList(expectedMavenResource));
    Assert.assertEquals(mavenBuild.getTestResources(), singletonList(expectedMavenTestResource));
  }
}
