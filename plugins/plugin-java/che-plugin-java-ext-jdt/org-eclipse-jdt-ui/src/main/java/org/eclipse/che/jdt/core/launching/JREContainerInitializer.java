/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/** @author Evgen Vidolob */
public class JREContainerInitializer extends ClasspathContainerInitializer {

  public static String JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";
  private static StandardVMType standardVMType = new StandardVMType();

  @Override
  public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
    if (containerPath.segment(0).equals(JRE_CONTAINER)) {
      IClasspathContainer container = new JREContainer(standardVMType, containerPath, project);
      JavaCore.setClasspathContainer(
          containerPath, new IJavaProject[] {project}, new IClasspathContainer[] {container}, null);
    }
  }
}
