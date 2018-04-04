/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.exception;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.multiuser.resource.model.Resource;

/**
 * Thrown in case when account doesn't have enough resources to perform some operation.
 *
 * <p>It contains detailed information about resources so required, available, missing amounts to
 * provide ability to construct user friendly message.
 *
 * @author Sergii Leschenko
 */
public class NoEnoughResourcesException extends Exception {
  private static final String MESSAGE =
      "Account has %s resources to use, but operation requires %s. It requires more %s.";

  private String message;
  private List<? extends Resource> availableResources;
  private List<? extends Resource> requiredResources;
  private List<? extends Resource> missedResources;

  public NoEnoughResourcesException(
      Resource availableResource, Resource requiredResource, Resource missedResource) {
    this(
        singletonList(availableResource),
        singletonList(requiredResource),
        singletonList(missedResource));
  }

  public NoEnoughResourcesException(
      List<? extends Resource> availableResources,
      List<? extends Resource> requiredResources,
      List<? extends Resource> missedResources) {
    this.availableResources = availableResources;
    this.requiredResources = requiredResources;
    this.missedResources = missedResources;
  }

  @Override
  public String getMessage() {
    if (message == null) {
      message =
          String.format(
              MESSAGE,
              resourcesToString(availableResources),
              resourcesToString(requiredResources),
              resourcesToString(missedResources));
    }
    return message;
  }

  public List<? extends Resource> getRequiredResources() {
    return requiredResources;
  }

  public List<? extends Resource> getAvailableResources() {
    return availableResources;
  }

  public List<? extends Resource> getMissingResources() {
    return missedResources;
  }

  private String resourcesToString(List<? extends Resource> resources) {
    return '['
        + resources
            .stream()
            .map(resource -> resource.getAmount() + resource.getUnit() + " " + resource.getType())
            .collect(Collectors.joining(", "))
        + ']';
  }
}
