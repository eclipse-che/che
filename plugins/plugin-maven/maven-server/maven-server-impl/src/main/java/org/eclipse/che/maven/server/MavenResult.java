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
package org.eclipse.che.maven.server;

import java.util.List;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;

/**
 * Contains result of maven invocation.
 *
 * @author Evgen Vidolob
 */
public class MavenResult {
  private MavenProject mavenProject;
  private DependencyResolutionResult dependencyResolutionResult;
  private List<Exception> exceptions;

  public MavenResult(MavenProject mavenProject, List<Exception> exceptions) {
    this(mavenProject, null, exceptions);
  }

  public MavenResult(
      MavenProject mavenProject,
      DependencyResolutionResult dependencyResolutionResult,
      List<Exception> exceptions) {
    this.mavenProject = mavenProject;
    this.dependencyResolutionResult = dependencyResolutionResult;
    this.exceptions = exceptions;
  }

  public MavenProject getMavenProject() {
    return mavenProject;
  }

  public DependencyResolutionResult getDependencyResolutionResult() {
    return dependencyResolutionResult;
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }
}
