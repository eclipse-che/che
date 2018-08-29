/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;

public class ResourceUtil {

  private ResourceUtil() {}

  public static IFile[] getFiles(ICompilationUnit[] cus) {
    List<IResource> files = new ArrayList<IResource>(cus.length);
    for (int i = 0; i < cus.length; i++) {
      IResource resource = cus[i].getResource();
      if (resource != null && resource.getType() == IResource.FILE) files.add(resource);
    }
    return files.toArray(new IFile[files.size()]);
  }

  public static IFile getFile(ICompilationUnit cu) {
    IResource resource = cu.getResource();
    if (resource != null && resource.getType() == IResource.FILE) return (IFile) resource;
    else return null;
  }

  // ----- other ------------------------------

  public static IResource getResource(Object o) {
    if (o instanceof IResource) return (IResource) o;
    if (o instanceof IJavaElement) return getResource((IJavaElement) o);
    return null;
  }

  private static IResource getResource(IJavaElement element) {
    if (element.getElementType() == IJavaElement.COMPILATION_UNIT)
      return ((ICompilationUnit) element).getResource();
    else if (element instanceof IOpenable) return element.getResource();
    else return null;
  }
}
