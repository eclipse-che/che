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
   * Ensures {@code element} has got {@code value} in next steps: ensures element is visible and has
   * empty value, sets {@code value} by sending corresponding keys to {@code element} and waits
   * until element has got the {@code value}. Checks text by {@link WebElement#getAttribute(String)}
   *
   * @param elementLocator
   * @param value
   */
  public void setValue(By elementLocator, String value) {
    waitVisibility(elementLocator).clear();
    waitValue(elementLocator, "");
    sendKeysTo(elementLocator, value);
    waitValue(elementLocator, value);
  }

  /**
   * Ensures {@code element} has got {@code value} in next steps: ensures element is visible and has
   * empty value, sets {@code value} by sending corresponding keys to {@code element} and waits
   * until element has got the {@code value}. Checks text by {@link WebElement#getAttribute(String)}
   *
   * @param webElement
   * @param value
   */
  public void setValue(WebElement webElement, String value) {
    waitVisibility(webElement).clear();
    waitValue(webElement, "");
    sendKeysTo(webElement, value);
    waitValue(webElement, value);
  }

  /**
   * Ensures {@code element} has got {@code value} in next steps: ensures element is visible and has
   * empty value, sets {@code value} by sending corresponding keys to {@code element} and waits
   * until element has got the {@code value}. Checks text by {@link WebElement#getText()}
   *
   * @param elementLocator
   * @param value
   */
  public void setText(By elementLocator, String value) {
    waitVisibility(elementLocator).clear();
    waitText(elementLocator, "");
    sendKeysTo(elementLocator, value);
    waitText(elementLocator, value);
  }

  /**
   * Ensures {@code element} has got {@code value} in next steps: ensures element is visible and has
   * empty value, sets {@code value} by sending corresponding keys to {@code element} and waits
   * until element has got the {@code value}. Checks text by {@link WebElement#getText()}
   *
   * @param webElement
   * @param value
   */
  public void setText(WebElement webElement, String value) {
    waitVisibility(webElement).clear();
    waitText(webElement, "");
    sendKeysTo(webElement, value);
    waitText(webElement, value);
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be visible
   *
   * @param elementLocator
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitVisibility(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be visible
   *
   * @param elementLocator
   * @return found element
   */
  public WebElement waitVisibility(By elementLocator) {
    return waitVisibility(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until provided {@link WebElement} be visible
   *
   * @param webElement
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitVisibility(WebElement webElement, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOf(webElement));
  }

  /**
   * Waits until provided {@link WebElement} be visible
   *
   * @param webElement
   * @return found element
   */
  public WebElement waitVisibility(WebElement webElement) {
    return waitVisibility(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be attached to DOM it does not
   * mean that element is visible
   *
   * @param elementLocator
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitPresence(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(presenceOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided locator {@link By} be attached to DOM it does not
   * mean that element is visible
   *
   * @param elementLocator
   * @return found element
   */
  public WebElement waitPresence(By elementLocator) {
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
  public void sendKeysTo(By elementLocator, String text) {
    waitVisibility(elementLocator).sendKeys(text);
  }

  /**
   * Type text in the provided {@link WebElement}
   *
   * @param webElement
   * @param text
   */
  public void sendKeysTo(WebElement webElement, String text) {
    waitVisibility(webElement).sendKeys(text);
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and get text
   *
   * @param elementLocator
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(By elementLocator) {
    return waitVisibility(elementLocator).getAttribute("value");
  }

  /**
   * Waits visibility of provided {@link WebElement} and get text
   *
   * @param webElement
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(WebElement webElement) {
    return waitVisibility(webElement).getAttribute("value");
  }

  /**
   * Waits visibility of {@link WebElement} with provided locator {@link By} and get text
   *
   * @param fieldLocator
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(By fieldLocator) {
    return waitVisibility(fieldLocator).getText();
  }

  /**
   * Waits visibility of provided {@link WebElement} and get text
   *
   * @param webElement
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(WebElement webElement) {
    return waitVisibility(webElement).getText();
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param elementLocator
   * @param expectedValue
   * @param timeout waiting time in seconds
   */
  public void waitValue(By elementLocator, String expectedValue, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetValue(elementLocator).equals(expectedValue));
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param elementLocator
   * @param expectedValue
   */
  public void waitValue(By elementLocator, String expectedValue) {
    waitValue(elementLocator, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getAttribute(String)} be equivalent to provided
   * text
   *
   * @param webElement
   * @param expectedValue
   * @param timeout waiting time in seconds
   */
  public void waitValue(WebElement webElement, String expectedValue, int timeout) {
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
  public void waitValue(WebElement webElement, String expectedValue) {
    waitValue(webElement, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param elementLocator
   * @param expectedText
   * @param timeout waiting time in seconds
   */
  public void waitText(By elementLocator, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibilityAndGetText(elementLocator).equals(expectedText));
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param elementLocator
   * @param expectedText
   */
  public void waitText(By elementLocator, String expectedText) {
    waitText(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until text extracted by {@link WebElement#getText()} be equivalent to provided text
   *
   * @param webElement
   * @param expectedText
   * @param timeout waiting time in seconds
   */
  public void waitText(WebElement webElement, String expectedText, int timeout) {
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
  public void waitText(WebElement webElement, String expectedText) {
    waitText(webElement, expectedText, DEFAULT_TIMEOUT);
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
  public void moveCursorToAndDoubleClick(By elementLocator) {
    moveCursorTo(elementLocator);
    waitVisibility(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to provided {@link WebElement} and clicks twice on it by {@link
   * org.openqa.selenium.interactions.Action}. Gets rid of {@link
   * org.openqa.selenium.StaleElementReferenceException} by using {@link Actions#doubleClick()}.
   *
   * @param webElement
   */
  public void moveCursorToAndDoubleClick(WebElement webElement) {
    moveCursorTo(webElement);
    waitVisibility(webElement);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to {@link WebElement} with provided locator {@link By} and clicks once on it by
   * {@link org.openqa.selenium.interactions.Action}
   *
   * @param elementLocator
   */
  public void moveCursorToAndClick(By elementLocator) {
    moveCursorTo(elementLocator);
    waitVisibility(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to provided {@link WebElement} and clicks once on it by {@link
   * org.openqa.selenium.interactions.Action}
   *
   * @param webElement
   */
  public void moveCursorToAndClick(WebElement webElement) {
    moveCursorTo(webElement);
    waitVisibility(webElement);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to WebElement with provided locator which attached to DOM but it does not mean
   * that element is visible
   *
   * @param elementLocator {@link By}
   */
  public void moveCursorTo(By elementLocator) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(waitPresence(elementLocator))
        .perform();
  }

  /**
   * Moves cursor to provided WebElement which attached to DOM but it does not mean that element is
   * visible
   *
   * @param webElement
   */
  public void moveCursorTo(WebElement webElement) {
    actionsFactory.createAction(seleniumWebDriver).moveToElement(webElement).perform();
  }

  /**
   * Checks visibility state of {@link WebElement} with provided locator {@link By}
   *
   * @param elementLocator
   * @return state of element visibility
   */
  public boolean isVisibleDuringTimeout(By elementLocator) {
    try {
      webDriverWaitFactory
          .get(ATTACHING_ELEM_TO_DOM_SEC)
          .until(visibilityOfElementLocated(elementLocator));
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
  public boolean isVisibleDuringTimeout(WebElement webElement) {
    try {
      webDriverWaitFactory.get(ATTACHING_ELEM_TO_DOM_SEC).until(visibilityOf(webElement));
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }
}
