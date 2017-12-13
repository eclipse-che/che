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
package org.eclipse.che.plugin.openshift.client.kubernetes;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import java.util.List;
import org.eclipse.che.plugin.openshift.client.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KubernetesResourceUtil {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesResourceUtil.class);

  private KubernetesResourceUtil() {}

  public static Deployment getDeploymentByName(
      String deploymentName,
      String namespace,
      final Config config,
      final OpenShiftClientFactory ocFactory)
      throws IOException {
    DefaultKubernetesClient kubeClient = ocFactory.newKubeClient(config);
    Deployment deployment =
        kubeClient.extensions().deployments().inNamespace(namespace).withName(deploymentName).get();
    if (deployment == null) {
      LOG.warn("No Deployment with name {} could be found", deploymentName);
    }
    return deployment;
  }

  public static Service getServiceBySelector(
      final String selectorKey,
      final String selectorValue,
      final String namespace,
      final Config config,
      final OpenShiftClientFactory ocFactory) {
    DefaultKubernetesClient kubeClient = ocFactory.newKubeClient(config);
    ServiceList svcs = kubeClient.services().inNamespace(namespace).list();

    Service svc =
        svcs.getItems()
            .stream()
            .filter(s -> s.getSpec().getSelector().containsKey(selectorKey))
            .filter(s -> s.getSpec().getSelector().get(selectorKey).equals(selectorValue))
            .findAny()
            .orElse(null);

    if (svc == null) {
      LOG.warn("No Service with selector {}={} could be found", selectorKey, selectorValue);
    }
    return svc;
  }

  public static List<Route> getRoutesByLabel(
      final String labelKey,
      final String labelValue,
      final String namespace,
      final Config config,
      final OpenShiftClientFactory ocFactory)
      throws IOException {
    OpenShiftClient openShiftClient = ocFactory.newOcClient(config);
    RouteList routeList =
        openShiftClient.routes().inNamespace(namespace).withLabel(labelKey, labelValue).list();

    List<Route> items = routeList.getItems();

    if (items.isEmpty()) {
      LOG.warn("No Route with label {}={} could be found", labelKey, labelValue);
      throw new IOException(
          "No Route with label " + labelKey + "=" + labelValue + " could be found");
    }

    return items;
  }

  public static List<ReplicaSet> getReplicaSetByLabel(
      final String key,
      final String value,
      final String namespace,
      final Config config,
      final OpenShiftClientFactory ocFactory) {
    DefaultKubernetesClient kubeClient = ocFactory.newKubeClient(config);
    List<ReplicaSet> replicaSets =
        kubeClient
            .extensions()
            .replicaSets()
            .inNamespace(namespace)
            .withLabel(key, value)
            .list()
            .getItems();
    return replicaSets;
  }
}
