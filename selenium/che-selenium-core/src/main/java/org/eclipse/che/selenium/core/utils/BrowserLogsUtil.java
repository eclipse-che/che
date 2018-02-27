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

import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.logging.LogType.PERFORMANCE;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URL;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and store browser logs to the test logs. Log level and type are defined in {@link
 * org.eclipse.che.selenium.core.SeleniumWebDriver#doCreateDriver(URL)}
 */
public class BrowserLogsUtil {
  private static final Logger LOG = LoggerFactory.getLogger(BrowserLogsUtil.class);

  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public static List<LogEntry> getConsoleLogs(WebDriver seleniumWebDriver) {
    return seleniumWebDriver.manage().logs().get(BROWSER).getAll();
  }

  /**
   * get all logs from the active webdriver session
   *
   * @return all types of performance logs
   */
  public static List<LogEntry> getPerformanceLogs(WebDriver seleniumWebDriver) {
    return seleniumWebDriver.manage().logs().get(PERFORMANCE).getAll();
  }

  /** store browser logs to the test logs */
  public static void storeLogsToConsoleOutput(WebDriver seleniumWebDriver) {
    getConsoleLogs(seleniumWebDriver)
        .forEach(logEntry -> LOG.info("{} {}", logEntry.getLevel(), logEntry.getMessage()));
  }

  /**
   * combine the Network and Browser logs
   *
   * @return logs from browser console and requests/responses on CHE api
   */
  public static String getCombinedLogs(WebDriver seleniumWebDriver) {
    StringBuilder combinedLogs =
        new StringBuilder("Browser console logs:\n").append("---------------------\n");
    getConsoleLogs(seleniumWebDriver)
        .forEach(
            logEntry ->
                combinedLogs
                    .append(String.format("%s  %s \n", logEntry.getLevel(), logEntry.getMessage()))
                    .append("\n"));
    return combinedLogs.append(getNetworkDataSentOnCheApi(seleniumWebDriver)).toString();
  }

  /** filter data and get requests/responses that has been sent on CHE /api/ URL */
  public static String getNetworkDataSentOnCheApi(WebDriver seleniumWebDriver) {
    StringBuilder data = new StringBuilder("Network logs: \n").append("---------------\n");
    JsonParser jsonParser = new JsonParser();
    getPerformanceLogs(seleniumWebDriver)
        .forEach(
            logEntry -> {
              JsonElement jsonElement = jsonParser.parse(logEntry.getMessage());
              JsonObject jsonMessageNode =
                  jsonElement.getAsJsonObject().get("message").getAsJsonObject();
              String networkValue = jsonMessageNode.get("method").getAsString();

              if (networkValue.equals("Network.requestWillBeSent")) {
                data.append(getRequestsSentOnChe(jsonMessageNode));

              } else if (networkValue.equals("Network.responseReceived")) {
                data.append(getResponsesSentOnChe(jsonMessageNode));
              }
            });
    return data.toString();
  }

  /**
   * check that current request contains invocation to СHE api URL and provide information about URL
   * and http method/status
   *
   * @param requestMessage json representation of the message object from the log
   * @return info about request from the WebDriver
   */
  private static String getRequestsSentOnChe(JsonObject requestMessage) {
    JsonObject requestNode = requestMessage.getAsJsonObject("params").getAsJsonObject("request");
    StringBuilder requestInfo = new StringBuilder();
    if (isLogEntryContainsApiUrl(requestNode)) {
      requestInfo
          .append("Request Info :---------------> \n")
          .append("Method: " + requestNode.get("method"))
          .append("\n")
          .append("URL: " + requestNode.get("url"))
          .append("\n\n");
    }
    return requestInfo.toString();
  }

  /**
   * check that current response contains invocation to СHE api URL and provide information about
   * URL and http method/status
   *
   * @param requestMessage json representation of the message object from the log
   * @return info about request from the WebDriver
   */
  private static String getResponsesSentOnChe(JsonObject requestMessage) {
    JsonObject responseNode = requestMessage.getAsJsonObject("params").getAsJsonObject("response");
    StringBuilder responceInfo = new StringBuilder();
    if (isLogEntryContainsApiUrl(responseNode)) {
      responceInfo
          .append("Response Info : <--------------- \n")
          .append("Method: " + responseNode.get("status"))
          .append("\n")
          .append("URL: " + responseNode.get("url"))
          .append("\n\n");
    }
    return responceInfo.toString();
  }

  private static boolean isLogEntryContainsApiUrl(JsonObject node) {
    return (node.get("url").isJsonNull()) ? false : node.get("url").getAsString().contains("/api/");
  }
}
