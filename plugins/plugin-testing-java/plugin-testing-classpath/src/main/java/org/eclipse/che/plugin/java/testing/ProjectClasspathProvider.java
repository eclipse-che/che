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
package org.eclipse.che.plugin.java.testing;

import com.google.inject.name.Named;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Project classpath builder. */
@Singleton
public class ProjectClasspathProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectClasspathProvider.class);

  private final String workspacePath;

  @Inject
  public ProjectClasspathProvider(@Named("che.user.workspaces.storage") String workspacePath) {
    this.workspacePath = workspacePath;
  }

  /**
   * Builds classpath for the java project.
   *
   * @param javaProject java project
   * @return set of resources which are included to the classpath
   */
  public Set<String> getProjectClassPath(IJavaProject javaProject) {
    try {
      IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
      Set<String> result = new HashSet<>();
      for (IClasspathEntry classpathEntry : resolvedClasspath) {
        switch (classpathEntry.getEntryKind()) {
          case IClasspathEntry.CPE_LIBRARY:
            IPath path = classpathEntry.getPath();
            result.add(path.toOSString());
            break;

          case IClasspathEntry.CPE_SOURCE:
            IPath outputLocation = classpathEntry.getOutputLocation();
            if (outputLocation != null) {
              result.add(workspacePath + outputLocation.toOSString());
            }
            break;

          case IClasspathEntry.CPE_PROJECT:
            IPath projectPath = classpathEntry.getPath();
            JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
            IJavaProject project = javaModel.getJavaProject(projectPath.toOSString());
            result.addAll(getProjectClassPath(project));
            break;
        }
      }
      return result;
    } catch (JavaModelException e) {
      LOG.debug(e.getMessage(), e);
    }

    return Collections.emptySet();
  }
}
