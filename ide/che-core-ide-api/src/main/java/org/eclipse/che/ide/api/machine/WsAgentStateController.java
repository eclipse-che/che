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
package org.eclipse.che.ide.api.machine;

import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.websocket.MessageBus;

@Singleton
@Deprecated
public interface WsAgentStateController {

    @Deprecated
    void initialize(MachineImpl devMachine);

    /** @deprecated use {@link ServerImpl#getStatus()} */
    @Deprecated
    WsAgentState getState();

    /** Use JSON-RPC communication instead. */
    @Deprecated
    Promise<MessageBus> getMessageBus();
}
