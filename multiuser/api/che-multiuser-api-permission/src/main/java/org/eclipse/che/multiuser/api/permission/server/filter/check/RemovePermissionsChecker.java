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
package org.eclipse.che.multiuser.api.permission.server.filter.check;

import org.eclipse.che.api.core.ForbiddenException;

/**
 * Defines contract for domain specific checks, before remove permissions.
 *
 * @author Anton Korneta
 */
public interface RemovePermissionsChecker {

  /**
   * Checks if the current user is allowed to remove permissions.
   *
   * @param user user identifier
   * @param domainId permissions domain
   * @param instance instance associated with the permissions to be removed
   * @throws ForbiddenException when it is not allowed to remove permissions
   */
  void check(String user, String domainId, String instance) throws ForbiddenException;
}
