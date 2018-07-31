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
package org.eclipse.che.api.debug.shared.model.impl;

import com.google.common.base.Objects;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.Location;

/** @author Anatoliy Bazko */
public class BreakpointImpl implements Breakpoint {
  private final Location location;
  private boolean enabled;
  private BreakpointConfiguration breakpointConfiguration;

  public BreakpointImpl(
      Location location, boolean enabled, BreakpointConfiguration breakpointConfiguration) {
    this.location = location;
    this.enabled = enabled;
    this.breakpointConfiguration =
        breakpointConfiguration == null
            ? new BreakpointConfigurationImpl()
            : breakpointConfiguration;
  }

  public BreakpointImpl(Location location) {
    this(location, true, new BreakpointConfigurationImpl());
  }

  public BreakpointImpl(Breakpoint breakpoint) {
    this(breakpoint.getLocation(), breakpoint.isEnabled(), breakpoint.getBreakpointConfiguration());
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public BreakpointConfiguration getBreakpointConfiguration() {
    return breakpointConfiguration;
  }

  public void setBreakpointConfiguration(BreakpointConfiguration breakpointConfiguration) {
    this.breakpointConfiguration = breakpointConfiguration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BreakpointImpl)) return false;
    BreakpointImpl that = (BreakpointImpl) o;
    return enabled == that.enabled
        && Objects.equal(location, that.location)
        && Objects.equal(breakpointConfiguration, that.breakpointConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(location, enabled, breakpointConfiguration);
  }

  @Override
  public String toString() {
    return "BreakpointImpl{"
        + "location="
        + location
        + ", enabled="
        + enabled
        + ", breakpointConfiguration="
        + breakpointConfiguration
        + '}';
  }
}
