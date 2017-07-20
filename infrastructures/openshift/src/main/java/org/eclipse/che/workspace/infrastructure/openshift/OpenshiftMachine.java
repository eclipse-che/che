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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.Response;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
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
public class OpenshiftMachine implements Machine {
    private static final Logger LOG = LoggerFactory.getLogger(OpenshiftMachine.class);

    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";

    private final OpenshiftClientFactory clientFactory;
    private       Pod                    pod;
    private final String                 containerName;

    public OpenshiftMachine(OpenshiftClientFactory clientFactory, Pod pod, String containerName) {
        this.clientFactory = clientFactory;
        this.pod = pod;
        this.containerName = containerName;
    }

    public String getName() {
        return pod.getMetadata().getName() + "/" + containerName;
    }

    @Override
    public Map<String, String> getProperties() {
        return emptyMap();
    }

    @Override
    public Map<String, ? extends Server> getServers() {
        //TODO https://github.com/eclipse/che/issues/5687
        return new HashMap<>();
    }

    public void exec(String... command) throws InfrastructureException {
        ExecWatchdog watchdog = new ExecWatchdog();
        try (OpenShiftClient client = clientFactory.create();
             ExecWatch watch = client.pods()
                                     .inNamespace(pod.getMetadata().getNamespace())
                                     .withName(pod.getMetadata().getName())
                                     .inContainer(containerName)
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

        CompletableFuture<Pod> future = new CompletableFuture<>();
        Watch watch;
        try (OpenShiftClient client = clientFactory.create()) {
            Pod actualPod = client.pods()
                                  .inNamespace(pod.getMetadata().getNamespace())
                                  .withName(pod.getMetadata().getName())
                                  .get();

            if (actualPod == null) {
                throw new InternalInfrastructureException("Can't find created pod " + pod.getMetadata().getName());
            }
            String status = actualPod.getStatus().getPhase();
            LOG.info("Machine {} is {}", getName(), status);
            if (OPENSHIFT_POD_STATUS_RUNNING.equals(status)) {
                future.complete(actualPod);
                return;
            } else {
                watch = client.pods()
                              .inNamespace(pod.getMetadata().getNamespace())
                              .withName(pod.getMetadata().getName())
                              .watch(new Watcher<Pod>() {
                                         @Override
                                         public void eventReceived(Action action, Pod pod) {
                                             //TODO Replace with checking container status
                                             String phase = pod.getStatus().getPhase();
                                             LOG.info("Machine {} is {}", getName(), status);
                                             if (OPENSHIFT_POD_STATUS_RUNNING.equals(phase)) {
                                                 future.complete(pod);
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
            this.pod = future.get(timeoutMin, TimeUnit.MINUTES);
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
