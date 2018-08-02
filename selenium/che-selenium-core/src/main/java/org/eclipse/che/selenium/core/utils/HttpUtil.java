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
package org.eclipse.che.selenium.core.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

  /**
   * Gets the status code from URL.
   *
   * @param url url to check
   * @return {@link HttpURLConnection#getResponseCode()}
   */
  public static int getUrlResponseCode(String url) throws IOException {
    URL connectionUrl = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
    connection.setRequestMethod("GET");

    return connection.getResponseCode();
  }
}
