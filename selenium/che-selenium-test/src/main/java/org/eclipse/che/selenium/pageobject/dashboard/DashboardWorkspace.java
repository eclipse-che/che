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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
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

    String SETTINGS = "Settings";
    String WS_CONFIG = "Workspace Config";
    String RUNTIME = "Runtime";
    String STACK_LIBRARY = "Stack library";
    String STACK_IMPORT = "Stack import";
    String STACK_AUTHORING = "Stack authoring";
    String PROJECTS = "Projects";
    String SHARE = "Share";
  }

  // names of ready-to-go stacks
  public interface ReadyGoToStacks {
    String JAVA = "java-default";
    String NODE = "node-default";
    String JAVA_MYSQL = "java-mysql";
  }

  public interface RecipeTypeBtn {
    String COMPOSE = "compose";
    String DOCKERFILE = "dockerfile";
    String MACHINES_WARNING_MSG =
        "The environment should contain exactly one dev machine. "
            + "Switch on Dev property to have terminal, SSH and IDE tooling injected to the machine.";
  }

  public enum StackLibrary {
    BITNAMI_CODEIGNITER("Bitnami Codeigniter"),
    BITNAMI_SYMFONY("Bitnami Symfony"),
    BITNAMI_PLAY_FOR_JAVA("Bitnami Play for Java"),
    BITNAMI_RAILS("Bitnami Rails"),
    BITNAMI_EXPRESS("Bitnami Express"),
    BITNAMI_LARAVEL("Bitnami Laravel"),
    BITNAMI_SWIFT("Bitnami Swift");

    private String name;

    StackLibrary(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private interface Locators {
    String NEW_WORKSPACE_BTN =
        "//a[contains(@ng-href, 'create-workspace')]/span[text()='Add Workspace']";
    String TOOLBAR_TITLE_NAME =
        "//div[contains(@class,'che-toolbar-title')]/span[contains(text(),'%s')]";
    String WORKSPACES_LIST = "//ng-transclude[@class='che-list-content']";
    String WORKSPACE_ITEM_NAME =
        "//div[contains(@class, 'che-list-item-name')]/span[contains(text(),'%s')]";
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
    String NAME_WORKSPACE_INPUT = "//input[@placeholder='Name of the workspace']";
    String RUN_WORKSPACE_BTN = "//button/span[text()='Run']";
    String STOP_WORKSPACE_BTN = "//button/span[contains(text(),'Stop')]";
    String DELETE_WORKSPACE_BTN = "//button/span[text()='Delete']";
    String STATE_WORKSPACE = "//span[text()='%s']";
    String WORKSPACE_TITLE = "//span[contains(@class,'che-toolbar-title-label') and text()='%s']";
    String DELETE_BTN_DIALOG_WIND =
        "//button[@ng-click='cheConfirmDialogController.hide()']//span[text()='Delete']";
    String RAM_WORKSPACE = "//input[contains(@ng-model, 'cheWorkspaceRamAllocation')]";
    String CREATE_WS_FROM_STACK =
        "//md-radio-button[@aria-label='Create new workspace from stack']";
    String STACK_LIBRARY_ITEM = "//div[text()='%s']";
    String SAVE_BUTTON = "//md-toolbar//span[text()='Save']";
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
    String TAB_NAMES_IN_WS = "//md-tab-item//span[text()='%s']";
    String RECIPE_EDITOR =
        "//div[contains(@class, 'recipe-editor')]//div[@class='CodeMirror-code']";
    String WARNING_DIALOG_DELETE = "//div[@class='ng-binding' and text()=\"%s\"]";
    String WS_TIMEOUT_MESSAGE = "//div[@che-label-name='Idle timeout']//div[@ng-transclude]";
  }

  public enum StateWorkspace {
    STOPPED("stopped"),
    STARTING("starting"),
    RUNNING("running"),
    STOPPING("stopping");

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

  @FindBy(xpath = Locators.WS_TIMEOUT_MESSAGE)
  WebElement wsTimeotMessage;

  public void waitToolbarTitleName(String titleName) {
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOADER_TIMEOUT_SEC)
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

  public void clickOnImportWorkspaceBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(importWsRadioBtn))
        .click();
  }

  public void selectRecipeType(String recipeType) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.RECIPE_TYPE_BUTTON, recipeType))))
        .click();
  }

  /** set the focus into 'recipe-editor' form in the 'Stack authoring' */
  public void clickIntoWorkspaceRecipeEditor() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.RECIPE_EDITOR)))
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

  public void typeCustomRecipeUrl(String nameOfWs) {
    waitCustomRecipeUrlField();
    recipeUrlField.clear();
    recipeUrlField.sendKeys(nameOfWs);
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

  /**
   * select defined stack from 'READY-TO-GO STACKS' tab
   *
   * @param stackName name of stack from 'READY-TO-GO STACKS'
   */
  public void selectReadyToGoStack(String stackName) {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.STACK_NAME_XPATH, stackName))))
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
    String ramValue =
        "//div[@class='che-ram-allocation-slider ng-isolate-scope ng-valid']//div[@class='ng-binding']";
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
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.STATE_WORKSPACE, stateWorkspace.getStatus()))));
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
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, nameWorkspace))));
  }

  /** Select 'Create new workspace from stack' on the 'New Workspace' page */
  public void selectCreateNewWorkspaceFromStack() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.CREATE_WS_FROM_STACK)))
        .click();
  }

  /**
   * Select stack library by name
   *
   * @param stackLibrary name of stack
   */
  public void selectStackLibrary(StackLibrary stackLibrary) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.STACK_LIBRARY_ITEM, stackLibrary.getName()))))
        .click();
  }

  /** Click on 'Save' on the 'New Workspace' page */
  public void clickOnSaveBtn() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(saveBtn))
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

  /** Select 'Runtime' tab in workspace menu */
  public void selectRuntimeTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.RUNTIME_TAB)))
        .click();
  }

  /** Expand 'DEV-MACHINE' settings on the 'Runtime' tab */
  public void expandDevMachineSettings() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.DEV_MACHINE)))
        .click();
  }

  /**
   * Select tab into workspace menu
   *
   * @param tabName is the tab name into workspace menu
   */
  public void selectTabInWorspaceMenu(String tabName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_NAMES_IN_WS, tabName))))
        .click();
  }

  public void clickNewProjectButtonOnDashboard() throws Exception {
    dashboard.clickOnNewProjectLinkOnDashboard();
  }

  public void createNewWorkspaceFromStackLibrary(
      StackLibrary stackLibrary, String workspaceName, String projectName) {
    selectCreateNewWorkspaceFromStack();
    selectTabInWorspaceMenu(DashboardWorkspace.TabNames.STACK_LIBRARY);
    selectStackLibrary(stackLibrary);
    enterNameWorkspace(workspaceName);
    dashboardProject.selectTemplateProject(projectName);
    dashboardProject.clickOnCreateProject();
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
}
