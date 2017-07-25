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
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenshiftBootstrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftInternalRuntime extends InternalRuntime<OpenshiftRuntimeContext> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenshiftInternalRuntime.class);

    private final OpenshiftClientFactory        clientFactory;
    private final EventService                  eventService;
    private final OpenshiftBootstrapperFactory  openshiftBootstrapperFactory;
    private final Map<String, OpenshiftMachine> machines;
    private final int                           machineStartTimeoutMin;

    @Inject
    public OpenshiftInternalRuntime(@Assisted OpenshiftRuntimeContext context,
                                    URLRewriter urlRewriter,
                                    OpenshiftClientFactory clientFactory,
                                    EventService eventService,
                                    OpenshiftBootstrapperFactory openshiftBootstrapperFactory,
                                    @Named("che.infra.openshift.machine_start_timeout_min") int machineStartTimeoutMin) {
        super(context, urlRewriter, false);
        this.clientFactory = clientFactory;
        this.eventService = eventService;
        this.openshiftBootstrapperFactory = openshiftBootstrapperFactory;
        this.machineStartTimeoutMin = machineStartTimeoutMin;
        this.machines = new ConcurrentHashMap<>();
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
        String projectName = getContext().getIdentity().getWorkspaceId();

        prepareOpenshiftProject(projectName);

        // TODO Add Persistent Volumes claims for projects
        try (OpenShiftClient client = clientFactory.create()) {
            LOG.info("Creating pods from environment");
            for (Pod toCreate : getContext().getOpenshiftEnvironment().getPods().values()) {
                Pod createdPod = client.pods()
                                       .inNamespace(projectName)
                                       .create(toCreate);

                for (Container container : createdPod.getSpec().getContainers()) {
                    OpenshiftMachine machine = new OpenshiftMachine(clientFactory,
                                                                    projectName,
                                                                    createdPod.getMetadata().getName(),
                                                                    container.getName());
                    machines.put(machine.getName(), machine);
                    sendStartingEvent(machine.getName());
                }
            }

            LOG.info("Creating services from environment");
            for (Service service : getContext().getOpenshiftEnvironment().getServices().values()) {
                client.services()
                      .inNamespace(projectName)
                      .create(service);
            }

            LOG.info("Creating routes from environment");
            for (Route route : getContext().getOpenshiftEnvironment().getRoutes().values()) {
                client.routes()
                      .inNamespace(projectName)
                      .create(route);
            }

            LOG.info("Waiting until pods created by deployment configs become available and bootstrapping them");

            for (OpenshiftMachine machine : machines.values()) {
                machine.waitRunning(machineStartTimeoutMin);

                openshiftBootstrapperFactory.create(machine.getName(),
                                                    getContext().getIdentity(),
                                                    getContext().getMachineConfigs().get(machine.getName())
                                                                .getInstallers(),
                                                    machine)
                                            .bootstrap();

                sendRunningEvent(machine.getName());
            }
        } catch (RuntimeException e) {
            LOG.error("Failed to start of openshift runtime. " + e.getMessage(), e);
            throw new InfrastructureException(e.getMessage(), e);
        }

        LOG.info("Openshift Runtime for workspace {} started", getContext().getIdentity().getWorkspaceId());
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        return ImmutableMap.copyOf(machines);
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        LOG.info("Stopping workspace " + getContext().getIdentity().getWorkspaceId());
        try {
            cleanUpOpenshiftProject(getContext().getIdentity().getWorkspaceId());
        } catch (KubernetesClientException e) {
            //projects doesn't exist or is foreign
            LOG.info("Workspace {} was already stopped.", getContext().getIdentity().getWorkspaceId());
        }
    }

    private void prepareOpenshiftProject(String projectName) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            LOG.info("Trying to resolve project for workspace {}", getContext().getIdentity().getWorkspaceId());
            try {
                Project project = client.projects().withName(projectName).get();

                //TODO clean up project instead it recreation
                cleanUpOpenshiftProject(projectName);
                //Projects creation immediately after its removing doesn't work TODO Fix it
                client.projectrequests()
                      .createNew()
                      .withNewMetadata()
                      .withName(projectName)
                      .endMetadata()
                      .done();
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
        }
    }

    private void cleanUpOpenshiftProject(String projectName) {
        try (OpenShiftClient client = clientFactory.create()) {
            List<HasMetadata> toDelete = new ArrayList<>();
            toDelete.addAll(client.pods().inNamespace(projectName).list().getItems());
            toDelete.addAll(client.services().inNamespace(projectName).list().getItems());
            toDelete.addAll(client.routes().inNamespace(projectName).list().getItems());

            KubernetesList toDeleteList = new KubernetesList();
            toDeleteList.setItems(toDelete);

            client.lists().inNamespace(projectName).delete(toDeleteList);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
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
