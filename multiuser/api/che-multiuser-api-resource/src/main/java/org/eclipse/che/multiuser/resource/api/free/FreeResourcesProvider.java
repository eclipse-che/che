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
package org.eclipse.che.multiuser.resource.api.free;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.api.ResourcesProvider;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Provides free resources for account usage.
 *
 * <p>Returns free resources limits if it is specified for given account or default free resources
 * limit in other case
 *
 * <p>Default resources should be provided by {@link DefaultResourcesProvider} for different account
 * types
 *
 * @author Sergii Leschenko
 */
@Singleton
public class FreeResourcesProvider implements ResourcesProvider {
  public static final String FREE_RESOURCES_PROVIDER = "free";

  private final FreeResourcesLimitManager freeResourcesLimitManager;
  private final AccountManager accountManager;
  private final Map<String, DefaultResourcesProvider> defaultResourcesProviders;

  @Inject
  public FreeResourcesProvider(
      FreeResourcesLimitManager freeResourcesLimitManager,
      AccountManager accountManager,
      Set<DefaultResourcesProvider> defaultResourcesProviders) {
    this.freeResourcesLimitManager = freeResourcesLimitManager;
    this.accountManager = accountManager;
    this.defaultResourcesProviders =
        defaultResourcesProviders
            .stream()
            .collect(toMap(DefaultResourcesProvider::getAccountType, Function.identity()));
  }

  @Override
  public List<ProvidedResources> getResources(String accountId)
      throws ServerException, NotFoundException {
    Map<String, ResourceImpl> freeResources = new HashMap<>();
    String limitId = null;
    try {
      FreeResourcesLimit resourcesLimit = freeResourcesLimitManager.get(accountId);
      for (Resource resource : resourcesLimit.getResources()) {
        freeResources.put(resource.getType(), new ResourceImpl(resource));
      }
      limitId = resourcesLimit.getAccountId();
    } catch (NotFoundException ignored) {
      // there is no resources limit for given account
    }

    // add default resources which are not specified by limit
    for (ResourceImpl resource : getDefaultResources(accountId)) {
      freeResources.putIfAbsent(resource.getType(), resource);
    }

    if (!freeResources.isEmpty()) {
      return singletonList(
          new ProvidedResourcesImpl(
              FREE_RESOURCES_PROVIDER, limitId, accountId, -1L, -1L, freeResources.values()));
    } else {
      return emptyList();
    }
  }

  private List<ResourceImpl> getDefaultResources(String accountId)
      throws NotFoundException, ServerException {
    List<ResourceImpl> defaultResources = new ArrayList<>();
    final Account account = accountManager.getById(accountId);

    final DefaultResourcesProvider defaultResourcesProvider =
        defaultResourcesProviders.get(account.getType());
    if (defaultResourcesProvider != null) {
      defaultResources.addAll(defaultResourcesProvider.getResources(accountId));
    }

    return defaultResources;
  }
}
