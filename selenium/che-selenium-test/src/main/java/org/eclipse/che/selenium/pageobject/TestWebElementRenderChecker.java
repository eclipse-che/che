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
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
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
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElement list or context menu which need check
   * @param seconds timeout for check
   */
  public void waitElementIsRendered(WebElement webElement, int seconds) {
    FluentWait<WebDriver> waitElement = getFluentWait(seconds);

    waitElementIsStatic(waitElement, webElement);
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
    WebElement webElement =
        loadPageWebDriverWait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(webElementXpath)));

    waitElementIsRendered(webElement);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElementXpath list or context menu Xpath which need check
   * @param seconds timeout for check
   */
  public void waitElementIsRendered(String webElementXpath, int seconds) {
    WebElement webElement =
        loadPageWebDriverWait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(webElementXpath)));

    waitElementIsRendered(webElement, seconds);
  }

  private Boolean dimensionsAreEquivalent(
      WebElementDimension beforeDimension, Dimension afterDimension) {
    return beforeDimension.getWidth() == afterDimension.getWidth()
        && beforeDimension.getHeight() == afterDimension.getHeight();
  }

  private FluentWait<WebDriver> getFluentWait(int seconds) {
    return new FluentWait<WebDriver>(seleniumWebDriver)
        .withTimeout(seconds, TimeUnit.SECONDS)
        .pollingEvery(200, TimeUnit.MILLISECONDS)
        .ignoring(StaleElementReferenceException.class);
  }

  private void waitElementIsStatic(FluentWait<WebDriver> webDriverWait, WebElement webElement) {
    WebElementDimension beforeDimension = new WebElementDimension(0, 0);

    webDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> {
              Dimension secondDimension =
                  new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
                      .until(ExpectedConditions.visibilityOf(webElement))
                      .getSize();

              if (dimensionsAreEquivalent(beforeDimension, secondDimension)) {
                return true;
              } else {
                beforeDimension.setWidth(secondDimension.getWidth());
                beforeDimension.setHeight(secondDimension.getHeight());
                return false;
              }
            });
  }

  private class WebElementDimension {
    private int width;
    private int height;

    public WebElementDimension() {
      this.width = 0;
      this.height = 0;
    }

    public WebElementDimension(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public int getWidth() {
      return this.width;
    }

    public void setWidth(int width) {
      this.width = width;
    }

    public int getHeight() {
      return this.height;
    }

    public void setHeight(int height) {
      this.height = height;
    }
  }
}
