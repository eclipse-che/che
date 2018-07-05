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
package org.eclipse.che.selenium.core.webdriver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WebDriverWaitFactory {
  private SeleniumWebDriver seleniumWebDriver;
  private Map<Integer, WebDriverWait> webDriverWaits = new HashMap<>();

  @Inject
  public WebDriverWaitFactory(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  public WebDriverWait get() {
    return get(LOAD_PAGE_TIMEOUT_SEC);
  }

  public WebDriverWait get(int timeout) {
    if (!webDriverWaits.isEmpty()) {
      if (webDriverWaits.containsKey(timeout)) {
        return webDriverWaits.get(timeout);
      }
    }

    WebDriverWait webDriverWait = new WebDriverWait(seleniumWebDriver, timeout);
    webDriverWaits.put(timeout, webDriverWait);

    return webDriverWait;
  }

  /**
   * Creates an instance of the {@link WebDriverWait} with specified {@code timeout} and frequency
   * of attempts.
   *
   * @param timeout waiting time for condition in seconds.
   * @param pollingEvery delay between attempts.
   * @return instance of the {@link WebDriverWait} initialized by specified values.
   */
  public WebDriverWait get(int timeout, int pollingEvery) {
    long delayBetweenAttempts = SECONDS.toMillis(pollingEvery);
    return new WebDriverWait(seleniumWebDriver, timeout, delayBetweenAttempts);
  }
}
