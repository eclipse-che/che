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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WebDriverLogsReader {
  private static final Logger LOG = LoggerFactory.getLogger(WebDriverLogsReader.class);
  private SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WebDriverLogsReader(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }
  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public static List<LogEntry> readBrowserLogs(WebDriver seleniumWebDriver) {
    return seleniumWebDriver.manage().logs().get(BROWSER).getAll();
  }

  /**
   * get all logs from the active webdriver session
   *
   * @return all types of performance logs
   */
  public static List<LogEntry> readPerformanceLogs(WebDriver seleniumWebDriver) {
    return seleniumWebDriver.manage().logs().get(PERFORMANCE).getAll();
  }

  /** store browser logs to the test logs */
  public void logBrowserLogs() {
    readBrowserLogs(seleniumWebDriver)
        .forEach(logEntry -> LOG.info("{} {}", logEntry.getLevel(), logEntry.getMessage()));
  }

  /**
   * get all available logs of web driver
   *
   * @return logs from browser console and requests/responses on CHE api
   */
  public String getAllLogs() {
    StringBuilder combinedLogs =
        new StringBuilder("Browser console logs:\n").append("---------------------\n");
    readBrowserLogs(seleniumWebDriver)
        .forEach(
            logEntry ->
                combinedLogs
                    .append(String.format("%s  %s \n", logEntry.getLevel(), logEntry.getMessage()))
                    .append("\n"));
    return combinedLogs.append(readNetworkLogs(seleniumWebDriver)).toString();
  }

  /**
   * combine the Network and Browser logs
   *
   * @return logs from browser console and requests/responses on CHE api
   */
  public static String getAllLogs(SeleniumWebDriver seleniumWebDriver) {
    return new WebDriverLogsReader(seleniumWebDriver).getAllLogs();
  }

  /** filter data and get requests/responses that has been sent on CHE /api/ URL */
  public static String readNetworkLogs(WebDriver seleniumWebDriver) {
    StringBuilder data = new StringBuilder("Network logs: \n").append("---------------\n");
    JsonParser jsonParser = new JsonParser();
    for (LogEntry logEntry : readPerformanceLogs(seleniumWebDriver)) {
      JsonElement jsonElement = jsonParser.parse(logEntry.getMessage());
      JsonObject jsonMessageNode = jsonElement.getAsJsonObject().get("message").getAsJsonObject();
      String networkValue = jsonMessageNode.get("method").getAsString();

      if (networkValue.equals("Network.requestWillBeSent")) {
        data.append(extractCheRequests(jsonMessageNode));

      } else if (networkValue.equals("Network.responseReceived")) {
        data.append(extractCheResponses(jsonMessageNode));
      }
    }
    return data.toString();
  }

  /**
   * check that current request contains invocation to СHE api URL and provide information about URL
   * and http method/status
   *
   * @param requestMessage json representation of the message object from the log
   * @return info about request from the WebDriver
   */
  private static String extractCheRequests(JsonObject requestMessage) {
    JsonObject requestNode = requestMessage.getAsJsonObject("params").getAsJsonObject("request");
    StringBuilder requestInfo = new StringBuilder();
    if (isNodeFromCheTraffic(requestNode)) {
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
  private static String extractCheResponses(JsonObject requestMessage) {
    JsonObject responseNode = requestMessage.getAsJsonObject("params").getAsJsonObject("response");
    StringBuilder responceInfo = new StringBuilder();
    if (isNodeFromCheTraffic(responseNode)) {
      responceInfo
          .append("Response Info : <--------------- \n")
          .append("Method: " + responseNode.get("status"))
          .append("\n")
          .append("URL: " + responseNode.get("url"))
          .append("\n\n");
    }
    return responceInfo.toString();
  }

  private static boolean isNodeFromCheTraffic(JsonObject node) {
    return (node.get("url").isJsonNull()) ? false : node.get("url").getAsString().contains("/api/");
  }
}
