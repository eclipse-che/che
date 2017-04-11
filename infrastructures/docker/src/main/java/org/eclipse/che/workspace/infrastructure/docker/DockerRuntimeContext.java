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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.NotSupportedException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alexander Garagatyi
 */
// TODO
// workspace start interruption
// snapshots
// backups
// containers monitoring
public class DockerRuntimeContext extends RuntimeContext {
    private final DockerEnvironment                   dockerEnvironment;
    private final List<String>                        orderedServices;
    private final Queue<String>                       startQueue;
    private final CopyOnWriteArrayList<DockerMachine> machines;

    private final DockerNetworkLifecycle dockerNetworkLifecycle;
    private final DockerServiceStarter   serviceStarter;

    public DockerRuntimeContext(DockerEnvironment dockerEnvironment,
                                Environment environment,
                                RuntimeIdentity identity,
                                RuntimeInfrastructure infrastructure,
                                URL registryEndpoint,
                                List<String> orderedServices,
                                DockerNetworkLifecycle dockerNetworkLifecycle,
                                DockerServiceStarter serviceStarter)
            throws ValidationException,
                   ApiException,
                   IOException {
        super(environment, identity, infrastructure, registryEndpoint);
        this.dockerEnvironment = dockerEnvironment;
        this.orderedServices = orderedServices;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.startQueue = new ArrayDeque<>();
        this.machines = new CopyOnWriteArrayList<>();
    }

    // TODO what should be thrown if start is interrupted in another thread, in current thread
    // TODO which error will be thrown if start fails because of something we don't control
    // and admin doesn't want to know about this problem
    @Override
    protected InternalRuntime internalStart(Map<String, String> startOptions) throws ServerException {
        dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());
        String machine = startQueue.peek();
        while (machine != null) {
            // add infrastructural things such as backup/restore
            DockerService service = dockerEnvironment.getServices().get(machine);
            DockerMachine machineRuntime = startMachine(service, startOptions);
            machines.add(machineRuntime);
            // add agents start
            startQueue.poll();
            machine = startQueue.peek();
        }

        return getInternalRuntime();
        // TODO catch error. Cleanup env
    }

    private InternalRuntime getInternalRuntime() {
        return null;
    }

    private DockerMachine startMachine(DockerService service, Map<String, String> startOptions) {
        DockerMachine machineRuntime = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                                   service,
                                                                   startOptions);
        return machineRuntime;
    }

    private DockerService normalizeServiceSource(DockerService service, MachineSource machineSource) {
        DockerService serviceWithNormalizedSource = service;
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


    // TODO stop of starting WS - if not supported specific exception
    @Override
    protected void internalStop(Map<String, String> stopOptions) throws ServerException {

    }

    @Override
    public URL getOutputChannel() throws NotSupportedException, ServerException {
        throw new NotSupportedException();
    }
}
