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
package org.eclipse.che.ide.socketio;

import com.google.gwt.core.client.JavaScriptObject;

public class SocketIOOverlay extends JavaScriptObject {

    protected SocketIOOverlay() {}
    public final native SocketOverlay connect(String url) /*-{
        return this.connect(url, {
                   'reconnect': true,
                   'reconnection delay': 500,
                   'max reconnection attempts': 10
        });
    }-*/;

}
