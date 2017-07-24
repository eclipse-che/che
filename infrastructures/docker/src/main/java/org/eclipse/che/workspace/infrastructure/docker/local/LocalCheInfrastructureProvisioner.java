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

import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.workspace.infrastructure.docker.InfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.InstallerConfigApplier;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.ExecAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.TerminalVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.WorkspaceFolderPathProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.WsAgentVolumeProvider;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.shared.Utils.getDevMachineName;

/**
 * Infrastructure provisioner that uses volumes for workspaces projects storing because it relies on local Che usage.
 *
 * @author Alexander Garagatyi
 */
// TODO should add default RAM settings, hosts, and all other stuff from MachineStarter
@Singleton
public class LocalCheInfrastructureProvisioner implements InfrastructureProvisioner {
    private static final List<String> SNAPSHOT_EXCLUDED_DIRECTORIES = Collections.singletonList("/tmp");

    private final WorkspaceFolderPathProvider  workspaceFolderPathProvider;
    private final WindowsPathEscaper           pathEscaper;
    private final String                       projectFolderPath;
    private final WsAgentVolumeProvider        wsAgentVolumeProvider;
    private final DockerExtConfBindingProvider dockerExtConfBindingProvider;
    private final TerminalVolumeProvider       terminalVolumeProvider;
    private final ExecAgentVolumeProvider      execVolumeProvider;
    private final String                       projectsVolumeOptions;
    private final InstallerConfigApplier       installerConfigApplier;

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
        this.installerConfigApplier = installerConfigApplier;
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
    public void provision(EnvironmentImpl envConfig,
                          DockerEnvironment internalEnv,
                          RuntimeIdentity identity)
            throws InfrastructureException {
        String devMachineName = getDevMachineName(envConfig);
        if (devMachineName == null) {
            throw new InfrastructureException("ws-machine is not found on installers applying");
        }
        excludeDirsFromSnapshot(internalEnv);
        addProjects(internalEnv, identity.getWorkspaceId(), devMachineName);
        addInstallers(envConfig, internalEnv);
        addLabels(envConfig, internalEnv, identity);
    }

    private void addInstallers(EnvironmentImpl envConfig, DockerEnvironment internalEnv)
            throws InfrastructureException {

        for (Map.Entry<String, MachineConfigImpl> machineConfigEntry : envConfig.getMachines().entrySet()) {
            String machineName = machineConfigEntry.getKey();
            MachineConfigImpl machineConfig = machineConfigEntry.getValue();
            DockerContainerConfig containerConfig = internalEnv.getContainers().get(machineName);

            List<String> installers = machineConfig.getInstallers();
            if (installers.contains("org.eclipse.che.terminal")) {
                containerConfig.getVolumes().add(terminalVolumeProvider.get());
            }
            if (installers.contains("org.eclipse.che.exec")) {
                containerConfig.getVolumes().add(execVolumeProvider.get());
            }
            if (installers.contains("org.eclipse.che.ws-agent")) {
                containerConfig.getVolumes().add(wsAgentVolumeProvider.get());
                String extConfVolume = dockerExtConfBindingProvider.get();
                if (extConfVolume != null) {
                    containerConfig.getVolumes().add(extConfVolume);
                }
                containerConfig.getEnvironment().put(CheBootstrap.CHE_LOCAL_CONF_DIR,
                                                     DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
            }
        }
        try {
            installerConfigApplier.apply(envConfig, internalEnv);
        } catch (InstallerException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
    }

    private void addProjects(DockerEnvironment internalEnv, String workspaceId, String devMachineName)
            throws InfrastructureException {

        DockerContainerConfig containerConfig = internalEnv.getContainers().get(devMachineName);
        containerConfig.getVolumes().add(getProjectsVolumeSpec(workspaceId));
    }

    // create volume for each directory to exclude from a snapshot
    private void excludeDirsFromSnapshot(DockerEnvironment internalEnv) {
        // create volume for each directory to exclude from a snapshot
        internalEnv.getContainers()
                   .values()
                   .forEach(container -> container.getVolumes().addAll(SNAPSHOT_EXCLUDED_DIRECTORIES));
    }

    private void addLabels(EnvironmentImpl envConfig, DockerEnvironment internalEnv, RuntimeIdentity identity) {
        for (Map.Entry<String, ? extends MachineConfig> entry : envConfig.getMachines().entrySet()) {
            String name = entry.getKey();
            DockerContainerConfig container = internalEnv.getContainers().get(name);
            container.getLabels().putAll(Labels.newSerializer()
                                               .machineName(name)
                                               .runtimeId(identity)
                                               .servers(entry.getValue().getServers())
                                               .labels());
        }
    }

    // bind-mount volume for projects in a wsagent container
    private String getProjectsVolumeSpec(String workspaceId) throws InfrastructureException {
        String projectsHostPath;
        try {
            projectsHostPath = workspaceFolderPathProvider.getPath(workspaceId);
        } catch (IOException e) {
            throw new InfrastructureException("Error occurred on resolving path to files of workspace " + workspaceId);
        }
        String volumeSpec = format("%s:%s%s", projectsHostPath, projectFolderPath, projectsVolumeOptions);
        return SystemInfo.isWindows() ? pathEscaper.escapePath(volumeSpec) : volumeSpec;
    }
}
