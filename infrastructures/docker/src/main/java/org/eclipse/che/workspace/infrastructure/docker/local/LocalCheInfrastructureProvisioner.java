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
package org.eclipse.che.workspace.infrastructure.docker.local;

import com.google.common.base.Strings;

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.workspace.infrastructure.docker.DefaultInfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.InstallerConfigApplier;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.ExecAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.TerminalVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.WorkspaceFolderPathProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.WsAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    public LocalCheInfrastructureProvisioner(InstallerConfigApplier installerConfigApplier,
                                             WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                             WindowsPathEscaper pathEscaper,
                                             @Named("che.workspace.projects.storage") String projectFolderPath,
                                             @Nullable @Named("che.docker.volumes_projects_options") String projectsVolumeOptions,
                                             WsAgentVolumeProvider wsAgentVolumeProvider,
                                             DockerExtConfBindingProvider dockerExtConfBindingProvider,
                                             TerminalVolumeProvider terminalVolumeProvider,
                                             ExecAgentVolumeProvider execAgentVolumeProvider) {
        super(installerConfigApplier);
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
    public void provision(Environment envConfig,
                          DockerEnvironment internalEnv,
                          RuntimeIdentity identity)
            throws InfrastructureException {
        String devMachineName = getDevMachineName(envConfig);
        if (devMachineName == null) {
            throw new InfrastructureException("ws-machine is not found on installers applying");
        }

        DockerContainerConfig devMachine = internalEnv.getContainers().get(devMachineName);

        for (DockerContainerConfig machine : internalEnv.getContainers().values()) {
            ArrayList<String> volumes = new ArrayList<>(machine.getVolumes());
            volumes.add(terminalVolumeProvider.get());
            volumes.add(execVolumeProvider.get());
            machine.setVolumes(volumes);
        }

        // add bind-mount volume for projects in a workspace
        String projectFolderVolume;
        try {
            projectFolderVolume = String.format("%s:%s%s",
                                                workspaceFolderPathProvider.getPath(identity.getWorkspaceId()),
                                                projectFolderPath, projectsVolumeOptions);
        } catch (IOException e) {
            throw new InfrastructureException("Error occurred on resolving path to files of workspace " +
                                              identity.getWorkspaceId());
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
        for (DockerContainerConfig container : internalEnv.getContainers().values()) {
            volumes = new ArrayList<>(container.getVolumes());
            volumes.addAll(SNAPSHOT_EXCLUDED_DIRECTORIES);
            container.setVolumes(volumes);
        }
        HashMap<String, String> environmentVars = new HashMap<>(devMachine.getEnvironment());
        environmentVars.put(CheBootstrap.CHE_LOCAL_CONF_DIR, DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
        devMachine.setEnvironment(environmentVars);

        // apply basic infra (e.g. agents)
        super.provision(envConfig, internalEnv, identity);
    }
}
