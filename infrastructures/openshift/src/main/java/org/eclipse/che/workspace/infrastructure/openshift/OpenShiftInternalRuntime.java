/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServerCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.ServersReadinessChecker;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenShiftBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends InternalRuntime<OpenShiftRuntimeContext> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftInternalRuntime.class);

    private final EventService                  eventService;
    private final ServerCheckerFactory          serverCheckerFactory;
    private final OpenShiftBootstrapperFactory  bootstrapperFactory;
    private final Map<String, OpenShiftMachine> machines;
    private final int                           machineStartTimeoutMin;
    private final OpenShiftProject              project;

    @Inject
    public OpenShiftInternalRuntime(@Assisted OpenShiftRuntimeContext context,
                                    @Assisted OpenShiftProject project,
                                    URLRewriter.NoOpURLRewriter urlRewriter,
                                    EventService eventService,
                                    OpenShiftBootstrapperFactory bootstrapperFactory,
                                    ServerCheckerFactory serverCheckerFactory,
                                    @Named("che.infra.openshift.machine_start_timeout_min") int machineStartTimeoutMin) {
        super(context, urlRewriter, false);
        this.eventService = eventService;
        this.bootstrapperFactory = bootstrapperFactory;
        this.serverCheckerFactory = serverCheckerFactory;
        this.machineStartTimeoutMin = machineStartTimeoutMin;
        this.project = project;
        this.machines = new ConcurrentHashMap<>();
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
        try {
            project.cleanUp();

            prepareOpenShiftPVCs(getContext().getOpenShiftEnvironment().getPersistentVolumeClaims());

            List<Service> createdServices = new ArrayList<>();
            for (Service service : getContext().getOpenShiftEnvironment().getServices().values()) {
                createdServices.add(project.services().create(service));
            }

            List<Route> createdRoutes = new ArrayList<>();
            for (Route route : getContext().getOpenShiftEnvironment().getRoutes().values()) {
                createdRoutes.add(project.routes().create(route));
            }

            ServerResolver serverResolver = ServerResolver.of(createdServices, createdRoutes);

            for (Pod toCreate : getContext().getOpenShiftEnvironment().getPods().values()) {
                Pod createdPod = project.pods().create(toCreate);
                for (Container container : createdPod.getSpec().getContainers()) {
                    OpenShiftMachine machine = new OpenShiftMachine(createdPod.getMetadata().getName(),
                                                                    container.getName(),
                                                                    serverResolver.resolve(createdPod, container),
                                                                    project);
                    machines.put(machine.getName(), machine);
                    sendStartingEvent(machine.getName());
                }
            }

            //TODO Rework it to parallel waiting
            for (OpenShiftMachine machine : machines.values()) {
                machine.waitRunning(machineStartTimeoutMin);
                final String machineName = machine.getName();
                bootstrapperFactory.create(getContext().getIdentity(),
                                           getContext().getMachineConfigs()
                                                       .get(machineName)
                                                       .getInstallers(),
                                           machine)
                                   .bootstrap();

                ServersReadinessChecker check = new ServersReadinessChecker(machineName,
                                                                            machine.getServers(),
                                                                            serverCheckerFactory);
                check.startAsync(new ServerReadinessHandler(machineName));
                check.await();
                sendRunningEvent(machine.getName());
            }
        } catch (RuntimeException | InterruptedException e) {
            LOG.error("Failed to start of OpenShift runtime. " + e.getMessage(), e);
            project.cleanUp();
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return ImmutableMap.copyOf(machines);
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        project.cleanUp();
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
    }

    private void prepareOpenShiftPVCs(Map<String, PersistentVolumeClaim> pvcs) throws InfrastructureException {
        Set<String> existing = project.persistentVolumeClaims()
                                      .get()
                                      .stream()
                                      .map(p -> p.getMetadata().getName())
                                      .collect(toSet());

        for (Map.Entry<String, PersistentVolumeClaim> pvcEntry : pvcs.entrySet()) {
            if (!existing.contains(pvcEntry.getKey())) {
                project.persistentVolumeClaims().create(pvcEntry.getValue());
            }
        }
    }

    private class ServerReadinessHandler implements Consumer<String> {
        private String machineName;

        ServerReadinessHandler(String machineName) {
            this.machineName = machineName;
        }

        @Override
        public void accept(String serverRef) {
            final OpenShiftMachine machine = machines.get(machineName);
            if (machine == null) {
                // Probably machine was removed from the list during server check start due to some reason
                return;
            }

            machine.setStatus(serverRef, ServerStatus.RUNNING);

            eventService.publish(DtoFactory.newDto(ServerStatusEvent.class)
                                           .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
                                           .withMachineName(machineName)
                                           .withServerName(serverRef)
                                           .withStatus(ServerStatus.RUNNING)
                                           .withServerUrl(machine.getServers().get(serverRef).getUrl()));
        }
    }

    private void sendStartingEvent(String machineName) {
        eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                       .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
                                       .withEventType(MachineStatus.STARTING)
                                       .withMachineName(machineName));
    }

    private void sendRunningEvent(String machineName) {
        eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                       .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
                                       .withEventType(MachineStatus.RUNNING)
                                       .withMachineName(machineName));
    }
}
