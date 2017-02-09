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
package org.eclipse.che.plugin.svn.server;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.plugin.svn.shared.SubversionTypeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ValueProviderFactory} for indicating the project is a Subversion working copy.
 */
public class SubversionValueProviderFactory implements ValueProviderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SubversionValueProviderFactory.class);

    private final SubversionApi subversionApi;

    @Inject
    public SubversionValueProviderFactory(final SubversionApi subversionApi) {
        this.subversionApi = subversionApi;
    }

    @Override
    public ValueProvider newInstance(final FolderEntry project) {
        return new ReadonlyValueProvider() {
            @Override
            public List<String> getValues(final String attributeName) throws ValueStorageException {
                if (project == null) {
                    return Collections.emptyList();
                }
                LOG.debug("Asked value for attribute {}.", attributeName);
                if (attributeName == null) {
                    throw new ValueStorageException("Invalid attribute name: null");
                }
                switch (attributeName) {
                    case SubversionTypeConstant.SUBVERSION_ATTRIBUTE_REPOSITORY_URL:
                        final List<String> result = getRepositoryUrl(project);
                        LOG.debug("Attribute {}, returning value {}", attributeName,
                                  Arrays.toString(result.toArray(new String[result.size()])));
                        return result;
                    default:
                        throw new ValueStorageException("Unsupported attribute: " + attributeName);
                }
            }
        };
    }

    private List<String> getRepositoryUrl(final FolderEntry project) throws ValueStorageException {
        try {
            if (isSvn(project)) {
                final String path = getProjectPath(project);
                if (path != null) {
                    final String response = subversionApi.getRepositoryUrl(path);
                    return Collections.singletonList(response);
                } else {
                    LOG.debug("invalid project path");
                    throw new ValueStorageException("invalid project path");
                }
            } else {
                return Collections.emptyList();
            }
        } catch (ForbiddenException | ServerException e) {
            LOG.debug("svn info error", e);
            throw new ValueStorageException(e.getMessage());
        }
    }

    private boolean isSvn(final FolderEntry project) throws ForbiddenException, ServerException {
        LOG.debug("Searching for '.svn' in {}.", project.getPath());
        final VirtualFileEntry svn = project.getChild(".svn");
        if (svn != null && svn instanceof FolderEntry) {
            LOG.debug("Found it.");
            return true;
        } else {
            LOG.debug("Didn't find it.");
            return false;
        }
    }

    /**
     * Build the project absolute path.
     * 
     * @param project the project
     * @return the path, or null if this is not a valid path
     */
    private String getProjectPath(final FolderEntry project) {
        return project.getVirtualFile().toIoFile().getAbsolutePath();
    }
}
