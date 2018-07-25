/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.commons.exception;

import com.google.gwt.http.client.Response;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.rest.HTTPHeader;

/** @author Vitaliy Gulyy */
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
    this.parseJsonAttributes(response.getText());
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

    if (response.getText().isEmpty()) return response.getStatusText();
    else return response.getText();
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

  private native void parseJsonAttributes(String json) /*-{
          try {
              var attributes = JSON.parse(json).attributes;
              var exceptionAttributes = this.@org.eclipse.che.ide.commons.exception.ServerException::attributes;
              for(var key in attributes) {
                  exceptionAttributes.@java.util.Map::put(*)(key, attributes[key]);
              }
          } catch (e) {
              console.log(e.message, e);
          }
      }-*/;

  public boolean isErrorMessageProvided() {
    return errorMessageProvided;
  }
}
