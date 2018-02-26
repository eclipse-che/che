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
import java.net.URL;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and store browser logs to the test logs. Log level and type are defined in {@link
 * org.eclipse.che.selenium.core.SeleniumWebDriver#doCreateDriver(URL)}
 */
@Singleton
public class BrowserLogsUtil {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Logger LOG = LoggerFactory.getLogger(BrowserLogsUtil.class);

  @Inject
  public BrowserLogsUtil(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public List<LogEntry> getConsoleLogs() {
    return seleniumWebDriver.manage().logs().get(BROWSER).getAll();
  }

  /**
   * get all logs from the active webdriver session
   *
   * @return all types of performance logs
   */
  public List<LogEntry> getPerformanceLogs() {
    return seleniumWebDriver.manage().logs().get(PERFORMANCE).getAll();
  }

  /** store browser logs to the test logs */
  public void storeLogs() {
    getConsoleLogs()
        .forEach(logEntry -> LOG.info("{} {}", logEntry.getLevel(), logEntry.getMessage()));
  }

  /**
   * combine the Network and Browser logs
   *
   * @return logs from browser console and requests/responses on CHE api
   */
  public String getCombinedLogs() {
    StringBuilder combinedLogs = new StringBuilder();
    getConsoleLogs()
        .forEach(
            logEntry ->
                combinedLogs.append(
                    String.format("%s  %s \n", logEntry.getLevel(), logEntry.getMessage())));
    return combinedLogs.append(getNetworkDataSentOnCheApi()).toString();
  }

  /** filter data and get requests/responses that has been sent on CHE /api/ URL */
  public String getNetworkDataSentOnCheApi() {
    StringBuilder data = new StringBuilder();
    JsonParser jsonParser = new JsonParser();
    getPerformanceLogs()
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
  private String getRequestsSentOnChe(JsonObject requestMessage) {
    JsonObject requestNode = requestMessage.getAsJsonObject("params").getAsJsonObject("request");
    StringBuilder requestInfo = new StringBuilder();
    if (isLogEntryContainsApiUrl(requestNode)) {
      requestInfo
          .append("RequestInfo :---------------> \n")
          .append("Method: " + requestNode.get("method\n"))
          .append("URL: " + requestNode.get("url\n"));
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
  private String getResponsesSentOnChe(JsonObject requestMessage) {
    JsonObject responseNode = requestMessage.getAsJsonObject("params").getAsJsonObject("response");
    StringBuilder responceInfo = new StringBuilder();
    if (isLogEntryContainsApiUrl(responseNode)) {
      responceInfo
          .append("RequestInfo :---------------> \n")
          .append("Method: " + responseNode.get("method\n"))
          .append("URL: " + responseNode.get("url\n"));
    }
    return responceInfo.toString();
  }

  private boolean isLogEntryContainsApiUrl(JsonObject node) {
    return (node.get("url").isJsonNull()) ? false : node.get("url").getAsString().contains("/api/");
  }
}
