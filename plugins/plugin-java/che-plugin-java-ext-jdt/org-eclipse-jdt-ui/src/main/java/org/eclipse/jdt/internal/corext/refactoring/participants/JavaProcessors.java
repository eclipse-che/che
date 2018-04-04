/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.participants;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.corext.util.JdtFlags;

/** Utility class to deal with Java element processors. */
public class JavaProcessors {

  public static String[] computeAffectedNatures(IJavaElement element) throws CoreException {
    if (element instanceof IMember) {
      IMember member = (IMember) element;
      if (JdtFlags.isPrivate(member)) {
        return element.getJavaProject().getProject().getDescription().getNatureIds();
      }
    }
    IJavaProject project = element.getJavaProject();
    return ResourceProcessors.computeAffectedNatures(project.getProject());
  }

  public static String[] computeAffectedNaturs(IJavaElement[] elements) throws CoreException {
    Set<String> result = new HashSet<String>();
    for (int i = 0; i < elements.length; i++) {
      String[] natures = computeAffectedNatures(elements[i]);
      for (int j = 0; j < natures.length; j++) {
        result.add(natures[j]);
      }
    }
    return result.toArray(new String[result.size()]);
  }
}
