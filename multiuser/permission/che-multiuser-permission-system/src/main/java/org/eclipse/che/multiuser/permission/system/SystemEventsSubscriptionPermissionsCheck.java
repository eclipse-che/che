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
package org.eclipse.che.multiuser.permission.system;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.system.server.SystemEventsWebsocketBroadcaster;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionCheck;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;

/** @author Sergii Leshchenko */
@Singleton
public class SystemEventsSubscriptionPermissionsCheck implements RemoteSubscriptionPermissionCheck {
  @Inject
  public void register(RemoteSubscriptionPermissionManager permissionFilter) {
    permissionFilter.registerCheck(this, SystemEventsWebsocketBroadcaster.SYSTEM_STATE_METHOD_NAME);
  }

  @Override
  public void check(String methodName, Map<String, String> scope) throws ForbiddenException {
    EnvironmentContext.getCurrent()
        .getSubject()
        .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
  }
}
