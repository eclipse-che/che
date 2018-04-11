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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Musienko Maxim */
@Singleton
public class GitHistory {
  private final Loader loader;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public GitHistory(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.loader = loader;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
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
    seleniumWebDriverHelper.waitVisibility(historyForm, ELEMENT_TIMEOUT_SEC);
  }

  /** wait history form is closed */
  public void waitHistoryFormToClose() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.HISTORY_FORM_ID));
  }

  /** click on the 'Compare' button in the history form */
  public void clickCompareButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.id(Locators.COMPARE_BUTTON_HISTORY), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * click on row with specified number (numbers start with 0)
   *
   * @param rowIndex is a specified number
   */
  public void selectHistoryRowByIndex(int rowIndex) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.HISTORY_ROW_NUMBER, Integer.toString(rowIndex))));
  }

  /** wait appear text in history list */
  public void waitTextInHistoryList(final String expectedText) {
    seleniumWebDriverHelper.waitTextPresence(
        historyForm, expectedText, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait appearance history editor */
  public void waitHistoryEditor() {
    seleniumWebDriverHelper.waitVisibility(historyEditor);
  }

  /**
   * wait while content will appear into history editor
   *
   * @param expectedContent
   */
  public void waitContentIntoHistoryEditor(final String expectedContent) {
    seleniumWebDriverHelper.waitValuePresence(historyEditor, expectedContent);
  }

  public void waitCommitInHistoryForm(String text) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text)));
  }

  public void clickCommitInHistoryForm(String text) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text)));
  }

  public void waitCommitInHistoryFormNotPresent(String text) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(String.format(Locators.COMMIT_INTO_HISTORY_FORM, text)));
  }

  /** click on close button history */
  public void clickOnCloseButtonHistory() {
    seleniumWebDriverHelper.waitAndClick(closeButtonHistory, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    waitHistoryFormToClose();
  }

  public String getTopCommitRevision() {
    loader.waitOnClosed();
    int numberOfTopRevisionCell = 0;
    return getGitHistoryTopContent().split("\n")[numberOfTopRevisionCell];
  }

  /** Returns all cells from top row divided by "\n" */
  private String getGitHistoryTopContent() {
    String rowTopIndex = Integer.toString(0);
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(String.format(Locators.HISTORY_ROW_NUMBER, rowTopIndex)));
  }
}
