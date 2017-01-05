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
package org.eclipse.che.ide.api.machine.events;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.ide.api.machine.WsAgentState;

/**
 * Event that describes the fact that ws-agent state has been changed.
 *
 * @author Roman Nikitenko
 */
public class WsAgentStateEvent extends GwtEvent<WsAgentStateHandler> {

    /** Type class used to register this event. */
    public static Type<WsAgentStateHandler> TYPE = new Type<>();
    private final WsAgentState wsAgentState;

    /**
     * Create new {@link WsAgentStateEvent}.
     *
     * @param state
     *         the type of action
     */
    protected WsAgentStateEvent(WsAgentState state) {
        this.wsAgentState = state;
    }

    /**
     * Creates a ws-agent started event.
     */
    public static WsAgentStateEvent createWsAgentStartedEvent() {
        return new WsAgentStateEvent(WsAgentState.STARTED);
    }

    /**
     * Creates a ws-agent stopped event.
     */
    public static WsAgentStateEvent createWsAgentStoppedEvent() {
        return new WsAgentStateEvent(WsAgentState.STOPPED);
    }

    @Override
    public Type<WsAgentStateHandler> getAssociatedType() {
        return TYPE;
    }

    /** @return the state of ws-agent */
    public WsAgentState getWsAgentState() {
        return wsAgentState;
    }

    @Override
    protected void dispatch(WsAgentStateHandler handler) {
        switch (wsAgentState) {
            case STARTED:
                handler.onWsAgentStarted(this);
                break;
            case STOPPED:
                handler.onWsAgentStopped(this);
                break;
            default:
                break;
        }
    }
}
