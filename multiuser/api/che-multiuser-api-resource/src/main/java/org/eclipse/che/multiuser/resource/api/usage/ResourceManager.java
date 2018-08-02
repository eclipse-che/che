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
package org.eclipse.che.multiuser.resource.api.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.ResourcesProvider;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.model.ResourcesDetails;
import org.eclipse.che.multiuser.resource.spi.impl.ResourcesDetailsImpl;

/**
 * Facade for resources related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceManager {
  private final ResourceAggregator resourceAggregator;
  private final Set<ResourcesProvider> resourcesProviders;
  private final Set<ResourceUsageTracker> usageTrackers;
  private final AccountManager accountManager;
  private final Map<String, AvailableResourcesProvider> accountTypeToAvailableResourcesProvider;
  private final DefaultAvailableResourcesProvider defaultAvailableResourcesProvider;

  @Inject
  public ResourceManager(
      ResourceAggregator resourceAggregator,
      Set<ResourcesProvider> resourcesProviders,
      Set<ResourceUsageTracker> usageTrackers,
      AccountManager accountManager,
      Map<String, AvailableResourcesProvider> accountTypeToAvailableResourcesProvider,
      DefaultAvailableResourcesProvider defaultAvailableResourcesProvider) {
    this.resourceAggregator = resourceAggregator;
    this.resourcesProviders = resourcesProviders;
    this.usageTrackers = usageTrackers;
    this.accountManager = accountManager;
    this.accountTypeToAvailableResourcesProvider = accountTypeToAvailableResourcesProvider;
    this.defaultAvailableResourcesProvider = defaultAvailableResourcesProvider;
  }

  /**
   * Returns list of resources which are available for usage by given account.
   *
   * @param accountId id of account
   * @return list of resources which are available for usage by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurred while resources fetching
   */
  public List<? extends Resource> getTotalResources(String accountId)
      throws NotFoundException, ServerException {
    return getResourceDetails(accountId).getTotalResources();
  }

  /**
   * Returns list of resources which are available for usage by given account.
   *
   * @param accountId id of account
   * @return list of resources which are available for usage by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurred while resources fetching
   */
  public List<? extends Resource> getAvailableResources(String accountId)
      throws NotFoundException, ServerException {
    final Account account = accountManager.getById(accountId);
    final AvailableResourcesProvider availableResourcesProvider =
        accountTypeToAvailableResourcesProvider.get(account.getType());

    if (availableResourcesProvider == null) {
      return defaultAvailableResourcesProvider.getAvailableResources(accountId);
    }

    return availableResourcesProvider.getAvailableResources(accountId);
  }

  /**
   * Returns list of resources which are used by given account.
   *
   * @param accountId id of account
   * @return list of resources which are used by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurred while resources fetching
   */
  public List<? extends Resource> getUsedResources(String accountId)
      throws NotFoundException, ServerException {
    List<Resource> usedResources = new ArrayList<>();
    for (ResourceUsageTracker usageTracker : usageTrackers) {
      Optional<Resource> usedResource = usageTracker.getUsedResource(accountId);
      usedResource.ifPresent(usedResources::add);
    }
    return usedResources;
  }

  /**
   * Checks that specified account has available resources to use
   *
   * @param accountId account id
   * @param resources resources to check availability
   * @throws NotFoundException when account with specified id was not found
   * @throws NoEnoughResourcesException when account doesn't have specified available resources
   * @throws ServerException when any other error occurs
   */
  public void checkResourcesAvailability(String accountId, List<? extends Resource> resources)
      throws NotFoundException, NoEnoughResourcesException, ServerException {
    List<? extends Resource> availableResources = getAvailableResources(accountId);
    // check resources availability
    resourceAggregator.deduct(availableResources, resources);
  }

  /**
   * Returns detailed information about resources which given account can use.
   *
   * @param accountId account id
   * @return detailed information about resources which can be used by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurs
   */
  public ResourcesDetails getResourceDetails(String accountId)
      throws NotFoundException, ServerException {
    final List<ProvidedResources> resources = new ArrayList<>();
    for (ResourcesProvider resourcesProvider : resourcesProviders) {
      resources.addAll(resourcesProvider.getResources(accountId));
    }

    final List<Resource> allResources =
        resources
            .stream()
            .flatMap(providedResources -> providedResources.getResources().stream())
            .collect(Collectors.toList());

    return new ResourcesDetailsImpl(
        accountId,
        resources,
        new ArrayList<>(resourceAggregator.aggregateByType(allResources).values()));
  }
}
