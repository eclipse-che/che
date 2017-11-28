/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestWebElementRenderChecker {
  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait loadPageWebDriverWait;

  @Inject
  public TestWebElementRenderChecker(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loadPageWebDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElement list or context menu which need check
   * @param seconds timeout for check
   */
  public void waitElementIsRendered(WebElement webElement, int seconds) {
    waitElementIsStatic(getFluentWait(seconds), webElement);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElement list or context menu which need check
   */
  public void waitElementIsRendered(WebElement webElement) {
    waitElementIsRendered(webElement, LOAD_PAGE_TIMEOUT_SEC);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElementXpath list or context menu Xpath which need check
   */
  public void waitElementIsRendered(String webElementXpath) {
    waitElementIsRendered(getAndWaitWebElement(webElementXpath));
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElementXpath list or context menu Xpath which need check
   * @param seconds timeout for check
   */
  public void waitElementIsRendered(String webElementXpath, int seconds) {
    waitElementIsRendered(getAndWaitWebElement(webElementXpath), seconds);
  }

  private Boolean dimensionsAreEquivalent(AtomicInteger sizeHashCode, Dimension newDimension) {
    return sizeHashCode.get() == getSizeHashCode(newDimension);
  }

  private FluentWait<WebDriver> getFluentWait(int seconds) {
    return new FluentWait<WebDriver>(seleniumWebDriver)
        .withTimeout(seconds, TimeUnit.SECONDS)
        .pollingEvery(200, TimeUnit.MILLISECONDS)
        .ignoring(StaleElementReferenceException.class);
  }

  private void waitElementIsStatic(FluentWait<WebDriver> webDriverWait, WebElement webElement) {
    AtomicInteger sizeHashCode = new AtomicInteger();

    webDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> {
              Dimension newDimension = getAndWaitWebElement(webElement).getSize();

              if (dimensionsAreEquivalent(sizeHashCode, newDimension)) {
                return true;
              } else {
                sizeHashCode.set(getSizeHashCode(newDimension));
                return false;
              }
            });
  }

  /**
   * height is multiplied because we must avoid the situation when, in fact, different parties will
   * give the same sum for example 15 + 25 == 25 + 15, but 15 + 25*10000 != 25 + 15*10000
   *
   * @param dimension consist partial sizes
   * @return partial sizes sum with shift
   */
  private int getSizeHashCode(Dimension dimension) {
    return dimension.getWidth() + (dimension.getHeight() * 10000);
  }

  private WebElement getAndWaitWebElement(String webElementXpath) {
    return loadPageWebDriverWait.until(
        ExpectedConditions.visibilityOfElementLocated(By.xpath(webElementXpath)));
  }

  private WebElement getAndWaitWebElement(WebElement webElement) {
    return loadPageWebDriverWait.until(ExpectedConditions.visibilityOf(webElement));
  }
}
