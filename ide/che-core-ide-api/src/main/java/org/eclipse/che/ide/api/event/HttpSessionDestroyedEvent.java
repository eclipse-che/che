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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Fired when websocket closed with message from server that current http session was destroyed.
 *
 * @author Evgen Vidolob
 */
public class HttpSessionDestroyedEvent extends GwtEvent<HttpSessionDestroyedHandler> {
    public static Type<HttpSessionDestroyedHandler> TYPE = new Type<HttpSessionDestroyedHandler>();

    @Override
    public Type<HttpSessionDestroyedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HttpSessionDestroyedHandler handler) {
        handler.onHttpSessionDestroyed(this);
    }
}
