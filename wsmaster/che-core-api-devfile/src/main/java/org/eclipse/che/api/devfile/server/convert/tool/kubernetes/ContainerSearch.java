/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.convert.tool.kubernetes;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.Template;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Container search goes through a list of Kubernetes resources and recursively looks for containers
 * that match the provided criteria.
 *
 * <p>The deployment and pod name criteria work both on the {@code name} and, if name is not set on
 * given deployment/pod, on the {@code generateName} of the objects.
 */
public class ContainerSearch {

  private final @Nullable String deploymentName;
  private final @Nullable String podName;
  private final @Nullable String containerName;
  private final @Nullable Map<String, String> podSelector;
  private final @Nullable Map<String, String> deploymentSelector;

  /**
   * Constructs a new {@code ContainerSearch} instance in somewhat unsurprising manner.
   *
   * @param deploymentName only search for containers in pods of deployments with given name
   * @param podName only search for containers in pods with given name
   * @param containerName only search for containers with given name
   * @param podSelector only search for containers in pods with labels matching the selector
   * @param deploymentSelector only search for containers in pods of deployments with labels
   *     matching the selector
   */
  public ContainerSearch(
      @Nullable String deploymentName,
      @Nullable String podName,
      @Nullable String containerName,
      Map<String, String> podSelector,
      Map<String, String> deploymentSelector) {
    this.deploymentName = deploymentName;
    this.podName = podName;
    this.containerName = containerName;
    this.podSelector = podSelector;
    this.deploymentSelector = deploymentSelector;
  }

  /**
   * Searches for containers in the provided list of Kubernetes resources. If any given item in the
   * list can contain a container (i.e. it is a pod, deployment, Kubernetes list or Openshift
   * template) the item is searched for the containers recursively.
   *
   * @param list the list of Kubernetes resources to sift through
   * @return a list of containers found in the provided resource list
   */
  public List<Container> search(Collection<? extends KubernetesResource> list) {
    List<Container> ret = new ArrayList<>();

    search(list, ret, false, false);

    return ret;
  }

  private void search(
      Collection<? extends KubernetesResource> list,
      Collection<Container> results,
      boolean insideDeployment,
      boolean insidePod) {

    for (KubernetesResource o : list) {
      if (o instanceof Container) {
        if (matchesContainer(insidePod, insideDeployment, ((Container) o).getName())) {
          results.add((Container) o);
        }
      } else if (o instanceof Pod) {
        if (matchesPod(insideDeployment, ((Pod) o).getMetadata())) {
          search(((Pod) o).getSpec().getContainers(), results, insideDeployment, true);
        }
      } else if (o instanceof Deployment) {
        Deployment d = (Deployment) o;
        PodTemplateSpec p = d.getSpec().getTemplate();

        if (matchesDeployment(d.getMetadata()) && matchesPod(true, p.getMetadata())) {
          search(p.getSpec().getContainers(), results, true, true);
        }
      } else if (o instanceof Template) {
        search(((Template) o).getObjects(), results, false, false);
      } else if (o instanceof KubernetesList) {
        search(((KubernetesList) o).getItems(), results, false, false);
      }
    }
  }

  private boolean matchesContainer(
      boolean insidePod, boolean insideDeployment, String containerName) {
    if ((deploymentName != null || deploymentSelector != null) && !insideDeployment) {
      return false;
    }

    if ((podName != null || podSelector != null) && !insidePod) {
      return false;
    }

    return this.containerName == null || this.containerName.equals(containerName);
  }

  private boolean matchesPod(boolean insideDeployment, ObjectMeta podMeta) {
    if ((deploymentName != null || deploymentSelector != null) && !insideDeployment) {
      return false;
    }

    return matches(podMeta, podName, podSelector);
  }

  private boolean matchesDeployment(ObjectMeta deploymentMeta) {
    return matches(deploymentMeta, deploymentName, deploymentSelector);
  }

  private static boolean matches(
      ObjectMeta metaData, @Nullable String name, @Nullable Map<String, String> labels) {
    if (name == null) {
      return labels == null || matchesBySelector(metaData, labels);
    } else {
      boolean ret = matchesByName(metaData, name);
      return labels == null ? ret : ret && matchesBySelector(metaData, labels);
    }
  }

  private static boolean matchesByName(ObjectMeta metaData, String name) {
    if (name == null) {
      return true;
    }

    String metaName = metaData.getName();
    String metaGenerateName = metaData.getGenerateName();

    // do not compare by the generateName if a name exists
    if (metaName != null) {
      return name.equals(metaName);
    } else {
      return name.equals(metaGenerateName);
    }
  }

  private static boolean matchesBySelector(ObjectMeta metaData, Map<String, String> labels) {
    return SelectorFilter.test(metaData, labels);
  }
}
