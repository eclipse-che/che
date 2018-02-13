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
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

/** @author Igor Okhrimenko */
@Singleton
public class SeleniumWebDriverHelper {
  protected final int DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;
  protected final SeleniumWebDriver seleniumWebDriver;
  protected final WebDriverWaitFactory webDriverWaitFactory;
  protected final ActionsFactory actionsFactory;

  @Inject
  protected SeleniumWebDriverHelper(
      SeleniumWebDriver seleniumWebDriver,
      WebDriverWaitFactory webDriverWaitFactory,
      ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.webDriverWaitFactory = webDriverWaitFactory;
    this.actionsFactory = actionsFactory;
  }

  /**
   * Set text in provided field with checks which get text for compare by {@link
   * WebElement#getAttribute(String)}
   *
   * @param fieldLocator
   * @param value
   */
  public void setFieldValue(By fieldLocator, String value) {
    waitElementIsVisible(fieldLocator).clear();
    waitElementValue(fieldLocator, "");
    sendKeysToElement(fieldLocator, value);
    waitElementValue(fieldLocator, value);
  }

  /**
   * Set text in provided field with checks which get text for compare by {@link
   * WebElement#getAttribute(String)}
   *
   * @param webElement
   * @param value
   */
  public void setFieldValue(WebElement webElement, String value) {
    waitElementIsVisible(webElement).clear();
    waitElementValue(webElement, "");
    sendKeysToElement(webElement, value);
    waitElementValue(webElement, value);
  }

  /**
   * Set text in provided field with checks which get text for compare by {@link
   * WebElement#getText()}
   *
   * @param fieldLocator
   * @param value
   */
  public void setFieldText(By fieldLocator, String value) {
    waitElementIsVisible(fieldLocator).clear();
    waitElementText(fieldLocator, "");
    sendKeysToElement(fieldLocator, value);
    waitElementText(fieldLocator, value);
  }

