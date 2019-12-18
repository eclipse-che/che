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

import static java.lang.String.format;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.logging.LogType.PERFORMANCE;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverLogsReader {
  public static final String LOG_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss,SSS";
  private static final Logger LOG = LoggerFactory.getLogger(WebDriverLogsReader.class);

  private final SeleniumWebDriver seleniumWebDriver;
  private final String cheHost;

  @Inject
  public WebDriverLogsReader(
      @Named("che.host") String cheHost, SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.cheHost = cheHost;
  }

  @AssistedInject
  public WebDriverLogsReader(
      @Assisted SeleniumWebDriver seleniumWebDriver, @Named("che.host") String cheHost) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.cheHost = cheHost;
  }

  /**
   * Reads logs of browser from web driver.
   *
   * @return log messages from browser console
   */
  private LogEntries readBrowserLogs() {
    return seleniumWebDriver.manage().logs().get(BROWSER);
  }

  /**
   * Reads performance logs from web driver.
   *
   * @return all types of performance logs
   */
  private LogEntries readPerformanceLogs() {
    return seleniumWebDriver.manage().logs().get(PERFORMANCE);
  }

  /**
   * Gets all logs of web driver.
   *
   * @return logs from browser console and Eclipse Che network logs
   */
  public String getAllLogs() throws JsonParseException {
    return prepareBrowserLogs() + "\n" + prepareCheNetworkLogs();
  }

  private String prepareBrowserLogs() {
    StringBuilder browserLogsOutput =
        new StringBuilder("Browser console logs:\n").append("---------------------\n");

    readBrowserLogs()
        .forEach(
            logEntry ->
                browserLogsOutput.append(
                    format(
                        "%s %s %s\n\n",
                        formatTime(logEntry.getTimestamp()),
                        logEntry.getLevel(),
                        logEntry.getMessage())));

    return browserLogsOutput.toString();
  }

  /** filter data and get requests/responses that have been sent on CHE URL */
  private String prepareCheNetworkLogs() throws JsonParseException {
    StringBuilder networkLogsOutput =
        new StringBuilder("Eclipse Che network logs: \n").append("-------------------------\n");
    Map<String, List<Log>> networkLogs = new HashMap<>();
    for (LogEntry logEntry : readPerformanceLogs()) {
      Log log = JsonHelper.fromJson(logEntry.getMessage(), Log.class, null);
      if (log.isNetworkLog()) {
        log.setTimestamp(logEntry.getTimestamp());
        if (networkLogs.containsKey(log.getRequestId())) {
          networkLogs.get(log.getRequestId()).add(log);
        } else {
          ArrayList<Log> logs = new ArrayList<>();
          logs.add(log);
          networkLogs.put(log.getRequestId(), logs);
        }

        if (isLogFromCheTraffic(log.getRequestId(), networkLogs)) {
          networkLogsOutput.append(log.toString());
        } else {
          networkLogs.remove(log.getRequestId());
        }
      }
    }

    return networkLogsOutput.toString();
  }

  /**
   * Go through the {@code networkLogs} related to the certain {@code requestId} and return {@code
   * true} if at least one of them has url from Che traffic.
   */
  private boolean isLogFromCheTraffic(String requestId, Map<String, List<Log>> networkLogs) {
    return networkLogs
            .get(requestId)
            .stream()
            .filter(log -> isUrlFromCheTraffic(log.getUrl()))
            .count()
        > 0;
  }

  private boolean isUrlFromCheTraffic(String url) {
    return url != null
        && (url.matches(format("https?://%s[:\\d]*/api/.*", cheHost)) || url.matches("wss?://.*"));
  }

  private String formatTime(long timestamp) {
    return new SimpleDateFormat(LOG_TIME_FORMAT).format(new Date(timestamp));
  }
}
