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
package org.eclipse.che.plugin.machine.ssh;


import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

/**
 * Implementation of machine that represents ssh machine.
 *
 * @author Alexander Garagatyi
 * @author Max Shaposhnik
 * @see SshMachineInstanceProvider
 */
// todo try to avoid map of processes
public class SshMachineInstance  {
    private static final AtomicInteger pidSequence = new AtomicInteger(1);

    private       String                 id;
    private       String                 workspaceId;
    private final String                 envName;
    private final String                 owner;
    private       MachineRuntimeInfoImpl machineRuntime;
    private final MachineConfig          machineConfig;


    private final SshClient         sshClient;
    private final LineConsumer      outputConsumer;
    private final SshMachineFactory machineFactory;
    private       MachineStatus     status;

    private final Set<ServerConf>                               machinesServers;
    private final ConcurrentHashMap<Integer, SshMachineProcess> machineProcesses;


    public SshMachineInstance(Machine machine,
                              SshClient sshClient,
                              LineConsumer outputConsumer,
                              SshMachineFactory machineFactory,
                              Set<ServerConf> machinesServers) {
        this.id = machine.getId();
        this.workspaceId = machine.getWorkspaceId();
        this.envName = machine.getEnvName();
        this.owner = machine.getOwner();
        this.sshClient = sshClient;
        this.outputConsumer = outputConsumer;
        this.machineFactory = machineFactory;
        this.machineConfig = machine.getConfig();
        this.status = machine.getStatus();
        this.machinesServers = new HashSet<>(machinesServers.size() + machine.getConfig().getServers().size());
        this.machinesServers.addAll(machinesServers);
        this.machinesServers.addAll(machine.getConfig().getServers());
        this.machineProcesses = new ConcurrentHashMap<>();
    }

    public LineConsumer getLogger() {
        return outputConsumer;
    }

    public MachineRuntimeInfoImpl getRuntime() {
        // lazy initialization
        if (machineRuntime == null) {
            synchronized (this) {
                if (machineRuntime == null) {
                    UriBuilder uriBuilder = UriBuilder.fromUri("http://" + sshClient.getHost());

                    final Map<String, ServerImpl> servers = new HashMap<>();
                    for (ServerConf serverConf : machinesServers) {
                        servers.put(serverConf.getPort(), serverConfToServer(serverConf, uriBuilder.clone()));
                    }
                    machineRuntime = new MachineRuntimeInfoImpl(emptyMap(), emptyMap(), servers);
                }
            }
            // todo get env from client
        }
        return machineRuntime;
    }

    public SshMachineProcess getProcess(final int pid) throws NotFoundException, MachineException {
        final SshMachineProcess machineProcess = machineProcesses.get(pid);
        if (machineProcess == null) {
            throw new NotFoundException(format("Process with pid %s not found", pid));
        }
        try {
            machineProcess.checkAlive();
            return machineProcess;
        } catch (NotFoundException e) {
            machineProcesses.remove(pid);
            throw e;
        }
    }

    public List<SshMachineProcess> getProcesses() throws MachineException {
        // todo get children of session process
        return machineProcesses.values()
                               .stream()
                               .filter(SshMachineProcess::isAlive)
                               .collect(Collectors.toList());

    }

    public SshMachineProcess createProcess(Command command, String outputChannel) throws MachineException {
        final Integer pid = pidSequence.getAndIncrement();

        SshMachineProcess machineProcess = machineFactory.createInstanceProcess(command, outputChannel, pid, sshClient);

        machineProcesses.put(pid, machineProcess);

        return machineProcess;
    }


    public void destroy() throws MachineException {
        try {
            outputConsumer.close();
        } catch (IOException ignored) {
        }

        // session destroying stops all processes
        // todo kill all processes started by code, we should get parent pid of session and kill all children
        sshClient.stop();
    }

//    public InstanceNode getNode() {
//        return null;// todo
//    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public void copy(String sourcePath, String targetPath) throws MachineException {
        sshClient.copy(sourcePath, targetPath);
    }

    private ServerImpl serverConfToServer(ServerConf serverConf, UriBuilder uriBuilder) {
        String port = serverConf.getPort().split("/")[0];
        uriBuilder.port(Integer.parseInt(port));
        if (serverConf.getPath() != null) {
            uriBuilder.path(serverConf.getPath());
        }
        URI serverUri = uriBuilder.build();

        return new ServerImpl(serverConf.getRef(),
                              serverConf.getProtocol(),
                              serverUri.getHost() + ":" + serverUri.getPort(),
                              serverConf.getProtocol() != null ? serverUri.toString() : null,
                              null);
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public MachineConfig getMachineConfig() {
        return machineConfig;
    }

    public String getEnvName() {
        return envName;
    }

    public String getOwner() {
        return owner;
    }
}
