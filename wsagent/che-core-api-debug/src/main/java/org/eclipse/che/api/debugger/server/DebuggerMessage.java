/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debugger.server;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;

/**
 * A wrapper over {@link DebuggerEvent} to send data over {@link EventService}.
 * Contains type as identifier of the target debugger.
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
