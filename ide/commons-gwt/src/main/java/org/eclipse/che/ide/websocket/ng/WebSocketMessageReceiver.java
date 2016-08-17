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
package org.eclipse.che.ide.websocket.ng;

/**
 * The implementation of this interface receives WEB SOCKET messages corresponding
 * to the registered protocol according to the defined mapping. The protocol must
 * be mapped via MapBinder in an ordinary Gin module, for example:
 *
 * <pre>
 *     <code>
 *         GinMapBinder<String, WebSocketMessageReceiver> receivers =
 *         GinMapBinder.newMapBinder(binder(), String.class, WebSocketMessageReceiver.class);
 *         receivers.addBinding("protocol-name").to(CustomWebSocketMessageReceiver.class);
 *     </code>
 * </pre>
 *
 * All WEB SOCKET transmissions with the protocol field equal to "protocol-name" will
 * be processed with the <code>CustomWebSocketMessageReceiver</code> instance.
 *
 * @author Dmitry Kuleshov
 */
public interface WebSocketMessageReceiver {
    void receive(String message);
}
