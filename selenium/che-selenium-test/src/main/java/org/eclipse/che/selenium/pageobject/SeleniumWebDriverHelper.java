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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Igor Ohrimenko */
@Singleton
public class SeleniumWebDriverHelper {
  protected final SeleniumWebDriver seleniumWebDriver;
  protected final WebDriverWait loadPageWait;
  protected final WebDriverWait attachingElementToDomWait;

  @Inject
  protected SeleniumWebDriverHelper(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    this.attachingElementToDomWait =
        new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC);
  }

  public void setFieldValue(By fieldLocator, String value) {
    waitAndGetElement(fieldLocator).clear();
    waitFieldValue(fieldLocator, "");
    waitAndGetElement(fieldLocator).sendKeys(value);
    waitFieldValue(fieldLocator, value);
  }

  public void waitElementIsVisible(By elementLocator) {
    loadPageWait.until(visibilityOfElementLocated(elementLocator));
  }

  public String getFieldValue(By fieldLocator) {
    return waitAndGetElement(fieldLocator).getAttribute("value");
  }

  public WebElement waitAndGetElement(By locator) {
    return loadPageWait.until(visibilityOfElementLocated(locator));
  }

  public void waitFieldValue(By fieldLocator, String expectedValue) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>) driver -> getFieldValue(fieldLocator).equals(expectedValue));
  }

  public void waitAndClickOnElement(By elementLocator) {
    loadPageWait.until(visibilityOfElementLocated(elementLocator)).click();
  }

  public void waitAndClickOnElement(WebElement webElement) {
    loadPageWait.until(visibilityOf(webElement)).click();
  }

  public boolean elementIsVisible(By locator) {
    try {
      attachingElementToDomWait.until(visibilityOfElementLocated(locator));
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }
}
