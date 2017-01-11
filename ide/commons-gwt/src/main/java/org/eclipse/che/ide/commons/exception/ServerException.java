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
package org.eclipse.che.ide.commons.exception;

import org.eclipse.che.ide.rest.HTTPHeader;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitaliy Gulyy
 */

@SuppressWarnings("serial")
public class ServerException extends Exception {

    private Response response;

    private String message = "";

    private int errorCode;

    private Map<String, String> attributes = new HashMap<>();

    private boolean errorMessageProvided;

    public ServerException(Response response) {
        this.response = response;
        this.errorMessageProvided = checkErrorMessageProvided();
        this.message = getMessageFromJSON(response.getText());
        this.errorCode = getErrorCodeFromJSON(response.getText());
//        parseJsonAttributes(response.getText());
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

    public int getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getAttributes() {
        return attributes;
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


    private native int getErrorCodeFromJSON(String json) /*-{
        try {
            var result = JSON.parse(json).errorCode;
            if (result) {
                return result;
            }
        } catch (e) {
        }
        return -1;
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

//    private native void parseJsonAttributes(String json) /*-{
//        try {
//            var attributes = JSON.parse(json).attributes;
//            for(var key in attributes) {
//                this.@org.eclipse.che.ide.commons.exception.ServerException.attributes::put(Ljava/lang/String;Ljava/lang/String;)(key, attributes[key]);
//            }
//
//        } catch (e) {
//            console.log(e.message, e);
//        }
//    }-*/;

    public boolean isErrorMessageProvided() {
        return errorMessageProvided;
    }
}
