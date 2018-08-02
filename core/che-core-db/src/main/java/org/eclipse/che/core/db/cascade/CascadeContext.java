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
package org.eclipse.che.core.db.cascade;

/**
 * Context that is used only for sharing the state of the cascading operation among subscribers.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
public class CascadeContext {
  private Exception cause;

  /** Returns the cause which has changed the state of the context. */
  public Exception getCause() {
    return cause;
  }

  /** Returns the state of the context. */
  public boolean isFailed() {
    return cause != null;
  }

  /** Sets the context into failed state. */
  public CascadeContext fail(Exception cause) {
    this.cause = cause;
    return this;
  }
}
