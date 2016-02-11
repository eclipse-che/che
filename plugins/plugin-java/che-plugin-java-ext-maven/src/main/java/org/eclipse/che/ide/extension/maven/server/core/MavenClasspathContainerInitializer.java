/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.maven.server.core;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @author Evgen Vidolob
 */
public class MavenClasspathContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
        if (isMaven2ClasspathContainer(containerPath)) {

            IClasspathContainer container = MavenClasspathUtil.readMavenClasspath(project);
            JavaCore.setClasspathContainer(containerPath, new IJavaProject[]{project},
                                           new IClasspathContainer[]{container}, new NullProgressMonitor());
        }
    }

    public static boolean isMaven2ClasspathContainer(IPath containerPath) {
        return containerPath != null && containerPath.segmentCount() > 0
               && MavenClasspathContainer.CONTAINER_ID.equals(containerPath.segment(0));
    }

    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
        return true;
    }
}
