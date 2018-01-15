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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class GitReset {
  interface Locators {
    String RESET_TO_COMMIT_FORM = "gwt-debug-git-reset-window";
    String RESET_BTN = "git-reset-reset";
    String HARD_RESET_LABEL = "gwt-debug-git-reset-hard-label";
    String SOFT_RESET_LABEL = "gwt-debug-git-reset-soft-label";
    String COMMENT = "//div[text()='%s']";
    String COMMIT_ITEM = "//div[@id='gwt-debug-git-reset-window']//tbody[1]/tr[%s]";
  }

  @FindBy(id = Locators.RESET_TO_COMMIT_FORM)
  WebElement form;

  @FindBy(id = Locators.RESET_BTN)
  WebElement resetBtn;

  @FindBy(id = Locators.HARD_RESET_LABEL)
  WebElement hardResetLabel;

  @FindBy(id = Locators.SOFT_RESET_LABEL)
  WebElement softResetLabel;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitReset(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Wait 'Reset to commit' window is open */
  public void waitOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(form));
  }

  /** Wait 'Reset to commit' window is close */
  public void waitClose() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.RESET_TO_COMMIT_FORM)));
  }

  /** Click on 'Reset' button in the 'Reset Commit' window */
  public void clickResetBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(resetBtn))
        .click();
  }

  /** Select 'hard' in the 'Reset Commit' window */
  public void selectHardReset() {
    hardResetLabel.click();
  }

  /** Select 'hard' in the 'Reset Commit' window */
  public void selectSoftReset() {
    softResetLabel.click();
  }

  /**
   * Wait commit is present in the 'Reset Commit' window
   *
   * @param text text from comment
   */
  public void waitCommitIsPresent(String text) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMENT, text))));
  }

  /**
   * Select commit by number of line. Numbering starts at zero.
   *
   * @param numberLine number of line for commit
   */
  public void selectCommit(int numberLine) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.COMMIT_ITEM, numberLine))))
        .click();
  }
}
