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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Kuznetsov Mihail */
@Singleton
public class GitPush {
  interface Locators {
    String PUSH_BTN_ID = "git-remotes-push-push";
    String CANCEL_BTN_ID = "git-remotes-push-cancel";
    String REMOTE_BRANCH_SELECT = "gwt-debug-git-remotes-push-remoteBranch";
    String LOCAL_BRANCH_SELECT = "gwt-debug-git-remotes-push-localBranch";
    String REMOTE_REPOSITORY_SELECT = "gwt-debug-git-remotes-push-repository";
    String FORCE_PUSH_CHECKBOX = "gwt-debug-git-remotes-force-push-checkbox";
    String PUSH_FORM = "gwt-debug-git-remotes-push-window";
  }

  @FindBy(id = Locators.PUSH_BTN_ID)
  WebElement pushBtn;

  @FindBy(id = Locators.CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(id = Locators.REMOTE_BRANCH_SELECT)
  WebElement remoteBranchSelect;

  @FindBy(id = Locators.LOCAL_BRANCH_SELECT)
  WebElement localBranchSelect;

  @FindBy(id = Locators.REMOTE_REPOSITORY_SELECT)
  WebElement remoteRepositorySelect;

  @FindBy(id = Locators.FORCE_PUSH_CHECKBOX)
  WebElement forcePush;

  @FindBy(id = Locators.PUSH_FORM)
  WebElement form;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitPush(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PUSH_FORM)));
  }

  public void clickPushBtn() {
    pushBtn.click();
  }

  /** Set checked 'force push' check-box. */
  public void selectForcePushCheckBox() {
    forcePush.click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeSelected(By.id(Locators.FORCE_PUSH_CHECKBOX + "-input")));
  }

  public void clickCancelBtn() {
    cancelBtn.click();
  }

  public void waitPushBtnIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(pushBtn)));
  }

  public void selectRemoteRepository(String text) {
    new Select(remoteRepositorySelect).selectByVisibleText(text);
  }

  public void selectLocalBranchName(String text) {
    new Select(localBranchSelect).selectByVisibleText(text);
  }

  public void selectRemoteBranch(String text) {
    new Select(remoteBranchSelect).selectByVisibleText(text);
  }

  /**
   * click on remote repository wait the list of names remote repositories
   *
   * @param listNames is list of names remote repositories
   */
  public void waitListRemoteRepo(final String listNames) {
    remoteRepositorySelect.click();
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return remoteRepositorySelect.getText().equals(listNames);
              }
            });
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
