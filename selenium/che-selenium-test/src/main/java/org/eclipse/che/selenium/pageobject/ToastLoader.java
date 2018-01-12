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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * //
 *
 * @author Musienko Maxim
 */
@Singleton
public class ToastLoader {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public ToastLoader(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_FORM = "gwt-debug-popupLoader";
    String START_BUTTON = "//button[text()='Start']";
    String MAINFORM_WITH_TEXT_CONTAINER = "//div[text()='%s']";
  }

  @FindBy(id = Locators.MAIN_FORM)
  WebElement mainForm;

  @FindBy(xpath = Locators.START_BUTTON)
  WebElement startBtn;

  /** wait appearance toast loader widget */
  public void waitToastLoaderIsOpen() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));
  }

  /**
   * wait expected text in the widget
   *
   * @param expText
   */
  public void waitExpectedTextInToastLoader(String expText) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.MAINFORM_WITH_TEXT_CONTAINER, expText))));
  }

  /**
   * wait expected text in the widget
   *
   * @param expText expected text
   * @param timeout timeout sets in seconds
   */
  public void waitExpectedTextInToastLoader(String expText, int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.MAINFORM_WITH_TEXT_CONTAINER, expText))));
  }

  /** wait for closing of widget */
  public void waitToastLoaderIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM)));
  }

  /**
   * wait for closing of widget
   *
   * @param userTimeout timeout defined by user
   */
  public void waitToastLoaderIsClosed(int userTimeout) {
    new WebDriverWait(seleniumWebDriver, userTimeout)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM)));
  }

  /** wait appearance the 'Toast widget' with some text, and wait disappearance this one */
  public void waitAppeareanceAndClosing() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                // if loader appears
                try {
                  return !getTextFromToastLoader().isEmpty();
                }
                // if we have state exception during get element
                catch (StaleElementReferenceException ex) {
                  // average timeout for disappearance loader
                  WaitUtils.sleepQuietly(7);
                  return true;
                }
                // if loader does not appear
                catch (TimeoutException ex) {
                  return true;
                }
              }
            });
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                try {
                  return getTextFromToastLoader().isEmpty();
                } catch (org.openqa.selenium.TimeoutException ex) {
                  return true;
                }
              }
            });
    waitToastLoaderIsClosed();
  }

  /**
   * wait appearance the 'Toast widget' with some text, and wait disappearance this one
   *
   * @param userTimeout timeout defined by user
   */
  public void waitAppeareanceAndClosing(int userTimeout) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                // if loader appears
                try {
                  return !getTextFromToastLoader().isEmpty();
                }
                // if we have state exception during get element
                catch (StaleElementReferenceException ex) {
                  // average timeout for disappearance loader
                  WaitUtils.sleepQuietly(userTimeout);
                  return true;
                }
                // if loader does not appear
                catch (TimeoutException ex) {
                  return true;
                }
              }
            });
    new WebDriverWait(seleniumWebDriver, userTimeout)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                try {
                  return getTextFromToastLoader().isEmpty();
                } catch (org.openqa.selenium.TimeoutException ex) {
                  return true;
                }
              }
            });
    waitToastLoaderIsClosed(userTimeout);
  }

  public String getTextFromToastLoader() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm))
        .getText();
  }

  /** wait appearance of start button in the widget */
  public void waitStartButtonInToastLoader() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(startBtn));
  }

  /** wait appearance of start button in the widget and click on this one */
  public void clickOnStartButton() {
    waitStartButtonInToastLoader();
    startBtn.click();
  }
}
