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
package org.eclipse.che.ide.machine;

import org.eclipse.che.ide.api.machine.DevMachine;

/**
 * Initialize client's workspace agent component with specific developer machine. Usually, this interface
 * is not intended to be implemented by the third-party components.
 *
 * @author Vlad Zhukovskyi
 * @see DevMachine
 * @since 5.0.0
 */
public interface WsAgentInitializer {

    /**
     * Perform initialization of the client's workspace agent component.
     *
     * @param devMachine
     *         booted up developer machine
     * @param callback
     *         the callback which indicates that workspace agent has been initialized
     * @throws NullPointerException
     *         in case if {@code devMachine} is null. Reason includes:
     *         <ul>
     *         <li>Developer machine should not be a null</li>
     *         </ul>
     */
    void initialize(DevMachine devMachine, WsAgentCallback callback);

    interface WsAgentCallback {
        void onWsAgentInitialized();
    }
}
