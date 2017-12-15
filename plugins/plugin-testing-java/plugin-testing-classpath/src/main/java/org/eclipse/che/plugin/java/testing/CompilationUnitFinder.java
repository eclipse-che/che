/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.testing;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;

public class CompilationUnitFinder {

  public static ICompilationUnit findCompilationUnitByPath(
      IJavaProject javaProject, String filePath) {
    try {
      IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
      IPath packageRootPath = null;
      for (IClasspathEntry classpathEntry : resolvedClasspath) {
        if (filePath.startsWith(classpathEntry.getPath().toOSString())) {
          packageRootPath = classpathEntry.getPath();
          break;
        }
      }

      if (packageRootPath == null) {
        throw getRuntimeException(filePath);
      }

      String packagePath = packageRootPath.toOSString();
      if (!packagePath.endsWith("/")) {
        packagePath += '/';
      }

      String pathToClass = filePath.substring(packagePath.length());
      IJavaElement element = javaProject.findElement(new Path(pathToClass));
      if (element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT) {
        return (ICompilationUnit) element;
      } else {
        throw getRuntimeException(filePath);
      }
    } catch (JavaModelException e) {
      throw new RuntimeException("Can't find Compilation Unit.", e);
    }
  }

  private static RuntimeException getRuntimeException(String filePath) {
    return new RuntimeException("Can't find IClasspathEntry for path " + filePath);
  }
}
