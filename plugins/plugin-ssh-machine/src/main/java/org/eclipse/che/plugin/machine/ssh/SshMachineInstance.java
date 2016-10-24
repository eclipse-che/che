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
package org.eclipse.che.plugin.machine.ssh;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.impl.AbstractInstance;

import javax.inject.Inject;
import javax.inject.Named;
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
 * Implementation of {@link Instance} that uses represents ssh machine.
 *
 * @author Alexander Garagatyi
 * @see SshMachineInstanceProvider
 */
// todo try to avoid map of processes
public class SshMachineInstance extends AbstractInstance {
    private static final AtomicInteger pidSequence = new AtomicInteger(1);

    private final SshClient                                   sshClient;
    private final LineConsumer                                outputConsumer;
    private final SshMachineFactory                           machineFactory;

    private final Set<ServerConf>                             machinesServers;
    private final ConcurrentHashMap<Integer, InstanceProcess> machineProcesses;

    private MachineRuntimeInfoImpl machineRuntime;

    @Inject
    public SshMachineInstance(@Assisted Machine machine,
                              @Assisted SshClient sshClient,
                              @Assisted LineConsumer outputConsumer,
                              SshMachineFactory machineFactory,
                              @Named("machine.ssh.machine_servers") Set<ServerConf> machinesServers) {
        super(machine);
        this.sshClient = sshClient;
        this.outputConsumer = outputConsumer;
        this.machineFactory = machineFactory;
        this.machinesServers = new HashSet<>(machinesServers.size() + machine.getConfig().getServers().size());
        this.machinesServers.addAll(machinesServers);
        this.machinesServers.addAll(machine.getConfig().getServers());
        this.machineProcesses = new ConcurrentHashMap<>();
    }

    @Override
    public LineConsumer getLogger() {
        return outputConsumer;
    }

    @Override
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

    @Override
    public InstanceProcess getProcess(final int pid) throws NotFoundException, MachineException {
        final InstanceProcess machineProcess = machineProcesses.get(pid);
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

    @Override
    public List<InstanceProcess> getProcesses() throws MachineException {
        // todo get children of session process
        return machineProcesses.values()
                               .stream()
                               .filter(InstanceProcess::isAlive)
                               .collect(Collectors.toList());

    }

    @Override
    public InstanceProcess createProcess(Command command, String outputChannel) throws MachineException {
        final Integer pid = pidSequence.getAndIncrement();

        SshMachineProcess instanceProcess = machineFactory.createInstanceProcess(command, outputChannel, pid, sshClient);

        machineProcesses.put(pid, instanceProcess);

        return instanceProcess;
    }

    /**
     * Not implemented.<p/>
     *
     * {@inheritDoc}
     */
    @Override
    public MachineSource saveToSnapshot() throws MachineException {
        throw new MachineException("Snapshot feature is unsupported for ssh machine implementation");
    }

    @Override
    public void destroy() throws MachineException {
        try {
            outputConsumer.close();
        } catch (IOException ignored) {
        }

        // session destroying stops all processes
        // todo kill all processes started by code, we should get parent pid of session and kill all children
        sshClient.stop();
    }

    @Override
    public InstanceNode getNode() {
        return null;// todo
    }

    /**
     * Not implemented.<p/>
     *
     * {@inheritDoc}
     */
    @Override
    public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
        // todo
        throw new MachineException("File content reading is not implemented in ssh machine implementation");
    }

    /**
     * Not implemented.<p/>
     *
     * {@inheritDoc}
     */
    @Override
    public void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwrite) throws MachineException {
        //todo
        throw new MachineException("Copying is not implemented in ssh machine implementation");
    }

    @Override
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

}
