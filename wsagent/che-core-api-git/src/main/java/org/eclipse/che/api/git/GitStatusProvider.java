/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.project.server.VcsStatusProvider;

import javax.inject.Inject;

/**
 * Git implementation of {@link VcsStatusProvider}.
 *
 * @author Igor Vinokur
 */
public class GitStatusProvider implements VcsStatusProvider {
    private final GitConnectionFactory gitConnectionFactory;

    @Inject
    public GitStatusProvider(GitConnectionFactory gitConnectionFactory) {
        this.gitConnectionFactory = gitConnectionFactory;
    }

    @Override
    public String getVcsName() {
        return GitProjectType.TYPE_ID;
    }

    @Override
    public VcsStatus getStatus(String path) throws ServerException {
        try {
            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            Status status = gitConnectionFactory.getConnection(normalizedPath.split("/")[0]).status(StatusFormat.SHORT);
            String itemPath = normalizedPath.substring(normalizedPath.indexOf("/") + 1);
            if (status.getUntracked().contains(itemPath)) {
                return VcsStatus.UNTRACKED;
            } else if (status.getAdded().contains(itemPath)) {
                return VcsStatus.ADDED;
            } else if (status.getModified().contains(itemPath) || status.getChanged().contains(itemPath)) {
                return VcsStatus.MODIFIED;
            } else {
                return VcsStatus.NOT_MODIFIED;
            }
        } catch (GitException e) {
            throw new ServerException(e.getMessage());
        }
    }
}
