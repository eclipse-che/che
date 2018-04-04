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
