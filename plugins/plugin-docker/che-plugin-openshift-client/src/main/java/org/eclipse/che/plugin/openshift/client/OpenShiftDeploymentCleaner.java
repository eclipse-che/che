/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenShiftDeploymentCleaner {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftDeploymentCleaner.class);
  private static final int OPENSHIFT_POD_DELETION_TIMEOUT = 120;
  private static final int OPENSHIFT_WAIT_POD_DELAY = 1000;

  @Inject private OpenshiftWorkspaceEnvironmentProvider openshiftUserAccountProvider;

  public void cleanDeploymentResources(final String deploymentName, final String namespace)
      throws IOException {
    cleanUpWorkspaceResources(deploymentName, namespace);
    waitUntilWorkspacePodIsDeleted(deploymentName, namespace);
  }

  private void cleanUpWorkspaceResources(final String deploymentName, final String namespace)
      throws IOException {
    Deployment deployment =
        KubernetesResourceUtil.getDeploymentByName(
            deploymentName, namespace, openshiftUserAccountProvider.getWorkspacesOpenshiftConfig());
    Service service =
        KubernetesResourceUtil.getServiceBySelector(
            OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL,
            deploymentName,
            namespace,
            openshiftUserAccountProvider.getWorkspacesOpenshiftConfig());
    List<Route> routes =
        KubernetesResourceUtil.getRoutesByLabel(
            OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL,
            deploymentName,
            namespace,
            openshiftUserAccountProvider.getWorkspacesOpenshiftConfig());
    List<ReplicaSet> replicaSets =
        KubernetesResourceUtil.getReplicaSetByLabel(
            OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL,
            deploymentName,
            namespace,
            openshiftUserAccountProvider.getWorkspacesOpenshiftConfig());

    try (OpenShiftClient openShiftClient =
        new DefaultOpenShiftClient(openshiftUserAccountProvider.getWorkspacesOpenshiftConfig())) {
      if (routes != null) {
        for (Route route : routes) {
          LOG.info("Removing OpenShift Route {}", route.getMetadata().getName());
          openShiftClient.resource(route).delete();
        }
      }

      if (service != null) {
        LOG.info("Removing OpenShift Service {}", service.getMetadata().getName());
        openShiftClient.resource(service).delete();
      }

      if (deployment != null) {
        LOG.info("Removing OpenShift Deployment {}", deployment.getMetadata().getName());
        openShiftClient.resource(deployment).delete();
      }

      if (replicaSets != null && replicaSets.size() > 0) {
        LOG.info("Removing OpenShift ReplicaSets for deployment {}", deploymentName);
        replicaSets.forEach(rs -> openShiftClient.resource(rs).delete());
      }
    }
  }

  private void waitUntilWorkspacePodIsDeleted(final String deploymentName, final String namespace)
      throws OpenShiftException {
    try (OpenShiftClient client =
        new DefaultOpenShiftClient(openshiftUserAccountProvider.getWorkspacesOpenshiftConfig())) {
      for (int waitCount = 0; waitCount < OPENSHIFT_POD_DELETION_TIMEOUT; waitCount++) {
        List<Pod> pods =
            client
                .pods()
                .inNamespace(namespace)
                .withLabel(OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName)
                .list()
                .getItems();

        if (pods.size() == 0) {
          return;
        }
        Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.info("Thread interrupted while cleaning up workspace");
    }

    throw new OpenShiftException("Timeout while waiting for pods to terminate");
  }
}
