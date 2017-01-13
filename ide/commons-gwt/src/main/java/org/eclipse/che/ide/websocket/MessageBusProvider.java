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
package org.eclipse.che.ide.websocket;

import com.google.inject.Singleton;

/**
 * Class contains business logic which allows create message bus with necessary parameters or get created message bus.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class MessageBusProvider {

    private MessageBus messageBus;
    private MessageBus machineMessageBus;

    /** Returns instance of {@link MessageBusImpl}. Method can return null value when message bus hasn't created yet. */
    public MessageBus getMessageBus() {
        return messageBus;
    }

    /** Returns instance of {@link MachineMessageBus}. Method can return null value when message bus hasn't created yet. */
    public MessageBus getMachineMessageBus() {
        return machineMessageBus;
    }

    /**
     * Creates new instance of  {@link MessageBusImpl} and connects to web socket via special url. The method returns new
     * instance each time it is called. Message bus is created only one time when we start workspace.
     *
     * @return instance of {@link MessageBusImpl}
     */
    public MessageBus createMessageBus() {
        this.messageBus = new MessageBusImpl();

        return messageBus;
    }

    /**
     * Creates new instance of  {@link MachineMessageBus} and connects to web socket via special url. The method returns new
     * instance each time it is called. Message bus is created only one time when we start machine.
     *
     * @param webSocketUrl
     *         url which need to connect to web socket
     * @return instance of {@link MachineMessageBus}
     */
    public MessageBus createMachineMessageBus(String webSocketUrl) {
        this.machineMessageBus = new MachineMessageBus(webSocketUrl);

        return machineMessageBus;
    }
}
