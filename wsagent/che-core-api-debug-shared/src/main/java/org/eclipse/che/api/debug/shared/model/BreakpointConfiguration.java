/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
