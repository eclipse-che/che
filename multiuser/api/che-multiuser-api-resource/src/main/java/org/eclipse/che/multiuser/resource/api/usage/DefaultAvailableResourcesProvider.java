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
package org.eclipse.che.multiuser.resource.api.usage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of providing available resources for accounts.
 *
 * <p>By default account can use resources only by itself, so available resources equals to total
 * resources minus resources which are already used by account.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultAvailableResourcesProvider implements AvailableResourcesProvider {
  private static final Logger LOG =
      LoggerFactory.getLogger(DefaultAvailableResourcesProvider.class);

  private final Provider<ResourceManager> resourceManagerProvider;
  private final ResourceAggregator resourceAggregator;

  @Inject
  public DefaultAvailableResourcesProvider(
      Provider<ResourceManager> resourceManagerProvider, ResourceAggregator resourceAggregator) {
    this.resourceManagerProvider = resourceManagerProvider;
    this.resourceAggregator = resourceAggregator;
  }

  @Override
  public List<? extends Resource> getAvailableResources(String accountId)
      throws NotFoundException, ServerException {
    ResourceManager resourceManager = resourceManagerProvider.get();
    List<? extends Resource> totalResources = null;
    List<Resource> usedResources = null;
    try {
      totalResources = resourceManager.getTotalResources(accountId);
      usedResources = new ArrayList<>(resourceManager.getUsedResources(accountId));
      return resourceAggregator.deduct(totalResources, usedResources);
    } catch (NoEnoughResourcesException e) {
      LOG.warn(
          "Account with id {} uses more resources {} than he has {}.",
          accountId,
          format(usedResources),
          format(totalResources));
      return resourceAggregator.excess(totalResources, usedResources);
    }
  }

  /** Returns formatted string for list of resources. */
  private static String format(Collection<? extends Resource> resources) {
    return '['
        + resources
            .stream()
            .map(
                resource -> resource.getAmount() + resource.getUnit() + " of " + resource.getType())
            .collect(Collectors.joining(", "))
        + ']';
  }
}
