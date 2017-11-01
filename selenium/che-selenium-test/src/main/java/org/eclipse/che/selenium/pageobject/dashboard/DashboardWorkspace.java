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
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Alex
 * @author Andrey Chizhikov
 */
@Singleton
public class DashboardWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final Dashboard dashboard;
  private final DashboardProject dashboardProject;

  public interface TabNames {
    String OVERVIEW = "Overview";
    String PROJECTS = "Projects";
    String MACHINES = "Machines";
    String SERVERS = "Servers";
    String INSTALLERS = "Installers";
    String ENV_VARIABLES = "Env Variables";
    String CONFIG = "Config";
    String SSH = "Ssh";
    String SHARE = "Share";
  }

  private interface Locators {
    String NEW_WORKSPACE_BTN =
        "//a[contains(@ng-href, 'create-workspace')]/span[text()='Add Workspace']";
    String TOOLBAR_TITLE_NAME =
        "//div[contains(@class,'che-toolbar')]//span[contains(text(),'%s')]";
    String WORKSPACES_LIST = "//ng-transclude[@class='che-list-content']";
    String WORKSPACE_ITEM_NAME =
        "//div[contains(@class, 'workspace-name-clip')]//div[contains(@data-str, '%s')]";
    String EXPORT_WORKSPACE_BTN =
        "//button[contains(@class, 'che-button')]/span[text()='Export as a file']";
    String DOWNLOAD_WORKSPACE_BTN = "//che-button-default[@che-button-title='download']";
    String CLIPBOARD_JSON_WS_BTN = "//che-button-default[@che-button-title='clipboard']";
    String HIDE_JSON_WS_BTN = "//span[text()='Close']";
    String WORKSPACE_JSON_CONTENT = "//div[@class='CodeMirror-code']";
    String IMPORT_WORKSPACE_BTN =
        "//md-radio-button[@aria-label='Import an existing workspace configuration']";
    String RECIPE_TYPE_BUTTON = "//button/span[text()='%s']";
    String RECIPE_URL_FIELD = "//input[@placeholder='URL of the Recipe']";
    String OPEN_IN_IDE_WS_BTN = "//che-button-default[@che-button-title='Open']";
    String STACK_NAME_XPATH = "//md-card[contains(@ng-class,'%s')]";
    String NAME_WORKSPACE_INPUT = "//input[@placeholder='Name of the workspace *']";
    String RUN_WORKSPACE_BTN = "//button/span[text()='Run']";
    String STOP_WORKSPACE_BTN = "//button/span[contains(text(),'Stop')]";
    String DELETE_WORKSPACE_BTN = "//button/span[text()='Delete']";
    String WORKSPACE_STATE = "workspace-status";
    String WORKSPACE_TITLE = "//div[contains(@class,'toolbar-info')]/span[text()='%s']";
    String DELETE_BTN_DIALOG_WIND =
        "//button[@ng-click='cheConfirmDialogController.hide()']//span[text()='Delete']";
    String CREATE_WS_FROM_STACK =
        "//md-radio-button[@aria-label='Create new workspace from stack']";
    String STACK_LIBRARY_ITEM = "//div[text()='%s']";
    String RESENT_WS_NAVBAR = "//div[@class='admin-navbar-menu recent-workspaces']";
    String LEFT_SIDE_BAR = "//div[@class='left-sidebar-container']";
    String ADD_DEVLOPER_BTN = "//span[text()='Add Developer']";
    String REMOVE_DEVELOPER_ICON =
        "//span[text()='%s']//following::div[@tooltip='Remove member'][1]";
    String INPUT_SHARE_DIALOG = "//md-chips[contains(@class,'share-user-input')]//input";
    String SHARE_BTN_DIALOG = "//che-button-primary[@aria-disabled='false']//span[text()='Share']";
    String DEVELOPER_SHARE_ITEM = "//span[text()='%s']";
    String RUNTIME_TAB = "//span[text()='Runtime']";
    String DEV_MACHINE = "//div[@class='workspace-machine-config']//div[text()='dev-machine']";
    String WARNING_MSG = "//div[@class='workspace-environments-input']//div[text()='%s']";
    String CONFIG_MACHINE_SWITCH =
        "//div[text()='%s']//following::div[@class='config-dev-machine-switch ng-scope'][1]";
    String TAB_NAMES_IN_WS = "//md-pagination-wrapper//span[text()='%s']";
    String RECIPE_EDITOR =
        "//div[contains(@class, 'recipe-editor')]//div[@class='CodeMirror-code']";
    String WARNING_DIALOG_DELETE = "//div[@class='ng-binding' and text()=\"%s\"]";
    String WS_TIMEOUT_MESSAGE = "//div[@che-label-name='Idle timeout']//div[@ng-transclude]";
    String SAVE_BUTTON = "//che-button-save-flat//span[text()='Save']";
    String CANCEL_BUTTON = "//che-button-cancel-flat//span[text()='Cancel']";
    String DELETE_BUTTON = "//che-button-primary[@che-button-title='Delete']/button";
    String ADD_MACHINE_BUTTON = "//che-button-primary[@che-button-title = 'Add machine']/button";
    String RAM_WORKSPACE = "//input[contains(@name, 'memory')]";
    String MACHINE_NAME = "//span[@machine-name='%s']";
    String MACHINE_IMAGE = "//span[@machine-image='%s']";
    String EDIT_MACHINE = "//div[@edit-machine='%s']";
    String DELETE_MACHINE = "//div[@delete-machine='%s']";
    String NEW_MACHINE_NAME = "//div[@che-form='editMachineForm']//input";
    String EDIT_MACHINE_NAME_BUTTON = "//che-button-primary[@che-button-title='Edit']/button";
    String EDIT_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Edit the machine']";
    String REMOVE_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Remove machine']";
    String ADD_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Add a new machine']";
    String CANCEL_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Cancel']";
    String CLOSE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Close']";
    String DELETE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Delete']";
    String UPDATE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Update']";
    String ADD_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Add']";
    String MACHINE_BUTTON =
        "//che-machine-selector[@content-title='%s']//toggle-single-button[@id = 'workspace-machine-%s']//span";
    String ENV_VARIABLE = "env-variable-name-%s";
    String ADD_ENV_VARIABLE_BUTTON =
        "//che-button-primary[@che-button-title='Add Env Variable']/button";
    String ADD_ENV_VARIABLE_DIALOG_NAME =
        "//md-dialog/che-popup[@title='Add a new environment variable']";
    String VARIABLE_CHECKBOX = "//md-checkbox[@aria-label='Environment-Variable-%s']";
    String VARIABLE_NAME = "//span[@variable-name='%s']";
    String VARIABLE_VALUE = "//div[@id='env-variable-name-%s']//span[@variable-value='%s']";
    String NEW_ENV_VARIABLE_NAME = "variable-name-input";
    String NEW_ENV_VARIABLE_VALUE = "//textarea[@name='deskvalue']";
    String EDIT_ENV_VARIABLE = "//div[@edit-variable='%s']";
    String DELETE_ENV_VARIABLE = "//div[@delete-variable='%s']";
    String SERVER = "server-name-%s";
    String SERVER_REFERENCE = "//span[@server-reference='%s']";
    String SERVER_PORT = "//div[@id='server-name-%s']//span[@server-port='%s']";
    String SERVER_PROTOCOL = "//div[@id='server-name-%s']//span[@server-protocol='%s']";
    String EDIT_SERVER_BUTTON = "//div[@edit-server='%s']";
    String DELETE_SERVER_BUTTON = "//div[@delete-server='%s']";
    String ADD_SERVER_BUTTON = "//che-button-primary[@che-button-title='Add Server']/button";
    String ADD_NEW_SERVER_DIALOG_NAME = "//md-dialog/che-popup[@title='Add a new server']";
    String ADD_SERVER_REFERENCE_FIELD = "server-reference-input";
    String ADD_SERVER_PORT_FIELD = "server-port-input";
    String ADD_SERVER_PROTOCOL_FIELD = "server-protocol-input";
    String INSTALLER_NAME = "//span[@agent-name='%s']";
    String INSTALLER_DESCRIPTION = "//span[@agent-description='%s']";
    String INSTALLER_STATE = "//md-switch[@agent-switch='%s']";
    String ADD_NEW_PROJECT_BUTTON = "//che-button-primary[@che-button-title='Add Project']/button";
    String ADD_PROJECT_BUTTON = "//che-button-primary[@name='addButton']/button";
    String SAMPLE_CHECKBOX_XPATH = "//md-checkbox[@aria-label='Sample %s']";
    String WS_NAME_ERROR_MESSAGES = "//che-error-messages";
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

  @Inject
  public DashboardWorkspace(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      Dashboard dashboard,
      DashboardProject dashboardProject) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.dashboard = dashboard;
    this.dashboardProject = dashboardProject;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(xpath = Locators.NEW_WORKSPACE_BTN)
  WebElement newWorkspaceBtn;

  @FindBy(xpath = Locators.WORKSPACES_LIST)
  WebElement listOfWorkspaces;

  @FindBy(xpath = Locators.EXPORT_WORKSPACE_BTN)
  WebElement exportWsButton;

  @FindBy(xpath = Locators.DOWNLOAD_WORKSPACE_BTN)
  WebElement downloadWsJsonBtn;

  @FindBy(xpath = Locators.CLIPBOARD_JSON_WS_BTN)
  WebElement clipboardWsJsonBtn;

  @FindBy(xpath = Locators.HIDE_JSON_WS_BTN)
  WebElement hideJsonWsBtn;

  @FindBy(xpath = Locators.IMPORT_WORKSPACE_BTN)
  WebElement importWsRadioBtn;

  @FindBy(xpath = Locators.WORKSPACE_JSON_CONTENT)
  WebElement workspaceJsonContent;

  @FindBy(xpath = Locators.RECIPE_URL_FIELD)
  WebElement recipeUrlField;

  @FindBy(xpath = Locators.OPEN_IN_IDE_WS_BTN)
  WebElement openInIdeWsBtn;

  @FindBy(xpath = Locators.NAME_WORKSPACE_INPUT)
  WebElement nameWorkspaceInput;

  @FindBy(xpath = Locators.RUN_WORKSPACE_BTN)
  WebElement runWorkspaceBtn;

  @FindBy(xpath = Locators.STOP_WORKSPACE_BTN)
  WebElement stopWorkspaceBtn;

  @FindBy(xpath = Locators.DELETE_WORKSPACE_BTN)
  WebElement deleteWorkspaceBtn;

  @FindBy(xpath = Locators.DELETE_BTN_DIALOG_WIND)
  WebElement deleteItBtn;

  @FindBy(xpath = Locators.RAM_WORKSPACE)
  WebElement ramWorkspace;

  @FindBy(xpath = Locators.SAVE_BUTTON)
  WebElement saveBtn;

  @FindBy(xpath = Locators.DELETE_BUTTON)
  WebElement deleteBtn;

  @FindBy(xpath = Locators.CANCEL_BUTTON)
  WebElement cancelBtn;

  @FindBy(xpath = Locators.WS_TIMEOUT_MESSAGE)
  WebElement wsTimeotMessage;

  @FindBy(id = Locators.WORKSPACE_STATE)
  WebElement workspaceState;

  @FindBy(xpath = Locators.WS_NAME_ERROR_MESSAGES)
  WebElement errorMessages;

  public void waitToolbarTitleName(String titleName) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.TOOLBAR_TITLE_NAME, titleName))));
  }

  public void clickOnNewWorkspaceBtn() {
    dashboard.waitNotificationIsClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(newWorkspaceBtn))
        .click();
  }

  public void waitListWorkspacesOnDashboard() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(listOfWorkspaces));
  }

  public void waitExpTextFromListWsOnDashboard(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromListWorkspaces().contains(expText));
  }

  public String getTextFromListWorkspaces() {
    return listOfWorkspaces.getText();
  }

  public void selectWorkspaceItemName(String wsName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, wsName))))
        .click();
  }

  public void clickExportWorkspaceBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(exportWsButton))
        .click();
  }

  public void waitDownloadWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(downloadWsJsonBtn));
  }

  public void waitClipboardWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(clipboardWsJsonBtn));
  }

  public void clickOnHideWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(hideJsonWsBtn))
        .click();
  }

  public void clickIntoWorkspaceJsonContent() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(workspaceJsonContent))
        .click();
  }

  /**
   * wait the warning message when there is two or more machines
   *
   * @param mess is the message into workspace machine config
   */
  public void waitWarningMessage(String mess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.WARNING_MSG, mess))));
  }

  /**
   * switch the config machine
   *
   * @param nameMachine is the machine name
   */
  public void switchConfigMachine(String nameMachine) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.CONFIG_MACHINE_SWITCH, nameMachine))))
        .click();
  }

  public void waitCustomRecipeUrlField() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(recipeUrlField));
  }

  /** wait 'Open in IDE' btn on All workspaces page' */
  public void waitOpenInIdeWsEBtn() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.visibilityOf(openInIdeWsBtn));
  }

  /** click 'Open in IDE btn on All workspaces page' */
  public void clickOpenInIdeWsBtn() {
    dashboard.waitNotificationIsClosed();
    waitOpenInIdeWsEBtn();
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.elementToBeClickable(openInIdeWsBtn))
        .click();
  }

  public void enterNameWorkspace(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.visibilityOf(nameWorkspaceInput))
        .clear();
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.visibilityOf(nameWorkspaceInput))
        .sendKeys(nameWorkspace);
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(nameWorkspaceInput, nameWorkspace));
  }

  /**
   * enter value RAM of workspace
   *
   * @param ram is value of RAM
   */
  public void enterRamWorkspace(int ram) {
    String ramValue = "//input[@name='memory']";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(ramWorkspace))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(ramWorkspace))
        .sendKeys(String.valueOf(ram));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.xpath(ramValue), String.valueOf(ram)));
  }

  /**
   * Check state of workspace in 'Workspace Information'
   *
   * @param stateWorkspace expected state of workspace
   */
  public void checkStateOfWorkspace(StateWorkspace stateWorkspace) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElement(
                workspaceState, stateWorkspace.getStatus()));
  }

  /** click on 'RUN' button in 'Workspace Information' */
  public void clickOnRunWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(runWorkspaceBtn))
        .click();
  }

  /** click on 'STOP' button in 'Workspace Information' */
  public void clickOnStopWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(stopWorkspaceBtn))
        .click();
  }

  /** click on 'DELETE' button in 'Delete workspace' */
  public void clickOnDeleteWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(deleteWorkspaceBtn))
        .click();
  }

  /**
   * Check name of workspace in 'Workspace' tab
   *
   * @param nameWorkspace expected name of workspace
   */
  public void checkNameWorkspace(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_TITLE, nameWorkspace))));
  }

  /** Click on the delete/remove button in the dialog window */
  public void clickOnDeleteItInDialogWindow() {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(deleteItBtn))
        .click();
  }

  /** wait the workspace is not present on dashboard */
  public void waitWorkspaceIsNotPresent(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, nameWorkspace))));
  }

  public void clickOnSaveBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(saveBtn))
        .click();
  }

  public void clickOnDeleteBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(deleteBtn))
        .click();
  }

  public void clickOnCancelBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(cancelBtn))
        .click();
  }

  /** Return true if workspaces present on the navigation panel */
  public boolean workspacesIsPresent() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.LEFT_SIDE_BAR)));

    List<WebElement> workspaces =
        seleniumWebDriver.findElements(By.xpath(Locators.RESENT_WS_NAVBAR));
    return !(workspaces.size() == 0);
  }

  /**
   * Adds developer to share list
   *
   * @param email is an email of developer into sharing list
   */
  public void addDeveloperToShareList(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.ADD_DEVLOPER_BTN)))
        .click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.INPUT_SHARE_DIALOG)))
        .sendKeys(email + ",");
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SHARE_BTN_DIALOG)))
        .click();
  }

  /**
   * Delete a developer from sharing list
   *
   * @param email is an email of developer into sharing list
   */
  public void deleteDeveloperFromShareList(String email) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.REMOVE_DEVELOPER_ICON, email))))
        .click();
  }

  /** Wait the text into warning dialog delete or remove */
  public void waitTextInWarningDialogDelete(String expText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.WARNING_DIALOG_DELETE, expText))));
  }

  /**
   * Wait the email of developer is present in 'Share' tab
   *
   * @param email the email of developer
   */
  public void waitDeveloperIsPresentInShareTab(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.DEVELOPER_SHARE_ITEM, email))));
  }

  /**
   * Wait the email of developer is not present in 'Share' tab
   *
   * @param email the email of developer
   */
  public void waitDeveloperIsNotPresentInShareTab(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.DEVELOPER_SHARE_ITEM, email))));
  }

  /**
   * Select tab into workspace menu
   *
   * @param tabName is the tab name into workspace menu
   */
  public void selectTabInWorspaceMenu(String tabName) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_NAMES_IN_WS, tabName))))
        .click();
  }

  public void clickNewProjectButtonOnDashboard() throws Exception {
    dashboard.clickOnNewProjectLinkOnDashboard();
  }

  public String getWsTimeoutMessage() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(wsTimeotMessage))
        .getText();
  }

  public void waitWsTimeoutMessage(String expectedMessage) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver webdriver) -> webdriver.findElement(By.xpath(Locators.WS_TIMEOUT_MESSAGE)))
        .getText()
        .contains(expectedMessage);
  }

  /**
   * Check if machine exists in machines list
   *
   * @param machineName the name of machine
   */
  public void checkMachineExists(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.MACHINE_NAME, machineName))));
    loader.waitOnClosed();
  }

  public void checkMachineIsNotExists(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.MACHINE_NAME, machineName))));
    loader.waitOnClosed();
  }

  public void clickOnAddMachineButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.ADD_MACHINE_BUTTON)))
        .click();
  }

  /**
   * Click on the Edit Machine button
   *
   * @param machineName the name of machine
   */
  public void clickOnEditMachineButton(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.EDIT_MACHINE, machineName))))
        .click();
  }

  /**
   * Click on the Delete Machine button
   *
   * @param machineName the name of machine
   */
  public void clickOnDeleteMachineButton(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.DELETE_MACHINE, machineName))))
        .click();
  }

  /** Check that the Add New Machine dialog is opened */
  public void checkAddNewMachineDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.ADD_MACHINE_DIALOG_NAME)));
  }

  /** Check that the Edit Machine dialog is opened */
  public void checkEditTheMachineDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.EDIT_MACHINE_DIALOG_NAME)));
  }

  public void setMachineNameInDialog(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.NEW_MACHINE_NAME)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.NEW_MACHINE_NAME)))
        .sendKeys(machineName);
  }

  public void clickOnEditDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.EDIT_MACHINE_NAME_BUTTON)))
        .click();
  }

  public void clickOnCloseDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.CLOSE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnCancelDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.CANCEL_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnDeleteDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.DELETE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnUpdateDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.UPDATE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnAddDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.ADD_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnAddServerButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.ADD_SERVER_BUTTON)))
        .click();
  }

  public void waitAddServerDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.ADD_NEW_SERVER_DIALOG_NAME)));
  }

  public void enterReference(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.ADD_SERVER_REFERENCE_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.ADD_SERVER_REFERENCE_FIELD)))
        .sendKeys(name);
  }

  public void enterPort(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PORT_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PORT_FIELD)))
        .sendKeys(name);
  }

  public void enterProtocol(String protocol) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.ADD_SERVER_PROTOCOL_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.ADD_SERVER_PROTOCOL_FIELD)))
        .sendKeys(protocol);
  }

  public void checkServerName(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.SERVER_REFERENCE, serverName))));
  }

  public void checkServerExists(String serverName, String port) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.SERVER_PORT, serverName, port))));
    loader.waitOnClosed();
  }

  public void checkServerIsNotExists(String serverName, String port) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.SERVER_PORT, serverName, port))));
    loader.waitOnClosed();
  }

  public void clickOnDeleteServerButton(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.DELETE_SERVER_BUTTON, serverName))))
        .click();
  }

  public void clickOnEditServerButton(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.EDIT_SERVER_BUTTON, serverName))))
        .click();
  }

  public void selectMachine(String tabName, String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.MACHINE_BUTTON, tabName, machineName))))
        .click();
  }

  public void checkInstallerExists(String installerName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_NAME, installerName))));
  }

  public void switchInstallerState(String installerName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_STATE, installerName))))
        .click();
  }

  public Boolean getInstallerState(String installerName) {
    String state =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(format(Locators.INSTALLER_STATE, installerName))))
            .getAttribute("aria-checked");

    return Boolean.parseBoolean(state);
  }

  public String checkInstallerDescription(String installerName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_DESCRIPTION, installerName))))
        .getText();
  }

  public void clickOnAddEnvVariableButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.ADD_ENV_VARIABLE_BUTTON)))
        .click();
  }

  public void checkAddNewEnvVarialbleDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.ADD_ENV_VARIABLE_DIALOG_NAME)));
  }

  public void enterEnvVariableName(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.NEW_ENV_VARIABLE_NAME)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.NEW_ENV_VARIABLE_NAME)))
        .sendKeys(name);
  }

  public void enterEnvVariableValue(String value) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.NEW_ENV_VARIABLE_VALUE)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.NEW_ENV_VARIABLE_VALUE)))
        .sendKeys(value);
  }

  public void addNewEnvironmentVariable(String name, String value) {
    enterEnvVariableName(name);
    enterEnvVariableValue(value);
  }

  public Boolean checkEnvVariableExists(String varName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_NAME, varName))))
        .getText()
        .equals(varName);
  }

  public Boolean checkValueExists(String varName, String varValue) {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_VALUE, varName, varValue))))
        .isDisplayed();
  }

  public void checkValueIsNotExists(String varName, String varValue) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_VALUE, varName, varValue))));
    loader.waitOnClosed();
  }

  public void clickOnDeleteEnvVariableButton(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.DELETE_ENV_VARIABLE, varName))))
        .click();
  }

  public void clickOnEditEnvVariableButton(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.EDIT_ENV_VARIABLE, varName))))
        .click();
  }

  public void clickOnEnvVariableCheckbox(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_CHECKBOX, varName))))
        .click();
  }

  public void clickOnAddNewProjectButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.ADD_NEW_PROJECT_BUTTON)))
        .click();
  }

  public void clickOnAddProjects() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.ADD_PROJECT_BUTTON)))
        .click();
  }

  public void selectSample(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.SAMPLE_CHECKBOX_XPATH, name))))
        .click();
  }

  public Boolean isWorkspaceNameErrorMessageEquals(String message) {
    return errorMessages.getText().equals(message);
  }
}
