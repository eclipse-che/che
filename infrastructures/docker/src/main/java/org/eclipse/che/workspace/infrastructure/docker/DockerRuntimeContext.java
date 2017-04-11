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
import java.util.ArrayList;
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
    private final Queue<String>     startQueue;

    // synchronized, TODO consider reworking it into separate component
    private ArrayList<DockerMachine> machines;
    private Thread                   startThread;
    private boolean                  stopIsCalled;

    public DockerRuntimeContext(DockerEnvironment dockerEnvironment,
                                Environment environment,
                                RuntimeIdentity identity,
                                RuntimeInfrastructure infrastructure,
                                URL registryEndpoint,
                                List<String> orderedServices,
                                DockerNetworkLifecycle dockerNetworkLifecycle,
                                DockerServiceStarter serviceStarter)
            throws ValidationException, InfrastructureException {
        super(environment, identity, infrastructure, registryEndpoint);
        this.dockerEnvironment = dockerEnvironment;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>(orderedServices);
        this.machines = new ArrayList<>();
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

            String machine = startQueue.peek();
            while (machine != null) {
                DockerService service = dockerEnvironment.getServices().get(machine);
                checkStartInterruption();
                DockerMachine machineRuntime = startMachine(service, startOptions);
                machines.add(machineRuntime);
                // add agents start
                startQueue.poll();
                machine = startQueue.peek();
            }

            return getInternalRuntime();
        } catch (Exception e) {
            boolean interrupted = Thread.interrupted();
            try {
                destroyRuntime();
            } catch (Exception destExc) {
                LOG.error(destExc.getLocalizedMessage(), destExc);
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

    private DockerMachine startMachine(DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        if ("true".equals(startOptions.get("recover"))) {
            try {
                return startFromSnapshot(service, startOptions);
            } catch (SourceNotFoundException e) {
                // TODO what to do in that case?
                // slip to no recovering option
            }
        }
        return doStartMachine(service, startOptions);
    }

    private DockerMachine doStartMachine(DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        // TODO get machine source
        normalizeServiceSource(service, null);
        return serviceStarter.startService(dockerEnvironment.getNetwork(),
                                           service,
                                           startOptions);
    }

    private DockerMachine startFromSnapshot(DockerService service, Map<String, String> startOptions)
            throws InfrastructureException {
        // TODO set snapshot stuff
        return doStartMachine(service, startOptions);
    }

    private InternalRuntime getInternalRuntime() {
        return null;
    }

    private void destroyRuntime() throws InfrastructureException {
        for (DockerMachine dockerMachine : unsetMachines()) {
            try {
                dockerMachine.destroy();
            } catch (Exception e) {
                // TODO
            }
        }
        dockerNetworkLifecycle.destroyNetwork(dockerEnvironment.getNetwork());
    }

    synchronized private void addMachine(DockerMachine machine) throws InfrastructureException {
        if (machines != null) {
            machines.add(machine);
        } else {
            throw new InfrastructureException("");
        }
    }


    synchronized private List<DockerMachine> unsetMachines() throws InfrastructureException {
        if (machines != null) {
            ArrayList<DockerMachine> machines = this.machines;
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
