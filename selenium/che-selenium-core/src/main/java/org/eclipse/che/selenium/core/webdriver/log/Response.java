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
package org.eclipse.che.selenium.core.webdriver.log;

/**
 * This is POJO to extract 'response' value from web driver network log entry.
 *
 * @author Dmytro Nochevnov
 */
public interface Response {
  String getUrl();

  void setUrl(String url);

  String getStatus();

  void setStatus(String status);

  String getPayloadData();

  void setPayloadData(String payloadData);
}
