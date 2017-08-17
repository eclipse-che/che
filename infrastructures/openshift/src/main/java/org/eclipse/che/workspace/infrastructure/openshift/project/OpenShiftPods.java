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
package org.eclipse.che.workspace.infrastructure.openshift.project;

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

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static java.util.concurrent.CompletableFuture.allOf;

/**
 * Defines an internal API for managing {@link Pod} instances in
 * {@link OpenShiftPods#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftPods {
    private final String                 namespace;
    private final OpenShiftClientFactory clientFactory;

    OpenShiftPods(String namespace, OpenShiftClientFactory clientFactory) {
        this.namespace = namespace;
        this.clientFactory = clientFactory;
    }

    /**
     * Creates specified pod.
     *
     * @param pod
     *         pod to create
     * @return created pod
     * @throws InfrastructureException
     *         when any exception occurs
     */
    public Pod create(Pod pod) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            return client.pods()
                         .inNamespace(namespace)
                         .create(pod);
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    /**
     * Returns all existing pods.
     *
     * @throws InfrastructureException
     *         when any exception occurs
     */
    public List<Pod> get() throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            return client.pods()
                         .inNamespace(namespace)
                         .list()
                         .getItems();
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    /**
     * Returns optional with pod that have specified name.
     *
     * @throws InfrastructureException
     *         when any exception occurs
     */
    public Optional<Pod> get(String name) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            return Optional.ofNullable(client.pods()
                                             .inNamespace(namespace)
                                             .withName(name)
                                             .get());
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    /**
     * Waits until pod state will suit for specified predicate.
     *
     * @param name
     *         name of pod that should be watched
     * @param timeoutMin
     *         waiting timeout in minutes
     * @param predicate
     *         predicate to perform state check
     * @return pod that suit for specified predicate
     * @throws InfrastructureException
     *         when specified timeout is reached
     * @throws InfrastructureException
     *         when {@link Thread} is interrupted while waiting
     * @throws InfrastructureException
     *         when any other exception occurs
     */
    public Pod wait(String name, int timeoutMin, Predicate<Pod> predicate) throws InfrastructureException {
        CompletableFuture<Pod> future = new CompletableFuture<>();
        Watch watch = null;
        try (OpenShiftClient client = clientFactory.create()) {
            PodResource<Pod, DoneablePod> podResource = client.pods()
                                                              .inNamespace(namespace)
                                                              .withName(name);

            watch = podResource.watch(new Watcher<Pod>() {
                                          @Override
                                          public void eventReceived(Action action, Pod pod) {
                                              if (predicate.test(pod)) {
                                                  future.complete(pod);
                                              }
                                          }

                                          @Override
                                          public void onClose(KubernetesClientException cause) {
                                              future.completeExceptionally(new InfrastructureException("Waiting for pod '" + name + "' was interrupted"));
                                          }
                                      }
            );

            Pod actualPod = podResource.get();
            if (actualPod == null) {
                throw new InfrastructureException("Specified pod " + name + " doesn't exist");
            }
            if (predicate.test(actualPod)) {
                return actualPod;
            }
            try {
                return future.get(timeoutMin, TimeUnit.MINUTES);
            } catch (ExecutionException e) {
                throw new InfrastructureException(e.getCause().getMessage(), e);
            } catch (TimeoutException e) {
                throw new InfrastructureException("Waiting for pod '" + name + "' reached timeout");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfrastructureException("Waiting for pod '" + name + "' was interrupted");
            }
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        } finally {
            if (watch != null) {
                watch.close();
            }
        }
    }

    /**
     * Executes command in specified container.
     *
     * @param podName
     *         pod name where command will be executed
     * @param containerName
     *         container name where command will be executed
     * @param timeoutMin
     *         timeout to wait until process will be done
     * @param command
     *         command to execute
     * @throws InfrastructureException
     *         when specified timeout is reached
     * @throws InfrastructureException
     *         when {@link Thread} is interrupted while command executing
     * @throws InfrastructureException
     *         when any other exception occurs
     */
    public void exec(String podName,
                     String containerName,
                     int timeoutMin,
                     String[] command) throws InfrastructureException {
        ExecWatchdog watchdog = new ExecWatchdog();
        try (OpenShiftClient client = clientFactory.create();
             ExecWatch watch = client.pods()
                                     .inNamespace(namespace)
                                     .withName(podName)
                                     .inContainer(containerName)
                                     .usingListener(watchdog)
                                     .exec(encode(command))) {
            try {
                watchdog.wait(timeoutMin, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfrastructureException(e.getMessage(), e);
            }
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage());
        }
    }

    /**
     * Deletes all existing pods.
     *
     * <p>Note that this method will mark OpenShift pods as interrupted
     * and then will wait until all pods will be killed.
     *
     * @throws InfrastructureException
     *         when {@link Thread} is interrupted while command executing
     * @throws InfrastructureException
     *         when any other exception occurs
     */
    public void delete() throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            //pods are removed with some delay related to stopping of containers. It is need to wait them
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            List<CompletableFuture> deleteFutures = new ArrayList<>();
            for (Pod pod : pods) {
                PodResource<Pod, DoneablePod> podResource = client.pods()
                                                                  .inNamespace(namespace)
                                                                  .withName(pod.getMetadata().getName());
                CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
                deleteFutures.add(deleteFuture);
                podResource.watch(new DeleteWatcher(deleteFuture));
                podResource.delete();
            }
            CompletableFuture<Void> allRemoved = allOf(deleteFutures.toArray(new CompletableFuture[deleteFutures.size()]));
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

        public void wait(long timeout, TimeUnit timeUnit) throws InterruptedException, InfrastructureException {
            boolean isDone = latch.await(timeout, timeUnit);
            if (!isDone) {
                throw new InfrastructureException("Timeout reached while execution of command");
            }
        }
    }
}
