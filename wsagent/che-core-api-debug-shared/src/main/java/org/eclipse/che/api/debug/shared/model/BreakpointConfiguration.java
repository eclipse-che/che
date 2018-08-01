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
package org.eclipse.che.api.debug.shared.model;

/** @author Anatolii Bazko */
public interface BreakpointConfiguration {
  /** Indicates if condition is enabled. */
  boolean isConditionEnabled();

  /** Breakpoint hits only if condition is true. */
  String getCondition();

  /** Indicates if the number of hits is enabled. */
  boolean isHitCountEnabled();

  /** The number before breakpoint hits. */
  int getHitCount();

  /** Declares suspend policy. */
  SuspendPolicy getSuspendPolicy();
}
