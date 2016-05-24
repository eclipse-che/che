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
package org.eclipse.che.plugin.java.plain.server.generator;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.java.plain.server.projecttype.ClasspathBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.PLAIN_JAVA_PROJECT_ID;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {
    private static final String FILE_NAME    = "Main.java";

    @Inject
    private ClasspathBuilder classpathBuilder;

    @Override
    public void onCreateProject(FolderEntry baseFolder,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        String sourceFolderValue;
        if (attributes.containsKey(SOURCE_FOLDER) && !attributes.get(SOURCE_FOLDER).isEmpty()) {
            sourceFolderValue = attributes.get(SOURCE_FOLDER).getString();
        } else {
            sourceFolderValue = DEFAULT_SOURCE_FOLDER_VALUE;
        }

        baseFolder.createFolder(DEFAULT_OUTPUT_FOLDER_VALUE);
        FolderEntry sourceFolder = baseFolder.createFolder(sourceFolderValue);

        sourceFolder.createFile(FILE_NAME, getClass().getClassLoader().getResourceAsStream("files/main_class_content"));

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(baseFolder.getPath().toString());
        IJavaProject javaProject = JavaCore.create(project);

        classpathBuilder.generateClasspath(javaProject);
    }

    @Override
    public String getProjectType() {
        return PLAIN_JAVA_PROJECT_ID;
    }

}
