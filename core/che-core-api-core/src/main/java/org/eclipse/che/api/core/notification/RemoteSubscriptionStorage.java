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
package org.eclipse.che.api.core.notification;

import java.util.Set;

/**
 * Method - based storage of event subscriptions.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public interface RemoteSubscriptionStorage {

  /**
   * Returns all active subscriptions for the given method. It is recommended for implementations to
   * return copy of the stored set, so this method should not be used for modifying operations.
   *
   * @param method Method name
   * @return active subscriptions to this method
   */
  Set<RemoteSubscriptionContext> getByMethod(String method);

  /**
   * Adds new subscription to the given method subscriptions list
   *
   * @param method Method name
   * @param remoteSubscriptionContext new subscription
   */
  void addSubscription(String method, RemoteSubscriptionContext remoteSubscriptionContext);

  /**
   * Removes particular subscription from the given method subscriptions list
   *
   * @param method Method name
   * @param endpointId id of endpoint to remove
   */
  void removeSubscription(String method, String endpointId);
}
