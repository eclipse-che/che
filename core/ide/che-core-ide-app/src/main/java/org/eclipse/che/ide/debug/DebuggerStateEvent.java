/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.debug;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that describes the fact that debugger changed state.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerStateEvent extends GwtEvent<DebuggerStateEventHandler> {

    public static Type<DebuggerStateEventHandler> TYPE = new Type<>();

    private final DebuggerState state;
    private final Debugger      debugger;

    /**
     * Creates new {@link DebuggerStateEvent}.
     *
     * @param state
     *         debugger state
     * @param debugger
     *         the debugger
     */
    public DebuggerStateEvent(DebuggerState state, Debugger debugger) {
        this.state = state;
        this.debugger = debugger;
    }

    /**
     * Getter for {@link #debugger}
     */
    public Debugger getDebugger() {
        return debugger;
    }

    /**
     * Getter for {@link #state}
     */
    public DebuggerState getState() {
        return state;
    }

    /**
     * Factory method.
     */
    public static DebuggerStateEvent createInitializedStateEvent(Debugger debugger) {
        return new DebuggerStateEvent(DebuggerState.INITIALIZED, debugger);
    }

    /**
     * Factory method.
     */
    public static DebuggerStateEvent createConnectedStateEvent(Debugger debugger) {
        return new DebuggerStateEvent(DebuggerState.CONNECTED, debugger);
    }

    /**
     * Factory method.
     */
    public static DebuggerStateEvent createDisconnectedStateEvent(Debugger debugger) {
        return new DebuggerStateEvent(DebuggerState.DISCONNECTED, debugger);
    }

    public boolean isConnectedState() {
        return state == DebuggerState.CONNECTED;
    }

    public boolean isDisconnectedState() {
        return state == DebuggerState.DISCONNECTED;
    }

    public boolean isInitializedState() {
        return state == DebuggerState.INITIALIZED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type<DebuggerStateEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void dispatch(DebuggerStateEventHandler handler) {
        handler.onStateChanged(this);
    }

    public enum DebuggerState {
        INITIALIZED,
        DISCONNECTED,
        CONNECTED
    }
}
