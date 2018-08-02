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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.multiuser.resource.api.ResourceLockKeyProvider;

/**
 * Helps to lock resources while performing operations related to them.
 *
 * <p>Resources will be locked not by account id but by key which will be provided by {@link
 * ResourceLockKeyProvider} for specified account's type
 *
 * <p>It based on {@link StripedLocks} so it can be used in try-with-resources construction.
 *
 * <pre>
 * try (Unlocker u = resourceLocks.lock("account123")) {
 *    // check resources availability and perform operation here
 * }
 * </pre>
 *
 * @author Sergii Leschenko
 */
public class ResourcesLocks {

  private final AccountManager accountManager;
  private final Map<String, ResourceLockKeyProvider> accountTypeToLockProvider;
  private final StripedLocks stripedLocks;

  @Inject
  public ResourcesLocks(
      Set<ResourceLockKeyProvider> resourceLockKeyProviders, AccountManager accountManager) {
    this.accountManager = accountManager;
    this.stripedLocks = new StripedLocks(16);
    this.accountTypeToLockProvider =
        resourceLockKeyProviders
            .stream()
            .collect(
                Collectors.toMap(ResourceLockKeyProvider::getAccountType, Function.identity()));
  }

  /**
   * Acquire resources lock for specified account.
   *
   * @param accountId account id to lock resources
   * @return lock for unlocking resources when resources operation finishes
   * @throws NotFoundException when account with specified {@code account id} was not found
   * @throws ServerException when any other error occurs
   */
  public Unlocker lock(String accountId) throws NotFoundException, ServerException {
    final Account account = accountManager.getById(accountId);
    final ResourceLockKeyProvider resourceLockKeyProvider =
        accountTypeToLockProvider.get(account.getType());
    String lockKey;
    if (resourceLockKeyProvider == null) {
      // this account type doesn't have custom lock provider.
      // Lock resources by current account
      lockKey = accountId;
    } else {
      lockKey = resourceLockKeyProvider.getLockKey(accountId);
    }

    return stripedLocks.writeLock(lockKey);
  }

  public Unlocker lock(String... accountIds) throws NotFoundException, ServerException {
    // TODO It should be implemented for making possible lock resources by two or more accounts in
    // case of resources redistribution
    throw new UnsupportedOperationException("Not implemented.");
  }
}
