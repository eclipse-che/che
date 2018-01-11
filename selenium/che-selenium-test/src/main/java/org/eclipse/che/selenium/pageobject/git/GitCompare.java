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
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 10.02.16 */
@Singleton
public class GitCompare {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public GitCompare(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String COMPARE_BRANCH_FORM = "gwt-debug-git-compare-branch-mainForm";
    String ID_PREFIX_BRANCH = "gwt-debug-git-compare-branch-";
    String BRANCH_COMPARE_BUTTON = "git-compare-branch-compare";
    String COMPARE_BRANCH_CLOSE_BTN = "git-compare-branch-close";
    String MAIN_FORM_COMPARE = "//button[@id='git-compare-close-btn']/ancestor::div[2]";
    String LEFT_COMPARE_EDITOR = "gwt-debug-compareParentDiv_left_editor_id";
    String LEFT_COMPARE_STATUS =
        "//div[@id='gwt-debug-compareParentDiv_left_status_id' and text()='%s']";
    String RIGHT_COMPARE_EDITOR = "gwt-debug-compareParentDiv_right_editor_id";
    String COMPARE_CLOSE_BUTTON = "git-compare-close-btn";
    String COMPARE_REVISION_FORM = "gwt-debug-git-compare-revision-window";
    String REVISION_ITEM =
        "//div[@id='gwt-debug-git-compare-revision-window']//tr[@__gwt_row='%s']";
    String REVISION_COMPARE_BUTTON = "git-compare-revision-compare";
    String REVISION_CLOSE_BUTTON = "git-compare-revision-close";
    String GROUP_GIT_COMPARE_FORM = "//div[text()='Git Compare']/ancestor::div[3]";
    String PATH_TO_FILE_COMPARE =
        "//div[@id='gwt-debug-git-compare-window-changed-files']//div[text()='%s']";
    String GROUP_COMPARE_TEXT_AREA = "gwt-debug-git-compare-window-changed-files";
    String GROUP_COMPARE_CLOSE_BTN = "git-compare-btn-close";
    String GROUP_GIT_COMPARE_BTN = "git-compare-btn-compare";
  }

  @FindBy(id = Locators.COMPARE_BRANCH_FORM)
  WebElement compareBranchForm;

  @FindBy(id = Locators.BRANCH_COMPARE_BUTTON)
  WebElement gitCompareBranchBtn;

  @FindBy(id = Locators.COMPARE_BRANCH_CLOSE_BTN)
  WebElement gitCompareBranchCloseBtn;

  @FindBy(xpath = Locators.MAIN_FORM_COMPARE)
  WebElement mainFormCompare;

  @FindBy(id = Locators.LEFT_COMPARE_EDITOR)
  WebElement leftCompareEditor;

  @FindBy(id = Locators.RIGHT_COMPARE_EDITOR)
  WebElement rightCompareEditor;

  @FindBy(id = Locators.COMPARE_CLOSE_BUTTON)
  WebElement compareCloseBtn;

  @FindBy(id = Locators.COMPARE_REVISION_FORM)
  WebElement compareRevisionForm;

  @FindBy(id = Locators.REVISION_COMPARE_BUTTON)
  WebElement compareRevisionBtn;

  @FindBy(id = Locators.REVISION_CLOSE_BUTTON)
  WebElement closeRevisionBtn;

  /** wait the main form 'Git Compare' is open */
  public void waitGitCompareFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainFormCompare));
  }

  /** wait the main form 'Git Compare' is closed */
  public void waitGitCompareFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.MAIN_FORM_COMPARE)));
  }

  /** get text from left editor of the 'Git Compare' */
  public String getTextFromLeftEditorCompare() {
    return leftCompareEditor.getText();
  }

  /** get text from right editor of the 'Git Compare' */
  public String getTextFromRightEditorCompare() {
    return rightCompareEditor.getText();
  }

  /**
   * wait expected text into left editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitExpectedTextIntoLeftEditor(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getTextFromLeftEditorCompare().contains(expText));
  }

  /**
   * wait expected text is not present into right editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitTextNotPresentIntoRightEditor(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> !(getTextFromRightEditorCompare().contains(expText)));
  }

  /** set focus on the left compare editor */
  public void setFocusOnLeftCompareEditor() {
    leftCompareEditor.click();
  }

  /**
   * wait specified values for 'Line' and 'Column' of left compare container
   *
   * @param status is expected line and column position. For example: 'Line 2 : Column 1'
   */
  public void waitLineAndColumnInLeftCompare(String status) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(String.format(Locators.LEFT_COMPARE_STATUS, status))));
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
   * @param text
   */
  public void typeTextIntoGitCompareEditor(String text) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
    loader.waitOnClosed();
  }

  /** click on the 'Close' git compare button */
  public void clickOnCompareCloseButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(compareCloseBtn))
        .click();
  }

  /** wait the 'Compare with branch' form is open */
  public void waitCompareBranchIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(compareBranchForm));
  }

  /** wait the 'Compare with branch' form is closed */
  public void waitCompareBranchIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.COMPARE_BRANCH_FORM)));
  }

  /**
   * select a branch into the 'Compare with branch' form
   *
   * @param branchName is name of the branch
   */
  public void selectBranchIntoCompareBranchForm(String branchName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.ID_PREFIX_BRANCH + branchName)))
        .click();
  }

  /** click on the 'Compare' button into Compare with branch' form */
  public void clickOnBranchCompareButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(gitCompareBranchBtn))
        .click();
  }

  /** wait the 'Compare with revision' form is open */
  public void waitCompareRevisionIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(compareRevisionForm));
  }

  /** wait the 'Compare with revision' form is closed */
  public void waitCompareRevisionIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.COMPARE_REVISION_FORM)));
  }

  /**
   * select a revision with specified number into the 'Compare with revision' form numbers start
   * with 0
   *
   * @param revision
   */
  public void selectRevisionIntoCompareRevisionForm(int revision) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.REVISION_ITEM, Integer.toString(revision)))))
        .click();
  }

  /** click on the 'Compare' button into 'Compare with revision' form */
  public void clickOnRevisionCompareButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(compareRevisionBtn))
        .click();
  }

  /** click on the 'Close' button into 'Compare with revision' form */
  public void clickOnCloseRevisionButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(closeRevisionBtn))
        .click();
  }

  /** wait the group 'Git Compare' form is open */
  public void waitGroupGitCompareIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.GROUP_GIT_COMPARE_FORM)));
  }

  /** wait the group 'Git Compare' form is closed */
  public void waitGroupGitCompareIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.GROUP_GIT_COMPARE_FORM)));
  }

  /**
   * wait expected text into group git compare
   *
   * @param expText is expected value
   */
  public void waitExpTextInGroupGitCompare(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromGroupGitCompare().contains(expText));
  }

  /** get text from the group 'Git Compare' form */
  public String getTextFromGroupGitCompare() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.id(Locators.GROUP_COMPARE_TEXT_AREA)))
        .getText();
  }

  /**
   * Select file in the 'Git changed files tree panel'.
   *
   * @param name name of the item
   */
  public void selectChangedFile(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.PATH_TO_FILE_COMPARE, name))))
        .click();
  }

  /** click on the 'Compare' button into group 'Git Compare' form */
  public void clickOnGroupCompareButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.GROUP_GIT_COMPARE_BTN)))
        .click();
  }

  /** click on the 'Close' button into group 'Git Compare' form */
  public void clickOnGroupCloseButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.id(Locators.GROUP_COMPARE_CLOSE_BTN)))
        .click();
  }
}
