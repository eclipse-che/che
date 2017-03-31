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
package org.eclipse.che.api.machine.server;

import org.eclipse.che.api.core.model.machine.OldMachine;
import org.eclipse.che.api.core.model.machine.OldMachineConfig;
import org.eclipse.che.api.core.model.machine.MachineLimits;
import org.eclipse.che.api.core.model.machine.MachineProcess;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.OldServer;
import org.eclipse.che.api.core.model.machine.OldServerConf;
import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.OldMachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.OldMachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.OldServerDto;
import org.eclipse.che.api.machine.shared.dto.OldServerConfDto;
import org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Yevhenii Voevodin
 */
public final class DtoConverter {
    /**
     * Converts {@link OldMachineConfig} to {@link OldMachineConfigDto}.
     */
    public static OldMachineConfigDto asDto(OldMachineConfig config) {
        return newDto(OldMachineConfigDto.class).withName(config.getName())
                                                .withType(config.getType())
                                                .withDev(config.isDev())
                                                .withLimits(config.getLimits() == null ? null : asDto(config.getLimits()))
                                                .withSource(config.getSource() == null ? null : asDto(config.getSource()))
                                                .withServers(config.getServers()
                                                                .stream()
                                                                .map(DtoConverter::asDto)
                                                                .collect(Collectors.toList()))
                                                .withEnvVariables(config.getEnvVariables());
    }

    /**
     * Converts {@link MachineSource} to {@link MachineSourceDto}.
     */
    public static MachineSourceDto asDto(MachineSource source) {
        return newDto(MachineSourceDto.class).withType(source.getType()).withLocation(source.getLocation()).withContent(source.getContent());
    }

    /**
     * Converts {@link MachineLimits} to {@link MachineLimitsDto}.
     */
    public static MachineLimitsDto asDto(MachineLimits machineLimits) {
        return newDto(MachineLimitsDto.class).withRam(machineLimits.getRam());
    }

    /**
     * Converts {@link OldMachine} to {@link OldMachineDto}.
     */
    public static OldMachineDto asDto(OldMachine machine) {
        final OldMachineDto machineDto = newDto(OldMachineDto.class).withConfig(asDto(machine.getConfig()))
                                                                    .withId(machine.getId())
                                                                    .withStatus(machine.getStatus())
                                                                    .withOwner(machine.getOwner())
                                                                    .withEnvName(machine.getEnvName())
                                                                    .withWorkspaceId(machine.getWorkspaceId());
        if (machine.getRuntime() != null) {
            machineDto.withRuntime(asDto(machine.getRuntime()));
        }
        return machineDto;
    }

    /**
     * Converts {@link Machine} to {@link MachineDto}.
     */
    private static MachineDto asDto(Machine runtime) {
        final Map<String, ServerDto>servers = runtime.getServers()
                                                     .entrySet()
                                                     .stream()
                                                     .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue())));

        return newDto(MachineDto.class)//.withEnvVariables(runtime.getEnvVariables())
                                       .withProperties(runtime.getProperties())
                                       .withServers(servers);
    }

    /**
     * Converts {@link OldServer} to {@link OldServerDto}.
     */
    public static ServerDto asDto(Server server) {
        return newDto(ServerDto.class).withUrl(server.getUrl());
                     //  .withAddress(server.getAddress())
                     //  .withRef(server.getRef())
                     //  .withProtocol(server.getProtocol())
                     //  .withUrl(server.getUrl())
                     //  .withProperties(server.getProperties() == null ? null : asDto(server.getProperties()));
    }

    /**
     * Converts {@link ServerProperties} to {@link ServerPropertiesDto}.
     */
    public static ServerPropertiesDto asDto(ServerProperties serverProperties) {
        return newDto(ServerPropertiesDto.class).withPath(serverProperties.getPath())
                       .withInternalAddress(serverProperties.getInternalAddress())
                       .withInternalUrl(serverProperties.getInternalUrl());
    }

    /**
     * Converts {@link OldServerConf} to {@link OldServerConfDto}.
     */
    public static OldServerConfDto asDto(OldServerConf serverConf) {
        return newDto(OldServerConfDto.class).withRef(serverConf.getRef())
                                             .withPort(serverConf.getPort())
                                             .withProtocol(serverConf.getProtocol())
                                             .withPath(serverConf.getPath());
    }

    /**
     * Converts {@link Snapshot} to {@link SnapshotDto}.
     */
    public static SnapshotDto asDto(Snapshot snapshot) {
        return newDto(SnapshotDto.class).withType(snapshot.getType())
                                        .withDescription(snapshot.getDescription())
                                        .withCreationDate(snapshot.getCreationDate())
                                        .withDev(snapshot.isDev())
                                        .withId(snapshot.getId())
                                        .withWorkspaceId(snapshot.getWorkspaceId())
                                        .withLinks(null);
    }

    /**
     * Converts {@link MachineProcess} to {@link MachineProcessDto}.
     */
    public static MachineProcessDto asDto(MachineProcess machineProcess) {
        return newDto(MachineProcessDto.class).withPid(machineProcess.getPid())
                                              .withCommandLine(machineProcess.getCommandLine())
                                              .withAlive(machineProcess.isAlive())
                                              .withName(machineProcess.getName())
                                              .withAttributes(machineProcess.getAttributes())
                                              .withType(machineProcess.getType())
                                              .withOutputChannel(machineProcess.getOutputChannel())
                                              .withLinks(null);
    }

    private DtoConverter() {
    }
}
