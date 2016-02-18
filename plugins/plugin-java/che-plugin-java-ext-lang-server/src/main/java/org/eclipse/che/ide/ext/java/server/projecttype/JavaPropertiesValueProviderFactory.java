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
package org.eclipse.che.ide.ext.java.server.projecttype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.InvalidValueException;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.ide.ext.java.shared.Constants;

import java.util.List;

import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.ext.java.shared.Constants.CONTAINS_JAVA_FILES;

/**
 * Value
 *
 * @author gazarenkov
 * @author Florent Benoit
 */
public class JavaPropertiesValueProviderFactory implements ValueProviderFactory {

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new JavaPropertiesValueProvider(projectFolder);
    }

    static class JavaPropertiesValueProvider implements ValueProvider {

        /**
         * If true, it means that there are some java files in this folder or in its children.
         */
        private boolean containsJavaFiles;

        /**
         * Try to perform the check on java files only once with lazy check.
         */
        private boolean initialized = false;

        /**
         * The root folder of this project.
         */
        private final FolderEntry rootFolder;

        public JavaPropertiesValueProvider(final FolderEntry projectFolder) {
            this.rootFolder = projectFolder;
            this.initialized = false;
        }

        /**
         * Check recursively if the given folder contains java files or any of its children
         *
         * @param folderEntry
         *         the initial folder to check
         * @return true if the folder or a subfolder contains java files
         */
        protected boolean hasJavaFilesInFolder(final FolderEntry folderEntry) {
            try {
                return folderEntry.getChildFolders().stream().anyMatch(this::hasJavaFilesInFolder) ||
                       folderEntry.getChildFiles().stream().anyMatch(fileEntry -> fileEntry.getName().endsWith(".java"));
            } catch (ServerException e) {
                throw new IllegalStateException(String.format("Unable to get files from ''%s''", folderEntry.getName()), e);
            }
        }

        /**
         * Checks if java files are available in the root folder or in any children of the root folder
         *
         * @throws ValueStorageException
         *         if there is an error when checking
         */
        protected void init() throws ValueStorageException {
            try {
                this.containsJavaFiles = hasJavaFilesInFolder(rootFolder);
            } catch (IllegalStateException e) {
                throw new ValueStorageException(String.format("Unable to get files from ''%s''", rootFolder.getName()) + e.getMessage());
            }
            this.initialized = true;

        }

        @Override
        public List<String> getValues(final String attributeName) throws ValueStorageException {
            if (!initialized) {
                init();
            }
            if (attributeName.equals(Constants.LANGUAGE_VERSION)) {
                return singletonList(System.getProperty("java.version"));
            } else if (CONTAINS_JAVA_FILES.equals(attributeName)) {
                if (containsJavaFiles) {
                    return singletonList(valueOf(containsJavaFiles));
                } else {
                    throw new ValueStorageException("There are no Java files inside the project");
                }
            }
            return null;
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {

        }
    }
}