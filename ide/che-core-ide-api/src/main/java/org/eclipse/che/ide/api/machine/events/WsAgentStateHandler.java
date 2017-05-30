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

import com.google.gwt.event.shared.EventHandler;

/**
 * A handler for handling {@link WsAgentStateEvent}.
 *
 * @author Roman Nikitenko
 */
public interface WsAgentStateHandler extends EventHandler {

    /**
     * Called when ws-agent has been started.
     *
     * @param event
     *         the fired {@link WsAgentStateEvent}
     */
    void onWsAgentStarted(WsAgentStateEvent event);

    /**
     * Called when ws-agent has been stopped.
     *
     * @param event
     *         the fired {@link WsAgentStateEvent}
     */
    void onWsAgentStopped(WsAgentStateEvent event);
}
