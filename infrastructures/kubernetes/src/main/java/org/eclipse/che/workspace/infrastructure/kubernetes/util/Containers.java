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
      return KubernetesSize.toBytes(quantity.getAmount());
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
   * Returns the RAM request in bytes, if it is present in given container otherwise 0 will be
   * returned.
   */
  public static long getRamRequest(Container container) {
    final ResourceRequirements resources = container.getResources();
    final Quantity quantity;
    if (resources != null
        && resources.getLimits() != null
        && (quantity = resources.getRequests().get("memory")) != null
        && quantity.getAmount() != null) {
      return KubernetesSize.toBytes(quantity.getAmount());
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
}
