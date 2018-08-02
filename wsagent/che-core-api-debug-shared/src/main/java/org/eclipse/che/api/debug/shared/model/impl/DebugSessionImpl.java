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
package org.eclipse.che.api.debug.shared.model.impl;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebugSession;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;

/** @author Anatoliy Bazko */
public class DebugSessionImpl implements DebugSession {
  private final DebuggerInfo debuggerInfo;
  private final String id;
  private final String type;
  private final List<? extends Breakpoint> breakpoints;

  public DebugSessionImpl(
      DebuggerInfo debuggerInfo, String id, String type, List<? extends Breakpoint> breakpoints) {
    this.debuggerInfo = debuggerInfo;
    this.id = id;
    this.type = type;
    this.breakpoints = breakpoints;
  }

  @Override
  public DebuggerInfo getDebuggerInfo() {
    return debuggerInfo;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public List<? extends Breakpoint> getBreakpoints() {
    return breakpoints;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DebugSessionImpl)) return false;
    DebugSessionImpl that = (DebugSessionImpl) o;
    return Objects.equal(debuggerInfo, that.debuggerInfo)
        && Objects.equal(id, that.id)
        && Objects.equal(type, that.type)
        && Objects.equal(breakpoints, that.breakpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(debuggerInfo, id, type, breakpoints);
  }
}
