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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.plugin.java.plain.server.projecttype.ClasspathBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_LIBRARY_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {
    private static final String FILE_NAME    = "Main.java";

    @Inject
    private ClasspathBuilder classpathBuilder;

    @Inject
    private VirtualFileSystemProvider virtualFileSystemProvider;

    @Inject
    public PlainJavaProjectGenerator() {
    }

    @VisibleForTesting
    protected PlainJavaProjectGenerator(VirtualFileSystemProvider virtualFileSystemProvider,
                                        ClasspathBuilder classpathBuilder) {
        this.virtualFileSystemProvider = virtualFileSystemProvider;
        this.classpathBuilder = classpathBuilder;
    }

    @Override
    public void onCreateProject(Path projectPath,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        List<String> sourceFolders;
        if (attributes.containsKey(SOURCE_FOLDER) && !attributes.get(SOURCE_FOLDER).isEmpty()) {
            sourceFolders = attributes.get(SOURCE_FOLDER).getList();
        } else {
            sourceFolders = singletonList(DEFAULT_SOURCE_FOLDER_VALUE);
        }

        VirtualFileSystem vfs = virtualFileSystemProvider.getVirtualFileSystem();
        FolderEntry baseFolder  = new FolderEntry(vfs.getRoot().createFolder(projectPath.toString()));
        baseFolder.createFolder(DEFAULT_OUTPUT_FOLDER_VALUE);
        FolderEntry sourceFolder = baseFolder.createFolder(sourceFolders.get(0));

        sourceFolder.createFile(FILE_NAME, getClass().getClassLoader().getResourceAsStream("files/main_class_content"));

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(baseFolder.getPath().toString());
        IJavaProject javaProject = JavaCore.create(project);

        classpathBuilder.generateClasspath(javaProject, sourceFolders, singletonList(DEFAULT_LIBRARY_FOLDER_VALUE));
    }

    @Override
    public String getProjectType() {
        return JAVAC;
    }

}
