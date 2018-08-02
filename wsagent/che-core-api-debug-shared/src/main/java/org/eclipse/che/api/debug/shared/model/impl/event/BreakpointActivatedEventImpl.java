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
package org.eclipse.che.api.debug.shared.model.impl.event;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

/**
 * Event will be generated when breakpoint becomes active.
 *
 * @author Anatoliy Bazko
 */
public class BreakpointActivatedEventImpl extends DebuggerEventImpl
    implements BreakpointActivatedEvent {
  private final Breakpoint breakpoint;

  public BreakpointActivatedEventImpl(Breakpoint breakpoint) {
    super(DebuggerEvent.TYPE.BREAKPOINT_ACTIVATED);
    this.breakpoint = breakpoint;
  }

  @Override
  public Breakpoint getBreakpoint() {
    return breakpoint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BreakpointActivatedEventImpl)) return false;
    if (!super.equals(o)) return false;

    BreakpointActivatedEventImpl that = (BreakpointActivatedEventImpl) o;

    return !(breakpoint != null ? !breakpoint.equals(that.breakpoint) : that.breakpoint != null);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (breakpoint != null ? breakpoint.hashCode() : 0);
    return result;
  }
}
