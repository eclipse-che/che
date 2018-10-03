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

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.F12;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementSelectionStateToBe;
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** @author Ihor Okhrimenko */
@Singleton
public class SeleniumWebDriverHelper {
  private final int DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWaitFactory webDriverWaitFactory;
  private final ActionsFactory actionsFactory;
  private final UploadUtil uploadUtil;

  @Inject
  public SeleniumWebDriverHelper(
      SeleniumWebDriver seleniumWebDriver,
      WebDriverWaitFactory webDriverWaitFactory,
      ActionsFactory actionsFactory,
      UploadUtil uploadFileUtil) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.webDriverWaitFactory = webDriverWaitFactory;
    this.actionsFactory = actionsFactory;
    this.uploadUtil = uploadFileUtil;
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
    waitValueEqualsTo(elementLocator, "");
    waitAndSendKeysTo(elementLocator, value);
    waitValueEqualsTo(elementLocator, value);
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
    waitValueEqualsTo(webElement, "");
    waitAndSendKeysTo(webElement, value);
    waitValueEqualsTo(webElement, value);
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
    waitTextEqualsTo(elementLocator, "");
    waitAndSendKeysTo(elementLocator, value);
    waitTextEqualsTo(elementLocator, value);
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
    waitTextEqualsTo(webElement, "");
    waitAndSendKeysTo(webElement, value);
    waitTextEqualsTo(webElement, value);
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
  public void waitAndSendKeysTo(By elementLocator, String text) {
    waitAndSendKeysTo(elementLocator, text, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until {@link WebElement} with specified {@code elementLocator} is
   * visible, and types text in it.
   *
   * @param elementLocator locator of element in which {@code text} should be sent
   * @param text text for sending
   * @param timeout waiting timeout in seconds
   */
  public void waitAndSendKeysTo(By elementLocator, String text, int timeout) {
    waitVisibility(elementLocator, timeout).sendKeys(text);
  }

  /**
   * Waits until {@code webElement} is visible, and types text in it.
   *
   * @param webElement element in which {@code text} should be sent
   * @param text text for sending
   */
  public void waitAndSendKeysTo(WebElement webElement, String text) {
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
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(By elementLocator) {
    return waitVisibilityAndGetValue(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} visibility of {@link WebElement} with specified {@code
   * elementLocator} and gets text.
   *
   * @param elementLocator locator of element from which text should be got
   * @param timeout waiting time in seconds
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(By elementLocator, int timeout) {
    return waitVisibilityAndGetAttribute(elementLocator, "value", timeout);
  }

  /**
   * Waits visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element, text from which should be got
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(WebElement webElement) {
    return waitVisibilityAndGetValue(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element, text from which should be got
   * @param timeout waiting time in seconds
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetValue(WebElement webElement, int timeout) {
    return waitVisibilityAndGetAttribute(webElement, "value", timeout);
  }

  /**
   * Waits visibility of {@link WebElement} with provided {@code elementLocator} and gets text from
   * its {@code attribute}.
   *
   * @param elementLocator locator of element from which attribute should be got
   * @param attribute name of element attribute
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetAttribute(By elementLocator, String attribute) {
    return waitVisibilityAndGetAttribute(elementLocator, attribute, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility during {@code timeout} of {@link WebElement} with provided {@code
   * elementLocator} and gets text from its {@code attribute}.
   *
   * @param elementLocator locator of element from which attribute should be got
   * @param attribute name of element attribute
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetAttribute(By elementLocator, String attribute, int timeout) {
    return waitVisibility(elementLocator, timeout).getAttribute(attribute);
  }

  /**
   * Waits visibility of specified {@code element} and gets text from its {@code attribute}.
   *
   * @param element element from which attribute should be got
   * @param attribute name of element attribute
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetAttribute(WebElement element, String attribute) {
    return waitVisibilityAndGetAttribute(element, attribute, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility during {@code timeout} of specified {@code element} and gets text from its
   * {@code attribute}.
   *
   * @param element element from which attribute should be got
   * @param attribute name of element attribute
   * @return text which extracted from element by {@link WebElement#getAttribute(String)}
   */
  public String waitVisibilityAndGetAttribute(WebElement element, String attribute, int timeout) {
    return waitVisibility(element, timeout).getAttribute(attribute);
  }

  /**
   * Waits visibility of {@link WebElement} with provided {@code elementLocator} and gets text.
   *
   * @param elementLocator element from which text should be got
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(By elementLocator) {
    return waitVisibilityAndGetText(elementLocator, DEFAULT_TIMEOUT);
  }

  /**
   * Waits visibility during {@code timeout} of {@link WebElement} with provided {@code
   * elementLocator} and gets text.
   *
   * @param elementLocator element from which text should be got
   * @param timeout waiting time in seconds
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(By elementLocator, int timeout) {
    return waitVisibility(elementLocator, timeout).getText();
  }

  /**
   * Waits visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element from which text should be got
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(WebElement webElement) {
    return waitVisibilityAndGetText(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} visibility of provided {@code webElement} and gets text.
   *
   * @param webElement element from which text should be got
   * @param timeout waiting time in seconds
   * @return element text by {@link WebElement#getText()}
   */
  public String waitVisibilityAndGetText(WebElement webElement, int timeout) {
    return waitVisibility(webElement, timeout).getText();
  }

  /**
   * Waits during {@code timeout} until value extracted from {@link WebElement} with specified
   * {@code locator} by {@link WebElement#getAttribute(String)} equals to provided {@code expected}
   * one.
   *
   * @param locator locator of element in which value should be checked
   * @param expected expected value which should be present in the element
   * @param timeout waiting time in seconds
   */
  public void waitValueEqualsTo(By locator, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(timeout, () -> format("\nexpected:\n'%s'\nbut was:\n'%s'\n", expected, actual[0]))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetValue(locator, timeout);
                  return actual[0].contains(expected);
                });
  }

  /**
   * Waits until text extracted from {@link WebElement} with specified {@code elementLocator} by
   * {@link WebElement#getAttribute(String)} equals to provided {@code expectedValue}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedValue expected text which should be present in the element
   */
  public void waitValueEqualsTo(By elementLocator, String expectedValue) {
    waitValueEqualsTo(elementLocator, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until value extracted from specified {@code element} by {@link
   * WebElement#getAttribute(String)} equals to provided {@code expected} one.
   *
   * @param element element in which text should be checked
   * @param expected expected value which should be present in the element
   * @param timeout waiting time in seconds
   */
  public void waitValueEqualsTo(WebElement element, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(timeout, () -> format("\nexpected:\n'%s'\nbut was:\n'%s'\n", expected, actual[0]))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetValue(element, timeout);
                  return actual[0].equals(expected);
                });
  }

  /**
   * Waits until text extracted from specified {@code element} by {@link
   * WebElement#getAttribute(String)} equals to provided {@code expectedValue}.
   *
   * @param element element in which text should be checked
   * @param expectedValue expected text which should be present in the element
   */
  public void waitValueEqualsTo(WebElement element, String expectedValue) {
    waitValueEqualsTo(element, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} contains the defined {@code
   * expected} value.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param element element which should be checked
   * @param expected value which should be presented
   * @param timeout waiting time in seconds
   */
  public void waitValueContains(WebElement element, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(
            timeout,
            () -> format("\nactual value:\n'%s'\ndidn't contain:\n'%s'\n", actual[0], expected))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetValue(element, timeout);
                  return actual[0].contains(expected);
                });
  }

  /**
   * Waits until specified {@code element} contains the defined {@code expectedText}.
   *
   * <p>Note! The text is extracted by {@link WebElement#getAttribute(String)} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   */
  public void waitValueContains(WebElement element, String expectedText) {
    waitValueContains(element, expectedText, DEFAULT_TIMEOUT);
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
  public void waitValueContains(By elementLocator, String expectedText, int timeout) {
    waitValueContains(waitVisibility(elementLocator, timeout), expectedText, timeout);
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
  public void waitValueContains(By elementLocator, String expectedText) {
    waitValueContains(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until text extracted from {@link WebElement} with specified {@code
   * locator} by {@link WebElement#getText()} equals to provided {@code expected} one.
   *
   * @param locator locator of element in which text should be checked
   * @param expected expected text which should be present in the element
   * @param timeout waiting time in seconds
   */
  public void waitTextEqualsTo(By locator, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(timeout, () -> format("\nexpected:\n'%s'\nbut was:\n'%s'\n", expected, actual[0]))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetText(locator, timeout);
                  return actual[0].equals(expected);
                });
  }

  /**
   * Waits until text extracted from {@link WebElement} with specified {@code elementLocator} by
   * {@link WebElement#getText()} equals to provided {@code expectedText}.
   *
   * @param elementLocator locator of element in which text should be checked
   * @param expectedText expected text which should be present in the element
   */
  public void waitTextEqualsTo(By elementLocator, String expectedText) {
    waitTextEqualsTo(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until text extracted from specified {@code element} by {@link
   * WebElement#getText()} equals to provided {@code expected} one.
   *
   * @param element element in which text should be checked
   * @param expected expected text which element text should be equal to
   * @param timeout waiting time in seconds
   */
  public void waitTextEqualsTo(WebElement element, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(timeout, () -> format("\nexpected:\n'%s'\nbut was:\n'%s'\n", expected, actual[0]))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetText(element, timeout);
                  return actual[0].equals(expected);
                });
  }

  /**
   * Waits until text extracted from {@code webElement} by {@link WebElement#getText()} equals to
   * provided {@code expectedText}.
   *
   * @param webElement element in which text should be checked
   * @param expectedText expected text which element text should be equal to
   */
  public void waitTextEqualsTo(WebElement webElement, String expectedText) {
    waitTextEqualsTo(webElement, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} contains the specified {@code
   * expected} one.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expected text which should be presented
   * @param timeout waiting time in seconds
   */
  public void waitTextContains(WebElement element, String expected, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(
            timeout,
            () -> format("\nactual text:\n'%s'\ndidn't contain:\n'%s'\n", actual[0], expected))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetText(element, timeout);
                  return actual[0].contains(expected);
                });
  }

  /**
   * Waits until specified {@code element} contains the specified {@code expectedText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param expectedText text which should be presented
   */
  public void waitTextContains(WebElement element, String expectedText) {
    waitTextContains(element, expectedText, DEFAULT_TIMEOUT);
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
  public void waitTextContains(By elementLocator, String expectedText, int timeout) {
    waitTextContains(waitVisibility(elementLocator, timeout), expectedText, timeout);
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
  public void waitTextContains(By elementLocator, String expectedText) {
    waitTextContains(elementLocator, expectedText, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until specified {@code element} does not contain the specified
   * {@code absentText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param webElement element which should be checked
   * @param absentText text which should be absent
   * @param timeout waiting time in seconds
   */
  public void waitTextIsNotPresented(WebElement webElement, String absentText, int timeout) {
    String[] actual = new String[1];
    webDriverWaitFactory
        .get(
            timeout,
            () -> format("\nactual text:\n'%s'\ndid contain:\n'%s'\n", actual[0], absentText))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actual[0] = waitVisibilityAndGetText(webElement, timeout);
                  return !actual[0].contains(absentText);
                });
  }

  /**
   * Waits until {@link WebElement} which defined by {@code element} does not contain the specified
   * {@code absentText}.
   *
   * <p>Note! Text is extracted by {@link WebElement#getText()} method.
   *
   * @param element element which should be checked
   * @param absentText text which should be absent
   */
  public void waitTextIsNotPresented(WebElement element, String absentText) {
    waitTextIsNotPresented(element, absentText, DEFAULT_TIMEOUT);
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
   * Performs mouse's left button double click on element.
   *
   * @param elementLocator locator of element which should be double clicked
   */
  public void doubleClick(By elementLocator) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(seleniumWebDriver.findElement(elementLocator))
        .build()
        .perform();
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
   * Wait during {@code timeout} on visibility on element located at {@code elementLocator} and then
   * context click on it.
   *
   * @param elementLocator locator of element which should be context clicked on
   * @param timeout waiting time in seconds
   */
  public void waitAndContextClick(By elementLocator, int timeout) {
    waitVisibility(elementLocator, timeout);
    actionsFactory
        .createAction(seleniumWebDriver)
        .contextClick(seleniumWebDriver.findElement(elementLocator))
        .perform();
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
   * Determines whether or not this element {@code webElement} is enabled.
   *
   * @param webElement element which should be enabled
   * @return true if given element is enabled
   */
  public boolean waitVisibilityAndGetEnableState(WebElement webElement) {
    return waitVisibility(webElement).isEnabled();
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
            (ExpectedCondition<Boolean>) driver -> seleniumWebDriver.getWindowHandles().size() > 1);
  }

  /**
   * Waits during {@code timeout} until count of opened browser tabs are equals to {@code
   * expectedCount}.
   *
   * @param expectedCount count of the opened browsers tabs
   * @param timeout waiting time in seconds
   */
  public void waitWindowsCount(int expectedCount, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> seleniumWebDriver.getWindowHandles().size() == expectedCount);
  }

  /**
   * Waits until count of opened browser tabs are equals to {@code expectedCount}.
   *
   * @param expectedCount count of the opened browsers tabs
   */
  public void waitWindowsCount(int expectedCount) {
    waitWindowsCount(expectedCount, WIDGET_TIMEOUT_SEC);
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

  /**
   * Waits during {@code timeout} until attribute with specified {@code attributeName} has {@code
   * expectedValue}.
   *
   * @param elementLocator element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   * @param timeout waiting time
   */
  public void waitAttributeEqualsTo(
      By elementLocator, String attributeName, String expectedValue, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    waitVisibilityAndGetAttribute(elementLocator, attributeName, timeout)
                        .equals(expectedValue));
  }

  /**
   * Waits during {@code timeout} until attribute with specified {@code attributeName} has {@code
   * expected} value.
   *
   * @param element element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   * @param timeout waiting time in seconds
   */
  public void waitAttributeEqualsTo(
      WebElement element, String attributeName, String expectedValue, int timeout) {
    String[] actualValue = new String[1];
    webDriverWaitFactory
        .get(
            timeout,
            () ->
                format(
                    "\nexpected value of attribute '%s' was:\n'%s'\nbut actual was:\n'%s'\n",
                    attributeName, expectedValue, actualValue[0]))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actualValue[0] = waitVisibilityAndGetAttribute(element, attributeName, timeout);
                  return actualValue[0].contains(expectedValue);
                });
  }

  /**
   * Waits until attribute with specified {@code attributeName} has {@code expectedValue}.
   *
   * @param elementLocator element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   */
  public void waitAttributeEqualsTo(By elementLocator, String attributeName, String expectedValue) {
    waitAttributeEqualsTo(elementLocator, attributeName, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until attribute with specified {@code attributeName} has {@code expectedValue}.
   *
   * @param element element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   */
  public void waitAttributeEqualsTo(
      WebElement element, String attributeName, String expectedValue) {
    waitAttributeEqualsTo(element, attributeName, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until attribute with specified {@code attributeName} contains
   * {@code expectedValue}.
   *
   * @param locator element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   * @param timeout waiting time in seconds
   */
  public void waitAttributeContainsValue(
      By locator, String attributeName, String expectedValue, int timeout) {
    String[] actualValue = new String[1];
    webDriverWaitFactory
        .get(
            timeout,
            () ->
                format(
                    "\nactual attribute '%s' value:\n'%s'\ndidn't contain:\n'%s'\n",
                    attributeName, actualValue[0], expectedValue))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  actualValue[0] = waitVisibilityAndGetAttribute(locator, attributeName, timeout);
                  return actualValue[0].contains(expectedValue);
                });
  }

  /**
   * ~ Waits until attribute with specified {@code attributeName} contains {@code expectedValue}.
   *
   * @param elementLocator element which contains attribute
   * @param attributeName name of the attribute
   * @param expectedValue expected attribute value
   */
  public void waitAttributeContainsValue(
      By elementLocator, String attributeName, String expectedValue) {
    waitAttributeContainsValue(elementLocator, attributeName, expectedValue, DEFAULT_TIMEOUT);
  }

  /**
   * Waits until given {@code webElement} is selected.
   *
   * <p>Note! Uses only for checkboxes and radio buttons.
   *
   * @param webElement element which should be selected
   */
  public void waitElementIsSelected(WebElement webElement) {
    waitElementIsSelected(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until given {@code webElement} is selected.
   *
   * <p>Note! Uses only for checkboxes and radio buttons.
   *
   * @param webElement element which should be selected
   * @param timeout waiting time in seconds
   */
  public void waitElementIsSelected(WebElement webElement, int timeout) {
    webDriverWaitFactory.get(timeout).until(elementSelectionStateToBe(webElement, true));
  }

  /**
   * Waits until given {@code webElement} is not selected.
   *
   * <p>Note! Uses only for checkboxes and radio buttons.
   *
   * @param webElement element which should be not selected
   */
  public void waitElementIsNotSelected(WebElement webElement) {
    waitElementIsNotSelected(webElement, DEFAULT_TIMEOUT);
  }

  /**
   * Waits during {@code timeout} until given {@code webElement} is not selected.
   *
   * <p>Note! Uses only for checkboxes and radio buttons.
   *
   * @param webElement element which should be not selected
   * @param timeout waiting time in seconds
   */
  public void waitElementIsNotSelected(WebElement webElement, int timeout) {
    webDriverWaitFactory.get(timeout).until(elementSelectionStateToBe(webElement, false));
  }

  /**
   * Sets given {@code isCheckedWebElement} to specified state and waits its state
   *
   * <p>Note! Uses only for checkboxes and radio buttons.
   *
   * @param isCheckedWebElement element which should be selected
   * @param setWebElement element which should be clicked to change the state
   * @param state state of given element (true if the given element should be selected)
   */
  public void waitAndSetCheckbox(
      WebElement isCheckedWebElement, WebElement setWebElement, boolean state) {
    if (state) {
      if (!isCheckedWebElement.isSelected()) {
        waitAndClick(setWebElement);
        waitElementIsSelected(isCheckedWebElement);
      }
    } else {
      if (isCheckedWebElement.isSelected()) {
        waitAndClick(setWebElement);
        waitElementIsNotSelected(isCheckedWebElement);
      }
    }
  }

  /**
   * Performs clicking and holding an {@code element} during specified {@code timeout}.
   *
   * @param element target element
   * @param holdingTimeout time for element holding
   */
  public void clickAndHoldElementDuringTimeout(By element, int holdingTimeout) {
    clickAndHoldElementDuringTimeout(waitVisibility(element), holdingTimeout);
  }

  /**
   * Performs clicking and holding an {@code element} during specified {@code timeout}.
   *
   * @param element target element
   * @param holdingTimeout time for element holding
   */
  public void clickAndHoldElementDuringTimeout(WebElement element, int holdingTimeout) {
    Actions action = getAction();
    action.clickAndHold(waitVisibility(element)).perform();
    WaitUtils.sleepQuietly(holdingTimeout);
    action.release(waitVisibility(element)).perform();
  }

  /**
   * @see SeleniumWebDriverHelper#selectResourceToUpload(org.openqa.selenium.WebElement,
   *     java.nio.file.Path)
   */
  public void selectResourceToUpload(By elementLocator, Path localResource) throws IOException {
    selectResourceToUpload(seleniumWebDriver.findElement(elementLocator), localResource);
  }

  /**
   * Links resource to upload to input element of file type.
   *
   * @param webElement web element which is being used to upload the file
   * @param localResource path to the local file or directory
   * @return name of file which is linked to upload, or the name of zip file which contains
   *     uploading directory
   * @throws IOException if there is a problem with preparing resource to upload
   */
  public String selectResourceToUpload(WebElement webElement, Path localResource)
      throws IOException {
    Path readyToUploadFile = uploadUtil.prepareToUpload(seleniumWebDriver, localResource);
    webElement.sendKeys(readyToUploadFile.toString());

    return readyToUploadFile.getFileName().toString();
  }

  /**
   * Waits during {@code timeout} until specified {@code expression} returns "true".
   *
   * @param expression the condition which should be waits until it has a "true" state
   * @param timeout waiting time in seconds
   */
  public void waitSuccessCondition(ExpectedCondition<Boolean> expression, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until((ExpectedCondition<Boolean>) driver -> expression.apply(this.seleniumWebDriver));
  }

  /**
   * Waits until specified {@code expression} returns "true".
   *
   * @param expression the condition which should be waits until it has a "true" state
   */
  public void waitSuccessCondition(ExpectedCondition<Boolean> expression) {
    waitSuccessCondition(expression, DEFAULT_TIMEOUT);
  }

  public void closeCurrentWindowAndSwitchToAnother(String windowToSwitch) {
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(windowToSwitch);
  }

  public void pressArrowDown() {
    getAction().sendKeys(ARROW_DOWN.toString()).perform();
  }

  public void pressArrowUp() {
    getAction().sendKeys(ARROW_UP.toString()).perform();
  }

  public void pressEnter() {
    getAction().sendKeys(ENTER.toString()).perform();
  }

  public void pressCtrlF12() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys(F12)
        .keyUp(CONTROL)
        .perform();
  }

  /**
   * Waits until {@code action} stop throwing exception of {@code ignoredExceptionType} and during
   * {@code DEFAULT_TIMEOUT}.
   *
   * @param action action which should stop throwing of certain exception during timeout
   * @param ignoredExceptionTypes exceptions which should be ignored when action is performed
   */
  public void waitNoExceptions(
      Runnable action, Class<? extends WebDriverException>... ignoredExceptionTypes) {
    waitNoExceptions(action, DEFAULT_TIMEOUT, ignoredExceptionTypes);
  }

  /**
   * Waits until {@code action} stop throwing exception of {@code ignoredExceptionType} during
   * {@code timeoutInSec}.
   *
   * @param action action which should stop throwing of certain exception during timeout
   * @param timeoutInSec waiting time in seconds
   * @param ignoredExceptionTypes exceptions which should be ignored when action is being performed
   */
  public void waitNoExceptions(
      Runnable action,
      int timeoutInSec,
      Class<? extends WebDriverException>... ignoredExceptionTypes) {
    webDriverWaitFactory
        .get(timeoutInSec, asList(ignoredExceptionTypes))
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  action.run();
                  return true;
                });
  }
}
