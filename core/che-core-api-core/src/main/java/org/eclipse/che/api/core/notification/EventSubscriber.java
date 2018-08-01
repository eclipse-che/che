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
package org.eclipse.che.api.core.notification;

/**
 * Receives notification events from EventService.
 *
 * @author andrew00x
 * @see EventService
 */
public interface EventSubscriber<T> {
  /**
   * Receives notification that an event has been published to the EventService. If the method
   * throws an unchecked exception it is ignored.
   */
  void onEvent(T event);
}
