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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
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
   * read logs from browser console
   *
   * @return log messages from browser console
   */
  public List<LogEntry> getBrowserLogs() {
    return seleniumWebDriver.manage().logs().get(LogType.BROWSER).getAll();
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
