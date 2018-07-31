/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class SearchReplacePanel {
  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private final CodenvyEditor editor;

  @Inject
  public SearchReplacePanel(
      SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory, CodenvyEditor editor) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.editor = editor;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String SEARCH_REPLACE_PANEL = "//div[@class='textViewFind show']";
    String FIND_INPUT = "textViewFindInput";
    String REPLACE_INPUT = "textViewReplaceInput";
    String NEXT_BTN = "//button[text()='Next']";
    String PREVIOUS_BTN = "//button[text()='Previous']";
    String REPLACE_BTN = "//button[text()='Replace']";
    String REPLACE_ALL_BTN = "//button[text()='Replace All']";
    String TOGGLE_CASE_BTN = "//button[@title='Toggle Case Insensitive']";
    String TOGGLE_WHOLE_BTN = "//button[@title='Toggle Whole Word']";
    String TOGGLE_REGULAR_EXP_BTN = "//button[@title='Toggle Regular Expression']";
    String CLOSE_BTN = "textViewFindCloseButton";
  }

  @FindBy(xpath = Locators.SEARCH_REPLACE_PANEL)
  WebElement searchReplacePanel;

  @FindBy(className = Locators.FIND_INPUT)
  WebElement findInput;

  @FindBy(className = Locators.REPLACE_INPUT)
  WebElement replaceInput;

  @FindBy(xpath = Locators.NEXT_BTN)
  WebElement nextBtn;

  @FindBy(xpath = Locators.PREVIOUS_BTN)
  WebElement previousBtn;

  @FindBy(xpath = Locators.REPLACE_BTN)
  WebElement replaceBtn;

  @FindBy(xpath = Locators.REPLACE_ALL_BTN)
  WebElement replaceAllBtn;

  @FindBy(xpath = Locators.TOGGLE_CASE_BTN)
  WebElement toggleCaseBtn;

  @FindBy(xpath = Locators.TOGGLE_WHOLE_BTN)
  WebElement toggleWholeBtn;

  @FindBy(xpath = Locators.TOGGLE_REGULAR_EXP_BTN)
  WebElement toggleRegularExpBtn;

  @FindBy(className = Locators.CLOSE_BTN)
  WebElement closeBtn;

  /** Open 'Search and Replace' panel by using key combination ALT+F for opened file in editor */
  public void openSearchReplacePanel() {
    editor.waitActive();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).sendKeys("f").keyUp(Keys.CONTROL).perform();
    waitSearchReplacePanel();
  }

  /** Close 'Search and Replace' panel */
  public void closeSearchReplacePanel() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(closeBtn))
        .click();
  }

  /** Wait the 'Search and Replace' panel is opened */
  public void waitSearchReplacePanel() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(searchReplacePanel));
  }

  /**
   * Enter text in find input on the 'Search and Replace' panel
   *
   * @param text text for searching
   */
  public void enterTextInFindInput(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(findInput))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(findInput))
        .sendKeys(text);
  }

  /**
   * Enter text in replace input on the 'Search and Replace' panel
   *
   * @param text text for replacing
   */
  public void enterTextInReplaceInput(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(replaceInput))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(replaceInput))
        .sendKeys(text);
  }

  /** Click on 'Next' button on the 'Search and Replace' panel */
  public void clickOnNextBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nextBtn))
        .click();
  }

  /** Click on 'Previous' button on the 'Search and Replace' panel */
  public void clickOnPreviousBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(previousBtn))
        .click();
  }

  /** Click on 'Aa' button on the 'Search and Replace' panel */
  public void clickOnToggleCaseBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(toggleCaseBtn))
        .click();
  }

  /** Click on '\b' button on the 'Search and Replace' panel */
  public void clickOnToggleWholeBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(toggleWholeBtn))
        .click();
  }

  /** Click on '/.*\/' button on the 'Search and Replace' panel */
  public void clickOnToggleRegularExpressionBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(toggleRegularExpBtn))
        .click();
  }

  /** Click on 'Replace' button on the 'Search and Replace' panel */
  public void clickOnReplaceBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(replaceBtn))
        .click();
  }

  /** Click on 'Replace All' button on the 'Search and Replace' panel */
  public void clickOnReplaceAllBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(replaceAllBtn))
        .click();
  }
}
