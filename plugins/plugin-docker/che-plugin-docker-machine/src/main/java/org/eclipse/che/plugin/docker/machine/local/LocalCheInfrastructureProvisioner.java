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

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

/**
 * Infrastructure provisioner that uses volumes for workspaces projects storing because it relies on local Che usage.
 *
 * @author Alexander Garagatyi
 */
public class LocalCheInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    private final WorkspaceFolderPathProvider workspaceFolderPathProvider;
    private final WindowsPathEscaper          pathEscaper;
    private final String                      projectFolderPath;
	private final String                      volumesOptions;

    @Inject
    public LocalCheInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                             WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                             WindowsPathEscaper pathEscaper,
                                             @Named("che.workspace.projects.storage") String projectFolderPath,
                                             @Named("che.docker.volumes_agent_options") String volumeOptions) {
        super(agentConfigApplier);
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.pathEscaper = pathEscaper;
        this.projectFolderPath = projectFolderPath;
        this.volumesOptions = volumeOptions;
    }

    @Override
    public void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        // find dev machine name
        String devMachineName = envConfig.getMachines()
                                         .entrySet()
                                         .stream()
                                         .filter(entry -> entry.getValue()
                                                               .getAgents() != null &&
                                                          entry.getValue()
                                                               .getAgents()
                                                               .contains("org.eclipse.che.ws-agent"))
                                         .map(Map.Entry::getKey)
                                         .findAny()
                                         .orElseThrow(() -> new EnvironmentException(
                                                 "ws-machine is not found on agents applying"));

        // add bind-mount volume for projects in a workspace
        String projectFolderVolume;
        try {
            projectFolderVolume = format("%s:%s:%s",
                                         workspaceFolderPathProvider.getPath(internalEnv.getWorkspaceId()),
                                         projectFolderPath, volumesOptions);
        } catch (IOException e) {
            throw new EnvironmentException("Error occurred on resolving path to files of workspace " +
                                           internalEnv.getWorkspaceId());
        }
        internalEnv.getServices()
                   .get(devMachineName)
                   .getVolumes()
                   .add(SystemInfo.isWindows() ? pathEscaper.escapePath(projectFolderVolume)
                                               : projectFolderVolume);

        // apply basic infra (e.g. agents)
        super.provision(envConfig, internalEnv);
    }
}
