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

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_SOURCE;

/**
 * {@link ValueProviderFactory} for Plain Java project type.
 * Factory crates a class which provides values of Plain Java project's attributes.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaValueProviderFactory implements ValueProviderFactory {
    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new PlainJavaValueProvider(projectFolder);
    }

    private class PlainJavaValueProvider extends ReadonlyValueProvider {
        private FolderEntry projectFolder;

        public PlainJavaValueProvider(FolderEntry projectFolder) {
            this.projectFolder = projectFolder;
        }

        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            if (SOURCE_FOLDER.equals(attributeName)) {
                return getSourceFolders();
            } else if (OUTPUT_FOLDER.equals(attributeName)) {
                return getOutputFolder();
            }
            return null;
        }

        private List<String> getOutputFolder() throws ValueStorageException {
            String projectPath = projectFolder.getPath().toString();

            JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
            IJavaProject project = model.getJavaProject(projectPath);

            try {
                return Collections.singletonList(project.getOutputLocation().toOSString());
            } catch (JavaModelException e) {
                throw new ValueStorageException("Can't get output location: " + e.getMessage());
            }
        }

        private List<String> getSourceFolders() throws ValueStorageException {
            List<String> sourceFolders = new ArrayList<>();

            String projectPath = projectFolder.getPath().toString();

            JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
            IJavaProject project = model.getJavaProject(projectPath);

            try {
                IClasspathEntry[] classpath = project.getRawClasspath();

                for (IClasspathEntry entry : classpath) {
                    String entryPath = entry.getPath().toOSString();
                    if (CPE_SOURCE == entry.getEntryKind() && !entryPath.equals(projectPath)) {
                        if (entryPath.startsWith(projectPath)) {
                            sourceFolders.add(entryPath.substring(projectPath.length()));
                        } else {
                            sourceFolders.add(entryPath);
                        }
                    }
                }
            } catch (JavaModelException e) {
                throw new ValueStorageException(
                        "Classpath does not exist or an exception occurs while accessing its corresponding resource : " + e.getMessage());
            }

            return sourceFolders.isEmpty() ? singletonList(DEFAULT_SOURCE_FOLDER_VALUE) : sourceFolders;
        }
    }
}
