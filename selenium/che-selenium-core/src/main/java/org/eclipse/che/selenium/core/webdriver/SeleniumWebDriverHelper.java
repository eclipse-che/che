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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Ihor Okhrimenko */
@Singleton
public class SeleniumWebDriverHelper {
  protected final int DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;
  protected final SeleniumWebDriver seleniumWebDriver;
  protected final WebDriverWaitFactory webDriverWaitFactory;
  protected final ActionsFactory actionsFactory;
  protected final Logger LOG = LoggerFactory.getLogger(SeleniumWebDriverHelper.class);

  @Inject
  public SeleniumWebDriverHelper(
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
   * Waits during {@code timeout}, until {@link WebElement} with provided {@code elementLocator} is
   * visible
   *
   * @param elementLocator locator of element which should be checked
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitVisibility(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided {@code elementLocator} is visible.
   *
   * @param elementLocator locator of element which should be checked
   * @return found element
   */
  public WebElement waitVisibility(By elementLocator) {
    return waitVisibility(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until provided {@code webElement} is visible.
   *
   * @param webElement element which should be checked
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitVisibility(WebElement webElement, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOf(webElement));
  }

  /**
   * Waits until provided {@code webElement} is visible.
   *
   * @param webElement element which should be checked
   * @return found element
   */
  public WebElement waitVisibility(WebElement webElement) {
    return waitVisibility(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until all of {@link WebElement}, which are found by {@code elementsLocator}, are visible,
   * and gets them;
   *
   * @param elementsLocator common locator for multiple elements
   * @return all elements which found by specified {@code elementLocator},
   *     <p>throws {@link org.openqa.selenium.TimeoutException} if at least one of the found
   *     elements is not visible after timeout;
   */
  public List<WebElement> waitVisibilityOfAllElements(By elementsLocator) {
    return waitVisibilityOfAllElements(elementsLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during specified {@code timeout} until all of {@link WebElement}, which are found by
   * {@code elementsLocator}, are visible and gets them.
   *
   * @param elementsLocator common locator for multiple elements
   * @param timeout waiting time in seconds
   * @return all elements which found by specified {@code elementLocator},
   *     <p>throws {@link org.openqa.selenium.TimeoutException} if at least one of the found
   *     elements is not visible after timeout;
   */
  public List<WebElement> waitVisibilityOfAllElements(By elementsLocator, int timeout) {
    return webDriverWaitFactory
        .get(timeout)
        .until(visibilityOfAllElementsLocatedBy(elementsLocator));
  }

  /**
   * Waits until all of specified {@code elements}, are visible.
   *
   * @param elements web elements which should check
   * @return provided {@code elements},
   *     <p>throws {@link org.openqa.selenium.TimeoutException} if at least one of the specified
   *     {@code elements} is not visible after timeout;
   */
  public List<WebElement> waitAllVisibility(List<WebElement> elements) {
    return waitAllVisibility(elements, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during specified {@code timeout} until all of provided {@code elements}, are visible.
   *
   * @param elements web elements which should check
   * @param timeout waiting time in seconds
   * @return provided {@code elements},
   *     <p>throws {@link org.openqa.selenium.TimeoutException} if at least one of the specified
   *     {@code elements} is not visible after timeout;
   */
  public List<WebElement> waitAllVisibility(List<WebElement> elements, int timeout) {
    return webDriverWaitFactory.get(timeout).until(visibilityOfAllElements(elements));
  }

  /**
   * Waits visibility and gets {@link WebElement} for all of the {@code elementsLocators}
   *
   * @param elementsLocators locators for all elements which should be found
   * @return all found elements,
   *     <p>throws {@link org.openqa.selenium.TimeoutException} if at least one of the specified
   *     {@code elements} is not visible after timeout;
   */
  public List<WebElement> waitAllVisibilityBy(List<By> elementsLocators) {
    return waitAllVisibilityBy(elementsLocators, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility during timeout and gets {@link WebElement} for all of the {@code
   * elementsLocators}
   *
   * @param elementsLocators locators for all elements which should be found
   * @param timeout waiting timeout in seconds
   * @return all found elements
   */
  public List<WebElement> waitAllVisibilityBy(List<By> elementsLocators, int timeout) {
    return elementsLocators
        .stream()
        .map((elementLocator -> waitVisibility(elementLocator, timeout)))
        .collect(toList());
  }

  /**
   * Waits during {@code timeout}, until {@link WebElement} with provided {@code elementLocator} is
   * invisible.
   *
   * @param elementLocator locator of element which should be checked
   * @param timeout waiting time in seconds
   * @throw {@link org.openqa.selenium.TimeoutException} - if visible during timeout.
   */
  public void waitInvisibility(By elementLocator, int timeout) {
    webDriverWaitFactory.get(timeout).until(invisibilityOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided {@code elementLocator} is invisible.
   *
   * @param elementLocator locator of element which should be checked
   * @throw {@link org.openqa.selenium.TimeoutException} - if visible during timeout.
   */
  public void waitInvisibility(By elementLocator) {
    waitInvisibility(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until provided {@code webElement} is invisible.
   *
   * @param webElement element which should be checked
   * @param timeout waiting time in seconds
   * @throw {@link org.openqa.selenium.TimeoutException} - if visible during timeout.
   */
  public void waitInvisibility(WebElement webElement, int timeout) {
    webDriverWaitFactory.get(timeout).until(invisibilityOfAllElements(asList(webElement)));
  }

  /**
   * Waits until provided {@code webElement} is invisible.
   *
   * @param webElement element which should be checked
   * @throw {@link org.openqa.selenium.TimeoutException} - if visible during timeout.
   */
  public void waitInvisibility(WebElement webElement) {
    waitInvisibility(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until each item from specified {@code elements} be invisible.
   *
   * @param elements web elements which should be checked
   */
  public void waitAllInvisibility(List<WebElement> elements) {
    webDriverWaitFactory
        .get(DEFAULT_TIMEOUT)
        .until(ExpectedConditions.invisibilityOfAllElements(elements));
  }

  /**
   * Waits during {@code timeout} until each item from specified {@code elements} be invisible.
   *
   * @param elements web elements which should be checked
   * @param timeout waiting timeout in seconds
   */
  public void waitAllInvisibility(List<WebElement> elements, int timeout) {
    webDriverWaitFactory.get(timeout).until(ExpectedConditions.invisibilityOfAllElements(elements));
  }

  /**
   * Waits until each {@link WebElement}, which defined by locator from {@code elementsLocators}, be
   * invisible.
   *
   * @param elementsLocators locators of each elements which should be checked
   */
  public void waitAllInvisibilityBy(List<By> elementsLocators) {
    waitAllInvisibilityBy(elementsLocators, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until each {@link WebElement}, which defined by locator from {@code elementsLocators}, be
   * invisible.
   *
   * @param elementsLocators locators of each elements which should be checked
   * @param timeout waiting timeout in seconds
   */
  public void waitAllInvisibilityBy(List<By> elementsLocators, int timeout) {
    elementsLocators.forEach(element -> waitInvisibility(element, timeout));
  }

  /**
   * Waits during {@code timeout} until {@link WebElement} with provided {@code elementLocator} is
   * attached to DOM.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param elementLocator locator of element which should be checked
   * @param timeout waiting time in seconds
   * @return found element
   */
  public WebElement waitPresence(By elementLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(presenceOfElementLocated(elementLocator));
  }

  /**
   * Waits until {@link WebElement} with provided {@code elementLocator} is attached to DOM.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param elementLocator
   * @return found element
   */
  public WebElement waitPresence(By elementLocator) {
    return waitPresence(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until all {@link WebElement} with provided {@code elementsLocator} are attached to DOM.
   *
   * <p>Note! It does not mean that elements are visible.
   *
   * @param elementsLocator common locator for multiple elements
   * @return all found elements
   */
  public List<WebElement> waitPresenceOfAllElements(By elementsLocator) {
    return waitPresenceOfAllElements(elementsLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until all {@link WebElement} with provided {@code elementsLocator}
   * is attached to DOM.
   *
   * <p>Note! It does not mean that elements are visible.
   *
   * @param elementsLocator common locator for multiple elements
   * @param timeout waiting timeout in seconds
   * @return all found elements
   */
  public List<WebElement> waitPresenceOfAllElements(By elementsLocator, int timeout) {
    return webDriverWaitFactory.get(timeout).until(presenceOfAllElementsLocatedBy(elementsLocator));
  }

  /**
   * Waits until {@link WebElement} with specified {@code elementLocator} is visible, and types text
   * in it.
   *
   * @param elementLocator locator of element in which {@code text} should be sent
   * @param text text for sending
   */
  public void sendKeysTo(By elementLocator, String text) {
    waitVisibility(elementLocator).sendKeys(text);
  }

  /**
   * Waits until {@code webElement} is visible, and types text in it.
   *
   * @param webElement element in which {@code text} should be sent
   * @param text text for sending
   */
  public void sendKeysTo(WebElement webElement, String text) {
    waitVisibility(webElement).sendKeys(text);
  }

  /**
   * Types {@code text}
   *
   * @param text text for sending
   */
  public void sendKeys(String text) {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
  }

  /**
   * Waits visibility of {@link WebElement} with provided {@code elementLocator} and gets text.
   *
   * @param elementLocator locator of element from which text should be got
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(By elementLocator) {
    return waitVisibility(elementLocator).getAttribute("value");
  }

  /**
   * Waits visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element, text from which should be got
   * @return element text by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(WebElement webElement) {
    return waitVisibility(webElement).getAttribute("value");
  }

  /**
   * Waits visibility of {@link WebElement} with provided {@code elementLocator} and gets text.
   *
   * @param elementLocator element from which text should be got
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(By elementLocator) {
    return waitVisibility(elementLocator).getText();
  }

  /**
   * Waits visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element from which text should be got
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(WebElement webElement) {
    return waitVisibility(webElement).getText();
  }

  /**
   * Waits during {@code timeout} until text extracted from {@link WebElement} with specified {@code
   * elementLocator} by {@link WebElement#getAttribute(String)} is equivalent to provided {@code
   * expectedValue}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedValue expected text which should be present in the element
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
   * Waits until text extracted from {@link WebElement} with specified {@code elementLocator} by
   * {@link WebElement#getAttribute(String)} is equivalent to provided {@code expectedValue}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedValue expected text which should be present in the element
   */
  public void waitValue(By elementLocator, String expectedValue) {
    waitValue(elementLocator, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until text extracted from specified {@code webElement} by {@link
   * WebElement#getAttribute(String)} is equivalent to provided {@code expectedValue}.
   *
   * @param webElement element in which text should be checked
   * @param expectedValue expected text which should be present in the element
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
   * Waits until text extracted from specified {@code webElement} by {@link
   * WebElement#getAttribute(String)} is equivalent to provided {@code expectedValue}.
   *
   * @param webElement element in which text should be checked
   * @param expectedValue expected text which should be present in the element
   */
  public void waitValue(WebElement webElement, String expectedValue) {
    waitValue(webElement, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} contains the defined {@code
   * expectedText}.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   * @param timeout waiting time in seconds
   */
  public void waitValuePresence(WebElement element, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibility(element).getAttribute("value").contains(expectedText));
  }

  /**
   * Waits until specified {@code element} contains the defined {@code expectedText}.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   */
  public void waitValuePresence(WebElement element, String expectedText) {
    waitValuePresence(element, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until {@link WebElement} which defined by {@code elementLocator}
   * contains the specified {@code expectedText}.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param elementLocator locator of the element which should be checked
   * @param expectedText text which should be present
   * @param timeout waiting time in seconds
   */
  public void waitValuePresence(By elementLocator, String expectedText, int timeout) {
    waitValuePresence(waitVisibility(elementLocator, timeout), expectedText, timeout);
  }

  /**
   * Waits until {@link WebElement} which defined by {@code elementLocator} contains the specified
   * {@code expectedText}.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param elementLocator locator of the element which should be checked
   * @param expectedText text which should be present
   */
  public void waitValuePresence(By elementLocator, String expectedText) {
    waitValuePresence(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until text extracted from {@link WebElement} with specified {@code
   * elementLocator} by {@link WebElement#getText()} is equivalent to provided {@code expectedText}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedText expected text which should be present in the element
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
   * Waits until text extracted from {@link WebElement} with specified {@code elementLocator} by
   * {@link WebElement#getText()} is equivalent to provided {@code expectedText}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedText expected text which should be present in the element
   */
  public void waitText(By elementLocator, String expectedText) {
    waitText(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until text extracted from specified {@code webElement} by {@link
   * WebElement#getText()} is equivalent to provided {@code expectedText}.
   *
   * @param webElement element in which text should be checked
   * @param expectedText expected text which should be present in the element
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
   * Waits until text extracted from {@code webElement} by {@link WebElement#getText()} is
   * equivalent to provided {@code expectedText}.
   *
   * @param webElement element in which text should be checked
   * @param expectedText expected text which should be present in the element
   */
  public void waitText(WebElement webElement, String expectedText) {
    waitText(webElement, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} contains the specified {@code
   * expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   * @param timeout waiting time in seconds
   */
  public void waitTextPresence(WebElement element, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> waitVisibility(element).getText().contains(expectedText));
  }

  /**
   * Waits until specified {@code element} contains the specified {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   */
  public void waitTextPresence(WebElement element, String expectedText) {
    waitTextPresence(element, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until {@link WebElement} which defined by {@code elementLocator}
   * contains the specified {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param elementLocator locator of the element which should be checked
   * @param expectedText text which should be presented
   * @param timeout waiting time in seconds
   */
  public void waitTextPresence(By elementLocator, String expectedText, int timeout) {
    waitTextPresence(waitVisibility(elementLocator, timeout), expectedText, timeout);
  }

  /**
   * Waits until {@link WebElement} which defined by {@code elementLocator} contains the specified
   * {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param elementLocator locator of the element which should be checked
   * @param expectedText text which should be presented
   */
  public void waitTextPresence(By elementLocator, String expectedText) {
    waitTextPresence(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} does not contain the specified
   * {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should not be presented
   * @param timeout waiting time in seconds
   */
  public void waitTextIsNotPresented(WebElement element, String expectedText, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> !(waitVisibilityAndGetText(element).contains(expectedText)));
  }

  /**
   * Waits until {@link WebElement} which defined by {@code element} does not contain the specified
   * {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should not be presented
   */
  public void waitTextIsNotPresented(WebElement element, String expectedText) {
    waitTextIsNotPresented(element, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} of visibility the {@link WebElement} with provided {@code
   * elementLocator} and clicks once on it by {@link WebElement#click()}.
   *
   * @param elementLocator locator of element which should be clicked
   * @param timeout waiting time in seconds
   */
  public void waitAndClick(By elementLocator, int timeout) {
    webDriverWaitFactory.get(timeout).until(visibilityOfElementLocated(elementLocator)).click();
  }

  /**
   * Waits visibility of {@link WebElement} with provided {@code elementLocator} and clicks once on
   * it by {@link WebElement#click()}.
   *
   * @param elementLocator locator of element which should be clicked
   */
  public void waitAndClick(By elementLocator) {
    waitAndClick(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility of provided {@code webElement} and clicks once on it by {@link
   * WebElement#click()}.
   *
   * @param webElement element which should be clicked
   * @param timeout waiting time in seconds
   */
  public void waitAndClick(WebElement webElement, int timeout) {
    webDriverWaitFactory.get(timeout).until(visibilityOf(webElement)).click();
  }

  /**
   * Waits visibility of provided {@code webElement} and clicks once on it by {@link
   * WebElement#click()}.
   *
   * @param webElement element which should be clicked
   */
  public void waitAndClick(WebElement webElement) {
    waitAndClick(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Moves cursor to {@link WebElement} with provided {@code elementLocator}, and clicks twice on it
   * by {@link org.openqa.selenium.interactions.Action}. Gets rid of {@link
   * org.openqa.selenium.StaleElementReferenceException} by using {@link Actions#doubleClick()}.
   *
   * @param elementLocator locator of element which should be clicked
   */
  public void moveCursorToAndDoubleClick(By elementLocator) {
    moveCursorTo(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to provided {@code webElement}, and clicks twice on it by {@link
   * org.openqa.selenium.interactions.Action}. Gets rid of {@link
   * org.openqa.selenium.StaleElementReferenceException} by using {@link Actions#doubleClick()}.
   *
   * @param webElement element which should be clicked
   */
  public void moveCursorToAndDoubleClick(WebElement webElement) {
    moveCursorTo(webElement);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /** Performs mouse's left button double click . */
  public void doubleClick() {
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Moves cursor to {@link WebElement} with provided {@code elementLocator} and clicks once on it
   * by {@link org.openqa.selenium.interactions.Action}.
   *
   * @param elementLocator locator of element which should be clicked
   */
  public void moveCursorToAndClick(By elementLocator) {
    moveCursorTo(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to provided {@code webElement}, and clicks once on it by {@link
   * org.openqa.selenium.interactions.Action}.
   *
   * @param webElement element which should be clicked
   */
  public void moveCursorToAndClick(WebElement webElement) {
    moveCursorTo(webElement);
    actionsFactory.createAction(seleniumWebDriver).click().perform();
  }

  /**
   * Moves cursor to {@link WebElement} with provided {@code elementLocator} which attached to DOM.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param elementLocator element to which cursor should be moved
   */
  public void moveCursorTo(By elementLocator) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(waitPresence(elementLocator))
        .perform();
  }

  /**
   * Moves cursor to provided {@code webElement} which attached to DOM.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param webElement element to which cursor should be moved
   */
  public void moveCursorTo(WebElement webElement) {
    actionsFactory.createAction(seleniumWebDriver).moveToElement(webElement).perform();
  }

  /**
   * Moves cursor to {@link WebElement} with provided {@code elementLocator} which attached to DOM
   * and performs mouse's right button click.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param elementLocator locator of {@link WebElement} for which context menu will be opened.
   */
  public void moveCursorToAndContextClick(By elementLocator) {
    moveCursorTo(elementLocator);
    actionsFactory.createAction(seleniumWebDriver).contextClick().perform();
  }

  /**
   * Moves cursor to specified {@code webElement} which attached to DOM and performs mouse's right
   * button click.
   *
   * <p>Note! It does not mean that element is visible.
   *
   * @param element {@link WebElement} for which context menu will be opened.
   */
  public void moveCursorToAndContextClick(WebElement element) {
    moveCursorTo(element);
    actionsFactory.createAction(seleniumWebDriver).contextClick().perform();
  }

  /**
   * Checks visibility state of {@link WebElement} with provided {@code elementLocator}.
   *
   * @param elementLocator locator of element which should be checked
   * @return state of element visibility
   */
  public boolean isVisible(By elementLocator) {
    try {
      return seleniumWebDriver.findElement(elementLocator).isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  /**
   * Checks visibility state of provided {@code webElement}.
   *
   * @param webElement element which should be checked
   * @return state of element visibility
   */
  public boolean isVisible(WebElement webElement) {
    try {
      return webElement.isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  /**
   * Waits during {@code timeout} until frame which defined by {@code frameLocator} is available and
   * switches to it.
   *
   * @param frameLocator locator of the frame to which should be switched
   * @param timeout waiting time in seconds
   */
  public void waitAndSwitchToFrame(By frameLocator, int timeout) {
    webDriverWaitFactory.get(timeout).until(frameToBeAvailableAndSwitchToIt(frameLocator));
  }

  /**
   * Waits until frame which defined by {@code frameLocator} is available and switches to it.
   *
   * @param frameLocator locator of the frame to which should be switched
   */
  public void waitAndSwitchToFrame(By frameLocator) {
    waitAndSwitchToFrame(frameLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until {@code frame} is available and switches to it.
   *
   * @param frame web element which defines frame
   * @param timeout waiting time in seconds
   */
  public void waitAndSwitchToFrame(WebElement frame, int timeout) {
    webDriverWaitFactory.get(timeout).until(frameToBeAvailableAndSwitchToIt(frame));
  }

  /**
   * Waits until {@code frame} is available and switches to it.
   *
   * @param frame web element which defines frame
   */
  public void waitAndSwitchToFrame(WebElement frame) {
    waitAndSwitchToFrame(frame, DEFAULT_TIMEOUT);
  }

  /**
   * Creates {@link Actions} by using {@link ActionsFactory}.
   *
   * @param seleniumWebDriver webDriver by which actions factory should be initialized
   * @return created {@link Actions}
   */
  public Actions getAction(SeleniumWebDriver seleniumWebDriver) {
    return actionsFactory.createAction(seleniumWebDriver);
  }

  /**
   * Creates {@link Actions} by using {@link ActionsFactory}.
   *
   * @return created {@link Actions}
   */
  public Actions getAction() {
    return getAction(this.seleniumWebDriver);
  }

  /** Switches to IDE frame and waits for Project Explorer is available. */
  public String switchToIdeFrameAndWaitAvailability() {
    return switchToIdeFrameAndWaitAvailability(APPLICATION_START_TIMEOUT_SEC);
  }

  /**
   * Switches to IDE frame and waits during {@code timeout} for Project Explorer is available.
   *
   * @param timeout waiting time in seconds
   */
  public String switchToIdeFrameAndWaitAvailability(int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  waitAndSwitchToFrame(By.id("ide-application-iframe"), PREPARING_WS_TIMEOUT_SEC);
                  if (isVisible(By.id("gwt-debug-projectTree"))) {
                    return true;
                  }

                  seleniumWebDriver.switchTo().parentFrame();
                  return false;
                });

    return seleniumWebDriver.getWindowHandle();
  }

  /** Waits while in a browser appears more than one window */
  public void waitOpenedSomeWin() {
    webDriverWaitFactory
        .get(WIDGET_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                input -> {
                  Set<String> driverWindows = seleniumWebDriver.getWindowHandles();
                  return (driverWindows.size() > 1);
                });
  }

  /**
   * Switches to next browser window (this means that if opened 2 windows, and we are in the window
   * 1, we will be switched into the window 2)
   *
   * @param windowHandlerToSwitchFrom
   */
  public void switchToNextWindow(String windowHandlerToSwitchFrom) {
    waitOpenedSomeWin();
    for (String handle : seleniumWebDriver.getWindowHandles()) {
      if (!windowHandlerToSwitchFrom.equals(handle)) {
        seleniumWebDriver.switchTo().window(handle);
        break;
      }
    }
  }
}
