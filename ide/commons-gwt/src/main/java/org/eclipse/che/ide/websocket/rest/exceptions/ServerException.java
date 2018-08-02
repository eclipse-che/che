/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.websocket.rest.exceptions;

import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Pair;

/**
 * Thrown when there was an any exception was received from the server over WebSocket.
 *
 * @author Artem Zatsarynnyi
 */
@SuppressWarnings("serial")
public class ServerException extends Exception {

  private Message response;

  private String message;

  private int errorCode;

  private Map<String, String> attributes;

  private boolean errorMessageProvided;

  public ServerException(Message response) {
    this.response = response;
    this.message = JsonHelper.parseJsonMessage(response.getBody());
    this.errorCode = JsonHelper.parseErrorCode(response.getBody());
    this.errorMessageProvided = checkErrorMessageProvided();
    this.attributes = JsonHelper.parseErrorAttributes(response.getBody());
  }

  @Override
  public String getMessage() {
    return message;
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

  public int getErrorCode() {
    return errorCode;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
}
