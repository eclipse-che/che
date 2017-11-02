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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class ImportProjectFromLocation {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public ImportProjectFromLocation(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_FORM_ID = "gwt-debug-importProjectWizard-window";
    String SKIP_ROOT_FOLDER_CHECK_BOX_ID = "gwt-debug-zipImporter-skipFirstLevel-label";
    String KEEP_FOLLOWING_DIRECTORY = "gwt-debug-file-importProject-keepDirectory";
    String KEEP_DIRECTORY_CHECK_BOX = "gwt-debug-file-importProject-keepDirectory-input";
    String DIRECTORY_NAME = "gwt-debug-file-importProject-keepDirectoryName";
    String GITHUB_SOURCE_CONTROL_ID = "gwt-debug-projectWizard-GITHUB";
    String GIT_SOURCE_CONTROL_ID = "gwt-debug-projectWizard-GIT";
    String GIT_SOURCE_CONTROL_ZIP = "gwt-debug-projectWizard-ZIP";
    String URL_FIELD_ID_ = "gwt-debug-file-importProject-projectUrl";
    String NAME_FIELD_ID = "gwt-debug-file-importProject-projectName";
    String GITHUB_ACCOUNT = "gwt-debug-githubImporter-accountName";
    String LOAD_REPO_BTN_ID = "gwt-debug-githubImporter-loadRepo";
    String IMPORT_BTN_ID = "importProjectWizard-importButton";
    String CLOSE_ICON_CSS =
        "div#gwt-debug-importProjectWizard-window svg[width='8px'][height='8px']";
    String CATEGORY_PANEL_XPATH =
        "//table[@id='gwt-debug-githubImporter-repositories']//div[text()='%s']";
    String BRANCH_NAME = "gwt-debug-file-importProject-branchName";
    String IMPORT_PROJECT_BRANCH = "gwt-debug-file-importProject-branch-label";
    String IMPORT_BRANCH_CHECK_BOX = "gwt-debug-file-importProject-branch-input";
    String IMPORT_RECURSIVELY = "gwt-debug-file-importProject-recursive-label";
    String IMPORT_RECURSIVELY_CHECK_BOX = "gwt-debug-file-importProject-recursive-input";
    String RELATIVE_PATH = "gwt-debug-file-importProject-relativePath";
  }

  @FindBy(id = Locators.MAIN_FORM_ID)
  WebElement mainForm;

  @FindBy(id = Locators.KEEP_DIRECTORY_CHECK_BOX)
  WebElement keepDirCheckBox;

  @FindBy(id = Locators.KEEP_FOLLOWING_DIRECTORY)
  WebElement keepFollowingDir;

  @FindBy(id = Locators.DIRECTORY_NAME)
  WebElement directoryName;

  @FindBy(id = Locators.SKIP_ROOT_FOLDER_CHECK_BOX_ID)
  WebElement skipRootFolderOfArchive;

  @FindBy(id = Locators.GITHUB_SOURCE_CONTROL_ID)
  WebElement gitHubSourceItem;

  @FindBy(id = Locators.GIT_SOURCE_CONTROL_ID)
  WebElement gitSourceItem;

  @FindBy(id = Locators.GIT_SOURCE_CONTROL_ZIP)
  WebElement gitSourceZip;

  @FindBy(id = Locators.URL_FIELD_ID_)
  WebElement urlField;

  @FindBy(id = Locators.NAME_FIELD_ID)
  WebElement nameField;

  @FindBy(css = Locators.CLOSE_ICON_CSS)
  WebElement closeIcon;

  @FindBy(id = Locators.IMPORT_BTN_ID)
  WebElement importBtn;

  @FindBy(id = Locators.LOAD_REPO_BTN_ID)
  WebElement loadRepoBtn;

  @FindBy(id = Locators.GITHUB_ACCOUNT)
  WebElement githubAccountList;

  @FindBy(id = Locators.BRANCH_NAME)
  WebElement branchName;

  @FindBy(id = Locators.IMPORT_PROJECT_BRANCH)
  WebElement importBranch;

  @FindBy(id = Locators.IMPORT_BRANCH_CHECK_BOX)
  WebElement importBranchCheckBox;

  @FindBy(id = Locators.IMPORT_RECURSIVELY)
  WebElement importRecursively;

  @FindBy(id = Locators.IMPORT_RECURSIVELY_CHECK_BOX)
  WebElement importRecursivelyCheckBox;

  /** wait while main widget will appear */
  public void waitMainForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));
  }

  /** wait while all elements of the form appear */
  public void waitMainFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, PREPARING_WS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM_ID)));
  }

  /** wait while all elements of the form appear */
  public void waitMainFormIsClosed(int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.MAIN_FORM_ID)));
  }

  /**
   * type user uri into URL: field
   *
   * @param uri
   */
  public void typeURi(String uri) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(urlField))
        .clear();
    loader.waitOnClosed();
    urlField.sendKeys(uri);
  }

  /** click on 'Import recursively' checkbox */
  public void clickOnImportRecursivelyCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(importRecursively))
        .click();
  }

  /** wait the 'Import recursively' checkbox is selected */
  public void waitImportRecursivelyIsSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> importRecursivelyCheckBox.getAttribute("checked") != null);
  }

  /** wait the 'Import recursively' checkbox is not selected */
  public void waitImportRecursivelyIsNotSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> importRecursivelyCheckBox.getAttribute("checked") == null);
  }

  /**
   * type a name of the project
   *
   * @param nameOfProject
   */
  public void typeProjectName(String nameOfProject) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nameField));
    nameField.clear();
    loader.waitOnClosed();
    nameField.sendKeys(nameOfProject);
  }

  /** click on 'Import' button and wait what the 'Import project' window was closed */
  public void clickImportBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(importBtn))
        .click();
    waitMainFormIsClosed();
  }

  /** click on 'Import' button */
  public void clickImportBtnWithoutWait() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(importBtn))
        .click();
  }

  /** click on Git field */
  public void selectGitSourceItem() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(gitSourceItem));
    loader.waitOnClosed();
    gitSourceItem.click();
    loader.waitOnClosed();
  }

  /** click on GitHub field */
  public void selectGitHubSourceItem() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(gitHubSourceItem))
        .click();
  }

  /**
   * wait main form, select Git field type uri and name of importing project click on button import
   *
   * @param uri is user uri into URL: field
   * @param nameOfProject is name of project into name: field
   */
  public void waitAndTypeImporterAsGitInfo(String uri, String nameOfProject) {
    waitMainForm();
    selectGitSourceItem();
    loader.waitOnClosed();
    typeURi(uri);
    loader.waitOnClosed();
    typeProjectName(nameOfProject);
    loader.waitOnClosed();
    clickImportBtn();
    waitMainFormIsClosed();
    loader.waitOnClosed();
  }

  /** click on 'Keep following directory' */
  public void clickOnKeepDirectoryCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(keepFollowingDir))
        .click();
  }

  /** wait the 'Keep following directory' checkbox is selected */
  public void waitKeepDirectoryIsSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> keepDirCheckBox.getAttribute("checked") != null);
  }

  /** wait the 'Keep following directory' checkbox is not selected */
  public void waitKeepDirectoryIsNotSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> keepDirCheckBox.getAttribute("checked") == null);
  }

  /**
   * select specified item into github account dropdown list
   *
   * @param item item for selection
   */
  public void selectItemInAccountList(String item) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(githubAccountList));
    new Select(githubAccountList).selectByValue(item);
  }

  /**
   * type a name of the directory
   *
   * @param nameOfDirectory
   */
  public void typeDirectoryName(String nameOfDirectory) {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(directoryName));
    directoryName.clear();
    directoryName.sendKeys(nameOfDirectory);
  }

  /** select project by name in project list */
  public void selectProjectByName(String projectName) {
    String locator = String.format(Locators.CATEGORY_PANEL_XPATH, projectName);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
  }

  public void waitLoadRepoBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(loadRepoBtn));
  }

  public void clickLoadRepoBtn() {
    waitLoadRepoBtn();
    loadRepoBtn.click();
  }

  /**
   * type a name of the branch
   *
   * @param branch is name of the branch
   */
  public void typeBranchName(String branch) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(branchName));
    branchName.clear();
    branchName.sendKeys(branch);
  }

  /** click on 'Branch' */
  public void clickBranchCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(importBranch))
        .click();
  }

  /** wait the 'Branch' checkbox is selected */
  public void waitBranchIsSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> importBranchCheckBox.getAttribute("checked") != null);
  }

  /** wait the 'Branch' checkbox is not selected */
  public void waitBranchIsNotSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver webDriver) -> importBranchCheckBox.getAttribute("checked") == null);
  }

  /** wait appereance main widget, click on close ('x') icon and wait closing of the widget */
  public void closeWithIcon() {
    waitMainForm();
    closeIcon.click();
    waitMainFormIsClosed();
  }
}
