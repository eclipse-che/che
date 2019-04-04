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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Alex
 * @author Andrey Chizhikov
 */
@Singleton
public class WorkspaceDetails {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final Loader loader;
  private final Dashboard dashboard;

  private interface Locators {
    String WORKSPACE_STATE = "workspace-status";
    String RUN_WORKSPACE_BTN = "run-workspace-button";
    String STOP_WORKSPACE_BTN = "stop-workspace-button";
    String OPEN_IN_IDE_WS_BTN = "open-in-ide-button";
    String TAB_NAMES_IN_WS = "//md-pagination-wrapper//span[text()='%s']";
    String SAVE_CHANGED_BUTTON = "//button[@name='save-button']";
    String APPLY_CHANGES_BUTTON = "//che-button-save-flat[@class='apply-button']";
    String CANCEL_CHANGES_BUTTON = "//button[@name='cancel-button']";
    String CANCEL_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Cancel']";
    String CLOSE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Close']";
    String DELETE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Delete']";
    String UPDATE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Update']";
    String ADD_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Add']";
    String TOOLBAR_TITLE_NAME =
        "//div[contains(@class,'che-toolbar')]//span[contains(text(),'%s')]";
    String ORGANIZATION_NAME_ID = "namespace-name";
    String OPEN_ORGANIZATION_BUTTON_ID = "open-namespace-button";
  }

  public enum WorkspaceDetailsTab {
    OVERVIEW("Overview"),
    PROJECTS("Projects"),
    MACHINES("Containers"),
    SERVERS("Servers"),
    ENV_VARIABLES("Env Variables"),
    VOLUMES("Volumes"),
    CONFIG("Config"),
    SSH("SSH"),
    SHARE("Share");

    private final String tabTitle;

    WorkspaceDetailsTab(String tabTitle) {
      this.tabTitle = tabTitle;
    }

    public String getTabTitle() {
      return this.tabTitle;
    }
  }

  public enum StateWorkspace {
    STOPPED("Stopped"),
    STARTING("Starting"),
    RUNNING("Running"),
    STOPPING("Stopping");

    private final String status;

    StateWorkspace(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }
  }

  public enum ActionButton {
    SAVE_BUTTON(By.name("save-button")),
    APPLY_BUTTON(By.name("apply-button")),
    CANCEL_BUTTON(By.name("cancel-button"));

    private By buttonLocator;

    ActionButton(By buttonLocator) {
      this.buttonLocator = buttonLocator;
    }

    public By getLocator() {
      return this.buttonLocator;
    }
  }

  @Inject
  public WorkspaceDetails(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      Loader loader,
      Dashboard dashboard) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.loader = loader;
    this.dashboard = dashboard;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = Locators.RUN_WORKSPACE_BTN)
  WebElement runWorkspaceBtn;

  @FindBy(id = Locators.STOP_WORKSPACE_BTN)
  WebElement stopWorkspaceBtn;

  @FindBy(id = Locators.OPEN_IN_IDE_WS_BTN)
  WebElement openInIdeWsBtn;

  @FindBy(id = Locators.WORKSPACE_STATE)
  WebElement workspaceState;

  @FindBy(xpath = Locators.SAVE_CHANGED_BUTTON)
  WebElement saveBtn;

  @FindBy(xpath = Locators.APPLY_CHANGES_BUTTON)
  WebElement applyButton;

  @FindBy(xpath = Locators.CANCEL_CHANGES_BUTTON)
  WebElement cancelBtn;

  @FindBy(xpath = Locators.DELETE_DIALOG_BUTTON)
  WebElement deleteBtn;

  @FindBy(xpath = Locators.CLOSE_DIALOG_BUTTON)
  WebElement closeBtn;

  public WebElement wait(ActionButton actionButton) {
    return seleniumWebDriverHelper.waitVisibility(actionButton.getLocator());
  }

  public void waitAllInvisibility(ActionButton... actionButtons) {
    asList(actionButtons)
        .forEach(
            actionButton -> seleniumWebDriverHelper.waitInvisibility(actionButton.getLocator()));
  }

  public void waitAndClickOn(ActionButton actionButton) {
    wait(actionButton).click();
  }

  private void waitState(ActionButton actionButton, boolean enabled) {
    waitState(actionButton.getLocator(), enabled);
  }

  private void waitState(By locator, boolean enabled) {
    final String buttonStateAttribute = "aria-disabled";
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        locator, buttonStateAttribute, Boolean.toString(!enabled));
  }

  public void waitAllDisabled(ActionButton... actionButtons) {
    asList(actionButtons).forEach(actionButton -> waitState(actionButton, false));
  }

  public void waitAllEnabled(ActionButton... actionButtons) {
    asList(actionButtons).forEach(actionButton -> waitState(actionButton, true));
  }

  /**
   * Check state of workspace in 'Workspace Information'
   *
   * @param stateWorkspace expected state of workspace
   */
  public void checkStateOfWorkspace(StateWorkspace stateWorkspace) {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(textToBePresentInElement(workspaceState, stateWorkspace.getStatus()));
  }

  public void checkStateOfWorkspace(StateWorkspace stateWorkspace, int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(textToBePresentInElement(workspaceState, stateWorkspace.getStatus()));
  }

  /** click on 'RUN' button in 'Workspace Information' */
  public void clickOnRunWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(runWorkspaceBtn))
        .click();
  }

  /** click on 'STOP' button in 'Workspace Information' */
  public void clickOnStopWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(stopWorkspaceBtn))
        .click();
  }

  /** wait 'Open in IDE' btn on All workspaces page' */
  public void waitOpenInIdeWsEBtn() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(visibilityOf(openInIdeWsBtn));
  }

  /** click 'Open in IDE btn on All workspaces page' */
  public void clickOpenInIdeWsBtn() {
    dashboard.waitNotificationIsClosed();
    waitOpenInIdeWsEBtn();
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(elementToBeClickable(openInIdeWsBtn))
        .click();
  }

  /**
   * Select tab into workspace menu
   *
   * @param tab is the tab name into workspace menu
   */
  public void selectTabInWorkspaceMenu(WorkspaceDetailsTab tab) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.TAB_NAMES_IN_WS, tab.getTabTitle()))))
        .click();
  }

  public void clickOnSaveChangesBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(saveBtn))
        .click();
  }

  public void clickOnApplyChangesBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(applyButton))
        .click();
  }

  public void clickOnCancelChangesBtn() {
    // this timeout is needed for the Cancel to appears after renaming of a workspace
    WaitUtils.sleepQuietly(3);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(cancelBtn))
        .click();
  }

  /** Click on the delete/remove button in the dialog window */
  public void clickOnDeleteButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(deleteBtn))
        .click();
  }

  public void clickOnCloseButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(closeBtn))
        .click();
  }

  public void clickOnCancelButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.CANCEL_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnUpdateButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.UPDATE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnAddButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_DIALOG_BUTTON)))
        .click();
  }

  /**
   * Wait toolbar name is present on dashboard
   *
   * @param titleName name of user
   */
  public void waitToolbarTitleName(String titleName) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.TOOLBAR_TITLE_NAME, titleName))));
  }

  public String getOrganizationName() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ORGANIZATION_NAME_ID)))
        .getText();
  }

  public void clickOnOpenOrganizationButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.OPEN_ORGANIZATION_BUTTON_ID)))
        .click();
  }
}
