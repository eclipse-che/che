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
package org.eclipse.che.multiuser.resource.api.license;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.model.AccountLicense;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.AccountLicenseImpl;

/**
 * Facade for Account License related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class AccountLicenseManager {
  private final Set<ResourcesProvider> resourcesProviders;
  private final ResourceAggregator resourceAggregator;

  @Inject
  public AccountLicenseManager(
      Set<ResourcesProvider> resourcesProviders, ResourceAggregator resourceAggregator) {
    this.resourcesProviders = resourcesProviders;
    this.resourceAggregator = resourceAggregator;
  }

  /**
   * Returns license which given account can use.
   *
   * @param accountId account id
   * @return license which can be used by given account
   * @throws NotFoundException when account with specified id was not found
   * @throws ServerException when some exception occurs
   */
  public AccountLicense getByAccount(String accountId) throws NotFoundException, ServerException {
    final List<ProvidedResources> resources = new ArrayList<>();
    for (ResourcesProvider resourcesProvider : resourcesProviders) {
      resources.addAll(resourcesProvider.getResources(accountId));
    }

    final List<Resource> allResources =
        resources
            .stream()
            .flatMap(providedResources -> providedResources.getResources().stream())
            .collect(Collectors.toList());

    return new AccountLicenseImpl(
        accountId,
        resources,
        new ArrayList<>(resourceAggregator.aggregateByType(allResources).values()));
  }
}
