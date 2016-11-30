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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Removes temporary workspaces on server startup and shutdown.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class TemporaryWorkspaceRemover {

    private static final Logger LOG = getLogger(TemporaryWorkspaceRemover.class);

    private final WorkspaceDao workspaceDao;

    @Inject
    public TemporaryWorkspaceRemover(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }


    @PostConstruct
    public void initialize() {
        try {
            removeTemporaryWs();
        } catch (ServerException | ConflictException e) {
            LOG.warn("Unable to cleanup temporary workspaces on startup: " + e.getLocalizedMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            removeTemporaryWs();
        } catch (ServerException | ConflictException e) {
            LOG.warn("Unable to cleanup temporary workspaces on shutdown: " + e.getLocalizedMessage(), e);
        }
    }

    private void removeTemporaryWs() throws ServerException, ConflictException {
        for (WorkspaceImpl workspace : workspaceDao.getWorkspaces(true, 0, 0)) {
            workspaceDao.remove(workspace.getId());
        }
    }

}
