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

import static java.lang.String.format;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

  private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

  /**
   * Gets the status code from URL.
   *
   * @param url url to check
   * @return the HTTP Status-Code, or -1
   */
  public static int getUrlResponseCode(String url) throws IOException {
    try {
      URL connectionUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
      connection.setRequestMethod("GET");

      return connection.getResponseCode();
    } catch (IOException ex) {
      LOG.error(format("There was a problem with connecting to URL '%s'", url), ex);
      throw new IOException(ex.getMessage(), ex);
    }
  }
}
