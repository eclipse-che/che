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

import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.collections.js.JsoArray;
import org.eclipse.che.ide.websocket.rest.Pair;

/** @author Artem Zatsarynnyi */
public class Message extends Jso {

    protected Message() {
    }

    public static Message create() {
        return Jso.create().cast();
    }

    /**
     * Get message body.
     *
     * @return message body
     */
    public final String getBody() {
        return getStringField("body");
    }

    /**
     * Set message body.
     *
     * @param body
     *         message body
     */
    public final void setBody(String body) {
        addField("body", body);
    }

    /**
     * Get name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
     *
     * @return name of HTTP method
     */
    public final String getMethod() {
        return getStringField("method");
    }

    /**
     * Set name of HTTP method specified for resource method, e.g. GET, POST, PUT, etc.
     *
     * @param method
     *         name of HTTP method
     */
    public final void setMethod(String method) {
        addField("method", method);
    }

    /**
     * Get resource path.
     *
     * @return resource path
     */
    public final String getPath() {
        return getStringField("path");
    }

    /**
     * Set resource path.
     *
     * @param path
     *         resource path
     */
    public final void setPath(String path) {
        addField("path", path);
    }

    /**
     * Get HTTP headers.
     *
     * @return HTTP headers
     */
    public final JsoArray<Pair> getHeaders() {
        return getArrayField("headers").cast();
    }

    /**
     * Set HTTP headers.
     *
     * @param headers
     *         HTTP headers
     */
    public final void setHeaders(JsoArray<Pair> headers) {
        addField("headers", headers);
    }

    /**
     * Get response code.
     *
     * @return response code
     */
    public final int getResponseCode() {
        return getIntField("responseCode");
    }

    /**
     * Get response code.
     *
     * @param responseCode
     *         response code
     */
    public final void setResponseCode(int responseCode) {
        addField("responseCode", responseCode);
    }
}