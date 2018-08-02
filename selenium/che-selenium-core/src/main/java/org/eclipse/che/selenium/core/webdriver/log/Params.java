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
package org.eclipse.che.selenium.core.webdriver.log;

/**
 * This is POJO to extract 'params' value from web driver network log entry
 *
 * @author Dmytro Nochevnov
 */
public interface Params {
  String getRequestId();

  void setRequestId(String requestId);

  String getUrl();

  void setUrl(String url);

  Request getRequest();

  void setRequest(Request request);

  Response getResponse();

  void setResponse(Response request);

  String getTimestamp();

  void setTimestamp(String timestamp);
}
