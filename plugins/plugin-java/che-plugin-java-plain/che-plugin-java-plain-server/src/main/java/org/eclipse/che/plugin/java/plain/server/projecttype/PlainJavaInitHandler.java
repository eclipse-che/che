/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.plain.server.projecttype;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.plugin.java.server.projecttype.AbstractJavaInitHandler;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

/**
 * Init handler for simple java project.
 * Initialize classpath with JRE classpath entry container and 'src' source classpath entry.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler extends AbstractJavaInitHandler {

    private final ClasspathBuilder classpathBuilder;
    private final Provider<ProjectRegistry> projectRegistryProvider;

    @Inject
    public PlainJavaInitHandler(ClasspathBuilder classpathBuilder, Provider<ProjectRegistry> projectRegistryProvider) {
        this.classpathBuilder = classpathBuilder;
        this.projectRegistryProvider = projectRegistryProvider;
    }

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

        RegisteredProject project = projectRegistryProvider.get().getProject(javaProject.getPath().toOSString());
        List<String> sourceFolders = project.getAttributes().get(Constants.SOURCE_FOLDER);
        List<String> library = project.getAttributes().get(LIBRARY_FOLDER);

        classpathBuilder.generateClasspath(javaProject, sourceFolders, library);
    }

    @Override
    public String getProjectType() {
        return JAVAC;
    }
}
