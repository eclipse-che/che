/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.server.project.type;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.ide.ant.tools.AntUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.extension.ant.shared.AntAttributes.BUILD_FILE;
import static org.eclipse.che.ide.extension.ant.shared.AntAttributes.DEF_TEST_SRC_PATH;
import static org.eclipse.che.ide.extension.ant.shared.AntAttributes.SOURCE_FOLDER;
import static org.eclipse.che.ide.extension.ant.shared.AntAttributes.TEST_SOURCE_FOLDER;

/**
 * Provide value for specific property from Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntValueProviderFactory implements ValueProviderFactory {

    /**
     * Try to find build.xml in project root directory and parse it into {@link org.apache.tools.ant.Project} to ba able to obtain various
     * information from Ant build file.
     *
     * @param project
     *         current opened project in Codenvy
     * @return {@link org.apache.tools.ant.Project} object of parsed build file
     * @throws ServerException
     *         if error occurred while getting file on server side
     * @throws ForbiddenException
     *         if access to build file is forbidden
     * @throws ValueStorageException
     */
    protected VirtualFile getBuildXml(FolderEntry project) throws ServerException, ForbiddenException, ValueStorageException {
        VirtualFileEntry buildXml = project.getChild(BUILD_FILE);
        if (buildXml == null) {
            throw new ValueStorageException(BUILD_FILE + " does not exist.");
        }
        return buildXml.getVirtualFile();
    }

    /** @return instance of {@link ValueStorageException} with specified message. */
    protected ValueStorageException readException(Exception e) {
        return new ValueStorageException("Can't read build.xml: " + e.getMessage());
    }

    /** @return instance of {@link ValueStorageException} with specified message. */
    protected ValueStorageException writeException(Exception e) {
        return new ValueStorageException("Can't write build.xml: " + e.getMessage());
    }

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new AntValueProvider(projectFolder);
    }



    /** Provide access to value of various information from {@link org.apache.tools.ant.Project}. */
    protected class AntValueProvider implements ValueProvider {
        /** IDE project. */
        private final FolderEntry projectFolder;

        /** Create instance of {@link AntValueProvider}. */
        protected AntValueProvider(FolderEntry projectFolder) {
            this.projectFolder = projectFolder;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            try {
                org.apache.tools.ant.Project antProject = AntUtils.readProject(getBuildXml(projectFolder));
                if (SOURCE_FOLDER.equals(attributeName)) {
                    String srcDir = antProject.getProperty("src.dir");
                    if (srcDir == null) {
                        srcDir = DEF_TEST_SRC_PATH;
                    } else {
                        // Don't show absolute path (seems Ant parser resolves it automatically). User shouldn't know any absolute paths on our
                        // file system. This is temporary solution, this shouldn't be actual when get rid form ant parsers for build.xml files.
                        final java.nio.file.Path relPath = antProject.getBaseDir().toPath().relativize(Paths.get(srcDir));
                        srcDir = relPath.toString();
                    }
                    return Arrays.asList(srcDir);
                } else if(TEST_SOURCE_FOLDER.equals(attributeName)) {
                    String testDir = antProject.getProperty("test.dir");
                    if (testDir == null) {
                        testDir = DEF_TEST_SRC_PATH;
                    }
                    return Arrays.asList(testDir);
                }
                return Collections.emptyList();
            } catch (IOException | ForbiddenException | ServerException e) {
                throw readException(e);
            }
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException {

        }

    }
}
