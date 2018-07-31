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
package org.eclipse.che.maven.data;

import java.util.List;

/**
 * Data class for maven build config.
 *
 * @author Evgen Vidolob
 */
public class MavenBuild extends MavenBuildBase {
  private static final long serialVersionUID = 1L;

  private String outputDirectory;
  private String testOutputDirectory;

  private List<String> sources;
  private List<String> testSources;

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getTestOutputDirectory() {
    return testOutputDirectory;
  }

  public void setTestOutputDirectory(String testOutputDirectory) {
    this.testOutputDirectory = testOutputDirectory;
  }

  public List<String> getSources() {
    return sources;
  }

  public void setSources(List<String> sources) {
    this.sources = sources;
  }

  public List<String> getTestSources() {
    return testSources;
  }

  public void setTestSources(List<String> testSources) {
    this.testSources = testSources;
  }
}
