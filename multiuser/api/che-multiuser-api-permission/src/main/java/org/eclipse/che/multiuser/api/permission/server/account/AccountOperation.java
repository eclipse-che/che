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

/**
 * Actions that can be performed by users in accounts.
 *
 * @author Sergii Leshchenko
 */
public enum AccountOperation {
  /** When user creates workspace that will belong to account. */
  CREATE_WORKSPACE,

  /** When user does any operation with existing workspace. */
  MANAGE_WORKSPACES,

  /** When user retrieves information about account resources(like available, total, etc). */
  SEE_RESOURCE_INFORMATION
}
