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
package org.eclipse.che.plugin.maven.server.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class MavenClasspathUtil {
  private static final IClasspathEntry[] EMPTY = new IClasspathEntry[0];
  private static final Logger LOG = LoggerFactory.getLogger(MavenClasspathUtil.class);

  public static IClasspathContainer readMavenClasspath(IJavaProject javaProject) {
    IFile file = javaProject.getProject().getFile(".che/classpath.maven");
    IClasspathEntry[] entries;
    if (file.exists()) {
      try {
        char[] chars = Util.getResourceContentsAsCharArray(file);
        String content = new String(chars);
        if (!content.isEmpty()) {
          String[] pathToJars = content.split(":");
          List<IClasspathEntry> classpathEntry = new ArrayList<>();
          for (String path : pathToJars) {
            String srcPath = path.substring(0, path.lastIndexOf('.')) + "-sources.jar";
            classpathEntry.add(
                JavaCore.newLibraryEntry(
                    new org.eclipse.core.runtime.Path(path),
                    new org.eclipse.core.runtime.Path(srcPath),
                    null));
          }
          entries = classpathEntry.toArray(new IClasspathEntry[classpathEntry.size()]);
        } else {
          entries = EMPTY;
        }
      } catch (JavaModelException e) {
        LOG.error("Can't read maven classpath.", e);
        entries = EMPTY;
      }
    } else {
      entries = EMPTY;
    }

    return new MavenClasspathContainer(entries);
  }
}
