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
package org.eclipse.che.plugin.csharp.projecttype;

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
import org.eclipse.che.plugin.csharp.shared.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;

/**
 * @author Evgen Vidolob
 */
public class CreateNetCoreProjectHandler implements CreateProjectHandler {

    @Inject
    private VirtualFileSystemProvider virtualFileSystemProvider;

    private static final Logger LOG = LoggerFactory.getLogger(CreateNetCoreProjectHandler.class);

    private final String PROJECT_FILE_NAME = "project.json";

    @Override
    public void onCreateProject(Path projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        VirtualFileSystem vfs = virtualFileSystemProvider.getVirtualFileSystem();
        FolderEntry baseFolder  = new FolderEntry(vfs.getRoot().createFolder(projectPath.toString()));
        baseFolder.createFile(PROJECT_FILE_NAME, getProjectContent());
    }

    private byte[] getProjectContent() {
        String filename = "project.json.default";
        try {
            return toByteArray(getResource(filename));
        } catch (IOException e) {
            LOG.warn("File %s not found so content of %s will be empty.", filename, PROJECT_FILE_NAME);
            return new byte[0];
        }
    }

    @Override
    public String getProjectType() {
        return Constants.CSHARP_PROJECT_TYPE_ID;
    }
}
