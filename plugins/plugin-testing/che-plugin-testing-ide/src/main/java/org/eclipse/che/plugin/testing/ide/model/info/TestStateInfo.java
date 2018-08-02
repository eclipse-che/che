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
package org.eclipse.che.plugin.testing.ide.model.info;

/** Represents test/suite state info. */
public interface TestStateInfo {

  /**
   * Final(immutable) state, ie this state will not changed.
   *
   * @return
   */
  boolean isFinal();

  /** @return Test/Suite is running */
  boolean isInProgress();

  /** @return this Test/Suite has problem, which user can be notified */
  boolean isProblem();

  /** @return Test/Suite was launched */
  boolean wasLaunched();

  /** @return this Test/Suite was terminated by user */
  boolean wasTerminated();

  /** Describe test state type */
  TestStateDescription getDescription();
}
