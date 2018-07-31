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
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 10.02.16 */
@Singleton
public class GitCompare {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public GitCompare(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String COMPARE_BRANCH_FORM_ID = "gwt-debug-git-compare-branch-mainForm";
    String ID_PREFIX_BRANCH = "gwt-debug-git-compare-branch-";
    String BRANCH_COMPARE_BUTTON_ID = "git-compare-branch-compare";
    String COMPARE_BRANCH_CLOSE_BTN_ID = "git-compare-branch-close";
    String MAIN_FORM_COMPARE_XPATH = "//button[@id='git-compare-close-btn']/ancestor::table";
    String LEFT_COMPARE_EDITOR_ID = "gwt-debug-compareParentDiv_left_editor_id";
    String LEFT_COMPARE_STATUS_XPATH =
        "//div[@id='gwt-debug-compareParentDiv_left_status_id' and text()='%s']";
    String RIGHT_COMPARE_EDITOR_ID = "gwt-debug-compareParentDiv_right_editor_id";
    String NEXT_DIFF_BUTTON_ID = "git-compare-next-diff-btn";
    String PREVIOUS_DIFF_BUTTON_ID = "git-compare-prev-diff-btn";
    String COMPARE_CLOSE_BUTTON_ID = "git-compare-close-btn";
    String COMPARE_REVISION_FORM_ID = "gwt-debug-git-compare-revision-window";
    String REVISION_ITEM_XPATH =
        "//table[@id='gwt-debug-git-compare-revision-window']//tr[@__gwt_row='%s']";
    String REVISION_COMPARE_BUTTON_ID = "git-compare-revision-compare";
    String REVISION_CLOSE_BUTTON_ID = "git-compare-revision-close";
    String GROUP_GIT_COMPARE_FORM_XPATH = "//table[@title='Git Compare']";
    String PATH_TO_FILE_COMPARE_XPATH =
        "//div[@id='gwt-debug-git-compare-window-changed-files']//div[text()='%s']";
    String GROUP_COMPARE_TEXT_AREA_ID = "gwt-debug-git-compare-window-changed-files";
    String GROUP_COMPARE_CLOSE_BTN_ID = "git-compare-btn-close";
    String GROUP_GIT_COMPARE_BTN_ID = "git-compare-btn-compare";
  }

  @FindBy(id = Locators.COMPARE_BRANCH_FORM_ID)
  WebElement compareBranchForm;

  @FindBy(id = Locators.BRANCH_COMPARE_BUTTON_ID)
  WebElement gitCompareBranchBtn;

  @FindBy(id = Locators.COMPARE_BRANCH_CLOSE_BTN_ID)
  WebElement gitCompareBranchCloseBtn;

  @FindBy(xpath = Locators.MAIN_FORM_COMPARE_XPATH)
  WebElement mainFormCompare;

  @FindBy(id = Locators.LEFT_COMPARE_EDITOR_ID)
  WebElement leftCompareEditor;

  @FindBy(id = Locators.RIGHT_COMPARE_EDITOR_ID)
  WebElement rightCompareEditor;

  @FindBy(id = Locators.COMPARE_CLOSE_BUTTON_ID)
  WebElement compareCloseBtn;

  @FindBy(id = Locators.COMPARE_REVISION_FORM_ID)
  WebElement compareRevisionForm;

  @FindBy(id = Locators.REVISION_COMPARE_BUTTON_ID)
  WebElement compareRevisionBtn;

  @FindBy(id = Locators.REVISION_CLOSE_BUTTON_ID)
  WebElement closeRevisionBtn;

  @FindBy(id = Locators.NEXT_DIFF_BUTTON_ID)
  WebElement nextDiffButton;

  @FindBy(id = Locators.PREVIOUS_DIFF_BUTTON_ID)
  WebElement previousDiffButton;

  /** wait the main form 'Git Compare' is open */
  public void waitGitCompareFormIsOpen() {
    seleniumWebDriverHelper.waitVisibility(mainFormCompare);
  }

