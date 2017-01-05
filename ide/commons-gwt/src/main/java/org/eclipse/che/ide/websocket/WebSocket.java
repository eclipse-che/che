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
import org.eclipse.che.ide.websocket.events.MessageReceivedHandler;
import com.google.gwt.core.client.JavaScriptObject;


/**
 * Class that wraps native JavaScript WebSocket object.
 *
 * @author Artem Zatsarynnyi
 */
public class WebSocket extends JavaScriptObject {
    protected WebSocket() {
    }

    /**
     * Creates a new WebSocket instance.
     * WebSocket attempt to connect to their URL immediately upon creation.
     *
     * @param url
     *         WebSocket server URL
     * @return the created {@link WebSocket} object
     */
    public static native WebSocket create(String url) /*-{
       return new WebSocket(url);
    }-*/;

    /**
     * Creates a WebSocket object.
     * WebSocket attempt to connect to their URL immediately upon creation.
     *
     * @param url
     *         WebSocket server URL
     * @param protocol
     *         subprotocol name
     * @return the created {@link WebSocket} object
     */
    public static native WebSocket create(String url, String protocol) /*-{
       return new WebSocket(url, protocol);
    }-*/;

    /**
     * Closes the WebSocket connection. If the connection state
     * is already {@link MessageBus.ReadyState#CLOSED}, this method does nothing.
     */
    public final native void close() /*-{
       this.close();
    }-*/;

    /**
     * Method can be used to detect WebSocket support in the current browser.
     *
     * @return <code>true</code>  if WebSockets are supported;
     *         <code>false</code> if they are not.
     */
    public static native boolean isSupported() /*-{
       return !!window.WebSocket;
    }-*/;

    /**
     * Returns the state of the WebSocket connection.
     *
     * @return ready-state value
     */
    public final native short getReadyState() /*-{
       return this.readyState;
    }-*/;

    /**
     * Represents the number of bytes of UTF-8 text
     * that have been queued using send() method.
     *
     * @return the number of queued bytes
     */
    public final native int getBufferedAmount() /*-{
       return this.bufferedAmount;
    }-*/;

    /**
     * Transmits data to the server over the WebSocket connection.
     *
     * @param data
     *         the data to be sent to the server
     */
    public final native void send(String data) /*-{
       this.send(data);
    }-*/;

    /**
     * Sets the {@link org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler} to be notified when the WebSocket connection established.
     *
     * @param handler
     *         WebSocket open handler
     */
    public final native void setOnOpenHandler(ConnectionOpenedHandler handler) /*-{
       this.onopen = function () {
           handler.@org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler::onOpen()();
       };
    }-*/;

    /**
     * Sets the {@link ConnectionClosedHandler} to be notified when the WebSocket close.
     *
     * @param handler
     *         WebSocket close handler
     */
    public final native void setOnCloseHandler(ConnectionClosedHandler handler) /*-{
       this.onclose = function (event) {
           var webSocketClosedEventInstance = @org.eclipse.che.ide.websocket.events.WebSocketClosedEvent::new(ILjava/lang/String;Z)(event
               .code, event.reason, event.wasClean);
           handler.@org.eclipse.che.ide.websocket.events.ConnectionClosedHandler::onClose(Lorg/eclipse/che/ide/websocket/events/WebSocketClosedEvent;)(webSocketClosedEventInstance);
       };
    }-*/;

    /**
     * Sets the {@link org.eclipse.che.ide.websocket.events.ConnectionErrorHandler} to be notified when there is any error in communication.
     *
     * @param handler
     *         WebSocket error handler
     */
    public final native void setOnErrorHandler(ConnectionErrorHandler handler) /*-{
       this.onerror = function (event) {
           handler.@org.eclipse.che.ide.websocket.events.ConnectionErrorHandler::onError()();
       };
    }-*/;

    /**
     * Sets the {@link org.eclipse.che.ide.websocket.events.MessageReceivedHandler} to be notified when
     * client receives data from the WebSocket server.
     *
     * @param handler
     *         WebSocket message handler
     */
    public final native void setOnMessageHandler(MessageReceivedHandler handler) /*-{
       this.onmessage = function (event) {
           if (event.data instanceof Blob) {
               var reader = new FileReader();
               reader.onloadend = function() {
                   var e = @org.eclipse.che.ide.websocket.events.MessageReceivedEvent::new(Ljava/lang/String;)(reader.result);
                   handler.@org.eclipse.che.ide.websocket.events.MessageReceivedHandler::onMessageReceived(Lorg/eclipse/che/ide/websocket/events/MessageReceivedEvent;)(e);

               };

               //reader.readAsBinaryString(event.data);
               reader.readAsText(event.data);
           } else {
               var webSocketMessageEventInstance = @org.eclipse.che.ide.websocket.events.MessageReceivedEvent::new(Ljava/lang/String;)(event.data);
               handler.@org.eclipse.che.ide.websocket.events.MessageReceivedHandler::onMessageReceived(Lorg/eclipse/che/ide/websocket/events/MessageReceivedEvent;)(webSocketMessageEventInstance);
           }
       };
    }-*/;

}
