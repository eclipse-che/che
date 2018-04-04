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
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class GitHistory {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public GitHistory(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String HISTORY_FORM_ID = "gwt-debug-git-history-window";
    String HISTORY_ROW_NUMBER = "//tr[@__gwt_row=%s]";
    String HISTORY_EDITOR = "//textarea[@class='gwt-TextArea gwt-TextArea-readonly']";
    String CLOSE_BUTTON_HISTORY = "git-history-close";
    String COMPARE_BUTTON_HISTORY = "git-history-compare";
    String COMMIT_INTO_HISTORY_FORM =
        "//table[@id='gwt-debug-git-history-window']//following::div[contains(text(), '%s')]";
  }

  @FindBy(id = Locators.HISTORY_FORM_ID)
  WebElement historyForm;

  @FindBy(xpath = Locators.HISTORY_EDITOR)
  WebElement historyEditor;

  @FindBy(id = Locators.CLOSE_BUTTON_HISTORY)
  WebElement closeButtonHistory;

  /** wait appear history form */
  public void waitHistoryForm() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(historyForm));
  }

  /** wait history form is closed */
  public void waitHistoryFormToClose() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.HISTORY_FORM_ID)));
  }

  /** click on the 'Compare' button in the history form */
  public void clickCompareButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.id(Locators.COMPARE_BUTTON_HISTORY)))
        .click();
  }

  /**
   * get current value from the the editor form
   *
   * @return value of message from the editor form
   */
  public String getFullDescription() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(historyEditor));
    return historyEditor.getAttribute("value");
  }

  /**
   * click on row with specified number (numbers start with 0)
   *
   * @param rowIndex is a specified number
   */
  public void selectHistoryRowByIndex(int rowIndex) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.HISTORY_ROW_NUMBER, Integer.toString(rowIndex)))))
        .click();
    loader.waitOnClosed();
  }

  /** wait appear text in history list */
  public void waitTextInHistoryList(final String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>) webDriver -> historyForm.getText().contains(expectedText));
  }

  /** wait appearance history editor */
  public void waitHistoryEditor() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(historyEditor));
  }

  /**
   * wait while content will appear into history editor
   *
   * @param expectedContent
   */
  public void waitContentIntoHistoryEditor(final String expectedContent) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getFullDescription().contains(expectedContent));
  }

  public void waitCommitInHistoryForm(String text) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text))));
  }

  public void clickCommitInHistoryForm(String text) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text))))
        .click();
  }

  public void waitCommitInHistoryFormNotPresent(String text) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text))));
  }

  /** click on close button history */
  public void clickOnCloseButtonHistory() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(closeButtonHistory))
        .click();
    waitHistoryFormToClose();
  }
}
