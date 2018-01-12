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
package org.eclipse.che.multiuser.organization.api.resource;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageManager;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides available resources for organizational and suborganizational accounts.
 *
 * <p>Root organizational account can use resources by itself or share them for its
 * suborganizations. So available resources equal to total resources minus resources which are
 * already used by organization or by any of its suborganizations.
 *
 * <p>Suborganizational account can use all of parent resources or limited amount. So available
 * resource equal to minimum of parent available resources and parent shared resources minus
 * resources which are used by suborganization and its suborganizations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationalAccountAvailableResourcesProvider implements AvailableResourcesProvider {
  private static final Logger LOG =
      LoggerFactory.getLogger(OrganizationalAccountAvailableResourcesProvider.class);

  private final Provider<ResourceUsageManager> resourceUsageManagerProvider;
  private final ResourceAggregator resourceAggregator;
  private final OrganizationManager organizationManager;

  @Inject
  public OrganizationalAccountAvailableResourcesProvider(
      Provider<ResourceUsageManager> resourceUsageManagerProvider,
      ResourceAggregator resourceAggregator,
      OrganizationManager organizationManager) {
    this.resourceUsageManagerProvider = resourceUsageManagerProvider;
    this.resourceAggregator = resourceAggregator;
    this.organizationManager = organizationManager;
  }

  @Override
  public List<? extends Resource> getAvailableResources(String accountId)
      throws NotFoundException, ServerException {
    Organization organization = organizationManager.getById(accountId);

    if (organization.getParent() == null) {
      return getAvailableOrganizationResources(organization);
    } else {
      Organization parentOrganization = organizationManager.getById(organization.getParent());
      return resourceAggregator.min(
          resourceAggregator.intersection(
              getAvailableOrganizationResources(parentOrganization),
              getAvailableOrganizationResources(organization)));
    }
  }

  /**
   * Returns total resources minus resources which are already used by organization or by any of its
   * suborganizations.
   *
   * @param organization organization id to calculate its available resources
   * @return resources which are available for usage by specified organization
   * @throws NotFoundException when organization with specified id doesn't exist
   * @throws ServerException when any other exception occurs on calculation of available resources
   */
  @VisibleForTesting
  List<? extends Resource> getAvailableOrganizationResources(Organization organization)
      throws NotFoundException, ServerException {
    final ResourceUsageManager resourceUsageManager = resourceUsageManagerProvider.get();
    final List<? extends Resource> total =
        resourceUsageManager.getTotalResources(organization.getId());
    final List<Resource> unavailable =
        new ArrayList<>(resourceUsageManager.getUsedResources(organization.getId()));
    unavailable.addAll(getUsedResourcesBySuborganizations(organization.getQualifiedName()));
    try {
      return resourceAggregator.deduct(total, unavailable);
    } catch (NoEnoughResourcesException e) {
      LOG.warn(
          "Organization with id {} uses more resources {} than it has {}.",
          organization.getId(),
          format(unavailable),
          format(total));
      return resourceAggregator.excess(total, unavailable);
    }
  }

  /**
   * Returns resources which are used by suborganizations of specified organization.
   *
   * <p>Note that the result will includes used resources of all direct and nested suborganizations.
   *
   * @param parentQualifiedName parent qualified name, e.g. 'parentName/suborgName
   * @return resources which are used by suborganizations of specified organization.
   * @throws ServerException when any other exception occurs on calculation of used resources
   */
  @VisibleForTesting
  List<Resource> getUsedResourcesBySuborganizations(String parentQualifiedName)
      throws NotFoundException, ServerException {
    ResourceUsageManager resourceUsageManager = resourceUsageManagerProvider.get();
    List<Resource> usedResources = new ArrayList<>();
    for (Organization suborganization :
        Pages.iterate(
            (maxItems, skipCount) ->
                organizationManager.getSuborganizations(
                    parentQualifiedName, maxItems, skipCount))) {
      usedResources.addAll(resourceUsageManager.getUsedResources(suborganization.getId()));
    }
    return usedResources;
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
