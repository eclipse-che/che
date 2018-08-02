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
package org.eclipse.che.plugin.maven.server.core;

import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.resources.IProject;

/** @author Evgen Vidolob */
public class MavenProjectResolveTask implements MavenProjectTask {

  private final MavenProject mavenProject;
  private final MavenProjectManager projectManager;
  private final Runnable afterTask;

  public MavenProjectResolveTask(
      MavenProject mavenProject, MavenProjectManager projectManager, Runnable afterTask) {
    this.mavenProject = mavenProject;
    this.projectManager = projectManager;
    this.afterTask = afterTask;
  }

  @Override
  public void perform() {
    IProject project = mavenProject.getProject();
    if (!project.exists()) {
      return;
    }
    projectManager.resolveMavenProject(project, mavenProject);
    if (afterTask != null) {
      afterTask.run();
    }
  }
}
