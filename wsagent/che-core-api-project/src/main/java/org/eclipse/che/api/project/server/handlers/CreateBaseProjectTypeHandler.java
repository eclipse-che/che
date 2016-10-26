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
package org.eclipse.che.api.project.server.handlers;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;

/**
 * Handle creation new Blank project and create README file inside root folder of project.
 *
 *  @author Vitalii Parfonov
 */
@Singleton
public class CreateBaseProjectTypeHandler implements CreateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateBaseProjectTypeHandler.class);

    @Inject
    private VirtualFileSystemProvider virtualFileSystemProvider;

    @Inject
    public CreateBaseProjectTypeHandler() {
    }

    @VisibleForTesting
    protected CreateBaseProjectTypeHandler(VirtualFileSystemProvider virtualFileSystemProvider) {
        this.virtualFileSystemProvider = virtualFileSystemProvider;
    }

    private final String README_FILE_NAME = "README";

    @Override
    public void onCreateProject(Path projectPath,
                                Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        VirtualFileSystem vfs = virtualFileSystemProvider.getVirtualFileSystem();
        FolderEntry baseFolder  = new FolderEntry(vfs.getRoot().createFolder(projectPath.toString()));
        baseFolder.createFile(README_FILE_NAME, getReadmeContent());
    }

    @Override
    public String getProjectType() {
        return BaseProjectType.ID;
    }

    @VisibleForTesting
    protected byte[] getReadmeContent() {
        String filename = "README.blank";
        try {
            return toByteArray(getResource(filename));
        } catch (IOException e) {
            LOG.warn("File %s not found so content of %s will be empty.", filename, README_FILE_NAME);
            return new byte[0];
        }
    }
}
