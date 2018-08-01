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
package org.eclipse.che.multiuser.api.permission.server;

import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Implementation of {@link PermissionChecker} that use {@link PermissionsManager} for checking.
 *
 * @author Sergii Leschenko
 */
public class PermissionCheckerImpl implements PermissionChecker {
  private final PermissionsManager permissionsManager;

  @Inject
  public PermissionCheckerImpl(PermissionsManager permissionsManager) {
    this.permissionsManager = permissionsManager;
  }

  @Override
  public boolean hasPermission(String user, String domain, String instance, String action)
      throws ServerException, NotFoundException, ConflictException {
    return permissionsManager.exists(user, domain, instance, action)
        || permissionsManager.exists("*", domain, instance, action);
  }
}
