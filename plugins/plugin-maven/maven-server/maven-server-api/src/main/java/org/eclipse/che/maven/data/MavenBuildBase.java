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
package org.eclipse.che.maven.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Data class for maven base build config
 *
 * @author Evgen Vidolob
 */
public class MavenBuildBase implements Serializable {
  private static final long serialVersionUID = 1L;

  private String finalName;
  private String defaultGoal;
  private String directory;
  private List<MavenResource> resources = Collections.emptyList();
  private List<MavenResource> testResources = Collections.emptyList();
  private List<String> filters = Collections.emptyList();

  public String getFinalName() {
    return finalName;
  }

  public void setFinalName(String finalName) {
    this.finalName = finalName;
  }

  public String getDefaultGoal() {
    return defaultGoal;
  }

  public void setDefaultGoal(String defaultGoal) {
    this.defaultGoal = defaultGoal;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public List<MavenResource> getResources() {
    return resources;
  }

  public void setResources(List<MavenResource> resources) {
    this.resources = resources;
  }

  public List<String> getFilters() {
    return filters;
  }

  public void setFilters(List<String> filters) {
    this.filters = filters;
  }

  public List<MavenResource> getTestResources() {
    return testResources;
  }

  public void setTestResources(List<MavenResource> testResources) {
    this.testResources = testResources;
  }
}