  /** wait the main form 'Git Compare' is closed */
  public void waitGitCompareFormIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.MAIN_FORM_COMPARE_XPATH));
  }

  /** click on the 'Next' button in the git compare form */
  public void clickOnNextDiffButton() {
    seleniumWebDriverHelper.waitAndClick(nextDiffButton);
    waitGitCompareFormIsOpen();
  }

  /** click on the 'Previous' button in the git compare form */
  public void clickOnPreviousDiffButton() {
    seleniumWebDriverHelper.waitAndClick(previousDiffButton);
    waitGitCompareFormIsOpen();
  }

  /**
   * wait expected text into left editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitExpectedTextIntoLeftEditor(String expText) {
    seleniumWebDriverHelper.waitTextContains(leftCompareEditor, expText);
  }

  /**
   * wait expected text is not present into left editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitTextNotPresentIntoLeftEditor(String expText) {
    seleniumWebDriverHelper.waitTextIsNotPresented(leftCompareEditor, expText);
  }

  /**
   * wait expected text into right editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitExpectedTextIntoRightEditor(String expText) {
    seleniumWebDriverHelper.waitTextContains(rightCompareEditor, expText);
  }

  /**
   * wait expected text is not present into right editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitTextNotPresentIntoRightEditor(String expText) {
    seleniumWebDriverHelper.waitTextIsNotPresented(rightCompareEditor, expText);
  }

  /** set focus on the left compare editor */
  public void setFocusOnLeftCompareEditor() {
    seleniumWebDriverHelper.waitAndClick(leftCompareEditor);
  }

  /**
   * wait specified values for 'Line' and 'Column' of left compare container
   *
   * @param status is expected line and column position. For example: 'Line 2 : Column 1'
   */
  public void waitLineAndColumnInLeftCompare(String status) {
    seleniumWebDriverHelper.waitPresence(
        By.xpath(String.format(Locators.LEFT_COMPARE_STATUS_XPATH, status)));
  }

  /**
   * set the cursor in the specified line
   *
   * @param positionLine is the specified number line
   * @param status is expected line and column position
   */
  public void setCursorToLine(int positionLine, String status) {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).sendKeys("l").perform();
    Alert alert =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.alertIsPresent());
    seleniumWebDriver.switchTo().alert();
    alert.sendKeys(String.valueOf(positionLine));
    alert.accept();
    action.keyUp(Keys.CONTROL).perform();
    waitLineAndColumnInLeftCompare(status);
  }

  /**
   * type text to git compare form
   *
   * @param text text which should be typed
   */
  public void typeTextIntoGitCompareEditor(String text) {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
  }

  /** click on the 'Close' git compare button */
  public void clickOnCompareCloseButton() {
    seleniumWebDriverHelper.waitAndClick(compareCloseBtn, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait the 'Compare with branch' form is open */
  public void waitCompareBranchIsOpen() {
    seleniumWebDriverHelper.waitVisibility(compareBranchForm);
  }

  /** click on the 'Close' button into 'Compare with branch' form */
  public void clickOnCloseBranchCompareButton() {
    seleniumWebDriverHelper.waitAndClick(gitCompareBranchCloseBtn);
    waitCompareBranchIsClosed();
  }

  /** wait the 'Compare with branch' form is closed */
  public void waitCompareBranchIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.COMPARE_BRANCH_FORM_ID));
  }

  /**
   * select a branch into the 'Compare with branch' form
   *
   * @param branchName is name of the branch
   */
  public void selectBranchIntoCompareBranchForm(String branchName) {
    seleniumWebDriverHelper.waitAndClick(
        By.id(Locators.ID_PREFIX_BRANCH + branchName), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** click on the 'Compare' button into Compare with branch' form */
  public void clickOnBranchCompareButton() {
    seleniumWebDriverHelper.waitAndClick(gitCompareBranchBtn, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait the 'Compare with revision' form is open */
  public void waitCompareRevisionIsOpen() {
    seleniumWebDriverHelper.waitVisibility(compareRevisionForm);
  }

  /** wait the 'Compare with revision' form is closed */
  public void waitCompareRevisionIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.COMPARE_REVISION_FORM_ID));
  }

  /**
   * select a revision with specified number into the 'Compare with revision' form numbers start
   * with 0
   *
   * @param revision value of revision
   */
  public void selectRevisionIntoCompareRevisionForm(int revision) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.REVISION_ITEM_XPATH, Integer.toString(revision))),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** click on the 'Compare' button into 'Compare with revision' form */
  public void clickOnRevisionCompareButton() {
    seleniumWebDriverHelper.waitAndClick(compareRevisionBtn, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** click on the 'Close' button into 'Compare with revision' form */
  public void clickOnCloseRevisionButton() {
    seleniumWebDriverHelper.waitAndClick(closeRevisionBtn, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** wait the group 'Git Compare' form is open */
  public void waitGroupGitCompareIsOpen() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.GROUP_GIT_COMPARE_FORM_XPATH));
  }

  /** wait the group 'Git Compare' form is closed */
  public void waitGroupGitCompareIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.GROUP_GIT_COMPARE_FORM_XPATH));
  }

  /**
   * wait expected text into group git compare
   *
   * @param expText is expected value
   */
  public void waitExpTextInGroupGitCompare(String expText) {
    seleniumWebDriverHelper.waitTextContains(By.id(Locators.GROUP_COMPARE_TEXT_AREA_ID), expText);
  }

  /**
   * Select file in the 'Git changed files tree panel'.
   *
   * @param name name of the item
   */
  public void selectChangedFile(String name) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.PATH_TO_FILE_COMPARE_XPATH, name)),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** click on the 'Compare' button into group 'Git Compare' form */
  public void clickOnGroupCompareButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.id(Locators.GROUP_GIT_COMPARE_BTN_ID), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** click on the 'Close' button into group 'Git Compare' form */
  public void clickOnGroupCloseButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.id(Locators.GROUP_COMPARE_CLOSE_BTN_ID), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }
}
