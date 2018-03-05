/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.webdriver.log;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.webdriver.log.WebDriverLogsReader.LOG_TIME_FORMAT;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class deals with web driver log entry.
 *
 * @author Dmytro Nochevnov
 */
public class Log {
  private Message message;
  private long timestamp;

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  boolean isNetworkLog() {
    return isNetworkRequest()
        || isNetworkResponse()
        || isWebsocketCreated()
        || isWebsocketRequest()
        || isWebsocketResponse();
  }

  private boolean isNetworkRequest() {
    return "Network.requestWillBeSent".equals(getMessage().getMethod());
  }

  private boolean isNetworkResponse() {
    return "Network.responseReceived".equals(getMessage().getMethod());
  }

  private boolean isWebsocketCreated() {
    return "Network.webSocketCreated".equals(getMessage().getMethod());
  }

  private boolean isWebsocketRequest() {
    return "Network.webSocketFrameSent".equals(getMessage().getMethod());
  }

  private boolean isWebsocketResponse() {
    return "Network.webSocketFrameReceived".equals(getMessage().getMethod());
  }

  public String getUrl() {
    if (hasRequest()) {
      return getMessage().getParams().getRequest().getUrl();

    } else if (hasResponse()) {
      return getMessage().getParams().getResponse().getUrl();

    } else if (hasParams() && getMessage().getParams().getUrl() != null) {
      return getMessage().getParams().getUrl();
    }

    return "";
  }

  public String getRequestId() {
    if (hasParams()) {
      return getMessage().getParams().getRequestId();
    }

    return "";
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  private String getRequestMethod() {
    if (hasRequest()) {
      return getMessage().getParams().getRequest().getMethod();
    }

    return "";
  }

  private String getResponseStatus() {
    if (hasResponse()) {
      return getMessage().getParams().getResponse().getStatus();
    }

    return "";
  }

  private String getPayloadData() {
    if (hasResponse()) {
      return getMessage().getParams().getResponse().getPayloadData();
    }

    return "";
  }

  private boolean hasRequest() {
    return hasParams() && getMessage().getParams().getRequest() != null;
  }

  private boolean hasResponse() {
    return hasParams() && getMessage().getParams().getResponse() != null;
  }

  private boolean hasParams() {
    return getMessage() != null && getMessage().getParams() != null;
  }

  private String formatTime(long timestamp) {
    return new SimpleDateFormat(LOG_TIME_FORMAT).format(new Date(timestamp));
  }

  @Override
  public String toString() {
    String prefix = format("%s (id: %s)", formatTime(getTimestamp()), getRequestId());

    if (isNetworkRequest()) {
      return format("%s [REQUEST]  %s %s\n", prefix, getRequestMethod(), getUrl());

    } else if (isNetworkResponse()) {
      return format("%s [RESPONSE] %s %s\n", prefix, getResponseStatus(), getUrl());

    } else if (isWebsocketCreated()) {
      return format("%s [WEBSOCKET_CREATED] %s\n", prefix, getUrl());

    } else if (isWebsocketRequest()) {
      return format("%s [WEBSOCKET_REQUEST] %s\n", prefix, getPayloadData());

    } else if (isWebsocketResponse()) {
      return format("%s [WEBSOCKET_RESPONSE] %s\n", prefix, getPayloadData());
    }

    return super.toString();
  }
}
