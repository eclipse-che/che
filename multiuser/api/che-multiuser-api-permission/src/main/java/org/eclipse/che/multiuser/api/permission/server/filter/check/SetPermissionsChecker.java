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
package org.eclipse.che.multiuser.api.permission.server.filter.check;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Defines contract for domain specific checks, before set permissions.
 *
 * @author Anton Korneta
 */
public interface SetPermissionsChecker {

  /**
   * Checks if the current user is allowed to set permissions.
   *
   * @param permissions permission to set
   * @throws ForbiddenException when it is not allowed to set {@code permissions}
   */
  void check(Permissions permissions) throws ForbiddenException;
}
