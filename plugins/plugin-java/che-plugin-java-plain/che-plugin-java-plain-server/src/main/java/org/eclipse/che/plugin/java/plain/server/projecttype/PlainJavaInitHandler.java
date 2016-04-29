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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.plugin.java.server.projecttype.AbstractJavaInitHandler;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.PLAIN_JAVA_PROJECT_ID;

/**
 * Init handler for simple java project.
 * Initialize classpath with JRE classpath entry container and 'src' source classpath entry.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler extends AbstractJavaInitHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PlainJavaInitHandler.class);

    @Override
    protected void initializeClasspath(IJavaProject javaProject) throws ServerException {
        IClasspathEntry[] projectClasspath;
        try {
            projectClasspath = javaProject.getRawClasspath();
        } catch (JavaModelException e) {
            LOG.warn("Can't get classpath for: " + javaProject.getProject().getFullPath().toOSString(), e);
            throw new ServerException(e);
        }

        //default classpath
        IClasspathEntry[] defaultClasspath = new IClasspathEntry[]{JavaCore.newSourceEntry(javaProject.getPath())};
        if (!Arrays.equals(defaultClasspath, projectClasspath)) {
            //classpath is already initialized
            return;
        }

        List<IClasspathEntry> classpathEntries = new ArrayList<>();
        //create classpath container for default JRE
        IClasspathEntry jreContainer = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
        classpathEntries.add(jreContainer);

        //by default in simple java project sources placed in 'src' folder
        IFolder src = javaProject.getProject().getFolder(DEFAULT_SOURCE_FOLDER_VALUE);
        //if 'src' folder exist add this folder as source classpath entry
        if (src.exists()) {
            IClasspathEntry sourceEntry = JavaCore.newSourceEntry(src.getFullPath());
            classpathEntries.add(sourceEntry);
        }

        try {
            javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
        } catch (JavaModelException e) {
            LOG.warn("Can't set classpath for: " + javaProject.getProject().getFullPath().toOSString(), e);
            throw new ServerException(e);
        }
    }

    @Override
    public String getProjectType() {
        return PLAIN_JAVA_PROJECT_ID;
    }
}
