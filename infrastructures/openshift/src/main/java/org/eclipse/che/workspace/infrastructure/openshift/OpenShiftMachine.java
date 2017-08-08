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

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.Response;

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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.emptyMap;

/**
 * @author Sergii Leshchenko
 */
public class OpenShiftMachine implements Machine {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftMachine.class);

    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";

    private final OpenShiftClientFactory  clientFactory;
    private final String                  projectName;
    private final String                  podName;
    private final String                  containerName;
    private final Map<String, ServerImpl> servers;

    public OpenShiftMachine(OpenShiftClientFactory clientFactory,
                            String projectName,
                            String podName,
                            String containerName,
                            Map<String, ServerImpl> servers) {
        this.clientFactory = clientFactory;
        this.projectName = projectName;
        this.podName = podName;
        this.containerName = containerName;
        this.servers = servers;
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
        return servers;
    }

    public void setStatus(String name, ServerStatus status) {
        servers.get(name).setStatus(status);
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
        Watch watch = null;
        try (OpenShiftClient client = clientFactory.create()) {
            PodResource<Pod, DoneablePod> podResource = client.pods()
                                                              .inNamespace(projectName)
                                                              .withName(podName);

            Pod actualPod = podResource.get();
            if (actualPod == null) {
                throw new InternalInfrastructureException("Can't find created pod " + podName);
            }
            String status = actualPod.getStatus().getPhase();
            LOG.info("Machine {} is {}", getName(), status);
            if (OPENSHIFT_POD_STATUS_RUNNING.equals(status)) {
                future.complete(null);
            } else {
                watch = podResource.watch(new Watcher<Pod>() {
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
                try {
                    future.get(timeoutMin, TimeUnit.MINUTES);
                } catch (ExecutionException e) {
                    throw new InfrastructureException(e.getCause().getMessage(), e);
                } catch (TimeoutException e) {
                    throw new InfrastructureException("Starting of machine " + getName() + " reached timeout");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InfrastructureException("Starting of machine " + getName() + " was interrupted");
                }
            }
        } finally {
            if (watch != null) {
                watch.close();
            }
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
