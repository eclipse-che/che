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
package com.google.gwt.webworker.client.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for routing JsonMessages based on the message type that get
 * sent between worker and 'main' thread.
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class MessageFilter {
    private final Map<Integer, MessageRecipient<? extends Message>> messageRecipients = new HashMap<>();

    /**
     * Dispatches an incoming DTO message to a registered recipient.
     *
     * @param message
     */
    public <T extends Message> void dispatchMessage(T message) {
        @SuppressWarnings("unchecked")
        MessageRecipient<T> recipient = (MessageRecipient<T>)messageRecipients.get(message.getType());
        if (recipient != null) {
            recipient.onMessageReceived(message);
        }
    }

    /**
     * Adds a MessageRecipient for a given message type.
     *
     * @param messageType
     * @param recipient
     */
    public void registerMessageRecipient(
            int messageType, MessageRecipient<? extends Message> recipient) {
        messageRecipients.put(messageType, recipient);
    }

    /**
     * Removes any MessageRecipient registered for a given type.
     *
     * @param messageType
     */
    public void removeMessageRecipient(int messageType) {
        messageRecipients.remove(messageType);
    }

    /** Interface for receiving JSON messages. */
    public interface MessageRecipient<T extends Message> {
        void onMessageReceived(T message);
    }
}
