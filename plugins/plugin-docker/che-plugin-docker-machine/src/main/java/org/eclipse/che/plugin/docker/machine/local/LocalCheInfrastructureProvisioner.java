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

import com.google.common.base.Strings;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.DefaultInfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider;
import org.eclipse.che.plugin.docker.machine.ext.provider.ExecAgentVolumeProvider;
import org.eclipse.che.plugin.docker.machine.ext.provider.TerminalVolumeProvider;
import org.eclipse.che.plugin.docker.machine.ext.provider.WsAgentVolumeProvider;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.shared.Utils.getDevMachineName;

/**
 * Infrastructure provisioner that uses volumes for workspaces projects storing because it relies on local Che usage.
 *
 * @author Alexander Garagatyi
 */
public class LocalCheInfrastructureProvisioner extends DefaultInfrastructureProvisioner {
    private static final List<String> SNAPSHOT_EXCLUDED_DIRECTORIES = Arrays.asList("/tmp");

    private final WorkspaceFolderPathProvider  workspaceFolderPathProvider;
    private final WindowsPathEscaper           pathEscaper;
    private final String                       projectFolderPath;
    private final WsAgentVolumeProvider        wsAgentVolumeProvider;
    private final DockerExtConfBindingProvider dockerExtConfBindingProvider;
    private final TerminalVolumeProvider       terminalVolumeProvider;
    private final ExecAgentVolumeProvider      execVolumeProvider;
    private final String                       projectsVolumeOptions;

    @Inject
    public LocalCheInfrastructureProvisioner(AgentConfigApplier agentConfigApplier,
                                             WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                             WindowsPathEscaper pathEscaper,
                                             @Named("che.workspace.projects.storage") String projectFolderPath,
                                             @Nullable @Named("che.docker.volumes_projects_options") String projectsVolumeOptions,
                                             WsAgentVolumeProvider wsAgentVolumeProvider,
                                             DockerExtConfBindingProvider dockerExtConfBindingProvider,
                                             TerminalVolumeProvider terminalVolumeProvider,
                                             ExecAgentVolumeProvider execAgentVolumeProvider) {
        super(agentConfigApplier);
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.pathEscaper = pathEscaper;
        this.projectFolderPath = projectFolderPath;
        this.wsAgentVolumeProvider = wsAgentVolumeProvider;
        this.dockerExtConfBindingProvider = dockerExtConfBindingProvider;
        this.terminalVolumeProvider = terminalVolumeProvider;
        this.execVolumeProvider = execAgentVolumeProvider;
        if (!Strings.isNullOrEmpty(projectsVolumeOptions)) {
            this.projectsVolumeOptions = ":" + projectsVolumeOptions;
        } else {
            this.projectsVolumeOptions = "";
        }
    }

    @Override
    public void provision(EnvironmentImpl envConfig, CheServicesEnvironmentImpl internalEnv)
            throws EnvironmentException {
        String devMachineName = getDevMachineName(envConfig);
        if (devMachineName == null) {
            throw new EnvironmentException("ws-machine is not found on agents applying");
        }

        CheServiceImpl devMachine = internalEnv.getServices().get(devMachineName);

        for (CheServiceImpl machine : internalEnv.getServices().values()) {
            ArrayList<String> volumes = new ArrayList<>(machine.getVolumes());
            volumes.add(terminalVolumeProvider.get());
            volumes.add(execVolumeProvider.get());
            machine.setVolumes(volumes);
        }

        // add bind-mount volume for projects in a workspace
        String projectFolderVolume;
        try {
            projectFolderVolume = format("%s:%s%s",
                                         workspaceFolderPathProvider.getPath(internalEnv.getWorkspaceId()),
                                         projectFolderPath, projectsVolumeOptions);
        } catch (IOException e) {
            throw new EnvironmentException("Error occurred on resolving path to files of workspace " +
                                           internalEnv.getWorkspaceId());
        }
        List<String> devMachineVolumes = devMachine.getVolumes();
        devMachineVolumes.add(SystemInfo.isWindows() ? pathEscaper.escapePath(projectFolderVolume)
                                                     : projectFolderVolume);
        // add volume with ws-agent archive
        devMachineVolumes.add(wsAgentVolumeProvider.get());
        // add volume and variable to setup ws-agent configuration
        String dockerExtConfVolume = dockerExtConfBindingProvider.get();
        if (dockerExtConfVolume != null) {
            devMachineVolumes.add(dockerExtConfVolume);
        }
        // create volume for each directory to exclude from a snapshot
        List<String> volumes;
        for (CheServiceImpl service : internalEnv.getServices().values()) {
            volumes = new ArrayList<>(service.getVolumes());
            volumes.addAll(SNAPSHOT_EXCLUDED_DIRECTORIES);
            service.setVolumes(volumes);
        }
        HashMap<String, String> environmentVars = new HashMap<>(devMachine.getEnvironment());
        environmentVars.put(CheBootstrap.CHE_LOCAL_CONF_DIR, DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
        devMachine.setEnvironment(environmentVars);

        // apply basic infra (e.g. agents)
        super.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachineImpl machineConfig, CheServiceImpl internalMachine)
            throws EnvironmentException {
        List<String> volumes = internalMachine.getVolumes();
        volumes.add(terminalVolumeProvider.get());
        volumes.add(execVolumeProvider.get());

        super.provision(machineConfig, internalMachine);
    }
}
