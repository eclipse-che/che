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
package org.eclipse.che.plugin.docker.machine.local;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.machine.server.exception.MachineException;
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

    private final String workspaceFolder;
    private final String host;

    @Inject
    public LocalDockerNode(@Assisted("workspace") String workspaceId,
                           WorkspaceFolderPathProvider workspaceFolderNodePathProvider,
                           DockerConnectorConfiguration dockerConnectorConfiguration) throws IOException {

        workspaceFolder = workspaceFolderNodePathProvider.getPath(workspaceId);
        host = dockerConnectorConfiguration.getDockerHost();
    }

    @Override
    public void bindWorkspace() throws MachineException {

    }

    @Override
    public void unbindWorkspace() throws MachineException {

    }

    @Override
    public String getProjectsFolder() {
        return workspaceFolder;
    }


    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getIp() {
        return null;
    }
}
