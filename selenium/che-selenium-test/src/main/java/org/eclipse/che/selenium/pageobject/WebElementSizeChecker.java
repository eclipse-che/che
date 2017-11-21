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
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WebElementSizeChecker {
  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait loadPageWebDriverWait;

  @Inject
  public WebElementSizeChecker(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loadPageWebDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElement list or context menu which need check
   */
  public void waitElementIsOpen(WebElement webElement) {
    loadPageWebDriverWait.until(ExpectedConditions.visibilityOf(webElement));

    loadPageWebDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> {
              Dimension firstDimension;
              Dimension secondDimension;

              firstDimension = webElement.getSize();

              WaitUtils.sleepQuietly(500, TimeUnit.MILLISECONDS);

              secondDimension = webElement.getSize();

              return dimensionsAreEquivalent(firstDimension, secondDimension);
            });
  }

  /**
   * wait until element have the same size between two checks it means that element is fully opened
   *
   * @param webElementXpath list or context menu Xpath which need check
   */
  public void waitElementIsOpen(String webElementXpath) {
    WebElement webElement =
        loadPageWebDriverWait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(webElementXpath)));

    waitElementIsOpen(webElement);
  }

  private Boolean dimensionsAreEquivalent(Dimension firstDimension, Dimension secondDimension) {
    return firstDimension.getHeight() == secondDimension.getHeight()
        && firstDimension.getWidth() == secondDimension.getWidth();
  }
}
