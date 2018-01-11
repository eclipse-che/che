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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.Path;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.account.AccountOperation;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Restricts access to methods of {@link AccountLicenseService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link AccountLicenseService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/license/account{path:(/.*)?}")
public class LicenseServicePermissionsFilter extends CheMethodInvokerFilter {
  public static final String GET_LICENSE_METHOD = "getLicense";

  private final AccountManager accountManager;
  private final Map<String, AccountPermissionsChecker> permissionsCheckers;

  @Inject
  public LicenseServicePermissionsFilter(
      AccountManager accountManager, Set<AccountPermissionsChecker> permissionsCheckers) {
    this.accountManager = accountManager;
    this.permissionsCheckers =
        permissionsCheckers
            .stream()
            .collect(toMap(AccountPermissionsChecker::getAccountType, identity()));
  }

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    String accountId;
    switch (genericMethodResource.getMethod().getName()) {
      case GET_LICENSE_METHOD:
        accountId = ((String) arguments[0]);
        break;

      default:
        throw new ForbiddenException("The user does not have permission to perform this operation");
    }

    final Account account = accountManager.getById(accountId);
    final AccountPermissionsChecker permissionsChecker = permissionsCheckers.get(account.getType());
    if (permissionsChecker != null) {
      permissionsChecker.checkPermissions(accountId, AccountOperation.SEE_RESOURCE_INFORMATION);
    } else {
      throw new ForbiddenException("User is not authorized to perform given operation");
    }
  }
}
