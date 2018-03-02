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
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public LogEntries readBrowserLogs() {
    return seleniumWebDriver.manage().logs().get(BROWSER);
  }

  /**
   * get all logs from the active webdriver session
   *
   * @return all types of performance logs
   */
  public LogEntries readPerformanceLogs() {
    return seleniumWebDriver.manage().logs().get(PERFORMANCE);
  }

  /**
   * get all available logs of web driver
   *
   * @return logs from browser console and requests/responses on CHE api
   */
  public String getAllLogs() throws JsonParseException {
    return getBrowserLogs() + "\n" + readNetworkLogs();
  }

  private String getBrowserLogs() {
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
  private String readNetworkLogs() throws JsonParseException {
    StringBuilder networkLogsOutput =
        new StringBuilder("Network logs: \n").append("---------------\n");
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
        && (url.matches(format("https?://%s[:\\d]*/api/.*", cheHost))
            || url.matches(format("wss?://%s[:\\d]*/(api|connect|wsagent).*", cheHost)));
  }

  private String formatTime(long timestamp) {
    SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss,SSS");
    return dt.format(new Date(timestamp));
  }
}
