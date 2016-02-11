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
package org.eclipse.che.api.git;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.RemoteListRequest;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.InvalidValueException;
import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.ValueStorageException;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.vfs.impl.fs.LocalPathResolver;
import org.eclipse.che.vfs.impl.fs.VirtualFileImpl;

import javax.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.api.git.GitProjectType.VCS_PROVIDER_NAME;
import static org.eclipse.che.api.git.GitProjectType.GIT_CURRENT_BRANCH_NAME;
import static org.eclipse.che.api.git.GitProjectType.GIT_REPOSITORY_REMOTES;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Roman Nikitenko
 */
@Singleton
public class GitValueProviderFactory implements ValueProviderFactory {

    @Inject
    private GitConnectionFactory      gitConnectionFactory;
    @Inject
    private VirtualFileSystemRegistry vfsRegistry;
    @Inject
    private LocalPathResolver         localPathResolver;


    @Override
    public ValueProvider newInstance(final FolderEntry folder) {
        return new ValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {
                try (GitConnection gitConnection =
                             gitConnectionFactory.getConnection(resolveLocalPathByPath(folder.getPath(), folder.getWorkspace()))) {
                    //check whether the folder belongs to git repository
                    if (!gitConnection.isInsideWorkTree()) {
                        return Collections.EMPTY_LIST;
                    }
                    switch (attributeName) {
                        case VCS_PROVIDER_NAME:
                            return Collections.singletonList("git");
                        case GIT_CURRENT_BRANCH_NAME:
                            return Collections.singletonList(gitConnection.status(StatusFormat.LONG).getBranchName());
                        case GIT_REPOSITORY_REMOTES:
                            return gitConnection.remoteList(newDto(RemoteListRequest.class)).stream().map(Remote::getUrl)
                                                .collect(Collectors.toList());
                        default:
                            return Collections.EMPTY_LIST;
                    }
                } catch (ApiException e) {
                    throw new ValueStorageException(e.getMessage());
                }
            }

            @Override
            public void setValues(String attributeName, List<String> value) throws InvalidValueException {
                throw new InvalidValueException(
                        String.format("It is not possible to set value for attribute %s on project %s .git project values are read only",
                                      attributeName, folder.getPath()));
            }
        };
    }

    private String resolveLocalPathByPath(String folderPath, String wsId) throws ApiException {
        VirtualFileSystem vfs = vfsRegistry.getProvider(wsId).newInstance(null);
        Item gitProject = vfs.getItemByPath(folderPath, null, false, PropertyFilter.ALL_FILTER);
        final MountPoint mountPoint = vfs.getMountPoint();
        final VirtualFile virtualFile = mountPoint.getVirtualFile(gitProject.getPath());
        return localPathResolver.resolve((VirtualFileImpl)virtualFile);
    }
}
