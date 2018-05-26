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
