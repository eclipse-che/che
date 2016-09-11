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

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Responsible for keeping connection alive and reconnecting if needed.
 * If connection is closed and sustainer is active it tries to reconnect
 * according to its properties:
 *
 * <ul>
 * <li>reconnection delay - 500 milliseconds</li>
 * <li>reconnection limit - 5 attempts</li>
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketConnectionSustainer {
    private static final int RECONNECTION_DELAY = 500;
    private static final int RECONNECTION_LIMIT = 5;

    private final WebSocketConnection connection;

    private boolean active;
    private int     attempt;

    @Inject
    public WebSocketConnectionSustainer(WebSocketConnection connection) {
        this.connection = connection;
    }

    public void reset() {
        Log.debug(getClass(), "Resetting number of reconnection attempt number. Previous was: " + attempt);
        attempt = 0;
    }

    public void sustain() {
        if (++attempt > RECONNECTION_LIMIT) {
            Log.debug(getClass(), "Exceeding reconnection limit.");
            disable();
        }

        if (active) {
            Log.debug(getClass(), "Sustaining connection. Current attempt number: " + attempt);
            connection.open(RECONNECTION_DELAY);
        }
    }

    public void enable() {
        if (!active) {
            Log.debug(getClass(), "Sustainer enabled.");
            active = true;
        }
    }

    public void disable() {
        if (active) {
            Log.debug(getClass(), "Sustainer disabled");
            active = false;
        }
    }
}
