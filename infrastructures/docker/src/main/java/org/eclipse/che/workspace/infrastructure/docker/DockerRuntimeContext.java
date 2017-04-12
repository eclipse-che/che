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

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
// TODO
// workspace start interruption
// snapshots
// backups
// containers monitoring
public class DockerRuntimeContext extends RuntimeContext {
    private static final Logger LOG = getLogger(DockerRuntimeContext.class);

    // dependencies
    private final DockerNetworkLifecycle dockerNetworkLifecycle;
    private final DockerServiceStarter   serviceStarter;

    private final DockerEnvironment dockerEnvironment;
    // Should not be used after runtime start
    private final Queue<String>     startQueue;
    private final URLRewriter       urlRewriter;

    // synchronized, TODO consider reworking it into separate component
    // TODO volatile or something else?
    private volatile Map<String, DockerMachine> machines;
    private volatile Thread                     startThread;
    private volatile boolean                    stopIsCalled;

    public DockerRuntimeContext(DockerEnvironment dockerEnvironment,
                                Environment environment,
                                RuntimeIdentity identity,
                                RuntimeInfrastructure infrastructure,
                                URL registryEndpoint,
                                List<String> orderedServices,
                                DockerNetworkLifecycle dockerNetworkLifecycle,
                                DockerServiceStarter serviceStarter,
                                URLRewriter urlRewriter)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, registryEndpoint);
        this.dockerEnvironment = dockerEnvironment;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>(orderedServices);
        this.urlRewriter = urlRewriter;
        this.machines = new HashMap<>();
        this.stopIsCalled = false;
    }

    @Override
    public URL getOutputChannel() throws UnsupportedOperationException, InfrastructureException {
        throw new UnsupportedOperationException();
    }

    // TODO what should be thrown if start is interrupted in another thread, in current thread
    // TODO which error will be thrown if start fails because of something we don't control
    // and admin doesn't want to know about this problem
    // TODO if this thread is interrupted and stop is not called!
    // TODO interrupted exception, closedbyinteruptionexception
    // TODO add infrastructural things such as backup/restore
    // TODO simplify code
    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws InfrastructureException {
        setStartThread();
        try {
            checkStartInterruption();
            dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());

            String machineName = startQueue.peek();
            DockerMachine dockerMachine;
            while (machineName != null) {
                DockerService service = dockerEnvironment.getServices().get(machineName);
                checkStartInterruption();
                dockerMachine = startMachine(machineName, service, startOptions);
                addMachine(machineName, dockerMachine);
                // add agents start
                startQueue.poll();
                machineName = startQueue.peek();
            }

            return getInternalRuntime();
        } catch (InfrastructureException | RuntimeException e) {
            // TODO remove starting machine if present
            // TODO Check if interruption came from stop or because of another reason
            // TODO if because of another reason stop environment
            boolean runtimeDestroyingNeeded = true;
            synchronized (this) {
                if (stopIsCalled) {
                    runtimeDestroyingNeeded = false;
                }
            }
            boolean interrupted = Thread.interrupted();
            if (runtimeDestroyingNeeded) {
                try {
                    destroyRuntime();
                } catch (Exception destExc) {
                    LOG.error(destExc.getLocalizedMessage(), destExc);
                }
            }
            if (interrupted) {
                // TODO throw that it is interrupted
                throw new InfrastructureException("");
            }
            try {
                throw e;
            } catch (InfrastructureException rethrow) {
                throw rethrow;
            } catch (Exception wrap) {
                throw new InfrastructureException(wrap.getLocalizedMessage(), wrap);
            }
            // TODO InterruptedException | ClosedByInterruptException
        }
    }

    // TODO stop of starting WS - if not supported specific exception
    // TODO add warning on errors?
    // TODO in which cases to throw an exception?
    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        interruptStartThread();
        destroyRuntime();
    }

    private DockerMachine startMachine(String name, DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        if ("true".equals(startOptions.get("recover"))) {
            try {
                return startFromSnapshot(name, service, startOptions);
            } catch (SourceNotFoundException e) {
                // TODO what to do in that case?
                // slip to start without recovering
            }
        }
        DockerMachine dockerMachine = doStartMachine(name, service, startOptions);
        startAgents(dockerMachine);
        return dockerMachine;
    }

    private void startAgents(DockerMachine dockerMachine) {
        // TODO
    }

    private InternalRuntime getInternalRuntime() {
        return new DockerInternalRuntime(this, urlRewriter, Collections.unmodifiableMap(machines));
    }

    private void destroyRuntime() throws InfrastructureException {
        for (Map.Entry<String, DockerMachine> machineEntry : unsetMachines().entrySet()) {
            try {
                machineEntry.getValue().destroy();
            } catch (Exception e) {
                // TODO
            }
        }
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

    synchronized private void addMachine(String name, DockerMachine machine) throws InfrastructureException {
        if (machines != null) {
            machines.put(name, machine);
        } else {
            throw new InfrastructureException("");
        }
    }

    synchronized private Map<String, DockerMachine> unsetMachines() throws InfrastructureException {
        if (machines != null) {
            Map<String, DockerMachine> machines = this.machines;
            this.machines = null;
            return machines;
        }
        throw new InfrastructureException("");
    }

    synchronized private void setStartThread() throws InfrastructureException {
        if (startThread != null) {
            throw new InfrastructureException("Already started");
        }
        startThread = Thread.currentThread();
    }

    synchronized private void interruptStartThread() throws InfrastructureException {
        if (startThread == null) {
            // TODO specific exception
            throw new InfrastructureException("Stop of non started context not allowed");
        }
        if (stopIsCalled) {
            throw new InfrastructureException("Stop is called twice");
        }
        stopIsCalled = true;
        startThread.interrupt();
    }

    // TODO proper exception
    private void checkStartInterruption() throws InfrastructureException {
        if (Thread.interrupted()) {
            throw new InfrastructureException("Runtime start was interrupted");
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
}
