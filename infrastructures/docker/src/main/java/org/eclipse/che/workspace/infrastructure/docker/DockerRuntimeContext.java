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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.shared.Utils;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
// TODO Check what if start fails and interruption called or stop called
// TODO interrupted exception, closedbyinteruptionexception

// TODO stop of starting WS - if not supported specific exception
// TODO stop add warning on errors?
// TODO stop in which cases to throw an exception?

// TODO exception on start
// TODO remove starting machine if present
// TODO Check if interruption came from stop or because of another reason
// TODO if because of another reason stop environment
public class DockerRuntimeContext extends RuntimeContext {
    private static final Logger LOG = getLogger(DockerRuntimeContext.class);

    private final NetworkLifecycle  dockerNetworkLifecycle;
    private final MachineStarter    serviceStarter;
    private final DockerEnvironment dockerEnvironment;
    private final URLRewriter       urlRewriter;
    private final Queue<String>     startQueue;
    private final StartSynchronizer startSynchronizer;
    private final String            devMachineName;
    private final ContextsStorage   contextsStorage;

    @Inject
    public DockerRuntimeContext(@Assisted DockerRuntimeInfrastructure infrastructure,
                                @Assisted RuntimeIdentity identity,
                                @Assisted Environment environment,
                                @Assisted DockerEnvironment dockerEnvironment,
                                @Assisted List<String> orderedServices,
                                NetworkLifecycle dockerNetworkLifecycle,
                                MachineStarter serviceStarter,
                                URLRewriter urlRewriter,
                                AgentSorter agentSorter,
                                AgentRegistry agentRegistry,
                                ContextsStorage contextsStorage)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, agentSorter, agentRegistry);
        this.devMachineName = Utils.getDevMachineName(environment);
        this.dockerEnvironment = dockerEnvironment;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>(orderedServices);
        this.urlRewriter = urlRewriter;
        this.contextsStorage = contextsStorage;
        this.startSynchronizer = new StartSynchronizer();
    }

    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws InfrastructureException {
        startSynchronizer.setStartThread();
        try {
            contextsStorage.add(this);
            checkStartInterruption();
            dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());

            String machineName = startQueue.peek();
            DockerMachine dockerMachine;
            while (machineName != null) {
                DockerContainerConfig service = dockerEnvironment.getServices().get(machineName);
                checkStartInterruption();
                dockerMachine = startMachine(machineName, service, startOptions, machineName.equals(devMachineName));
                checkStartInterruption();
                startSynchronizer.addMachine(machineName, dockerMachine);
                startQueue.poll();
                machineName = startQueue.peek();
            }

            return getInternalRuntime();
        } catch (InfrastructureException | RuntimeException e) {
            boolean interrupted = Thread.interrupted();
            contextsStorage.remove(this);
            boolean runtimeDestroyingNeeded = !startSynchronizer.isStopCalled();
            if (runtimeDestroyingNeeded) {
                try {
                    destroyRuntime();
                } catch (Exception destExc) {
                    LOG.error(destExc.getLocalizedMessage(), destExc);
                }
            }
            if (interrupted) {
                throw new InfrastructureException("Docker runtime start was interrupted");
            }
            if (e instanceof InfrastructureException) {
                throw e;
            } else {
                throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        startSynchronizer.interruptStartThread();
        try {
            destroyRuntime();
        } finally {
            contextsStorage.remove(this);
        }
    }

    @Override
    public URL getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private DockerMachine startMachine(String name,
                                       DockerContainerConfig container,
                                       Map<String, String> startOptions,
                                       boolean isDev) throws InfrastructureException {
        DockerMachine dockerMachine;
        // TODO property name
        if ("true".equals(startOptions.get("recover"))) {
            try {
                // TODO set snapshot stuff #5101
//        container = normalizeSource(container, null);
                dockerMachine = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                   name,
                                                   container,
                                                   identity,
                                                   isDev);
            } catch (SourceNotFoundException e) {
                // slip to start without recovering
                dockerMachine = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                            name,
                                                            container,
                                                            identity,
                                                            isDev);
            }
        } else {
            dockerMachine = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                        name,
                                                        container,
                                                        identity,
                                                        isDev);
        }
        startAgents(name, dockerMachine);
        return dockerMachine;
    }

    private InternalRuntime getInternalRuntime() {
        return new DockerInternalRuntime(this,
                                         urlRewriter,
                                         startSynchronizer.getMachines());
    }

    private void destroyRuntime() throws InfrastructureException {
        for (Map.Entry<String, DockerMachine> dockerMachineEntry : startSynchronizer.removeMachines().entrySet()) {
            try {
                // TODO spi snapshot #5101
                dockerMachineEntry.getValue().destroy();
            } catch (InfrastructureException e) {
                LOG.error(format("Error occurs on destroying of docker machine '%s' in workspace '%s'. Container '%s'",
                                 dockerMachineEntry.getKey(),
                                 getIdentity().getWorkspaceId(),
                                 dockerMachineEntry.getValue().getContainer()),
                          e);
            }
        }
        // TODO what happens when context throws exception here
        dockerNetworkLifecycle.destroyNetwork(dockerEnvironment.getNetwork());
    }

    // TODO Do not remove, will be used in snapshot #5101
    private DockerContainerConfig normalizeSource(DockerContainerConfig containerConfig,
                                                  MachineSource machineSource) {
        DockerContainerConfig serviceWithNormalizedSource = new DockerContainerConfig(containerConfig);
        if ("image".equals(machineSource.getType())) {
            serviceWithNormalizedSource.setBuild(null);
            serviceWithNormalizedSource.setImage(machineSource.getLocation());
        } else {
            // dockerfile
            serviceWithNormalizedSource.setImage(null);
            if (machineSource.getContent() != null) {
                serviceWithNormalizedSource.setBuild(new DockerBuildContext(null,
                                                                            null,
                                                                            machineSource.getContent(),
                                                                            null));
            } else {
                serviceWithNormalizedSource.setBuild(new DockerBuildContext(machineSource.getLocation(),
                                                                            null,
                                                                            null,
                                                                            null));
            }
        }
        return serviceWithNormalizedSource;
    }

    // TODO rework to agent launchers
    private void startAgents(String machineName, DockerMachine dockerMachine) throws InfrastructureException {
        InternalMachineConfig machineConfig = internalMachines.get(machineName);
        if (machineConfig == null) {
            throw new InfrastructureException("Machine %s is not found in internal machines config of RuntimeContext");
        }
        for (InternalMachineConfig.ResolvedAgent resolvedAgent : machineConfig.getAgents()) {
            Thread thread = new Thread(() -> {
                try {
                    dockerMachine.exec(resolvedAgent.getScript(), MessageProcessor.getDevNull());
                } catch (InfrastructureException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void checkStartInterruption() throws InfrastructureException {
        if (Thread.interrupted()) {
            throw new InfrastructureException("Docker infrastructure runtime start was interrupted");
        }
    }

    static class StartSynchronizer {
        private Thread                     startThread;
        private boolean                    stopCalled;
        private Map<String, DockerMachine> machines;

        public StartSynchronizer() {
            this.stopCalled = false;
            this.machines = new HashMap<>();
        }

        public synchronized Map<String, DockerMachine> getMachines() {
            return Collections.unmodifiableMap(machines);
        }

        public synchronized void addMachine(String name, DockerMachine machine) throws InternalInfrastructureException {
            if (machines != null) {
                machines.put(name, machine);
            } else {
                throw new InternalInfrastructureException("Machines entities are missing");
            }
        }

        public synchronized Map<String, DockerMachine> removeMachines() throws InfrastructureException {
            if (machines != null) {
                Map<String, DockerMachine> machines = this.machines;
                // unset to identify error if method called second time
                this.machines = null;
                return machines;
            }
            throw new InfrastructureException("");
        }

        public synchronized void setStartThread() throws InternalInfrastructureException {
            if (startThread != null) {
                throw new InternalInfrastructureException("Docker infrastructure context of workspace already started");
            }
            startThread = Thread.currentThread();
        }

        public synchronized void interruptStartThread() throws InfrastructureException {
            if (startThread == null) {
                throw new InternalInfrastructureException("Stop of non started context not allowed");
            }
            if (stopCalled) {
                throw new InternalInfrastructureException("Stop is called twice");
            }
            stopCalled = true;
            startThread.interrupt();
        }

        public synchronized boolean isStopCalled() {
            return stopCalled;
        }
    }
}
