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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by aleksandr shmaraev on 31.10.14. */
@Singleton
public class GitFetch {

  interface Locators {
    String FETCH_FORM = "gwt-debug-git-remotes-fetch-window";
    String CANCEL_BTN_ID = "git-remotes-fetch-cancel";
    String FETCH_BTN_ID = "git-remotes-fetch-fetch";
    String REMOTE_REPOSITORY_SELECT = "gwt-debug-git-remotes-fetch-repository";
    String REMOTE_BRANCH_SELECT = "gwt-debug-git-remotes-fetch-remoteBranch";
    String LOCAL_BRANCH_SELECT = "gwt-debug-git-remotes-fetch-localBranch";
    String REMOVE_DELETED_REFS = "gwt-debug-git-remotes-fetch-removeDeletedRefs-input";
    String FETCH_ALL_BRANCHES = "gwt-debug-git-remotes-fetch-fetchAllBranches-input";
  }

  @FindBy(id = Locators.FETCH_FORM)
  WebElement fetchForm;

  @FindBy(id = Locators.CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(id = Locators.FETCH_BTN_ID)
  WebElement fetchBtn;

  @FindBy(id = Locators.REMOTE_REPOSITORY_SELECT)
  WebElement remoteRepositorySelect;

  @FindBy(id = Locators.REMOTE_BRANCH_SELECT)
  WebElement remoteBranchSelect;

  @FindBy(id = Locators.LOCAL_BRANCH_SELECT)
  WebElement localBranchSelect;

  @FindBy(id = Locators.REMOVE_DELETED_REFS)
  WebElement removeDeletedRefs;

  @FindBy(id = Locators.FETCH_ALL_BRANCHES)
  WebElement fetchAllBranches;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitFetch(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.visibilityOf(fetchForm));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.FETCH_FORM)));
  }

  public void clickFetchBtn() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.elementToBeClickable(fetchBtn));
    fetchBtn.click();
  }

  public void clickFetchCancelBtn() {
    cancelBtn.click();
  }

  public void clickFetchAllBranches() {
    fetchAllBranches.click();
  }

  public void chooseRemoteRepository(String text) {
    new Select(remoteRepositorySelect).selectByVisibleText(text);
  }

  public void chooseRemoteBranch(String text) {
    new Select(remoteBranchSelect).selectByVisibleText(text);
  }

  public void chooseLocalBranch(String text) {
    new Select(localBranchSelect).selectByVisibleText(text);
  }

  public void waitFetchBtnIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(fetchBtn)));
  }

  /**
   * click on remote branch wait the list of names remote branches
   *
   * @param listNames is the list of names remote branches
   */
  public void waitListRemoteBranches(final String listNames) {
    remoteBranchSelect.click();
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return remoteBranchSelect.getText().equals(listNames);
              }
            });
  }
}
