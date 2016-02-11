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
package org.eclipse.che.ide.commons.exception;

import org.eclipse.che.ide.rest.HTTPHeader;
import com.google.gwt.http.client.Response;

/**
 * @author Vitaliy Gulyy
 */

@SuppressWarnings("serial")
public class ServerException extends Exception {

    private Response response;

    private String message = "";

    private boolean errorMessageProvided;

    public ServerException(Response response) {
        this.response = response;
        this.errorMessageProvided = checkErrorMessageProvided();
        this.message = getMessageFromJSON(response.getText());
    }

    public ServerException(Response response, String message) {
        this.response = response;
        this.message = message;
    }

    public int getHTTPStatus() {
        return response.getStatusCode();
    }

    public String getStatusText() {
        return response.getStatusText();
    }

    @Override
    public String getMessage() {
        if (message != null) {
            return message;
        }

        if (response.getText().isEmpty())
            return response.getStatusText();
        else
            return response.getText();
    }

    @Override
    public String toString() {
        return getMessage();
    }

    private native String getMessageFromJSON(String json) /*-{
        try {
            return JSON.parse(json).message;
        } catch (e) {
            return null;
        }
    }-*/;

    public String getHeader(String key) {
        return response.getHeader(key);
    }

    private boolean checkErrorMessageProvided() {
        String value = response.getHeader(HTTPHeader.JAXRS_BODY_PROVIDED);
        if (value != null) {
            return true;
        }

        return false;
    }

    public boolean isErrorMessageProvided() {
        return errorMessageProvided;
    }
}
