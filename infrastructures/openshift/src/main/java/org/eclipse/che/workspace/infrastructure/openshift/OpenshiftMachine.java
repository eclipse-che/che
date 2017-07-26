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
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RoutePort;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.Response;

import com.google.common.collect.ArrayListMultimap;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftMachine implements Machine {
    private static final Logger LOG = LoggerFactory.getLogger(OpenshiftMachine.class);

    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";

    private final OpenshiftClientFactory clientFactory;
    private final String                 projectName;
    private final String                 podName;
    private final String                 containerName;

    public OpenshiftMachine(OpenshiftClientFactory clientFactory,
                            String projectName,
                            String podName,
                            String containerName) {
        this.clientFactory = clientFactory;
        this.projectName = projectName;
        this.podName = podName;
        this.containerName = containerName;
    }

    public String getName() {
        return podName + "/" + containerName;
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
    }

    @Override
    public Map<String, ? extends Server> getServers() {
        //TODO https://github.com/eclipse/che/issues/5688
        try (OpenShiftClient client = clientFactory.create()) {
            //TODO Explore maybe it is possible to request required service by field selector
            List<Service> matchedServices = client.services()
                                                  .inNamespace(projectName)
                                                  .list()
                                                  .getItems()
                                                  .stream()
                                                  .filter(this::isMachineExposedByService)
                                                  .collect(Collectors.toList());


            //check matching by container
            Container cointaner = getContainer();
            ArrayListMultimap<String, ServicePort> matchedServicesPorts = ArrayListMultimap.create();
            for (ContainerPort containerPort : cointaner.getPorts()) {
                Integer port = containerPort.getContainerPort();

                for (Service service : matchedServices) {
                    for (ServicePort servicePort : service.getSpec().getPorts()) {
                        if (port.equals(servicePort.getPort())) {
                            // container is accessible via this service
                            matchedServicesPorts.put(service.getMetadata().getName(), servicePort);
                        }
                    }
                }
            }

            List<Route> routes = client.routes()
                                       .inNamespace(projectName)
                                       .list()
                                       .getItems();
            Map<String, ServerImpl> servers = new HashMap<>();
            for (Route route : routes) {
                String serviceName = route.getSpec().getTo().getName();
                List<ServicePort> servicePorts = matchedServicesPorts.get(serviceName);
                if (servicePorts != null) {
                    RoutePort routePort = route.getSpec().getPort();
                    for (ServicePort servicePort : servicePorts) {
                        String portReference = routePort.getTargetPort().getStrVal();
                        if (portReference != null) {
                            if (portReference.equals(servicePort.getName())) {
                                servers.put(portReference, new ServerImpl("http://" + route.getSpec().getHost(),
                                                                          ServerStatus.UNKNOWN));
                            }
                            continue;
                        }

                        Integer portNumber = routePort.getTargetPort().getIntVal();
                        if (portNumber != null) {
                            if (portNumber.equals(servicePort.getPort())) {
                                servers.put(Integer.toString(portNumber),
                                            new ServerImpl("http://" + route.getSpec().getHost(),
                                                           ServerStatus.UNKNOWN));
                            }
                            continue;
                        }
                    }
                }
            }

            return servers;
        }
    }

    private Pod getPod() {
        try (OpenShiftClient client = clientFactory.create()) {
            return client.pods()
                         .inNamespace(projectName)
                         .withName(podName)
                         .get();
        }
    }

    private Container getContainer() {
        //TODO https://github.com/eclipse/che/issues/5688
        return getPod().getSpec()
                       .getContainers()
                       .stream()
                       .filter(c -> containerName.equals(c.getName()))
                       .findAny()
                       .orElseThrow(() -> new IllegalStateException("Corresponding pod for openshift machine doesn't exit."));
    }

    private boolean isMachineExposedByService(Service service) {
        Map<String, String> labels = getPod().getMetadata().getLabels();
        Map<String, String> selectorLabels = service.getSpec().getSelector();
        for (Map.Entry<String, String> selectorLabelEntry : selectorLabels.entrySet()) {
            if (!selectorLabelEntry.getValue().equals(labels.get(selectorLabelEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public void exec(String... command) throws InfrastructureException {
        ExecWatchdog watchdog = new ExecWatchdog();
        try (OpenShiftClient client = clientFactory.create();
             ExecWatch watch = client.pods()
                                     .inNamespace(projectName)
                                     .withName(podName)
                                     .inContainer(containerName)
                                     //TODO Investigate why redirection output and listener doesn't work together
                                     .usingListener(watchdog)
                                     .exec(encode(command))) {
            try {
                //TODO Make it configurable
                watchdog.wait(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfrastructureException(e.getMessage(), e);
            }
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage());
        }
    }

    public void waitRunning(int timeoutMin) throws InfrastructureException {
        LOG.info("Waiting machine {}", getName());

        CompletableFuture<Void> future = new CompletableFuture<>();
        Watch watch;
        try (OpenShiftClient client = clientFactory.create()) {
            Pod actualPod = client.pods()
                                  .inNamespace(projectName)
                                  .withName(podName)
                                  .get();

            if (actualPod == null) {
                throw new InternalInfrastructureException("Can't find created pod " + podName);
            }
            String status = actualPod.getStatus().getPhase();
            LOG.info("Machine {} is {}", getName(), status);
            if (OPENSHIFT_POD_STATUS_RUNNING.equals(status)) {
                future.complete(null);
                return;
            } else {
                watch = client.pods()
                              .inNamespace(projectName)
                              .withName(podName)
                              .watch(new Watcher<Pod>() {
                                         @Override
                                         public void eventReceived(Action action, Pod pod) {
                                             //TODO Replace with checking container status
                                             String phase = pod.getStatus().getPhase();
                                             LOG.info("Machine {} is {}", getName(), status);
                                             if (OPENSHIFT_POD_STATUS_RUNNING.equals(phase)) {
                                                 future.complete(null);
                                             }
                                         }

                                         @Override
                                         public void onClose(KubernetesClientException cause) {
                                             if (!future.isDone()) {
                                                 future.completeExceptionally(
                                                         new InfrastructureException("Machine watching is interrupted"));
                                             }
                                         }
                                     }
                              );
            }
        }

        try {
            future.get(timeoutMin, TimeUnit.MINUTES);
            watch.close();
        } catch (ExecutionException e) {
            throw new InfrastructureException(e.getCause().getMessage(), e);
        } catch (TimeoutException e) {
            throw new InfrastructureException("Starting of machine " + getName() + " reached timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InfrastructureException("Starting of machine " + getName() + " was interrupted");
        }
    }

    private String[] encode(String[] toEncode) throws InfrastructureException {
        String[] encoded = new String[toEncode.length];
        for (int i = 0; i < toEncode.length; i++) {
            try {
                encoded[i] = URLEncoder.encode(toEncode[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InfrastructureException(e.getMessage(), e);
            }
        }
        return encoded;
    }

    private class ExecWatchdog implements ExecListener {
        private final CountDownLatch latch;

        private ExecWatchdog() {
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void onOpen(Response response) {
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            latch.countDown();
        }

        @Override
        public void onClose(int code, String reason) {
            latch.countDown();
        }

        public void wait(long timeout, TimeUnit timeUnit) throws InterruptedException {
            latch.await(timeout, timeUnit);
        }
    }
}
