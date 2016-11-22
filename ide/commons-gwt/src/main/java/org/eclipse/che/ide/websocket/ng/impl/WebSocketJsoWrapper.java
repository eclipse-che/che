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
package org.eclipse.che.ide.websocket.ng.impl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Very simple native javascript websocket wrapper
 *
 * @author Dmitry Kuleshov
 */
public class WebSocketJsoWrapper extends JavaScriptObject {
    protected WebSocketJsoWrapper() {
    }

    public static native WebSocketJsoWrapper connect(String url, WebSocketEndpoint endpoint) /*-{
        var webSocket = new WebSocket(url);
        webSocket.onopen = function () {
            endpoint.@org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint::onOpen(Ljava/lang/String;)(url);
        };

        webSocket.onclose = function () {
            endpoint.@org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint::onClose(Ljava/lang/String;)(url);
        };

        webSocket.onerror = function () {
            endpoint.@org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint::onError(Ljava/lang/String;)(url);
        };

        webSocket.onmessage = function (event) {
            endpoint.@org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint::onMessage(Ljava/lang/String;Ljava/lang/String;)(url, event.data);
        };
        return webSocket;
    }-*/;

    public final native void close() /*-{
        this.close();
    }-*/;

    public final native boolean isClosed() /*-{
        return this.readyState == this.CLOSED;
    }-*/;

    public final native boolean isClosing() /*-{
        return this.readyState == this.CLOSING;
    }-*/;

    public final native boolean isOpen() /*-{
        return this.readyState == this.OPEN;
    }-*/;

    public final native boolean isConnecting() /*-{
        return this.readyState == this.CONNECTING;
    }-*/;

    public final native void send(final String data) /*-{
        this.send(data);
    }-*/;
}
