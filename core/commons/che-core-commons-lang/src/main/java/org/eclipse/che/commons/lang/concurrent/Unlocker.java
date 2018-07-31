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
package org.eclipse.che.commons.lang.concurrent;

/**
 * An interface that allows implementations to enclose locked instance and unlock it later by
 * calling {@link #unlock()}.
 *
 * <p>This is designed to be used in try-with-resources statement.
 *
 * <p>The example:
 *
 * <pre>
 *     try (@SuppressWarnings("unused") Unlocker u = customLocks.lock("key")) {
 *         // do something in lock
 *     }
 * </pre>
 *
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
public interface Unlocker extends AutoCloseable {

  /** Unlocks the corresponding lock in implementation specific manner. */
  void unlock();

  @Override
  default void close() {
    unlock();
  }
}
