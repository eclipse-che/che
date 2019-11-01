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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Musienko Maxim */
@Singleton
public class GitStatusBar {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public GitStatusBar(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
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

  /** wait appears git status Bar information panel */
  public void waitGitStatusBarInfoPanel() {
    seleniumWebDriverHelper.waitVisibility(gitStatusBarInfoPanel);
  }

  public String getTextStatus() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(gitStatusBarInfoPanel);
  }

  public void waitCloseGitStatusBarInfoPanel() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.GIT_BAR_INFO_PANEL));
  }

  public void waitMessageInGitTab(final String message) {
    waitGitStatusBarInfoPanel();

    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            seleniumWebDriverHelper.waitSuccessCondition(
                webDriver -> getTextStatus().contains(message), LOAD_PAGE_TIMEOUT_SEC),
        StaleElementReferenceException.class);
  }

  /** wait and click on navigate button in the 'git console' */
  public void waitAndClickOnNavigateBtn() {
    seleniumWebDriverHelper.waitAndClick(gitStatusBarNaviBtn);
  }

  /** wait and click on remove button in the 'git console' */
  public void waitAndClickOnRemoveBtn() {
    seleniumWebDriverHelper.waitAndClick(gitStatusBarRemoveBtn);
  }

  /** click on the 'minimize 'button */
  public void waitAndClickOnStatusBarMinimizeBtn() {
    seleniumWebDriverHelper.waitAndClick(gitBarHideBtn);
  }
}
