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
public class GitPull {
  interface Locators {
    String PULL_BTN_ID = "git-remotes-pull-pull";
    String CANCEL_BTN_ID = "git-remotes-pull-cancel";
    String REMOTE_BRANCH_SELECT = "gwt-debug-git-remotes-pull-remoteBranch";
    String LOCAL_BRANCH_SELECT = "gwt-debug-git-remotes-pull-localBranch";
    String REMOTE_REPOSITORY_SELECT = "gwt-debug-git-remotes-pull-repository";
    String PULL_FORM = "gwt-debug-git-remotes-pull-window";
  }

  @FindBy(id = Locators.PULL_BTN_ID)
  WebElement pullBtn;

  @FindBy(id = Locators.CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(id = Locators.REMOTE_BRANCH_SELECT)
  WebElement remoteBranchSelect;

  @FindBy(id = Locators.LOCAL_BRANCH_SELECT)
  WebElement localBranchSelect;

  @FindBy(id = Locators.REMOTE_REPOSITORY_SELECT)
  WebElement remoteRepositorySelect;

  @FindBy(id = Locators.PULL_FORM)
  WebElement form;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitPull(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PULL_FORM)));
  }

  public void clickPullBtn() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.elementToBeClickable(pullBtn));
    pullBtn.click();
  }

  public void clickCancelBtn() {
    cancelBtn.click();
  }

  public void waitPullBtnIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(pullBtn)));
  }

  public void waitPullRemoteRepository(String text) {
    new Select(remoteRepositorySelect).selectByVisibleText(text);
  }

  public void waitLocalBranchName(String text) {
    new Select(localBranchSelect).selectByVisibleText(text);
  }

  public void waitRemoteBranchName(String text) {
    new Select(remoteBranchSelect).selectByVisibleText(text);
  }

  public void chooseRemoteRepository(String nameRepo) {
    new Select(remoteRepositorySelect).selectByVisibleText(nameRepo);
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
