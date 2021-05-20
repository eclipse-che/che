/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;

/**
 * Helps to simplify the interaction with the {@link Container}.
 *
 * @author Anton Korneta
 */
public class Containers {

  private Containers() {}

  /**
   * Returns the RAM limit in bytes, if it is present in given container otherwise 0 will be
   * returned.
   */
  public static long getRamLimit(Container container) {
    final ResourceRequirements resources = container.getResources();
    final Quantity quantity;
    if (resources != null
        && resources.getLimits() != null
        && (quantity = resources.getLimits().get("memory")) != null
        && quantity.getAmount() != null) {
      return Quantity.getAmountInBytes(quantity).longValue();
    }
    return 0;
  }

  /**
   * Sets given RAM limit in bytes to specified container. Note if the container already contains a
   * RAM limit, it will be overridden, other resources won't be affected.
   */
  public static void addRamLimit(Container container, long ramLimit) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToLimits("memory", new Quantity(String.valueOf(ramLimit))).build());
  }

  /**
   * Sets given RAM limit in kubernetes notion to specified container. Note if the container already
   * contains a RAM limit, it will be overridden, other resources won't be affected.
   */
  public static void addRamLimit(Container container, String limitInK8sNotion) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToLimits("memory", new Quantity(limitInK8sNotion)).build());
  }

  /**
   * Returns the RAM request in bytes, if it is present in given container otherwise 0 will be
   * returned.
   */
  public static long getRamRequest(Container container) {
    final ResourceRequirements resources = container.getResources();
    final Quantity quantity;
    if (resources != null
        && resources.getRequests() != null
        && (quantity = resources.getRequests().get("memory")) != null
        && quantity.getAmount() != null) {
      return Quantity.getAmountInBytes(quantity).longValue();
    }
    return 0;
  }

  /**
   * Sets given RAM request in bytes to specified container. Note if the container already contains
   * a RAM limit, it will be overridden, other resources won't be affected.
   */
  public static void addRamRequest(Container container, long ramRequest) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToRequests("memory", new Quantity(String.valueOf(ramRequest))).build());
  }

  /**
   * Sets given RAM request in kubernetes notion to specified container. Note if the container
   * already contains a RAM request, it will be overridden, other resources won't be affected.
   */
  public static void addRamRequest(Container container, String limitInK8sNotion) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToRequests("memory", new Quantity(limitInK8sNotion)).build());
  }

  /**
   * Returns the CPU limit in cores, if it is present in given container otherwise 0 will be
   * returned.
   */
  public static float getCpuLimit(Container container) {
    final ResourceRequirements resources = container.getResources();
    final Quantity quantity;
    if (resources != null
        && resources.getLimits() != null
        && (quantity = resources.getLimits().get("cpu")) != null
        && quantity.getAmount() != null) {
      return KubernetesSize.toCores(quantity);
    }
    return 0;
  }

  /**
   * Sets given CPU limit in cores to specified container. Note if the container already contains a
   * CPU limit, it will be overridden, other resources won't be affected.
   */
  public static void addCpuLimit(Container container, float cpuLimit) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToLimits("cpu", new Quantity(Float.toString(cpuLimit))).build());
  }

  /**
   * Sets given CPU limit in kubernetes notion to specified container. Note if the container already
   * contains a CPU limit, it will be overridden, other resources won't be affected.
   */
  public static void addCpuLimit(Container container, String limitInK8sNotion) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToLimits("cpu", new Quantity(limitInK8sNotion)).build());
  }

  /**
   * Returns the CPU request in bytes, if it is present in given container otherwise 0 will be
   * returned.
   */
  public static float getCpuRequest(Container container) {
    final ResourceRequirements resources = container.getResources();
    final Quantity quantity;
    if (resources != null
        && resources.getRequests() != null
        && (quantity = resources.getRequests().get("cpu")) != null
        && quantity.getAmount() != null) {
      return KubernetesSize.toCores(quantity);
    }
    return 0;
  }

  /**
   * Sets given CPU request in bytes to specified container. Note if the container already contains
   * a CPU limit, it will be overridden, other resources won't be affected.
   */
  public static void addCpuRequest(Container container, float cpuRequest) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToRequests("cpu", new Quantity(Float.toString(cpuRequest))).build());
  }

  /**
   * Sets given CPU request in kubernetes notion to specified container. Note if the container
   * already contains a CPU request, it will be overridden, other resources won't be affected.
   */
  public static void addCpuRequest(Container container, String limitInK8sNotion) {
    final ResourceRequirementsBuilder resourceBuilder;
    if (container.getResources() != null) {
      resourceBuilder = new ResourceRequirementsBuilder(container.getResources());
    } else {
      resourceBuilder = new ResourceRequirementsBuilder();
    }
    container.setResources(
        resourceBuilder.addToRequests("cpu", new Quantity(limitInK8sNotion)).build());
  }
}
