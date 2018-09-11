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
package org.eclipse.che.multiuser.api.permission.server.jsonrpc;

import static java.lang.String.format;
import static org.eclipse.che.api.core.notification.RemoteSubscriptionManager.SUBSCRIBE_JSON_RPC_METHOD;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.notification.dto.EventSubscription;

/**
 * Filters invocation of {@link
 * org.eclipse.che.api.core.notification.RemoteSubscriptionManager#SUBSCRIBE_JSON_RPC_METHOD} and
 * performs the corresponding {@link RemoteSubscriptionPermissionCheck} to make sure that user is
 * authorized to listen to requested events.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class RemoteSubscriptionPermissionManager {

  private final Map<String, RemoteSubscriptionPermissionCheck> methodToCheck;

  public RemoteSubscriptionPermissionManager() {
    this.methodToCheck = new HashMap<>();
  }

  @Inject
  void register(RequestHandlerManager requestHandlerManager) {
    requestHandlerManager.registerMethodInvokerFilter(
        new RemoteSubscriptionFilter(), SUBSCRIBE_JSON_RPC_METHOD);
  }

  /**
   * Registers permissions check for the specified methods
   *
   * @param permissionCheck permissions check that should be invoked before subscription
   * @param methods methods to filter
   * @throws IllegalStateException if any of specified methods already has registered check
   */
  public synchronized void registerCheck(
      RemoteSubscriptionPermissionCheck permissionCheck, String... methods) {
    for (String method : methods) {
      if (methodToCheck.containsKey(method)) {
        throw new IllegalStateException(
            format("Permissions check is already registered for method '%s'", method));
      }

      methodToCheck.put(method, permissionCheck);
    }
  }

  private class RemoteSubscriptionFilter extends JsonRpcPermissionsFilterAdapter {
    @Override
    protected void doAccept(String method, Object... params) throws ForbiddenException {
      EventSubscription param = (EventSubscription) params[0];

      RemoteSubscriptionPermissionCheck permissionCheck = methodToCheck.get(param.getMethod());
      if (permissionCheck != null) {
        permissionCheck.check(param.getMethod(), param.getScope());
      }
    }
  }
}
