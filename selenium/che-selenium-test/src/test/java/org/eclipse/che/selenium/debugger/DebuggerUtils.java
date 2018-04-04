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
package org.eclipse.che.selenium.debugger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Musienko Maxim */
public class DebuggerUtils {
  private static final Logger LOG = LoggerFactory.getLogger(DebuggerUtils.class);

  /**
   * We need use other thread for send request in flow application. Because request will be stopped
   * in breakpoint. And test cannot be continued.
   *
   * @param appUrl the application url of launched application in the Che
   * @param data request message for application
   * @param contentType content type
   * @param successCode success code
   */
  public CompletableFuture<String> gotoDebugAppAndSendRequest(
      final String appUrl, final String data, String contentType, int successCode)
      throws ExecutionException {
    return CompletableFuture.supplyAsync(
        () -> {
          HttpURLConnection connection = null;
          String response;
          BufferedReader br;
          StringBuilder responseData = new StringBuilder();
          try {
            URL url = new URL(appUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream output = connection.getOutputStream();
            output.write(data.getBytes("UTF-8"));
            if (connection.getResponseCode() != successCode) {
              throw new RuntimeException(
                  new Exception(
                          "Cannot do request for application: " + connection.getResponseCode())
                      + IoUtil.readStream(connection.getErrorStream()));
            }
            output.close();
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            while ((response = br.readLine()) != null) {
              responseData.append(response);
            }
            LOG.debug(responseData.toString());
            br.close();
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return e.getMessage();
          } finally {
            if (connection != null) {
              connection.disconnect();
            }
          }
          return responseData.toString();
        });
  }
}
