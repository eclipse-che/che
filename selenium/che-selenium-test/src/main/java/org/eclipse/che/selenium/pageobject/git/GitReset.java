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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Andrey Chizhikov */
@Singleton
public class GitReset {
  @Inject SeleniumWebDriverHelper seleniumWebDriverHelper;

  interface Locators {
    String RESET_TO_COMMIT_FORM     = "gwt-debug-git-reset-window";
    String RESET_BTN                = "git-reset-reset";
    String HARD_RESET_LABEL         = "gwt-debug-git-reset-hard-label";
    String SOFT_RESET_LABEL         = "gwt-debug-git-reset-soft-label";
    String MIXED_RESET_LABEL        = "gwt-debug-git-reset-mixed-label";
    String COMMENT                  = "//div[text()='%s']";
    String MAIN_FORM_XPATH_TEMPLATE = "//div[@id='gwt-debug-git-reset-mainForm']";
    String COMMIT_ITEM              = MAIN_FORM_XPATH_TEMPLATE + "//tbody[1]/tr[%s]";
    String ITEM_WITH_TEXT_XPATH     = MAIN_FORM_XPATH_TEMPLATE + "//tbody//div[contains(.,'%s')]";
  }

  @FindBy(id = Locators.RESET_TO_COMMIT_FORM)
  WebElement form;

  @FindBy(id = Locators.RESET_BTN)
  WebElement resetBtn;

  @FindBy(id = Locators.HARD_RESET_LABEL)
  WebElement hardResetLabel;

  @FindBy(id = Locators.SOFT_RESET_LABEL)
  WebElement softResetLabel;

  @FindBy(id = Locators.MIXED_RESET_LABEL)
  WebElement mixedResetLabel;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitReset(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Wait 'Reset to commit' window is open */
  public void waitOpen() {
    seleniumWebDriverHelper.waitVisibility(form);
  }

  /** Wait 'Reset to commit' window is close */
  public void waitClose() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.RESET_TO_COMMIT_FORM));
  }

  /** Click on 'Reset' button in the 'Reset Commit' window */
  public void clickResetBtn() {
    seleniumWebDriverHelper.waitVisibility(resetBtn).click();
  }

  /** Select 'hard' in the 'Reset Commit' window */
  public void selectHardReset() {
    hardResetLabel.click();
  }

  /** Select 'hard' in the 'Reset Commit' window */
  public void selectSoftReset() {
    softResetLabel.click();
  }

  /** Select 'mixed' in the 'Reset Commit' window */
  public void selectMixedReset() {
    mixedResetLabel.click();
  }

  /**
   * Wait commit is present in the 'Reset Commit' window
   *
   * @param text text from comment
   */
  public void waitCommitIsPresent(String text) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(String.format(Locators.COMMENT, text)));
  }

  /**
   * Select commit by number of line. Numbering starts at zero.
   *
   * @param numberLine number of line for commit
   */
  public void selectCommitByNumber(int numberLine) {
    seleniumWebDriverHelper
        .waitVisibility(By.xpath(String.format(Locators.COMMIT_ITEM, numberLine)))
        .click();
  }

  public void selectCommitByText(String text) {
    seleniumWebDriverHelper
        .waitVisibility(By.xpath(String.format(Locators.ITEM_WITH_TEXT_XPATH, text)))
        .click();
  }
}
