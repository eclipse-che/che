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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BrowserLogsUtils {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Logger LOG = LoggerFactory.getLogger(BrowserLogsUtils.class);

  @Inject
  public BrowserLogsUtils(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  public String getChromeConsoleLogs() {
    StringBuilder result = new StringBuilder();
    seleniumWebDriver
        .manage()
        .logs()
        .get(LogType.BROWSER)
        .forEach(
            logEntry -> {
              result.append(String.format("\n%s %s", logEntry.getLevel(), logEntry.getMessage()));
            });

    return result.toString();
  }

  public void addBrowserLogsToTheTestLogs() {
    LOG.info(getChromeConsoleLogs());
  }
}
