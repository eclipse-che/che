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

import static org.openqa.selenium.logging.LogType.CLIENT;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
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
  private final TestApiEndpointUrlProvider apiEndpointProvider;
  private String logDir = "/home/mmusiienko/webdriver-log-example/%s";

  @Inject
  public BrowserLogsUtil(
      SeleniumWebDriver seleniumWebDriver, TestApiEndpointUrlProvider apiEndpointProvider) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.apiEndpointProvider = apiEndpointProvider;
  }

  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public List<LogEntry> getLogs() {
    return seleniumWebDriver.manage().logs().get(CLIENT).getAll();
  }

  /**
   * read logs from browser by type
   *
   * @param logType
   * @return
   */
  public List<LogEntry> getLogs(String logType) {

    return seleniumWebDriver.manage().logs().get(logType).getAll();
  }

  /** store browser logs to the test logs */
  public void storeLogs() {
    getLogs()
        .forEach(
            logEntry -> {
              try {
                Files.write(
                    Paths.get(String.format(logDir, "driver.txt")),
                    ("Level: " + logEntry.getLevel() + "Message: " + logEntry.getMessage() + "\n")
                        .getBytes("utf-8"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }

  /** @param logs */
  public void getNetworkRequests(List<LogEntry> logs) {
    JsonParser jsonParser = new JsonParser();
    logs.forEach(
        logEntry -> {
          JsonElement jsonElement = jsonParser.parse(logEntry.getMessage());
          JsonObject jsonMessageNode =
              jsonElement.getAsJsonObject().get("message").getAsJsonObject();
          String networkValue = jsonMessageNode.get("method").getAsString();

          if (networkValue.equals("Network.requestWillBeSent")) {
            getApiRequests(jsonMessageNode);

          } else if (networkValue.equals("Network.responseReceived")) {
            getApiResponses(jsonMessageNode);
          }
        });
  }

  public void shadowSearcher(List<LogEntry> logs) {
    JsonParser jsonParser = new JsonParser();
    logs.forEach(
        logEntry -> {
          try {
            Files.write(
                Paths.get(String.format(logDir, "profiler.txt")),
                ("Level: " + logEntry.getLevel() + "Message: " + logEntry.getMessage() + "\n")
                    .getBytes("utf-8"),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  private void getApiRequests(JsonObject requestMessage) {
    JsonObject requestNode = requestMessage.getAsJsonObject("params").getAsJsonObject("request");
    if (isLogEntryContainsApiUrl(requestNode)) {
      System.out.println(
          "RequestInfo :---------------> \n"
              + "Method: "
              + requestNode.get("method")
              + "\n"
              + "URL: "
              + requestNode.get("url")
              + "\n");
    }
  }

  private void getApiResponses(JsonObject requestMessage) {
    JsonObject responseNode = requestMessage.getAsJsonObject("params").getAsJsonObject("response");
    if (isLogEntryContainsApiUrl(responseNode)) {
      System.out.println(
          "Responce info: <---------------: \n"
              + "Status: "
              + responseNode.get("status")
              + "\n"
              + "URL: "
              + responseNode.get("url")
              + "\n");
    }
  }

  private boolean isLogEntryContainsApiUrl(JsonObject node) {
    return (node.get("url").isJsonNull()) ? false : node.get("url").getAsString().contains("/api/");
  }
}
