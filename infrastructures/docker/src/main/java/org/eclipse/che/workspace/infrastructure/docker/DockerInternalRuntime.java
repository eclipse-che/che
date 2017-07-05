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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.docker.exception.SourceNotFoundException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.server.ServerCheckerFactory;
import org.eclipse.che.workspace.infrastructure.docker.server.ServersReadinessChecker;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotDao;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotException;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
public class DockerInternalRuntime extends InternalRuntime<DockerRuntimeContext> {

    private static final Logger LOG = getLogger(DockerInternalRuntime.class);

    private final StartSynchronizer    startSynchronizer;
    private final Map<String, String>  properties;
    private final Queue<String>        startQueue;
    private final ContextsStorage      contextsStorage;
    private final NetworkLifecycle     dockerNetworkLifecycle;
    private final String               devMachineName;
    private final DockerEnvironment    dockerEnvironment;
    private final DockerMachineStarter serviceStarter;
    private final SnapshotDao          snapshotDao;
    private final DockerRegistryClient dockerRegistryClient;
    private final RuntimeIdentity      identity;
    private final EventService         eventService;
    private final BootstrapperFactory  bootstrapperFactory;
    private final ServerCheckerFactory serverCheckerFactory;

    @Inject
    public DockerInternalRuntime(@Assisted DockerRuntimeContext context,
                                 @Assisted String devMachineName,
                                 @Assisted List<String> orderedServices,
                                 @Assisted DockerEnvironment dockerEnvironment,
                                 @Assisted RuntimeIdentity identity,
                                 URLRewriter urlRewriter,
                                 ContextsStorage contextsStorage,
                                 NetworkLifecycle dockerNetworkLifecycle,
                                 DockerMachineStarter serviceStarter,
                                 SnapshotDao snapshotDao,
                                 DockerRegistryClient dockerRegistryClient,
                                 EventService eventService,
                                 BootstrapperFactory bootstrapperFactory,
                                 ServerCheckerFactory serverCheckerFactory) {
        super(context, urlRewriter);
        this.devMachineName = devMachineName;
        this.dockerEnvironment = dockerEnvironment;
        this.identity = identity;
        this.eventService = eventService;
        this.bootstrapperFactory = bootstrapperFactory;
        this.serverCheckerFactory = serverCheckerFactory;
        this.properties = new HashMap<>();
        this.startSynchronizer = new StartSynchronizer();
        this.contextsStorage = contextsStorage;
        this.dockerNetworkLifecycle = dockerNetworkLifecycle;
        this.serviceStarter = serviceStarter;
        this.snapshotDao = snapshotDao;
        this.dockerRegistryClient = dockerRegistryClient;
        this.startQueue = new ArrayDeque<>(orderedServices);
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
        startSynchronizer.setStartThread();
        try {
            contextsStorage.add((DockerRuntimeContext)getContext());
            dockerNetworkLifecycle.createNetwork(dockerEnvironment.getNetwork());

            boolean restore = isRestoreEnabled(startOptions);

            String machineName = startQueue.peek();
            while (machineName != null) {
                DockerContainerConfig service = dockerEnvironment.getServices().get(machineName);
                eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                               .withIdentity(DtoConverter.asDto(identity))
                                               .withEventType(MachineStatus.STARTING)
                                               .withMachineName(machineName));
                try {
                    if (restore) {
                        restoreMachine(machineName, service);
                    } else {
                        startMachine(machineName, service);
                    }
                    eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withIdentity(DtoConverter.asDto(identity))
                                                   .withEventType(MachineStatus.RUNNING)
                                                   .withMachineName(machineName));
                } catch (InfrastructureException e) {
                    eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                                   .withIdentity(DtoConverter.asDto(identity))
                                                   .withEventType(MachineStatus.FAILED)
                                                   .withMachineName(machineName)
                                                   .withError(e.getMessage()));
                    throw e;
                }
                startQueue.poll();
                machineName = startQueue.peek();
            }
        } catch (InfrastructureException | InterruptedException | RuntimeException e) {
            boolean interrupted = e instanceof InterruptedException || Thread.interrupted();
            contextsStorage.remove((DockerRuntimeContext)getContext());
            boolean runtimeDestroyingNeeded = !startSynchronizer.isStopCalled();
            if (runtimeDestroyingNeeded) {
                try {
                    destroyRuntime(null);
                } catch (Exception destExc) {
                    LOG.error(destExc.getLocalizedMessage(), destExc);
                }
            }
            if (interrupted) {
                throw new InfrastructureException("Docker runtime start was interrupted");
            }
            if (e instanceof InfrastructureException) {
                throw (InfrastructureException)e;
            } else {
                throw new InternalInfrastructureException(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        startSynchronizer.interruptStartThread();
        try {
            destroyRuntime(stopOptions);
        } finally {
            contextsStorage.remove((DockerRuntimeContext)getContext());
        }
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return startSynchronizer.getMachines()
                                .entrySet()
                                .stream()
                                .collect(toMap(Map.Entry::getKey, e -> new MachineImpl(e.getValue())));
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    private void restoreMachine(String name, DockerContainerConfig originalConfig)
            throws InfrastructureException, InterruptedException {
        try {
            SnapshotImpl snapshot = snapshotDao.getSnapshot(identity.getWorkspaceId(), identity.getEnvName(), name);
            startMachine(name, configForSnapshot(snapshot, originalConfig));
        } catch (NotFoundException | SourceNotFoundException x) {
            LOG.warn("Snapshot for machine '{}:{}:{}' is missing, machine will be started from source",
                     identity.getWorkspaceId(),
                     identity.getEnvName(),
                     name);
            startMachine(name, originalConfig);
        } catch (SnapshotException x) {
            throw new InternalInfrastructureException(x);
        }
    }

    private void startMachine(String name, DockerContainerConfig containerConfig)
            throws InfrastructureException, InterruptedException {
        InternalMachineConfig machineCfg = getContext().getMachineConfigs().get(name);

        Map<String, String> labels = new HashMap<>(containerConfig.getLabels());
        labels.putAll(Labels.newSerializer()
                            .machineName(name)
                            .runtimeId(identity)
                            .servers(machineCfg.getServers())
                            .labels());
        containerConfig.setLabels(labels);

        DockerMachine machine = serviceStarter.startService(dockerEnvironment.getNetwork(),
                                                            name,
                                                            containerConfig,
                                                            identity,
                                                            devMachineName.equals(name));
        try {
            startSynchronizer.addMachine(name, machine);
        } catch (InfrastructureException e) {
            // destroy machine only in case its addition fails
            // in other cases cleanup of whole runtime will be performed
            destroyMachineQuietly(name, machine);
            throw e;
        }
        bootstrapperFactory.create(name, identity, machine, machineCfg.getInstallers()).bootstrap();

        ServersReadinessChecker readinessChecker = new ServersReadinessChecker(name,
                                                                               machine.getServers(),
                                                                               new ServerReadinessHandler(name),
                                                                               serverCheckerFactory);
        readinessChecker.startAsync();
        readinessChecker.await();
    }

    // TODO support configuration properties as well
    private boolean isRestoreEnabled(Map<String, String> startOpts) {
        return Boolean.parseBoolean(startOpts.get("restore"));
    }

    // TODO support configuration properties as well
    private boolean isSnapshotEnabled(Map<String, String> stopOpts) {
        return stopOpts != null && Boolean.parseBoolean(stopOpts.get("create-snapshot"));
    }

    private class ServerReadinessHandler implements Consumer<String> {
        private String machineName;

        public ServerReadinessHandler(String machineName) {
            this.machineName = machineName;
        }

        @Override
        public void accept(String serverRef) {
            DockerMachine machine = startSynchronizer.getMachines().get(machineName);
            if (machine == null) {
                // Probably machine was removed from the list during server check start due to some reason
                return;
            }
            machine.setServerStatus(serverRef, ServerStatus.RUNNING);

            eventService.publish(DtoFactory.newDto(ServerStatusEvent.class)
                                           .withIdentity(DtoConverter.asDto(identity))
                                           .withMachineName(machineName)
                                           .withServerName(serverRef)
                                           .withStatus(ServerStatus.RUNNING)
                                           .withServerUrl(machine.getServers().get(serverRef).getUrl()));
        }
    }

    private DockerContainerConfig configForSnapshot(SnapshotImpl snapshot, DockerContainerConfig originCfg)
            throws NotFoundException, SnapshotException {
        MachineSourceImpl machineSource = snapshot.getMachineSource();
        // Snapshot image location has SHA-256 digest which needs to be removed,
        // otherwise it will be pulled without tag and cause problems
        String imageName = machineSource.getLocation();
        if (imageName.contains("@sha256:")) {
            machineSource.setLocation(imageName.substring(0, imageName.indexOf('@')));
        }
        return normalizeSource(originCfg, machineSource);
    }

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

    private void destroyRuntime(Map<String, String> stopOptions) throws InfrastructureException {
        Map<String, DockerMachine> machines = startSynchronizer.removeMachines();
        if (isSnapshotEnabled(stopOptions)) {
            snapshotMachines(machines);
        }
        for (Map.Entry<String, DockerMachine> entry : machines.entrySet()) {
            destroyMachineQuietly(entry.getKey(), entry.getValue());
            eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                           .withEventType(MachineStatus.STOPPED)
                                           .withIdentity(DtoConverter.asDto(identity))
                                           .withMachineName(entry.getKey()));
        }
        // TODO what happens when context throws exception here
        dockerNetworkLifecycle.destroyNetwork(dockerEnvironment.getNetwork());
    }

    /**
     * Destroys specified machine with suppressing exception that occurs while destroying.
     */
    private void destroyMachineQuietly(String machineName, DockerMachine machine) {
        try {
            machine.destroy();
        } catch (InfrastructureException e) {
            LOG.error(format("Error occurs on destroying of docker machine '%s' in workspace '%s'. Container '%s'",
                             machineName,
                             getContext().getIdentity().getWorkspaceId(),
                             machine.getContainer()),
                      e);
        }
    }

    /**
     * Prepare snapshots of all active machines.
     *
     * @param machines
     *         the active machines map
     */
    private void snapshotMachines(Map<String, DockerMachine> machines) {
        List<SnapshotImpl> newSnapshots = new ArrayList<>();
        final RuntimeIdentity identity = getContext().getIdentity();
        for (Map.Entry<String, DockerMachine> dockerMachineEntry : machines.entrySet()) {
            SnapshotImpl snapshot = SnapshotImpl.builder()
                                                .generateId()
                                                .setType("docker") //TODO: do we need that at all?
                                                .setWorkspaceId(identity.getWorkspaceId())
                                                .setDescription(identity.getEnvName())
                                                .setDev(dockerMachineEntry.getKey().equals(devMachineName))
                                                .setEnvName(identity.getEnvName())
                                                .setMachineName(dockerMachineEntry.getKey())
                                                .useCurrentCreationDate()
                                                .build();
            try {
                DockerMachineSource machineSource = dockerMachineEntry.getValue().saveToSnapshot();
                snapshot.setMachineSource(new MachineSourceImpl(machineSource));
                newSnapshots.add(snapshot);
            } catch (SnapshotException e) {
                LOG.error(format("Error occurs on snapshotting of docker machine '%s' in workspace '%s'. Container '%s'",
                                 dockerMachineEntry.getKey(),
                                 identity.getWorkspaceId(),
                                 dockerMachineEntry.getValue().getContainer()),
                          e);
            }
        }
        try {
            List<SnapshotImpl> removed = snapshotDao.replaceSnapshots(identity.getWorkspaceId(),
                                                                      identity.getEnvName(),
                                                                      newSnapshots);
            if (!removed.isEmpty()) {
                LOG.info("Removing old snapshots binaries, workspace id '{}', snapshots to remove '{}'", identity.getWorkspaceId(),
                         removed.size());
                removeBinaries(removed);
            }
        } catch (SnapshotException e) {
            LOG.error(format("Couldn't remove existing snapshots metadata for workspace '%s'", identity.getWorkspaceId()), e);
            removeBinaries(newSnapshots);
        }
    }

    /**
     * Removes binaries of all the snapshots, continues to remove
     * snapshots if removal of binaries for a single snapshot fails.
     *
     * @param snapshots
     *         the list of snapshots to remove binaries
     */
    private void removeBinaries(Collection<? extends SnapshotImpl> snapshots) {
        for (SnapshotImpl snapshot : snapshots) {
            try {
                dockerRegistryClient.removeInstanceSnapshot(snapshot.getMachineSource());
            } catch (SnapshotException x) {
                LOG.error(format("Couldn't remove snapshot '%s', workspace id '%s'", snapshot.getId(), snapshot.getWorkspaceId()), x);
            }
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

        public synchronized Map<String, ? extends DockerMachine> getMachines() {
            return machines != null ? machines : Collections.emptyMap();
        }

        public synchronized void addMachine(String name, DockerMachine machine) throws InternalInfrastructureException {
            if (machines != null) {
                machines.put(name, machine);
            } else {
                throw new InternalInfrastructureException("Start of runtime is canceled.");
            }
        }

        public synchronized Map<String, DockerMachine> removeMachines() throws InfrastructureException {
            if (machines != null) {
                Map<String, DockerMachine> machines = this.machines;
                // unset to identify error if method called second time
                this.machines = null;
                return machines;
            }
            throw new InfrastructureException("Runtime doesn't have machines to remove");
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
