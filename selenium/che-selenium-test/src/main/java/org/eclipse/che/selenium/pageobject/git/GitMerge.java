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
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by Andrienko Alexander on 30.09.14. */
@Singleton
public class GitMerge {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitMerge(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locator {
    String MERGE_VIEW_ID = "gwt-debug-git-merge-window";
    String MERGE_REFERENCE_PANEL = "gwt-debug-git-merge-referencesPanel";
    String EXPAND_REMOTE_BRANCHES = "//span[text()='Remote Branches']/preceding::div[1]";
    String EXPAND_LOCAL_BRANCHES = "//span[text()='Local Branches']/preceding::div[1]";
    String ITEM_IN_TREE = "//div[@id='gwt-debug-git-merge-referencesPanel']//span[text()='%s']";
    String CANCEL_BUTTON_ID = "git-merge-cancel";
    String MERGE_BTN_ID = "git-merge-merge";
  }

  @FindBy(id = Locator.MERGE_VIEW_ID)
  private WebElement mergeView;

  @FindBy(id = Locator.MERGE_REFERENCE_PANEL)
  private WebElement mergeReferencePanel;

  @FindBy(xpath = Locator.EXPAND_LOCAL_BRANCHES)
  private WebElement expandLocalBranches;

  @FindBy(xpath = Locator.EXPAND_REMOTE_BRANCHES)
  private WebElement expandRemoteBranches;

  @FindBy(id = Locator.CANCEL_BUTTON_ID)
  private WebElement mergeCancelBtn;

  @FindBy(id = Locator.MERGE_BTN_ID)
  private WebElement mergeMergeBtn;

  public void waitMergeView() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(mergeView));
  }

  public void waitMergePanel() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.visibilityOf(mergeReferencePanel));
  }

  public void waitMergeExpandRemoteBranchIcon() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.visibilityOf(expandRemoteBranches));
  }

  public void waitMergeExpandRemoteBranchIconIsNotPresent() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locator.EXPAND_REMOTE_BRANCHES)));
  }

  public void waitMergeExpandLocalBranchIcon() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.visibilityOf(expandLocalBranches));
  }

  public void clickMergeExpandRemoteBranchIcon() {
    expandRemoteBranches.click();
  }

  /**
   * click on remote branch wait the list of the names remote branches
   *
   * @param listNames is the list of the names remote branches
   */
  public void waitListRemoteBranches(final String listNames) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return mergeReferencePanel.getText().contains(listNames);
              }
            });
  }

  public void clickMergeExpandLocalBranchIcon() {
    expandLocalBranches.click();
  }

  public void waitItemInMergeList(String branchName) {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locator.ITEM_IN_TREE, branchName))));
  }

  public void selectItemInMergeList(String branchName) {
    waitItemInMergeList(branchName);
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locator.ITEM_IN_TREE, branchName))))
        .click();
  }

  public void clickCancelMergeBtn() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(By.id(Locator.CANCEL_BUTTON_ID)));
    mergeCancelBtn.click();
  }

  public void clickMergeBtn() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(mergeMergeBtn));
    mergeMergeBtn.click();
  }

  public void waitMergeBtnIsDisabled() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.not(ExpectedConditions.elementToBeClickable(mergeMergeBtn)));
  }

  public void waitMergeViewClosed() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locator.MERGE_VIEW_ID)));
  }
}
