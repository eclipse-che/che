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

/**
 * Abstract implementation of {@link WsAgentStateHandler}.
 *
 * Need to allow user to use only one event handling if need.
 *
 * @author Vlad Zhukovskyi
 */
public abstract class AbstractWsAgentStateHandler implements WsAgentStateHandler {
    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
       //to be override
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        //to be override
    }
}
