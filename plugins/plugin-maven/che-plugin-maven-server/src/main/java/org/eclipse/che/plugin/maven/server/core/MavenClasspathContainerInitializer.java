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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IJavaProject;

/** @author Evgen Vidolob */
@Singleton
public class MavenClasspathContainerInitializer extends ClasspathContainerInitializer {

  private final ClasspathManager classpathManager;
  private final MavenProjectManager mavenProjectManager;

  @Inject
  public MavenClasspathContainerInitializer(
      ClasspathManager classpathManager, MavenProjectManager mavenProjectManager) {
    this.classpathManager = classpathManager;
    this.mavenProjectManager = mavenProjectManager;
  }

  @Override
  public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
    if (isMaven2ClasspathContainer(containerPath)) {

      MavenProject mavenProject = mavenProjectManager.findMavenProject(project.getProject());
      if (mavenProject != null) {
        classpathManager.updateClasspath(mavenProject);
      } else {
        throw new CoreException(
            new Status(
                IStatus.ERROR,
                "maven",
                "Can't find maven project: " + project.getProject().getFullPath().toOSString()));
      }
    }
  }

  public static boolean isMaven2ClasspathContainer(IPath containerPath) {
    return containerPath != null
        && containerPath.segmentCount() > 0
        && MavenClasspathContainer.CONTAINER_ID.equals(containerPath.segment(0));
  }

  @Override
  public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
    return true;
  }
}
