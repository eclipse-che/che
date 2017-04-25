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
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;
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
import static org.eclipse.che.plugin.docker.client.MessageProcessor.DEV_NULL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
// TODO workspace start interruption
// TODO snapshots, create, remove
// TODO backups
// TODO containers monitoring
// TODO consider reworking synchronized, it into separate component; volatile or something else?

// TODO what should be thrown if start is interrupted in another thread, in current thread
// TODO which error will be thrown if start fails because of something we don't control
// and admin doesn't want to know about this problem
// TODO if this thread is interrupted and stop is not called!
// TODO interrupted exception, closedbyinteruptionexception
// TODO add infrastructural things such as backup/restore
// TODO simplify code ub start env method

// TODO stop of starting WS - if not supported specific exception
// TODO stop add warning on errors?
// TODO stop in which cases to throw an exception?

// TODO proper exception in case of interruption in start/stop threads

// TODO exception on start
// TODO remove starting machine if present
// TODO Check if interruption came from stop or because of another reason
// TODO if because of another reason stop environment
public class DockerRuntimeContext extends RuntimeContext {
    private static final Logger LOG = getLogger(DockerRuntimeContext.class);

    private final NetworkLifecycle  dockerNetworkLifecycle;
    private final ServiceStarter    serviceStarter;
    private final DockerEnvironment dockerEnvironment;
    private final URLRewriter       urlRewriter;
    private final Queue<String>     startQueue;
    private final StartSynchronizer startSynchronizer;

    @Inject
    public DockerRuntimeContext(@Assisted DockerRuntimeInfrastructure infrastructure,
                                @Assisted RuntimeIdentity identity,
                                @Assisted Environment environment,
                                @Assisted DockerEnvironment dockerEnvironment,
                                @Assisted List<String> orderedServices,
                                NetworkLifecycle dockerNetworkLifecycle,
                                ServiceStarter serviceStarter,
                                URLRewriter urlRewriter,
                                AgentSorter agentSorter,
                                AgentRegistry agentRegistry)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, agentSorter, agentRegistry);
        this.dockerEnvironment = dockerEnvironment;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>(orderedServices);
        this.urlRewriter = urlRewriter;
        this.startSynchronizer = new StartSynchronizer();
    }

    @Override
    public URL getOutputChannel() throws UnsupportedOperationException, InfrastructureException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws InfrastructureException {
        startSynchronizer.setStartThread();
        try {
            checkStartInterruption();
            dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());

            String machineName = startQueue.peek();
            DockerMachine dockerMachine;
            while (machineName != null) {
                DockerService service = dockerEnvironment.getServices().get(machineName);
                checkStartInterruption();
                dockerMachine = startMachine(machineName, service, startOptions);
                checkStartInterruption();
                startSynchronizer.addMachine(machineName, dockerMachine);
                startQueue.poll();
                machineName = startQueue.peek();
            }

            return getInternalRuntime();
        } catch (InfrastructureException | RuntimeException e) {
            boolean interrupted = Thread.interrupted();
            boolean runtimeDestroyingNeeded = !startSynchronizer.isStopCalled();
            if (runtimeDestroyingNeeded) {
                try {
                    destroyRuntime();
                } catch (Exception destExc) {
                    LOG.error(destExc.getLocalizedMessage(), destExc);
                }
            }
            if (interrupted) {
                // TODO throw StartInterruptedException
                throw new InfrastructureException("");
            }
            try {
                throw e;
            } catch (InfrastructureException rethrow) {
                throw rethrow;
            } catch (Exception wrap) {
                throw new InfrastructureException(wrap.getLocalizedMessage(), wrap);
            }
        }
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        startSynchronizer.interruptStartThread();
        destroyRuntime();
    }

    private DockerMachine startMachine(String name, DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        // TODO property name
        if ("true".equals(startOptions.get("recover"))) {
            try {
                return startFromSnapshot(name, service, startOptions);
            } catch (SourceNotFoundException e) {
                // slip to start without recovering
            }
        }
        DockerMachine dockerMachine = doStartMachine(name, service, startOptions);
        startAgents(name, dockerMachine);
        return dockerMachine;
    }

    private InternalRuntime getInternalRuntime() {
        return new DockerInternalRuntime(this,
                                         urlRewriter,
                                         Collections.unmodifiableMap(startSynchronizer.getMachines()));
    }

    private void destroyRuntime() throws InfrastructureException {
        for (Map.Entry<String, DockerMachine> dockerMachineEntry : startSynchronizer.removeMachines().entrySet()) {
            try {
                // TODO snapshot
                dockerMachineEntry.getValue().destroy();
            } catch (RuntimeException e) {
                LOG.error(format("Could not destroy docker machine '%s' of workspace '%s'. Container '%s'",
                                 dockerMachineEntry.getKey(),
                                 getIdentity().getWorkspaceId(),
                                 dockerMachineEntry.getValue().getContainer()),
                          e);
            }
        }
        // TODO what happens when context throws exception here
        dockerNetworkLifecycle.destroyNetwork(dockerEnvironment.getNetwork());
    }

    private DockerMachine startFromSnapshot(String name, DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        // TODO set snapshot stuff
        // TODO should snapshots data be stored in separate table of DB as it is done now?
        return doStartMachine(name, service, startOptions);
    }

    private DockerMachine doStartMachine(String name, DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        // TODO get machine source
        normalizeServiceSource(service, null);
        return serviceStarter.startService(dockerEnvironment.getNetwork(),
                                           name,
                                           service,
                                           identity,
                                           startOptions);
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
                    dockerMachine.exec(resolvedAgent.getScript(), DEV_NULL);
                } catch (InfrastructureException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    private DockerService normalizeServiceSource(DockerService service, MachineSource machineSource) {
        DockerService serviceWithNormalizedSource = service;
        // TODO normalize source in service?
        if (machineSource != null) {
            serviceWithNormalizedSource = new DockerService(service);
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
        }
        return serviceWithNormalizedSource;
    }

    private void checkStartInterruption() throws InfrastructureException {
        if (Thread.interrupted()) {
            // TODO throw StartInterruptedException
            throw new InfrastructureException("Runtime start was interrupted");
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
            return new HashMap<>(machines);
        }

        public synchronized void addMachine(String name, DockerMachine machine) throws InfrastructureException {
            if (machines != null) {
                machines.put(name, machine);
            } else {
                // TODO throw StartInterruptedException
                throw new InfrastructureException("");
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

        public synchronized void setStartThread() throws InfrastructureException {
            if (startThread != null) {
                throw new InfrastructureException("Already started");
            }
            startThread = Thread.currentThread();
        }

        public synchronized void interruptStartThread() throws InfrastructureException {
            if (startThread == null) {
                throw new InfrastructureException("Stop of non started context not allowed");
            }
            if (stopCalled) {
                throw new InfrastructureException("Stop is called twice");
            }
            stopCalled = true;
            startThread.interrupt();
        }

        public synchronized boolean isStopCalled() {
            return stopCalled;
        }
    }
}
