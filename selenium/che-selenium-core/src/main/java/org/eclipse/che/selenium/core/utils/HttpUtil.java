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
