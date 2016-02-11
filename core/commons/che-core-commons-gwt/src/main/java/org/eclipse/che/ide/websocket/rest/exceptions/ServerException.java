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
package org.eclipse.che.ide.websocket.rest.exceptions;

import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Pair;

import java.util.List;

/**
 * Thrown when there was an any exception was received from the server over WebSocket.
 *
 * @author Artem Zatsarynnyi
 */
@SuppressWarnings("serial")
public class ServerException extends Exception {
    private Message response;

    private boolean errorMessageProvided;

    public ServerException(Message response) {
        this.response = response;
        this.errorMessageProvided = checkErrorMessageProvided();
    }

    @Override
    public String getMessage() {
        if (response.getBody() == null || response.getBody().isEmpty())
            return null;
        return response.getBody();
    }

    public int getHTTPStatus() {
        return response.getResponseCode();
    }

    public String getHeader(String key) {
        List<Pair> headers = response.getHeaders().toList();
        if (headers != null) {
            for (Pair header : headers) {
                if (key.equals(header.getName())) {
                    return header.getValue();
                }
            }
        }

        return null;
    }

    private boolean checkErrorMessageProvided() {
        String value = getHeader(HTTPHeader.JAXRS_BODY_PROVIDED);
        if (value != null) {
            return true;
        }
        return false;
    }

    public boolean isErrorMessageProvided() {
        return errorMessageProvided;
    }
}
