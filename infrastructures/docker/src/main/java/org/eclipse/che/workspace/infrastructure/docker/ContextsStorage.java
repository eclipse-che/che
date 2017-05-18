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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * @author Alexander Garagatyi
 */
@Singleton
public class ContextsStorage {
    private final Map<String, DockerRuntimeContext> contexts;

    public ContextsStorage() {
        this.contexts = new ConcurrentHashMap<>();
    }

    public void add(DockerRuntimeContext context) throws InternalInfrastructureException {
        String workspaceId = context.getIdentity().getWorkspaceId();
        if (contexts.putIfAbsent(workspaceId, context) != null) {
            throw new InternalInfrastructureException(format("Context of workspace %s already exists", workspaceId));
        }
    }

    public DockerRuntimeContext get(String workspaceId) throws NotFoundException {
        DockerRuntimeContext context = contexts.get(workspaceId);
        if (context == null) {
            throw new NotFoundException(format("Docker runtime context with workspace id %s not found", workspaceId));
        }
        return context;
    }

    public void remove(DockerRuntimeContext context) {
        String workspaceId = context.getIdentity().getWorkspaceId();
        contexts.remove(workspaceId);
    }
}
