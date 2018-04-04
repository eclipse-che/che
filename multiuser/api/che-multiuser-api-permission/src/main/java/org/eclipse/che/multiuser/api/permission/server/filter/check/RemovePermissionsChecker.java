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
