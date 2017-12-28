/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by aleksandr shmaraaev on 16.09.14. */
@Singleton
public class GitCommit {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitCommit(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_FORM_COMMIT = "gwt-debug-git-commit-window";
    String COMMIT_AMEND = "gwt-debug-git-commit-amend-label";
    String PUSH_AFTER_COMMIT = "gwt-debug-push-after-commit-check-box";
    String ENTER_MESSAGE = "gwt-debug-git-commit-message";
    String BTN_COMMIT = "git-commit-commit";
    String BTN_CANCEL = "git-commit-cancel";
    String BRANCHES_DROPDOWN =
        "//span[@id='gwt-debug-push-after-commit-check-box']/following-sibling::select";
    String TREE_ITEM_CHECK_BOX =
        "//div[@id='gwt-debug-git-commit-changed-files']"
            + "//div[text()='%s']/ancestor::div[1]/preceding-sibling::span";
  }

  @FindBy(id = Locators.MAIN_FORM_COMMIT)
  private WebElement mainFormCommit;

  @FindBy(id = Locators.ENTER_MESSAGE)
  private WebElement enterMessage;

  @FindBy(id = Locators.BTN_COMMIT)
  private WebElement btnCommit;

  @FindBy(id = Locators.BTN_CANCEL)
  private WebElement btnCancel;

  @FindBy(id = Locators.COMMIT_AMEND)
  private WebElement commitAmend;

  @FindBy(id = Locators.PUSH_AFTER_COMMIT)
  private WebElement pushAfterCommit;

  public void waitMainFormCommit() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.visibilityOf(mainFormCommit));
  }

  public void waitMainFormCommitIsClosed() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM_COMMIT)));
  }

  public boolean isWidgetOpened() {
    try {
      return btnCancel.isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  public void typeCommitMsg(String text) {
    enterMessage.clear();
    enterMessage.sendKeys(text);
  }

  public void clickOnBtnCommit() {
    btnCommit.click();
  }

  public void clickOnCancelBtn() {
    btnCancel.click();
  }

  public void clickOnPushAfterCommitCheckBox() {
    pushAfterCommit.click();
  }

  public void selectRemoteBranch(String branch) {
    seleniumWebDriver.findElement(By.xpath(Locators.BRANCHES_DROPDOWN)).click();
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(
                    Locators.BRANCHES_DROPDOWN + String.format("//option[text()='%s']", branch))))
        .click();
  }

  public void clickAmendCommit() {
    commitAmend.click();
  }

  public void waitAmendCommitIsSelected() {
    final WebElement element =
        seleniumWebDriver.findElement(By.id("gwt-debug-git-commit-amend-input"));
    new WebDriverWait(seleniumWebDriver, 5)
        .until((ExpectedCondition<Boolean>) webDriver -> element.getAttribute("checked") != null);
  }

  /**
   * Click the check-box of the item in the 'Git changed files tree panel'.
   *
   * @param itemName name of the item
   */
  public void clickItemCheckBox(String itemName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TREE_ITEM_CHECK_BOX, itemName))))
        .click();
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' to be selected..
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeSelected(String itemName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeSelected(
                (By.xpath(String.format(Locators.TREE_ITEM_CHECK_BOX + "//input", itemName)))));
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' to be indeterminate.
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeIndeterminate(String itemName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(
                            By.xpath(
                                String.format(Locators.TREE_ITEM_CHECK_BOX + "//input", itemName)))
                        .getAttribute("id")
                        .endsWith("indeterminate"));
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' to be unselected.
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeUnSelected(String itemName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementSelectionStateToBe(
                (By.xpath(String.format(Locators.TREE_ITEM_CHECK_BOX + "//input", itemName))),
                false));
  }
}
