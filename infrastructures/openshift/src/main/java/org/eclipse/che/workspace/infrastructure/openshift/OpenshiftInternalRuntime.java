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
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.openshift.bootstrapper.OpenshiftBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenshiftEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftInternalRuntime extends InternalRuntime<OpenshiftRuntimeContext> {
    private static final Logger LOG = LoggerFactory.getLogger(OpenshiftInternalRuntime.class);

    private final RuntimeIdentity               identity;
    private final OpenshiftEnvironment          kubernetesEnvironment;
    private final OpenshiftClientFactory        clientFactory;
    private final InstallerRegistry             installerRegistry;
    private final EventService                  eventService;
    private final OpenshiftBootstrapperFactory  openshiftBootstrapperFactory;
    private final Map<String, OpenshiftMachine> machines;
    private final int                           machineStartTimeoutMin;

    @Inject
    public OpenshiftInternalRuntime(@Assisted OpenshiftRuntimeContext context,
                                    @Assisted RuntimeIdentity identity,
                                    @Assisted OpenshiftEnvironment openshiftEnvironment,
                                    URLRewriter urlRewriter,
                                    OpenshiftClientFactory clientFactory,
                                    InstallerRegistry installerRegistry,
                                    EventService eventService,
                                    OpenshiftBootstrapperFactory openshiftBootstrapperFactory,
                                    @Named("che.infra.openshift.machine_start_timeout_min") int machineStartTimeoutMin) {
        super(context, urlRewriter, false);
        this.identity = identity;
        this.kubernetesEnvironment = openshiftEnvironment;
        this.clientFactory = clientFactory;
        this.installerRegistry = installerRegistry;
        this.eventService = eventService;
        this.openshiftBootstrapperFactory = openshiftBootstrapperFactory;
        this.machineStartTimeoutMin = machineStartTimeoutMin;
        this.machines = new ConcurrentHashMap<>();
    }

    @Override
    protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
        prepareOpenshiftProject();

        // TODO Add Persistent Volumes claims for projects
        try (OpenShiftClient client = clientFactory.create()) {
            LOG.info("Creating pods from environment");
            for (Pod toCreate : kubernetesEnvironment.getPods().values()) {
                Pod createdPod = client.pods()
                                       .inNamespace(identity.getWorkspaceId())
                                       .create(toCreate);
                kubernetesEnvironment.addPod(createdPod);

                for (Container container : createdPod.getSpec().getContainers()) {
                    OpenshiftMachine machine = new OpenshiftMachine(clientFactory, createdPod, container.getName());
                    machines.put(machine.getName(), machine);
                    sendStartingEvent(machine.getName());
                }
            }

            LOG.info("Creating services from environment");
            for (Service service : kubernetesEnvironment.getServices().values()) {
                kubernetesEnvironment.addService(client.services()
                                                       .inNamespace(identity.getWorkspaceId())
                                                       .create(service));
            }

            LOG.info("Creating routes from environment");
            for (Route route : kubernetesEnvironment.getRoutes().values()) {
                kubernetesEnvironment.addRoute(client.routes()
                                                     .inNamespace(identity.getWorkspaceId())
                                                     .create(route));
            }

            LOG.info("Waiting until pods created by deployment configs become available and bootstrapping them");

            for (OpenshiftMachine machine : machines.values()) {
                machine.waitRunning(machineStartTimeoutMin);

                //TODO Installers should be already known https://github.com/eclipse/che/issues/5687
                List<InstallerImpl> installers;
                try {
                    installers = installerRegistry.getOrderedInstallers(asList("org.eclipse.che.ws-agent",
                                                                               "org.eclipse.che.terminal"))
                                                  .stream()
                                                  .map(InstallerImpl::new)
                                                  .collect(toList());
                } catch (InstallerException e) {
                    throw new InfrastructureException(e.getMessage(), e);
                }

                openshiftBootstrapperFactory.create(machine.getName(),
                                                    identity,
                                                    installers,
                                                    machine)
                                            .bootstrap();

                sendRunningEvent(machine.getName());
            }
        } catch (RuntimeException e) {
            LOG.error("Failed to start of openshift runtime. " + e.getMessage(), e);
            throw new InfrastructureException(e.getMessage(), e);
        }

        LOG.info("Openshift Runtime for workspace {} started", identity.getWorkspaceId());
    }

    @Override
    public Map<String, ? extends Machine> getInternalMachines() {
        //TODO will be reworked during https://github.com/eclipse/che/issues/5688
        Map<String, MachineImpl> machines = this.machines.entrySet()
                                                         .stream()
                                                         .collect(toMap(Map.Entry::getKey,
                                                                        e -> new MachineImpl(e.getValue())));

        String workspaceId = identity.getWorkspaceId();
        try (OpenShiftClient client = clientFactory.create()) {
            List<Route> routes = client.routes().inNamespace(workspaceId).list().getItems();
            List<Service> services = client.services().inNamespace(workspaceId).list().getItems();

            for (Route route : routes) {
                String serviceName = route.getSpec().getTo().getName();

                //TODO Implement fetching protocol from it
                Service service = services.stream()
                                          .filter(s -> s.getMetadata().getName().equals(serviceName))
                                          .findAny()
                                          .get();

                List<Pod> servicesPods = client.pods()
                                               .inNamespace(workspaceId)
                                               .withLabels(service.getSpec().getSelector())
                                               .list()
                                               .getItems();

                for (Pod servicesPod : servicesPods) {
                    for (Container container : servicesPod.getSpec().getContainers()) {
                        for (ContainerPort containerPort : container.getPorts()) {
                            for (ServicePort servicePort : service.getSpec().getPorts()) {
                                if (containerPort.getContainerPort().equals(servicePort.getPort())) {
                                    String portName = route.getSpec().getPort().getTargetPort().getStrVal();

                                    machines.get(servicesPod.getMetadata().getName() + "/" + container.getName())
                                            .getServers()
                                            .put(portName,
                                                 new ServerImpl("http://" + route.getSpec().getHost(),
                                                                ServerStatus.UNKNOWN));
                                }
                            }

                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            LOG.error("Error occurs while resolving machines in workspace " + identity.getWorkspaceId(), e);
            return emptyMap();
        }
        return machines;
    }

    @Override
    protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {
        LOG.info("Stopping workspace " + identity.getWorkspaceId());
        try {
            cleanUpOpenshiftProject();
        } catch (KubernetesClientException e) {
            //projects doesn't exist or is foreign
            LOG.info("Workspace {} was already stopped.", identity.getWorkspaceId());
        }
    }

    private void prepareOpenshiftProject() throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            String namespace = identity.getWorkspaceId();
            LOG.info("Trying to resolve project for workspace {}", identity.getWorkspaceId());
            try {
                Project project = client.projects().withName(namespace).get();

                //TODO clean up project instead it recreation
                cleanUpOpenshiftProject();
                //Projects creation immediately after its removing doesn't work TODO Fix it
                client.projectrequests()
                      .createNew()
                      .withNewMetadata()
                      .withName(namespace)
                      .endMetadata()
                      .done();
            } catch (KubernetesClientException e) {
                if (e.getCode() == 403) {
                    // project is foreign or doesn't exist

                    //try to create project
                    client.projectrequests()
                          .createNew()
                          .withNewMetadata()
                          .withName(namespace)
                          .endMetadata()
                          .done();
                } else {
                    throw new InfrastructureException(e.getMessage(), e);
                }
            }

            LOG.info("Created new project for workspace {}", identity.getWorkspaceId());
        }
    }

    private void cleanUpOpenshiftProject() {
        try (OpenShiftClient client = clientFactory.create()) {
            List<HasMetadata> toDelete = new ArrayList<>();
            toDelete.addAll(client.pods().inNamespace(identity.getWorkspaceId()).list().getItems());
            toDelete.addAll(client.services().inNamespace(identity.getWorkspaceId()).list().getItems());
            toDelete.addAll(client.routes().inNamespace(identity.getWorkspaceId()).list().getItems());

            KubernetesList toDeleteList = new KubernetesList();
            toDeleteList.setItems(toDelete);

            client.lists().inNamespace(identity.getWorkspaceId()).delete(toDeleteList);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
    }

    private void sendStartingEvent(String machineName) {
        eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                       .withIdentity(DtoConverter.asDto(identity))
                                       .withEventType(MachineStatus.STARTING)
                                       .withMachineName(machineName));
    }

    private void sendRunningEvent(String machineName) {
        eventService.publish(DtoFactory.newDto(MachineStatusEvent.class)
                                       .withIdentity(DtoConverter.asDto(identity))
                                       .withEventType(MachineStatus.RUNNING)
                                       .withMachineName(machineName));
    }
}
