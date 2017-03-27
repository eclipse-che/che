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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String SERVER_ID_GROUP   = "(?<serverId>serverid-[0-9a-z]+)";

    private static final String  CONTAINER_NAME_REGEX   = "^" + NODE_HOST_GROUP + WORKSPACE_ID_GROUP + "_" + MACHINE_ID_GROUP + "_" + SERVER_ID_GROUP + "_.+$";
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile(CONTAINER_NAME_REGEX);
    private static final Logger LOG = LoggerFactory.getLogger(DockerContainerNameGenerator.class);

    /**
     * Return generated name for docker container. Method generate name for docker container in format:
     * <br><p>workspaceId + "_" + machineId + "_"  + serverId + "_" + userName +"_" + machineName</p>
     * <b>Notice: if generated container name contains incorrect symbols for creation docker container, then we skip this symbols</b>
     *
     * @param workspaceId
     *         unique workspace id, see more (@link WorkspaceConfig#getId)
     * @param machineId
     *         unique machine id, see more {@link Machine#getId()}
     * @param serverId
     *         unique server containter id
     * @param userName
     *         name of the user who is docker container owner
     * @param machineName
     *         name of the workspace machine, see more {@link MachineConfig#getName()}
     */
    @Override
    public String generateContainerName(String workspaceId, String machineId, String serverId, String userName, String machineName) {
        String containerName = workspaceId + '_' + machineId + '_' + serverId + '_' + userName + '_' + machineName;
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
            String serverId = matcher.group("serverId");
            containerNameInfo = new ContainerNameInfo(workspaceId, machineId, serverId);
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
        private final String serverId;

        private ContainerNameInfo(String workspaceId, String machineId, String serverId) {
            this.workspaceId = workspaceId;
            this.machineId = machineId;
            this.serverId = serverId;
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

        /**
         * Return serverId of the server.
         */
        public String getServerId() {
            return serverId;
        }

        @Override
        public String toString() {
            return "ContainerNameInfo{" +
                   "workspaceId='" + workspaceId + '\'' +
                   ", machineId='" + machineId + '\'' +
                   ", serverId='" + serverId + '\'' +
                   '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ContainerNameInfo)) return false;
            final ContainerNameInfo other = (ContainerNameInfo)obj;
            return Objects.equals(workspaceId, other.workspaceId)
                   && Objects.equals(machineId, other.machineId)
                   && Objects.equals(serverId, other.serverId);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(workspaceId);
            hash = 31 * hash + Objects.hashCode(machineId);
            hash = 31 * hash + Objects.hashCode(serverId);
            return hash;
        }
    }
}

