/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.gwt.stub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ProjectStub extends MavenProjectStub {

  public ProjectStub() {
    MavenXpp3Reader pomReader = new MavenXpp3Reader();
    Model model;
    try {
      model = pomReader.read(ReaderFactory.newXmlReader(new File(getBasedir(), "pom.xml")));
      setModel(model);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    setGroupId(model.getGroupId());
    setArtifactId(model.getArtifactId());
    setVersion(model.getVersion());
    setName(model.getName());
    setUrl(model.getUrl());
    setPackaging(model.getPackaging());

    List<String> compileSourceRoots = new ArrayList<>();
    compileSourceRoots.add(getBasedir() + "/src/main/java");
    setCompileSourceRoots(compileSourceRoots);

    List<String> testCompileSourceRoots = new ArrayList<>();
    testCompileSourceRoots.add(getBasedir() + "/src/test/java");
    setTestCompileSourceRoots(testCompileSourceRoots);

    setupBuild(model);
    setupDependencyArtifacts(model);
  }

  private void setupDependencyArtifacts(Model model) {
    Set<Artifact> artifacts = new HashSet<>();
    ArtifactStubFactory artifactStubFactory = new ArtifactStubFactory();

    for (Dependency dependency : model.getDependencies()) {
      Artifact artifact;

      try {
        artifact =
            artifactStubFactory.createArtifact(
                dependency.getGroupId(),
                dependency.getArtifactId(),
                System.getProperty("currentVersion"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      List<String> patterns =
          dependency
              .getExclusions()
              .stream()
              .map(exclusion -> exclusion.getGroupId() + ":" + exclusion.getArtifactId())
              .collect(Collectors.toList());

      artifact.setDependencyFilter(new ExcludesArtifactFilter(patterns));
      artifacts.add(artifact);
    }

    setDependencyArtifacts(artifacts);
  }

  private void setupBuild(Model model) {
    Build build = new Build();

    build.setFinalName(model.getArtifactId());
    build.setDirectory(getBasedir() + "/target");
    build.setSourceDirectory(getBasedir() + "/src/main/java");
    build.setOutputDirectory(getBasedir() + "/target/classes");
    build.setTestSourceDirectory(getBasedir() + "/src/test/java");
    build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
    build.setPlugins(model.getBuild().getPlugins());

    setBuild(build);
  }

  @Override
  public File getBasedir() {
    return new File(super.getBasedir() + "/src/test/projects/" + getProjectFolder());
  }

  @Override
  public Xpp3Dom getGoalConfiguration(
      String pluginGroupId, String pluginArtifactId, String executionId, String goalId) {
    Plugin plugin = getPlugin(pluginGroupId + ':' + pluginArtifactId);
    Object configuration = plugin.getConfiguration();

    return (Xpp3Dom) configuration;
  }

  protected String getProjectFolder() {
    return "project";
  }
}
