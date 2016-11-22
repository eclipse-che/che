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

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.eclipse.che.ide.websocket.ng.impl.WebSocketPropertyManager.Properties.ATTEMPTS;
import static org.eclipse.che.ide.websocket.ng.impl.WebSocketPropertyManager.Properties.DELAY;
import static org.eclipse.che.ide.websocket.ng.impl.WebSocketPropertyManager.Properties.SUSTAINER_ENABLED;
import static org.eclipse.che.ide.websocket.ng.impl.WebSocketPropertyManager.Properties.URL;

/**
 * Singleton implementation of properties manager to keep all web socket
 * connections properties in a one handy place. Basically is a wrapper with
 * some additional functionality around a map. Each entry is represented by a
 * key - web socket connection url and a value - a map of properties related
 * to a connection.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class WebSocketPropertyManager {
    /**
     * Map to store properties.
     * Key - web socket url identifying a connection
     * Value - map of connection properties
     */
    private final Map<String, Map<String, String>> properties = new HashMap<>();

    /**
     * Initialized default properties for a connection. The implementation of
     * web socket infrastructure is implemented in such way that this method
     * must be necessarily called before any interactions with a connection.
     * Default values are:
     * <ul>
     * <li>URL - url of this connection</li>
     * <li>Reconnection delay: 0</li>
     * <li>Reconnection attempts: 0</li>
     * <li>Sustainer status: enabled</li>
     * </ul>
     *
     * @param url
     *         identifier of a web socket connection
     */
    public void initializeConnection(String url) {
        final HashMap<String, String> properties = new HashMap<>();
        properties.put(URL, url);
        properties.put(DELAY, "0");
        properties.put(ATTEMPTS, "0");
        properties.put(SUSTAINER_ENABLED, TRUE.toString());

        this.properties.put(url, properties);
    }

    /**
     * Sets a string value of a property
     *
     * @param url
     *         identifier that corresponds to a connection
     * @param key
     *         key
     * @param value
     *         value
     */
    public void setProperty(String url, String key, String value) {
        getPropertiesMap(url).put(key, value);
    }

    /**
     * Gets a value of a property
     *
     * @param url
     *         identifier that corresponds to a connection
     * @param key
     *         key
     *
     * @return value of a propery
     */
    public String getProperty(String url, String key) {
        return getPropertiesMap(url).get(key);
    }

    public String getUrl(String url) {
        return getProperty(url, URL);
    }

    public void setReConnectionAttempts(String url, int attempts) {
        setProperty(url, ATTEMPTS, Integer.toString(attempts));
    }

    public int getReConnectionAttempts(String url) {
        return Integer.valueOf(getProperty(url, ATTEMPTS));
    }

    public void setConnectionDelay(String url, int delay) {
        setProperty(url, DELAY, Integer.toString(delay));
    }

    public int getConnectionDelay(String url) {
        return Integer.valueOf(getProperty(url, DELAY));
    }

    public void enableSustainer(String url) {
        setProperty(url, SUSTAINER_ENABLED, TRUE.toString());
    }

    public void disableSustainer(String url) {
        setProperty(url, SUSTAINER_ENABLED, FALSE.toString());
    }

    public boolean sustainerEnabled(String url) {
        return Boolean.valueOf(getProperty(url, SUSTAINER_ENABLED));
    }

    private Map<String, String> getPropertiesMap(String url) {
        if (properties.containsKey(url)) {
            return properties.get(url);
        }

        final String error = "Connection is not properly initialized, no properties set. Call 'initializedConnection' first";
        Log.error(getClass(), error);
        throw new IllegalStateException(error);
    }

    interface Properties {

        /**
         * Defines a delay in milliseconds for connection/reconnection
         */
        String DELAY             = "delay";
        /**
         * Defines current number of reconnection attempts
         */
        String ATTEMPTS          = "attempts";
        /**
         * Url of the connection
         */
        String URL               = "url";
        /**
         * Current connection sustainer status
         */
        String SUSTAINER_ENABLED = "sustainer-status";
    }
}
