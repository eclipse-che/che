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
package org.eclipse.che.api.debugger.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

/**
 * A wrapper over {@link DebuggerEvent} to send data over {@link EventService}. Contains type as
 * identifier of the target debugger.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerMessage {
  private final DebuggerEvent debuggerEvent;
  private final String debuggerType;

  public DebuggerMessage(DebuggerEvent debuggerEvent, String debuggerType) {
    this.debuggerEvent = debuggerEvent;
    this.debuggerType = debuggerType;
  }

  public DebuggerEvent getDebuggerEvent() {
    return debuggerEvent;
  }

  public String getDebuggerType() {
    return debuggerType;
  }
}
