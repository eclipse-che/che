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
package org.eclipse.che.api.system.shared.event;

/**
 * Defines set of system event types.
 *
 * @author Yevhenii Voevodin
 */
public enum EventType {

  /** Published when system status is changed. */
  STATUS_CHANGED,

  /**
   * Published when system is starting shutting down a service. This is the first event published
   * for a certain service.
   *
   * <pre>
   *     STOPPING_SERVICE -> (0..N)SERVICE_ITEM_STOPPED -> SERVICE_STOPPED
   * </pre>
   */
  STOPPING_SERVICE,

  /**
   * Published after service item is stopped. Events of such type are published between {@link
   * #STOPPING_SERVICE} and {@link #SERVICE_STOPPED} events.
   */
  SERVICE_ITEM_STOPPED,

  /**
   * Published when shutting down of a service is finished. The last event in the chain for a
   * certain service.
   */
  SERVICE_STOPPED
}
