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

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.environment.server.AgentConfigApplier;
import org.eclipse.che.api.environment.server.EnvConfigAgentApplier;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Alexander Garagatyi
 */
public class LocalEnvConfigAgentApplier extends EnvConfigAgentApplier {
    private final WorkspaceFolderPathProvider workspaceFolderPathProvider;
    private final String                      projectFolderPath;

    @Inject
    public LocalEnvConfigAgentApplier(AgentConfigApplier agentConfigApplier,
                                      WorkspaceFolderPathProvider workspaceFolderPathProvider,
                                      @Named("che.machine.projects.internal.storage") String projectFolderPath) {
        super(agentConfigApplier);
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        this.projectFolderPath = projectFolderPath;
    }

    @Override
    public void apply(Environment envConf,
                      CheServicesEnvironmentImpl internalEnv) throws AgentException {
        super.apply(envConf, internalEnv);

        // TODO there is no way to find dev-machine in env
        String devMachineName = envConf.getMachines()
                                       .entrySet()
                                       .stream()
                                       .filter(entry -> entry.getValue()
                                                             .getAgents()
                                                             .contains("org.eclipse.che.ws-agent"))
                                       .map(Map.Entry::getKey)
                                       .findAny()
                                       .orElseThrow(() -> new AgentException("ws-machine is not found on agents applying"));


        String projectFolderVolume;
        try {
            projectFolderVolume = format("%s:%s:Z",
                                         workspaceFolderPathProvider.getPath(internalEnv.getWorkspaceId()),
                                         projectFolderPath);
        } catch (IOException e) {
            throw new AgentException("Error occurred on resolving oath to files of workspace " +
                                     internalEnv.getWorkspaceId());
        }
        internalEnv.getServices()
                   .get(devMachineName)
                   .getVolumes()
                   .add(SystemInfo.isWindows() ? escapePath(projectFolderVolume)
                                               : projectFolderVolume);
    }

    // TODO move to commons utilities

    /**
     * Escape path for Windows system with boot@docker according to rules given here :
     * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
     *
     * @param path
     *         path to escape
     * @return escaped path
     */
    @VisibleForTesting
    String escapePath(String path) {
        String esc;
        if (path.indexOf(":") == 1) {
            //check and replace only occurrence of ":" after disk label on Windows host (e.g. C:/)
            // but keep other occurrences it can be marker for docker mount volumes
            // (e.g. /path/dir/from/host:/name/of/dir/in/container                                               )
            esc = path.replaceFirst(":", "").replace('\\', '/');
            esc = Character.toLowerCase(esc.charAt(0)) + esc.substring(1); //letter of disk mark must be lower case
        } else {
            esc = path.replace('\\', '/');
        }
        if (!esc.startsWith("/")) {
            esc = "/" + esc;
        }
        return esc;
    }
}
