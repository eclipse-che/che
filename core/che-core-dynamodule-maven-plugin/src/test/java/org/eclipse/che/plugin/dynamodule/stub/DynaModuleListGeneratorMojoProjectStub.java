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
package org.eclipse.che.plugin.dynamodule.stub;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;
import org.mockito.Mockito;

/**
 * Stub for the pom.xml file
 *
 * @author Florent Benoit
 */
public class DynaModuleListGeneratorMojoProjectStub extends MavenProjectStub {

  /** {@inheritDoc} */
  @Override
  public File getBasedir() {
    return new File(super.getBasedir() + "/src/test/projects/project");
  }

  private List<Dependency> dependencies;

  /** Default constructor */
  public DynaModuleListGeneratorMojoProjectStub() {
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
    setDependencies(model.getDependencies());

    Build build = new Build();
    build.setFinalName(model.getArtifactId());
    build.setDirectory(getBasedir() + "/target");
    build.setSourceDirectory(getBasedir() + "/src/main/java");
    build.setOutputDirectory(getBasedir() + "/target/classes");
    build.setTestSourceDirectory(getBasedir() + "/src/test/java");
    build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
    setBuild(build);

    List compileSourceRoots = new ArrayList();
    compileSourceRoots.add(getBasedir() + "/src/main/java");
    setCompileSourceRoots(compileSourceRoots);

    List testCompileSourceRoots = new ArrayList();
    testCompileSourceRoots.add(getBasedir() + "/src/test/java");
    setTestCompileSourceRoots(testCompileSourceRoots);
  }

  /** Use of mockito artifact */
  @Override
  public Artifact getArtifact() {
    Artifact artifact = Mockito.mock(Artifact.class);
    when(artifact.getArtifactId()).thenReturn(getModel().getArtifactId());
    when(artifact.getGroupId()).thenReturn(getModel().getGroupId());
    when(artifact.getVersion()).thenReturn(getModel().getVersion());
    when(artifact.getVersionRange())
        .thenReturn(VersionRange.createFromVersion(getModel().getVersion()));
    return artifact;
  }

  @Override
  public Set<Artifact> getDependencyArtifacts() {
    Artifact artifact = Mockito.mock(Artifact.class);
    when(artifact.getArtifactId()).thenReturn(getDependencies().get(0).getArtifactId());
    when(artifact.getGroupId()).thenReturn(getDependencies().get(0).getGroupId());
    when(artifact.getVersion()).thenReturn(System.getProperty("currentVersion"));
    when(artifact.getType()).thenReturn(getDependencies().get(0).getType());
    HashSet hashSet = new HashSet<Artifact>();
    hashSet.add(artifact);
    return hashSet;
  }

  @Override
  public List<Dependency> getDependencies() {
    return dependencies;
  }

  @Override
  public void setDependencies(List<Dependency> dependencies) {
    this.dependencies = dependencies;
  }
}
