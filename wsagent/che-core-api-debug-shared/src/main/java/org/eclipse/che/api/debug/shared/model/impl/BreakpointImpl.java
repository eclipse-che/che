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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Conditions;
import org.eclipse.che.api.debug.shared.model.Location;

/** @author Anatoliy Bazko */
public class BreakpointImpl implements Breakpoint {
  private final Location location;
  private Conditions conditions;
  private boolean enabled;

  public BreakpointImpl(Location location, Conditions conditions, boolean enabled) {
    this.location = location;
    this.conditions = conditions;
    this.enabled = enabled;
  }

  public BreakpointImpl(Location location) {
    this(location, new ConditionsImpl(null, 0), false);
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public Conditions getConditions() {
    return conditions;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BreakpointImpl)) return false;

    BreakpointImpl that = (BreakpointImpl) o;

    if (enabled != that.enabled) return false;
    if (location != null ? !location.equals(that.location) : that.location != null) return false;
    return !(conditions != null ? !conditions.equals(that.conditions) : that.conditions != null);
  }

  @Override
  public int hashCode() {
    int result = location != null ? location.hashCode() : 0;
    result = 31 * result + (enabled ? 1 : 0);
    result = 31 * result + (conditions != null ? conditions.hashCode() : 0);
    return result;
  }
}
