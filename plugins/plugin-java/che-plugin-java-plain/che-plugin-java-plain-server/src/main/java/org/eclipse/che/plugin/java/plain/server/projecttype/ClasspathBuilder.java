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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for generating simple classpath for the Java project. Classpath is contained source
 * folder and JRE container.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PlainJavaInitHandler.class);

  /**
   * Generates classpath with default entries.
   *
   * @param project java project which need to contain classpath
   * @param sourceFolders list of the project's source folders
   * @param library list of the project's library folders
   * @throws ServerException happens when some problems with setting classpath
   */
  public void generateClasspath(
      IJavaProject project, List<String> sourceFolders, List<String> library)
      throws ServerException {
    List<IClasspathEntry> classpathEntries = new ArrayList<>();
    // create classpath container for default JRE
    IClasspathEntry jreContainer =
        JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
    classpathEntries.add(jreContainer);

    addSourceFolders(project, sourceFolders, classpathEntries);

    addJars(project, library, classpathEntries);

    try {
      project.setRawClasspath(
          classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
    } catch (JavaModelException e) {
      LOG.warn("Can't set classpath for: " + project.getProject().getFullPath().toOSString(), e);
      throw new ServerException(e);
    }
  }

  private void addJars(
      IJavaProject project, List<String> library, final List<IClasspathEntry> classpathEntries) {
    if (library == null || library.isEmpty()) {
      return;
    }

    for (String libFolder : library) {

      if (libFolder.isEmpty()) {
        continue;
      }

      IFolder libraryFolder = project.getProject().getFolder(libFolder);
      if (!libraryFolder.exists()) {
        return;
      }

      try {
        libraryFolder.accept(
            proxy -> {
              if (IResource.FILE != proxy.getType()) {
                return true;
              }

              IPath path = proxy.requestFullPath();
              if (!path.toString().endsWith(".jar")) {
                return false;
              }

              IClasspathEntry libEntry =
                  JavaCore.newLibraryEntry(proxy.requestResource().getLocation(), null, null);
              classpathEntries.add(libEntry);

              return false;
            },
            IContainer.INCLUDE_PHANTOMS);
      } catch (CoreException e) {
        LOG.warn("Can't read folder structure: " + libraryFolder.getFullPath().toString());
      }
    }
  }

  private void addSourceFolders(
      IJavaProject project, List<String> sourceFolders, List<IClasspathEntry> classpathEntries) {
    for (String source : sourceFolders) {
      IFolder src = project.getProject().getFolder(source);
      if (src.exists()) {
        IClasspathEntry sourceEntry = JavaCore.newSourceEntry(src.getFullPath());
        classpathEntries.add(sourceEntry);
      }
    }
  }
}
