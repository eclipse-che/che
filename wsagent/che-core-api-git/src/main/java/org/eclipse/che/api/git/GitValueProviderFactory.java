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
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.api.git.GitProjectType.GIT_CURRENT_BRANCH_NAME;
import static org.eclipse.che.api.git.GitProjectType.GIT_REPOSITORY_REMOTES;
import static org.eclipse.che.api.git.GitProjectType.VCS_PROVIDER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Roman Nikitenko
 */
@Singleton
public class GitValueProviderFactory implements ValueProviderFactory {

    @Inject
    private GitConnectionFactory gitConnectionFactory;

    @Override
    public ValueProvider newInstance(final FolderEntry folder) {
        return new ReadonlyValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {
                if (folder == null) {
                    return Collections.emptyList();
                }
                try (GitConnection gitConnection = gitConnectionFactory.getConnection(resolveLocalPath(folder))) {
                    //check whether the folder belongs to git repository
                    if (!gitConnection.isInsideWorkTree()) {
                        return Collections.emptyList();
                    }

                    switch (attributeName) {
                        case VCS_PROVIDER_NAME:
                            return Collections.singletonList("git");
                        case GIT_CURRENT_BRANCH_NAME:
                            return Collections.singletonList(gitConnection.getCurrentBranch());
                        case GIT_REPOSITORY_REMOTES:
                            return gitConnection.remoteList(newDto(RemoteListRequest.class))
                                                .stream()
                                                .map(Remote::getUrl)
                                                .collect(Collectors.toList());
                        default:
                            return Collections.emptyList();
                    }
                } catch (ApiException e) {
                    throw new ValueStorageException(e.getMessage());
                }
            }
        };
    }

    private String resolveLocalPath(FolderEntry folder) throws ApiException {
        return folder.getVirtualFile().toIoFile().getAbsolutePath();
    }
}
