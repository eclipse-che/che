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
package org.eclipse.che.plugin.docker.machine.local;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import java.io.IOException;

/**
 *
 * Local directory as a workspace (projects tree) to be bound to Machines
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public class LocalDockerNode implements DockerNode {

    private final String host;

    @Inject
    public LocalDockerNode(@Assisted("workspace") String workspaceId,
                           WorkspaceFolderPathProvider workspaceFolderNodePathProvider,
                           DockerConnectorConfiguration dockerConnectorConfiguration) throws IOException {

        // TODO investigate whether we can remove that after abandonment of native Che
        workspaceFolderNodePathProvider.getPath(workspaceId);
        host = dockerConnectorConfiguration.getDockerHost();
    }

    @Override
    public void bindWorkspace() throws ServerException {}

    @Override
    public void unbindWorkspace() throws ServerException {}

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getIp() {
        return null;
    }
}
