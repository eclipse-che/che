/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Mykola Morhun */
public class GitPanel {
  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private final WebDriverWait uiWait;
  private final WebDriverWait loadWait;

  @Inject
  public GitPanel(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;

    this.uiWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.loadWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String GIT_PANEL_BUTTON_ID = "gwt-debug-partButton-Git";
    String GIT_PANEL_ID = "gwt-debug-git-panel-content";
    String REPOSITORIES_LIST_ID = "gwt-debug-git-panel-repositories";
    String REPOSITORIES =
        "//div[@id=\"" + REPOSITORIES_LIST_ID + "\"]/child::div[@id[starts-with(.,\"gwt-uid-\")]]";
    String CHANGED_FILES_CONTAINER_ID = "gwt-debug-git-panel-changed-files";

    String REPOSITORY_BY_NAME = REPOSITORIES + "//div[text()=\"%s\"]";
    String CHANGES_LABEL_BY_REPOSITORY_NAME =
        REPOSITORY_BY_NAME + "/parent::div/preceding-sibling::span[starts-with(text(),\"∓\")]";
    String CHANGED_FILE_BY_NAME =
        "//div[@id=\"" + CHANGED_FILES_CONTAINER_ID + "\"]/descendant::div[text()=\"%s\"]";
  }

  @FindBy(id = Locators.GIT_PANEL_BUTTON_ID)
  private WebElement gitPanelButton;

  @FindBy(id = Locators.GIT_PANEL_ID)
  private WebElement gitPanel;

  /** Opens git panel and waits until it will be ready. */
  public void openPanel() {
    uiWait.until(visibilityOf(gitPanelButton)).click();
    uiWait.until(visibilityOf(gitPanel));
  }

  /** Returns number of repositories registered in the git panel. */
  public int countRepositories() {
    return seleniumWebDriver.findElements(By.xpath(Locators.REPOSITORIES)).size();
  }

  /**
   * Waits until panel has specified number of git repositories.
   *
   * @param quantity expected number of repositories in the git panel
   */
  public void waitRepositories(int quantity) {
    loadWait.until((ExpectedCondition<Boolean>) webDriver -> countRepositories() == quantity);
  }

  /** Returns true if given repository present into repositories list. */
  public boolean isRepositoryPresent(String repository) {
    return seleniumWebDriver
            .findElements(By.xpath(String.format(Locators.REPOSITORY_BY_NAME, repository)))
            .size()
        == 1;
  }

  /**
   * Waits until given repository presents in the repositories list.
   *
   * @param repository name of repository to wait for
   */
  public void waitRepositoryPresent(String repository) {
    loadWait.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath(String.format(Locators.REPOSITORY_BY_NAME, repository))));
  }

  /**
   * Checks whether any changes was made in the given repository. If no changes were made true will
   * be returned, false otherwise. This method looks on repository label only.
   *
   * @param repository name of repository
   */
  public boolean isRepositoryClean(String repository) {
    return seleniumWebDriver
            .findElements(
                By.xpath(String.format(Locators.CHANGES_LABEL_BY_REPOSITORY_NAME, repository)))
            .size()
        == 0;
  }

  /**
   * Waits until specified repository has no changes in it.
   *
   * @param repository name of repository to test
   */
  public void waitRepositoryToBeClean(String repository) {
    loadWait.until((ExpectedCondition<Boolean>) webDriver -> isRepositoryClean(repository));
  }

  /**
   * Gets number of changes from label of given repository.
   *
   * @param repository from which label should be read.
   * @return number of changes in given repository
   */
  public int getRepositoryChanges(String repository) {
    String labelText =
        seleniumWebDriver
            .findElement(
                By.xpath(String.format(Locators.CHANGES_LABEL_BY_REPOSITORY_NAME, repository)))
            .getText();
    return Integer.valueOf(labelText.substring(1)); // substring removes '-+' sign
  }

  /**
   * Waits until specified repository has specified number of changes in it. Number of changes
   * should be positive. To check changes existing use {@link #waitRepositoryToBeClean}
   *
   * @param repository name of repository to test
   * @param changes expected number of changes
   */
  public void waitRepositoryToHaveChanges(String repository, int changes) {
    loadWait.until(
        (ExpectedCondition<Boolean>) webDriver -> getRepositoryChanges(repository) == changes);
  }

  /**
   * Selects given repository in the repositories list.
   *
   * @param repository name of repository to select
   */
  public void selectRepository(String repository) {
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.REPOSITORY_BY_NAME, repository)))
        .click();
  }

  /**
   * Checks whether file present in changes file list.
   *
   * @param filename name of file
   */
  private boolean isFileInChangesList(String filename) {
    return seleniumWebDriver
            .findElements(By.xpath(String.format(Locators.CHANGED_FILE_BY_NAME, filename)))
            .size()
        == 1;
  }

  /**
   * Waits for file to be present in changes file list.
   *
   * @param filename name of file
   */
  public void waitFileInChangesList(String filename) {
    loadWait.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath(String.format(Locators.CHANGED_FILE_BY_NAME, filename))));
  }

  /**
   * Waits for file to has gone in changes file list.
   *
   * @param filename name of file
   */
  public void waitFileGoneInChangesList(String filename) {
    loadWait.until((ExpectedCondition<Boolean>) webDriver -> !isFileInChangesList(filename));
  }

  public void openDiffForChangedFileWithDoubleClick(String filename) {
    waitFileInChangesList(filename);
    WebElement changedFileItem =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.CHANGED_FILE_BY_NAME, filename)));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(changedFileItem).perform();
  }

  public void openDiffForChangedFileWithEnterKey(String filename) {
    waitFileInChangesList(filename);
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.CHANGED_FILE_BY_NAME, filename)))
        .click();
    seleniumWebDriver.getKeyboard().sendKeys(Keys.ENTER);
  }
}
