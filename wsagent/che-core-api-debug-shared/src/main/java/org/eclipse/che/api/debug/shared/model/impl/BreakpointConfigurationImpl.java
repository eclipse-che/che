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
package org.eclipse.che.api.debug.shared.model.impl;

import com.google.common.base.Objects;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;

/** @author Anatolii Bazko */
public class BreakpointConfigurationImpl implements BreakpointConfiguration {
  private boolean isConditionEnabled;
  private String condition;
  private boolean isHitCountEnabled;
  private int hitCount;
  private SuspendPolicy suspendPolicy;

  public BreakpointConfigurationImpl(
      boolean isConditionEnabled,
      String condition,
      boolean isHitCountEnabled,
      int hitCount,
      SuspendPolicy suspendPolicy) {
    this.isConditionEnabled = isConditionEnabled;
    this.condition = condition;
    this.isHitCountEnabled = isHitCountEnabled;
    this.hitCount = hitCount;
    this.suspendPolicy = suspendPolicy;
  }

  public BreakpointConfigurationImpl() {
    this(false, null, false, 0, SuspendPolicy.ALL);
  }

  public BreakpointConfigurationImpl(String condition) {
    this(true, condition, false, 0, null);
  }

  public BreakpointConfigurationImpl(int hitCount) {
    this(false, null, true, hitCount, null);
  }

  public BreakpointConfigurationImpl(SuspendPolicy suspendPolicy) {
    this(false, null, false, 0, suspendPolicy);
  }

  public BreakpointConfigurationImpl(BreakpointConfiguration breakpointConfiguration) {
    this(
        breakpointConfiguration.isConditionEnabled(),
        breakpointConfiguration.getCondition(),
        breakpointConfiguration.isHitCountEnabled(),
        breakpointConfiguration.getHitCount(),
        breakpointConfiguration.getSuspendPolicy());
  }

  @Override
  public boolean isConditionEnabled() {
    return isConditionEnabled;
  }

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public boolean isHitCountEnabled() {
    return isHitCountEnabled;
  }

  @Override
  public int getHitCount() {
    return hitCount;
  }

  @Override
  public SuspendPolicy getSuspendPolicy() {
    return suspendPolicy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BreakpointConfigurationImpl)) return false;
    BreakpointConfigurationImpl that = (BreakpointConfigurationImpl) o;
    return isConditionEnabled == that.isConditionEnabled
        && isHitCountEnabled == that.isHitCountEnabled
        && hitCount == that.hitCount
        && Objects.equal(condition, that.condition)
        && suspendPolicy == that.suspendPolicy;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        isConditionEnabled, condition, isHitCountEnabled, hitCount, suspendPolicy);
  }

  @Override
  public String toString() {
    return "BreakpointConfigurationImpl{"
        + "isConditionEnabled="
        + isConditionEnabled
        + ", condition='"
        + condition
        + '\''
        + ", isHitCountEnabled="
        + isHitCountEnabled
        + ", hitCount="
        + hitCount
        + ", suspendPolicy="
        + suspendPolicy
        + '}';
  }
}
