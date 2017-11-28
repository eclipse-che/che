/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.multiuser.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.READ;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.SEARCH;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.getActions;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.filter.check.DefaultSetPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.server.filter.check.SetPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Stack domain specific set permission checker.
 *
 * @author Anton Korneta
 */
@Singleton
public class StackDomainSetPermissionsChecker implements SetPermissionsChecker {

  private final DefaultSetPermissionsChecker defaultChecker;

  @Inject
  public StackDomainSetPermissionsChecker(DefaultSetPermissionsChecker defaultChecker) {
    this.defaultChecker = defaultChecker;
  }

  @Override
  public void check(Permissions permissions) throws ForbiddenException {
    if (!"*".equals(permissions.getUserId())) {
      defaultChecker.check(permissions);
      return;
    }
    final Set<String> unsupportedPublicActions = new HashSet<>(permissions.getActions());
    unsupportedPublicActions.remove(READ);

    // public search is supported only for admins
    if (EnvironmentContext.getCurrent()
        .getSubject()
        .hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)) {
      unsupportedPublicActions.remove(SEARCH);
    } else {
      defaultChecker.check(permissions);
    }

    if (!unsupportedPublicActions.isEmpty()) {
      throw new ForbiddenException(
          "Following actions are not supported for setting as public:"
              + getActions()
                  .stream()
                  .filter(a -> !(a.equals(READ) || a.equals(SEARCH)))
                  .collect(toList()));
    }
  }
}
