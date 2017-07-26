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
package org.eclipse.che.plugin.openshift.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DoneableDeployment;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

@Singleton
public class OpenShiftDeploymentCleaner {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftDeploymentCleaner.class);
    private static final int OPENSHIFT_POD_DELETION_TIMEOUT = 120;

    public void cleanDeploymentResources(final String deploymentName, final String namespace) throws IOException {
        scaleDownDeployment(deploymentName, namespace);
        cleanUpWorkspaceResources(deploymentName, namespace);
        waitUntilWorkspacePodIsDeleted(deploymentName, namespace);
    }

    private void scaleDownDeployment(String deploymentName, final String namespace) throws OpenShiftException {
        try (OpenShiftClient openShiftClient = new DefaultOpenShiftClient()) {
            ScalableResource<Deployment, DoneableDeployment> deployment = openShiftClient.extensions()
                           .deployments()
                           .inNamespace(namespace)
                           .withName(deploymentName);

            if (deployment != null) {
                deployment.scale(0, true);
            }
        }
    }

    private void cleanUpWorkspaceResources(final String deploymentName, final String namespace) throws IOException {
        Deployment deployment = KubernetesResourceUtil.getDeploymentByName(deploymentName, namespace);
        Service service = KubernetesResourceUtil.getServiceBySelector(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, namespace);
        List<Route> routes = KubernetesResourceUtil.getRoutesByLabel(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, namespace);
        List<ReplicaSet> replicaSets = KubernetesResourceUtil.getReplicaSetByLabel(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, namespace);

        try (OpenShiftClient openShiftClient = new DefaultOpenShiftClient()) {

            if (deployment != null) {
                LOG.info("Removing OpenShift Deployment {}", deployment.getMetadata().getName());
                openShiftClient.resource(deployment).delete();
            }

            if (replicaSets != null && replicaSets.size() > 0) {
                LOG.info("Removing OpenShift ReplicaSets for deployment {}", deploymentName);
                replicaSets.forEach(rs -> openShiftClient.resource(rs).delete());
            }

            if (routes != null) {
                for (Route route: routes) {
                    LOG.info("Removing OpenShift Route {}", route.getMetadata().getName());
                    openShiftClient.resource(route).delete();
                }
            }

            if (service != null) {
                LOG.info("Removing OpenShift Service {}", service.getMetadata().getName());
                openShiftClient.resource(service).delete();
            }
        }
    }
    
    private void waitUntilWorkspacePodIsDeleted(final String deploymentName, final String namespace) throws OpenShiftException {
        try (OpenShiftClient client = new DefaultOpenShiftClient()) {
            FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> pods = client.pods()
                                                                                              .inNamespace(namespace)
                                                                                              .withLabel(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

            int numberOfPodsToStop = pods.list().getItems().size();
            LOG.info("Number of workspace pods to stop {}", numberOfPodsToStop);
            if (numberOfPodsToStop > 0) {
                final CountDownLatch podCount = new CountDownLatch(numberOfPodsToStop);
                pods.watch(new Watcher<Pod>() {
                    @Override
                    public void eventReceived(Action action, Pod pod) {
                        try {
                            switch (action) {
                                case ADDED:
                                case MODIFIED:
                                case ERROR:
                                    break;
                                case DELETED:
                                    LOG.info("Pod {} deleted", pod.getMetadata().getName());
                                    podCount.countDown();
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.error("Failed to process {} on Pod {}. Error: ", action, pod, e);
                        }
                    }

                    @Override
                    public void onClose(KubernetesClientException ex) {
                    }
                });

                try {
                    LOG.info("Waiting for all pods to be deleted for deployment '{}'", deploymentName);
                    podCount.await(OPENSHIFT_POD_DELETION_TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOG.error("Exception while waiting for pods to be deleted", e);
                    throw new OpenShiftException("Timeout while waiting for pods to terminate", e);
                }
            }
        }
    }

}
