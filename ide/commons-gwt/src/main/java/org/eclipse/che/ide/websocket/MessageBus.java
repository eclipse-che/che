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

import org.eclipse.che.ide.websocket.events.ConnectionClosedHandler;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.events.MessageReceivedHandler;
import org.eclipse.che.ide.websocket.events.ReplyHandler;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

/**
 * WebSocket message bus, that provides two asynchronous messaging patterns: RPC and list-based PubSub.
 *
 * @author Artem Zatsarynnyi
 */
public interface MessageBus extends MessageReceivedHandler {
    /** This enumeration used to describe the ready state of the WebSocket connection. */
    public static enum ReadyState {

        /** The WebSocket object is created but connection has not yet been established. */
        CONNECTING(0),

        /**
         * Connection is established and communication is possible. A WebSocket must
         * be in the open state in order to send and receive data over the network.
         */
        OPEN(1),

        /** Connection is going through the closing handshake. */
        CLOSING(2),

        /** The connection has been closed or could not be opened. */
        CLOSED(3);

        private final int value;

        private ReadyState(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Return the ready state of the WebSocket connection.
     *
     * @return ready state of the WebSocket
     */
    ReadyState getReadyState();

    /**
     * Send {@link Message} on Rest.
     *
     * @param message
     *         {@link Message} to send
     * @param callback
     *         callback for receiving reply to message. May be <code>null</code>.
     * @throws WebSocketException
     *         throws if an any error has occurred while sending data
     */
    void send(Message message, RequestCallback callback) throws WebSocketException;

    /**
     * Sends a message to an address.
     *
     * @param address
     *         the address of receiver
     * @param message
     *         the message
     * @throws WebSocketException
     *         throws if an any error has occurred while sending data
     */
    void send(String address, String message) throws WebSocketException;

    /**
     * Sends a message to an address on Websocket, providing an replyHandler.
     *
     * @param address
     *         the address of receiver
     * @param message
     *         the message
     * @param replyHandler
     *         the handler callback
     * @throws WebSocketException
     *         throws if an any error has occurred while sending data
     */
    void send(String address, String message, ReplyHandler replyHandler) throws WebSocketException;

    /**
     * Sets the {@link ConnectionOpenedHandler} to be notified when the {@link MessageBus} opened.
     *
     * @param handler
     *         {@link ConnectionOpenedHandler}
     */
    void addOnOpenHandler(ConnectionOpenedHandler handler);


    /**
     * Removes the given handler.
     *
     * @param handler
     *         {@link ConnectionOpenedHandler}
     */
    void removeOnOpenHandler(ConnectionOpenedHandler handler);

    /**
     * Sets the {@link ConnectionClosedHandler} to be notified when the {@link MessageBus} closed.
     *
     * @param handler
     *         {@link ConnectionClosedHandler}
     */
    void addOnCloseHandler(ConnectionClosedHandler handler);

    /**
     * Removes the given handler.
     *
     * @param handler
     *         {@link ConnectionClosedHandler}
     */
    void removeOnCloseHandler(ConnectionClosedHandler handler);

    /**
     * Sets the {@link ConnectionErrorHandler} to be notified when there is any error in communication over WebSocket.
     *
     * @param handler
     *         {@link ConnectionErrorHandler}
     */
    void addOnErrorHandler(ConnectionErrorHandler handler);

    /**
     * Subscribes a new {@link MessageHandler} which will listener for messages sent to the specified channel.
     * Upon the first subscribe to a channel, a message is sent to the server to
     * subscribe the client for that channel. Subsequent subscribes for a channel
     * already previously subscribed to do not trigger a send of another message
     * to the server because the client has already a subscription, and merely registers
     * (client side) the additional handler to be fired for events received on the respective channel.
     *
     * @param channel
     *         channel identifier
     * @param handler
     *         {@link MessageHandler} to subscribe
     * @throws WebSocketException
     *         throws if an any error has occurred while subscribing
     */
    void subscribe(String channel, MessageHandler handler) throws WebSocketException;

    /**
     * Unsubscribes a previously subscribed handler listening on the specified channel.
     * If it's the last unsubscribe to a channel, a message is sent to the server to
     * unsubscribe the client for that channel.
     *
     * @param channel
     *         channel identifier
     * @param handler
     *         {@link MessageHandler} to unsubscribe
     * @throws WebSocketException
     *         throws if an any error has occurred while unsubscribing
     * @throws IllegalArgumentException
     *         throws if provided handler not subscribed to any channel
     */
    void unsubscribe(String channel, MessageHandler handler) throws WebSocketException;

    /**
     * Unsubscribes a previously subscribed handler listening on the specified channel ignoring errors.
     * If it's the last unsubscribe to a channel, a message is sent to the server to
     * unsubscribe the client for that channel.
     *
     * @param channel
     *         channel identifier
     * @param handler
     *         {@link MessageHandler} to unsubscribe
     */
    void unsubscribeSilently(String channel, MessageHandler handler);

    /**
     * Check if a provided handler is subscribed to a provided channel or not.
     *
     * @param handler
     *         {@link MessageHandler} to check
     * @param channel
     *         channel to check
     * @return <code>true</code> if handler subscribed to channel and <code>false</code> if not
     */
    boolean isHandlerSubscribed(MessageHandler handler, String channel);

    /**
     * Cancels attempts to reconnect by WebSocket
     */
    void cancelReconnection();
}
