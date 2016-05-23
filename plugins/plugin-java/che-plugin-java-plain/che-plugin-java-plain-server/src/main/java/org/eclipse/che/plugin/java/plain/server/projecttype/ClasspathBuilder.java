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

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

/**
 * Utility class for generating simple classpath for the Java project. Classpath is contained source folder and JRE container.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PlainJavaInitHandler.class);

    /**
     * Generates classpath with default entries.
     *
     * @param project
     *         java project which need to contain classpath
     * @throws ServerException
     *         happens when some problems with setting classpath
     */
    public void generateClasspath(IJavaProject project) throws ServerException {
        List<IClasspathEntry> classpathEntries = new ArrayList<>();
        //create classpath container for default JRE
        IClasspathEntry jreContainer = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
        classpathEntries.add(jreContainer);

        //by default in simple java project sources placed in 'src' folder
        IFolder src = project.getProject().getFolder(DEFAULT_SOURCE_FOLDER_VALUE);
        //if 'src' folder exist add this folder as source classpath entry
        if (src.exists()) {
            IClasspathEntry sourceEntry = JavaCore.newSourceEntry(src.getFullPath());
            classpathEntries.add(sourceEntry);
        }

        try {
            project.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]), null);
        } catch (JavaModelException e) {
            LOG.warn("Can't set classpath for: " + project.getProject().getFullPath().toOSString(), e);
            throw new ServerException(e);
        }
    }
}
