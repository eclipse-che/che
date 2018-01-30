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
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.logging.LogType.CLIENT;
import static org.openqa.selenium.logging.LogType.DRIVER;
import static org.openqa.selenium.logging.LogType.PERFORMANCE;
import static org.openqa.selenium.logging.LogType.PROFILER;
import static org.openqa.selenium.logging.LogType.SERVER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BrowserLogsUtil {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Logger LOG = LoggerFactory.getLogger(BrowserLogsUtil.class);

  @Inject
  public BrowserLogsUtil(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  /**
   * read logs of provided type
   *
   * @param logType
   * @see LogType
   * @return provided type of browser logs
   */
  public List<LogEntry> getLogs(String logType) {
    Logs logs = seleniumWebDriver.manage().logs();
    List<LogEntry> result = Collections.emptyList();

    switch (logType) {
      case BROWSER:
        {
          result = logs.get(BROWSER).getAll();
          break;
        }

      case CLIENT:
        {
          result = logs.get(CLIENT).getAll();
          break;
        }

      case DRIVER:
        {
          result = logs.get(DRIVER).getAll();
          break;
        }

      case PERFORMANCE:
        {
          result = logs.get(PERFORMANCE).getAll();
          break;
        }

      case PROFILER:
        {
          result = logs.get(PROFILER).getAll();
          break;
        }

      case SERVER:
        {
          result = logs.get(SERVER).getAll();
          break;
        }
    }

    return result;
  }

  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public List<LogEntry> getBrowserLogs() {
    return getLogs(BROWSER);
  }

  /**
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public String getBrowserLogsAsString() {
    StringBuilder result = new StringBuilder();
    getBrowserLogs()
        .forEach(
            logEntry ->
                result.append(format("%s %s\n", logEntry.getLevel(), logEntry.getMessage())));

    return result.toString();
  }

  /** append browser logs to the test logs */
  public void appendBrowserLogs() {
    getBrowserLogs()
        .forEach(logEntry -> LOG.info("{} {}", logEntry.getLevel(), logEntry.getMessage()));
  }
}
