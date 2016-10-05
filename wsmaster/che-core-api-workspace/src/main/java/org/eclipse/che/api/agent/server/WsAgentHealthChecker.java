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
package org.eclipse.che.api.agent.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.workspace.shared.dto.WsAgentHealthStateDto;

/**
 * Describes a mechanism for checking ws agent's state.
 * It needs when Workspace Agent (WS Agent) stops to respond and projects disappear from the project tree,
 * and the page shows 'Cannot get project types' error.
 * It may happens for example, when OOM happens in a WS Agent and kernel kills WS Agent process.
 * Problem here that we can't detect properly OOM error but we can check if WS Agent is alive for user.
 * <p>
 * If client (IDE) lost WebSocket connection to the WS Agent - in this case IDE will request some other service in our infrastructure to
 * check WS Agent state, here we have two ways:
 * <p>
 * 1/ WS Agent was shutdown by OS. If it not available for this service too, a user should be notified that the workspace is broken
 * probably because of OOM (it will be just suggest because we not sure about reason).
 * <p>
 * 2/ WS Agent is working well and is accessible for our infrastructure, in this case user has networking problem. It can be not
 * well configured proxy server or other problems which are not related to our responsibility.
 *
 * @author Vitalii Parfonov
 */
public interface WsAgentHealthChecker {

    /**
     * Verifies if ws agent is alive.
     *
     * @param machine
     *         machine instance
     * @return state of the ws agent
     * @throws ServerException
     *         if internal server error occurred
     */
    WsAgentHealthStateDto check(Machine machine) throws ServerException;
}
