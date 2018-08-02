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
