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
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyMap;

/**
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
public class OpenShiftInternalRuntime extends InternalRuntime<OpenShiftRuntimeContext> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftInternalRuntime.class);

    private final OpenShiftClientFactory        clientFactory;
    private final EventService                  eventService;
    private final ServerCheckerFactory          serverCheckerFactory;
    private final OpenShiftBootstrapperFactory  bootstrapperFactory;
    private final Map<String, OpenShiftMachine> machines;
    private final int                           machineStartTimeoutMin;

    @Inject
    public OpenShiftInternalRuntime(@Assisted OpenShiftRuntimeContext context,
                                    URLRewriter.NoOpURLRewriter urlRewriter,
                                    OpenShiftClientFactory clientFactory,
                                    EventService eventService,
                                    OpenShiftBootstrapperFactory bootstrapperFactory,
                                    ServerCheckerFactory serverCheckerFactory,
                                    @Named("che.infra.openshift.machine_start_timeout_min") int machineStartTimeoutMin) {
        super(context, urlRewriter, false);
        this.clientFactory = clientFactory;
        this.eventService = eventService;
        this.bootstrapperFactory = bootstrapperFactory;
        this.serverCheckerFactory = serverCheckerFactory;
        this.machineStartTimeoutMin = machineStartTimeoutMin;
        this.machines = new ConcurrentHashMap<>();
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
        String projectName = getContext().getIdentity().getWorkspaceId();

        // TODO Add Persistent Volumes claims for projects
        try (OpenShiftClient client = clientFactory.create()) {
            prepareOpenShiftProject(projectName);

            LOG.info("Creating pods from environment");
            for (Pod toCreate : getContext().getOpenShiftEnvironment().getPods().values()) {
                Pod createdPod = client.pods()
                                       .inNamespace(projectName)
                                       .create(toCreate);

                for (Container container : createdPod.getSpec().getContainers()) {
                    OpenShiftMachine machine = new OpenShiftMachine(clientFactory,
                                                                    projectName,
                                                                    createdPod.getMetadata().getName(),
                                                                    container.getName());
                    machines.put(machine.getName(), machine);
                    sendStartingEvent(machine.getName());
                }
            }

            LOG.info("Creating services from environment");
            for (Service service : getContext().getOpenShiftEnvironment().getServices().values()) {
                client.services()
                      .inNamespace(projectName)
                      .create(service);
            }

            LOG.info("Creating routes from environment");
            for (Route route : getContext().getOpenShiftEnvironment().getRoutes().values()) {
                client.routes()
                      .inNamespace(projectName)
                      .create(route);
            }

            LOG.info("Waiting until pods created by deployment configs become available and bootstrapping them");

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
            //TODO OpenShift client throws runtime exception investigate what should be mapped to InternalInfrastructureException
            LOG.error("Failed to start of openshift runtime. " + e.getMessage(), e);
            throw new InfrastructureException(e.getMessage(), e);
        }

        LOG.info("OpenShift Runtime for workspace {} started", getContext().getIdentity().getWorkspaceId());
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return ImmutableMap.copyOf(machines);
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        LOG.info("Stopping workspace " + getContext().getIdentity().getWorkspaceId());
        try {
            cleanUpOpenShiftProject(getContext().getIdentity().getWorkspaceId());
        } catch (KubernetesClientException e) {
            //projects doesn't exist or is foreign
            LOG.info("Workspace {} was already stopped.", getContext().getIdentity().getWorkspaceId());
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
    }

    private void prepareOpenShiftProject(String projectName) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            LOG.info("Trying to resolve project for workspace {}", getContext().getIdentity().getWorkspaceId());
            try {
                client.projects().withName(projectName).get();
                cleanUpOpenShiftProject(projectName);
            } catch (KubernetesClientException e) {
                if (e.getCode() == 403) {
                    // project is foreign or doesn't exist

                    //try to create project
                    client.projectrequests()
                          .createNew()
                          .withNewMetadata()
                          .withName(projectName)
                          .endMetadata()
                          .done();
                } else {
                    throw new InfrastructureException(e.getMessage(), e);
                }
            }

            LOG.info("Created new project for workspace {}", getContext().getIdentity().getWorkspaceId());
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    private void cleanUpOpenShiftProject(String projectName) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            List<HasMetadata> toDelete = new ArrayList<>();
            toDelete.addAll(client.services().inNamespace(projectName).list().getItems());
            toDelete.addAll(client.routes().inNamespace(projectName).list().getItems());

            //services and routes will be removed immediately
            client.lists()
                  .inNamespace(projectName)
                  .delete(new KubernetesListBuilder().withItems(toDelete)
                                                     .build());

            //pods are removed with some delay related to stopping of containers. It is need to wait them
            List<Pod> pods = client.pods().inNamespace(projectName).list().getItems();
            List<CompletableFuture> deleteFutures = new ArrayList<>();
            for (Pod pod : pods) {
                PodResource<Pod, DoneablePod> podResource = client.pods()
                                                                  .inNamespace(projectName)
                                                                  .withName(pod.getMetadata().getName());
                CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
                deleteFutures.add(deleteFuture);
                podResource.watch(new DeleteWatcher(deleteFuture));
                podResource.delete();
            }
            CompletableFuture<Void> allRemoved =
                    CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[deleteFutures.size()]));
            try {
                allRemoved.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfrastructureException("Interrupted while waiting for workspace stop. " + e.getMessage());
            } catch (ExecutionException e) {
                throw new InfrastructureException("Error occurred while waiting for pod removing. " + e.getMessage());
            }
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    private class ServerReadinessHandler implements Consumer<String> {
        private String machineName;

        public ServerReadinessHandler(String machineName) {
            this.machineName = machineName;
        }

        @Override
        public void accept(String serverRef) {
            final OpenShiftMachine machine = machines.get(machineName);
            if (machine == null) {
                // Probably machine was removed from the list during server check start due to some reason
                return;
            }
            // TODO set server status
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

    private static class DeleteWatcher implements Watcher<Pod> {
        private final CompletableFuture<Void> future;

        private DeleteWatcher(CompletableFuture<Void> future) {
            this.future = future;
        }

        @Override
        public void eventReceived(Action action, Pod hasMetadata) {
            if (action == Action.DELETED) {
                future.complete(null);
            }
        }

        @Override
        public void onClose(KubernetesClientException e) {
            future.completeExceptionally(e);
        }
    }
}
