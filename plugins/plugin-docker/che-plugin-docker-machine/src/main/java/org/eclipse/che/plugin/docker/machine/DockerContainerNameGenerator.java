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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.environment.server.ContainerNameGenerator;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for generation docker container name or parsing important information from docker container name.
 *
 * @author Alexander Andrienko
 */
public class DockerContainerNameGenerator implements ContainerNameGenerator {
    private static final String NODE_HOST_GROUP    = "(/|(/[0-9a-z.-]+/))?";
    private static final String WORKSPACE_ID_GROUP = "(?<workspaceId>workspace[0-9a-z]+)";
    private static final String MACHINE_ID_GROUP   = "(?<machineId>machine[0-9a-z]+)";

    private static final String  CONTAINER_NAME_REGEX   = "^" + NODE_HOST_GROUP + WORKSPACE_ID_GROUP + "_" + MACHINE_ID_GROUP + "_.+$";
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile(CONTAINER_NAME_REGEX);

    /**
     * Return generated name for docker container. Method generate name for docker container in format:
     * <br><p>workspaceId + "_" + machineId + "_" + userName +"_" + machineName</p>
     * <b>Notice: if generated container name contains incorrect symbols for creation docker container, then we skip this symbols</b>
     *
     * @param workspaceId
     *         unique workspace id, see more (@link WorkspaceConfig#getId)
     * @param machineId
     *         unique machine id, see more {@link Machine#getId()}
     * @param userName
     *         name of the user who is docker container owner
     * @param machineName
     *         name of the workspace machine, see more {@link MachineConfig#getName()}
     */
    @Override
    public String generateContainerName(String workspaceId, String machineId, String userName, String machineName) {
        String containerName = workspaceId + '_' + machineId + '_' + userName + '_' + machineName;
        return containerName.toLowerCase().replaceAll("[^a-z0-9_-]+", "");
    }

    /**
     * Parse machine's {@code containerName} to get information about this container (like workspaceId, machineId).
     * Notice: method doesn't parse information about userName or machineName, because we do not give guarantees
     * about the integrity of this data(see more {@link #generateContainerName(String, String, String, String)})
     * Notice: container name can contains node host (e.g. "/node-host.dev.box/workspacebbx2_machineic3_user321_ws-machine)
     *
     * @param containerName
     *         name of the container
     * @return information about container
     */
    public Optional<ContainerNameInfo> parse(String containerName) {
        Matcher matcher = CONTAINER_NAME_PATTERN.matcher(containerName);
        ContainerNameInfo containerNameInfo = null;
        if (matcher.matches()) {
            String workspaceId = matcher.group("workspaceId");
            String machineId = matcher.group("machineId");
            containerNameInfo = new ContainerNameInfo(workspaceId, machineId);
        }
        return Optional.ofNullable(containerNameInfo);
    }

    /**
     * Class contains information about docker container, which was parsed from docker container name.
     * Usually used {@link #parse(String)}
     */
    public static class ContainerNameInfo {

        private final String workspaceId;
        private final String machineId;

        private ContainerNameInfo(String workspaceId, String machineId) {
            this.workspaceId = workspaceId;
            this.machineId = machineId;
        }

        /**
         * Return machineId of the docker container.
         */
        public String getMachineId() {
            return machineId;
        }

        /**
         * Return workspaceId of the docker container.
         */
        public String getWorkspaceId() {
            return workspaceId;
        }

        @Override
        public String toString() {
            return "ContainerNameInfo{" +
                   "workspaceId='" + workspaceId + '\'' +
                   ", machineId='" + machineId + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ContainerNameInfo)) return false;
            final ContainerNameInfo other = (ContainerNameInfo)obj;
            return Objects.equals(workspaceId, other.workspaceId)
                   && Objects.equals(machineId, other.machineId);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(workspaceId);
            hash = 31 * hash + Objects.hashCode(machineId);
            return hash;
        }
    }
}

