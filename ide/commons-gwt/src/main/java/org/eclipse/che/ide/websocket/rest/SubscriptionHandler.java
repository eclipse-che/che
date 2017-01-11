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
package org.eclipse.che.ide.websocket.rest;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.exceptions.ServerException;

import java.util.List;

/**
 * Handler to receive messages by subscription.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class SubscriptionHandler<T> implements MessageHandler {
    /** Deserializer for the body of the {@link Message}. */
    private final Unmarshallable<T> unmarshaller;

    /** An object deserialized from the response. */
    private T payload;

    public SubscriptionHandler() {
        this(null);
    }

    /**
     * Constructor retrieves unmarshaller with initialized (this is important!) object.
     * When response comes callback calls <code>Unmarshallable.unmarshal()</code>
     * which populates the object.
     *
     * @param unmarshaller
     *         {@link Unmarshallable}
     */
    public SubscriptionHandler(Unmarshallable<T> unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    /**
     * Perform actions when {@link Message} was received.
     *
     * @param message
     *         received {@link Message}
     */
    public void onMessage(Message message) {

        if (isSuccessful(message)) {
            try {
                if (unmarshaller != null) {
                    unmarshaller.unmarshal(message);
                    payload = unmarshaller.getPayload();
                }
                onMessageReceived(payload);
            } catch (UnmarshallerException e) {
                onErrorReceived(e);
            }
        } else {
            onErrorReceived(new ServerException(message));
        }
    }

    @Override
    public void onMessage(String message) {
    }

    /**
     * Is message successful?
     *
     * @param message
     *         {@link Message}
     * @return <code>true</code> if message is successful and <code>false</code> if not
     */
    protected final boolean isSuccessful(Message message) {
        List<Pair> headers = message.getHeaders().toList();
        for (Pair header : headers) {
            if ("x-everrest-websocket-message-type".equals(header.getName()) && "none".equals(header.getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Invokes if response is successfully received and response status code is in set of success codes.
     *
     * @param result
     */
    protected abstract void onMessageReceived(T result);

    /**
     * Invokes if an error received from the server.
     *
     * @param exception
     *         caused failure
     */
    protected abstract void onErrorReceived(Throwable exception);
}