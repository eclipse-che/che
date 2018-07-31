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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class GitStatusBar {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitStatusBar(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String GIT_BAR_TAB_ID = "gwt-debug-partButton-Processes";
    String GIT_BAR_INFO_PANEL = "(//div[@id='gwt-debug-gitOutputConsoleLines'])[last()]";
    String GIT_BAR_NAVIGATE_BTN = "//div[@id='gwt-debug-consolePart']/preceding::div[6]";
    String GIT_BAR_REMOVE_BUTTON = "//div[@id='gwt-debug-consolePart']/preceding::div[2]";
    String GIT_BAR_HIDE_BTN = "//div[@id='gwt-debug-infoPanel']//div[@id='gwt-debug-hideButton']";
  }

  @FindBy(id = Locators.GIT_BAR_TAB_ID)
  WebElement statusBarTab;

  @FindBy(xpath = Locators.GIT_BAR_INFO_PANEL)
  WebElement gitStatusBarInfoPanel;

  @FindBy(xpath = Locators.GIT_BAR_NAVIGATE_BTN)
  WebElement gitStatusBarNaviBtn;

  @FindBy(xpath = Locators.GIT_BAR_REMOVE_BUTTON)
  WebElement gitStatusBarRemoveBtn;

  @FindBy(xpath = Locators.GIT_BAR_HIDE_BTN)
  WebElement gitBarHideBtn;

  /** wait appears git statusbar tab */
  public void waitGitStatusBarOpenedTab() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(statusBarTab));
  }

  /** wait appears git status Bar information panel */
  public void waitGitStatusBarInfoPanel() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(gitStatusBarInfoPanel));
  }

  public String getTextStatus() {
    return gitStatusBarInfoPanel.getText();
  }

  public void waitCloseGitStatusBarInfoPanel() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.GIT_BAR_INFO_PANEL)));
  }

  /** click on statusbar tab */
  public void clickOnGitStatusBarTab() {
    waitGitStatusBarOpenedTab();
    statusBarTab.click();
  }

  public void waitMessageInGitTab(final String message) {
    waitGitStatusBarInfoPanel();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) webDriver -> getTextStatus().contains(message));
  }

  /** wait and click on navigate button in the 'git console' */
  public void waitAndClickOnNavigateBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(gitStatusBarNaviBtn));
    gitStatusBarNaviBtn.click();
  }

  /** wait and click on remove button in the 'git console' */
  public void waitAndClickOnRemoveBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(gitStatusBarRemoveBtn));
    gitStatusBarRemoveBtn.click();
  }

  /** click on the 'minimize 'button */
  public void clickOnStatusBarMinimizeBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(gitBarHideBtn))
        .click();
  }
}
