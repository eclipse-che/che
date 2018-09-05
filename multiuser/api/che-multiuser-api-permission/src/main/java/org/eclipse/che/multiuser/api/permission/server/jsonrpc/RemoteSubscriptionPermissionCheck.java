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

import java.util.Map;
import org.eclipse.che.api.core.ForbiddenException;

/**
 * Check that should be performed before remote subscribing.
 *
 * @author Sergii Leshchenko
 * @see org.eclipse.che.api.core.notification.RemoteSubscriptionManager
 */
public interface RemoteSubscriptionPermissionCheck {

  /**
   * Check that the current subject is allowed to listen to the specified method events
   *
   * @param methodName method name to subscribe
   * @param scope scope of subscription
   * @throws ForbiddenException if the current subject does not have needed permissions
   * @throws ForbiddenException if any other exception occurred during permissions check
   */
  void check(String methodName, Map<String, String> scope) throws ForbiddenException;
}
