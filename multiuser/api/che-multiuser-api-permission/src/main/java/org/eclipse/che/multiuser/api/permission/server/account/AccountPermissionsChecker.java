/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server.account;

import org.eclipse.che.api.core.ForbiddenException;

/**
 * Defines permissions checking for accounts with some type.
 *
 * @author Sergii Leshchenko
 */
public interface AccountPermissionsChecker {
  /**
   * Checks that current subject is authorized to perform given operation with specified account
   *
   * @param accountId account to check
   * @param operation operation that is going to be performed
   * @throws ForbiddenException when user doesn't have permissions to perform specified operation
   */
  void checkPermissions(String accountId, AccountOperation operation) throws ForbiddenException;

  /** Returns account type for which this class tracks check resources permissions. */
  String getAccountType();
}
