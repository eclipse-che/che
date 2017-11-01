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
package org.eclipse.che.ide.debug.model.impl;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;

/** @author Anatolii Bazko */
public class BreakpointImpl implements Breakpoint {
  private final Location location;
  private boolean enabled;
  private String condition;
  private boolean active;

  public BreakpointImpl(Breakpoint breakpoint) {}

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
  public String getCondition() {
    return condition;
  }

  @Override
  public void setCondition(String condition) {
    this.condition = condition;
  }
}
