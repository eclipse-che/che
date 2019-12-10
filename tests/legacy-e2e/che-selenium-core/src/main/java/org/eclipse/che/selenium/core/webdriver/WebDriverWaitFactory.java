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
package org.eclipse.che.selenium.core.webdriver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WebDriverWaitFactory {
  private SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WebDriverWaitFactory(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  public WebDriverWait get() {
    return get(LOAD_PAGE_TIMEOUT_SEC);
  }

  public WebDriverWait get(int timeoutInSec) {
    return new WebDriverWait(seleniumWebDriver, timeoutInSec);
  }

  /**
   * Creates an instance of the {@link FluentWait} with specified {@code timeoutInSec} and error
   * message supplier.
   *
   * @param timeoutInSec waiting time in seconds.
   * @param messageSupplier error message supplier
   * @return
   */
  public FluentWait<WebDriver> get(int timeoutInSec, Supplier<String> messageSupplier) {
    return new WebDriverWait(seleniumWebDriver, timeoutInSec).withMessage(messageSupplier);
  }

  /**
   * Creates an instance of the {@link FluentWait} with specified {@code timeoutInSec} and ignoring
   * exception of {@code ignoringExceptionType}.
   *
   * @param timeoutInSec waiting time in seconds.
   * @param ignoredExceptionTypes exceptions which are ignoring during timeout.
   * @return
   */
  public FluentWait<WebDriver> get(
      int timeoutInSec, Collection<Class<? extends Throwable>> ignoredExceptionTypes) {
    return new WebDriverWait(seleniumWebDriver, timeoutInSec).ignoreAll(ignoredExceptionTypes);
  }

  /**
   * Creates an instance of the {@link WebDriverWait} with specified {@code timeoutInSec} and
   * frequency of attempts.
   *
   * @param timeoutInSec waiting time in seconds.
   * @param delayBetweenAttemptsInSec delay between attempts.
   * @return instance of the {@link WebDriverWait} initialized by specified values.
   */
  public WebDriverWait get(int timeoutInSec, int delayBetweenAttemptsInSec) {
    long delayBetweenAttempts = SECONDS.toMillis(delayBetweenAttemptsInSec);
    return new WebDriverWait(seleniumWebDriver, timeoutInSec, delayBetweenAttempts);
  }
}