  /**
   * Set text in provided field with checks which get text for compare by {@link
   * WebElement#getText()}
   *
   * @param webElement
   * @param value
   */
  public void setFieldText(WebElement webElement, String value) {
    waitElementIsVisible(webElement).clear();
    waitElementText(webElement, "");
    sendKeysToElement(webElement, value);
    waitElementText(webElement, value);
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be visible
   *
   * @param elementLocator
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitElementIsVisible(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be visible
   *
   * @param elementLocator
   * @return found element
   */
  public WebElement waitElementIsVisible(By elementLocator) {
    return waitElementIsVisible(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until provided {@link WebElement} be visible
   *
   * @param webElement
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitElementIsVisible(WebElement webElement, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOf(webElement));
  }

  /**
   * Waits until provided {@link WebElement} be visible
   *
   * @param webElement
   * @return found element
   */
  public WebElement waitElementIsVisible(WebElement webElement) {
    return waitElementIsVisible(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be attached to DOM it does not
   * mean that element is visible
   *
   * @param elementLocator
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitElementIsPresent(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(presenceOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be attached to DOM it does not
   * mean that element is visible
   *
   * @param elementLocator
   * @return found element
   */
  public WebElement waitElementIsPresent(By elementLocator) {
    return webDriverWaitFactory
        .get(DEFAULT_TIMEOUT)
        .until(presenceOfElementLocated(elementLocator));
  }

  /**
   * Type text in the {@link WebElement} with provided locator {@link By}
   *
   * @param elementLocator
   * @param text
   */
  public void sendKeysToElement(By elementLocator, String text) {
    waitElementIsVisible(elementLocator).sendKeys(text);
  }

  /**
   * Type text in the provided {@link WebElement}
   *
   * @param webElement
   * @param text
   */
  public void sendKeysToElement(WebElement webElement, String text) {
    waitElementIsVisible(webElement).sendKeys(text);
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and get text
   *
   * @param fieldLocator
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(By fieldLocator) {
    return waitElementIsVisible(fieldLocator).getAttribute("value");
  }

  /**
   * Waits visibility of provided {@link WebElement} and get text
   *
   * @param webElement
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(WebElement webElement) {
    return waitElementIsVisible(webElement).getAttribute("value");
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and get text
   *
   * @param fieldLocator
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(By fieldLocator) {
    return waitElementIsVisible(fieldLocator).getText();
  }

  /**
   * Waits visibility of provided {@link WebElement} and get text
   *
   * @param webElement
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(WebElement webElement) {
    return waitElementIsVisible(webElement).getText();
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param fieldLocator
   * @param expectedValue
   * @param timeout waiting time in seconds
   */
  public void waitElementValue(By fieldLocator, String expectedValue, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetValue(fieldLocator).equals(expectedValue));
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param fieldLocator
   * @param expectedValue
   */
  public void waitElementValue(By fieldLocator, String expectedValue) {
    waitElementValue(fieldLocator, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param webElement
   * @param expectedValue
   * @param timeout waiting time in seconds
   */
  public void waitElementValue(WebElement webElement, String expectedValue, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetValue(webElement).equals(expectedValue));
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param webElement
   * @param expectedValue
   */
  public void waitElementValue(WebElement webElement, String expectedValue) {
    waitElementValue(webElement, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param fieldLocator
   * @param expectedText
   * @param timeout waiting time in seconds
   */
  public void waitElementText(By fieldLocator, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetText(fieldLocator).equals(expectedText));
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param fieldLocator
   * @param expectedText
   */
  public void waitElementText(By fieldLocator, String expectedText) {
    waitElementText(fieldLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param webElement
   * @param expectedText
   * @param timeout waiting time in seconds
   */
  public void waitElementText(WebElement webElement, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetText(webElement).equals(expectedText));
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param webElement
   * @param expectedText
   */
  public void waitElementText(WebElement webElement, String expectedText) {
    waitElementText(webElement, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and clicks once on it
   * by {@link WebElement#click()}
   *
   * @param elementLocator
   * @param timeout waiting time in seconds
   */
  public void waitAndClick(By elementLocator, int timeout) {
    webDriverWaitFactory.get(timeout).until(visibilityOfElementLocated(elementLocator)).click();
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and clicks once on it
   * by {@link WebElement#click()}
   *
   * @param elementLocator
   */
  public void waitAndClick(By elementLocator) {
    waitAndClick(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility of provided {@link WebElement} and clicks once on it by {@link
   * WebElement#click()}
   *
   * @param webElement
   * @param timeout waiting time in seconds
   */
  public void waitAndClick(WebElement webElement, int timeout) {
    webDriverWaitFactory.get(timeout).until(visibilityOf(webElement)).click();
  }

  /**
   * Waits visibility of provided {@link WebElement} and clicks once on it by {@link
   * WebElement#click()}
   *
   * @param webElement
   */
  public void waitAndClick(WebElement webElement) {
    waitAndClick(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Moves cursor to provided {@link WebElement} and clicks twice on it by {@link
   * org.openqa.selenium.interactions.Action}. Gets rid of {@link
   * org.openqa.selenium.StaleElementReferenceException} by using {@link Actions#doubleClick()}.
   *
   * @param elementLocator
   */
  public void moveCursorToElementAndDoubleClick(By elementLocator) {
    moveCursorToElement(elementLocator);
    waitElementIsVisible(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to provided {@link WebElement} and clicks twice on it by {@link
   * org.openqa.selenium.interactions.Action}. Gets rid of {@link
   * org.openqa.selenium.StaleElementReferenceException} by using {@link Actions#doubleClick()}.
   *
   * @param webElement
   */
  public void moveCursorToElementAndDoubleClick(WebElement webElement) {
    moveCursorToElement(webElement);
    waitElementIsVisible(webElement);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to {@link WebElement} with provided locator {@link By} and clicks once on it by
   * {@link org.openqa.selenium.interactions.Action}
   *
   * @param elementLocator
   */
  public void moveCursorToElementAndClick(By elementLocator) {
    moveCursorToElement(elementLocator);
    waitElementIsVisible(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to provided {@link WebElement} and clicks once on it by {@link
   * org.openqa.selenium.interactions.Action}
   *
   * @param webElement
   */
  public void moveCursorToElementAndClick(WebElement webElement) {
    moveCursorToElement(webElement);
    waitElementIsVisible(webElement);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to WebElement with provided locator which attached to DOM but it does not mean
   * that element is visible
   *
   * @param elementLocator {@link By}
   */
  public void moveCursorToElement(By elementLocator) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(waitElementIsPresent(elementLocator))
        .perform();
  }

  /**
   * Moves cursor to provided WebElement which attached to DOM but it does not mean that element is
   * visible
   *
   * @param webElement
   */
  public void moveCursorToElement(WebElement webElement) {
    actionsFactory.createAction(seleniumWebDriver).moveToElement(webElement).perform();
  }

  /**
   * Checks visibility state of {@link WebElement} with provided locator {@link By}
   *
   * @param locator
   * @return state of element visibility
   */
  public boolean isElementVisible(By locator) {
    try {
      webDriverWaitFactory
          .get(ATTACHING_ELEM_TO_DOM_SEC)
          .until(visibilityOfElementLocated(locator));
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }

  /**
   * Checks visibility state of provided {@link WebElement}
   *
   * @param webElement
   * @return state of element visibility
   */
  public boolean isElementVisible(WebElement webElement) {
    try {
      webDriverWaitFactory.get(ATTACHING_ELEM_TO_DOM_SEC).until(visibilityOf(webElement));
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }
}
