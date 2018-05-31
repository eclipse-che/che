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
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ADD_OR_IMPORT_PROJECT_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ALL_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.BOTTOM_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.EDIT_WORKSPACE_DIALOG_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ERROR_MESSAGE;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.FILTER_SELECTED_SUGGESTION_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.INCREMENT_MEMORY_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.MULTI_MACHINE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.OPEN_IN_IDE_DIALOG_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.ORGANIZATIONS_LIST_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.QUICK_START_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.SINGLE_MACHINE_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOOLBAR_TITLE_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.TOP_CREATE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.WORKSPACE_CREATED_DIALOG;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Ann Shumilova
 * @author Ihor Okhrimenko
 */
@Singleton
public class NewWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;
  private final ActionsFactory actionsFactory;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;
  private static final int DEFAULT_TIMEOUT = TestTimeoutsConstants.DEFAULT_TIMEOUT;

  @Inject
  public NewWorkspace(
      SeleniumWebDriver seleniumWebDriver,
      ActionsFactory actionsFactory,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String WORKSPACE_NAME_INPUT = "workspace-name-input";
    String ERROR_MESSAGE = "new-workspace-error-message";
    String TOOLBAR_TITLE_ID = "New_Workspace";
    String SELECT_ALL_STACKS_TAB = "all-stacks-button";
    String SELECT_QUICK_START_STACKS_TAB = "quick-start-button";
    String SELECT_SINGLE_MACHINE_STACKS_TAB = "single-machine-button";
    String SELECT_MULTI_MACHINE_STACKS_TAB = "multi-machine-button";
    String ADD_STACK_BUTTON = "search-stack-input";
    String FILTERS_STACK_BUTTON = "filter-stacks-button";
    String FILTER_STACK_INPUT = "filter-stack-input";
    String FILTER_SUGGESTION_TEXT =
        "//div[contains(@class,'stack-library-filter-suggestion-text')]";
    String FILTER_SUGGESTION_BUTTON =
        "//div[@name='suggestionText' and text()='%s']/../div[@name='suggestionButton']";
    String FILTER_SELECTED_SUGGESTION_BUTTON = "//button[@class='md-chip-remove ng-scope']";
    String SEARCH_INPUT = "//div[@id='search-stack-input']//input";
    String CLEAR_INPUT = "//div[@id='search-stack-input']//div[@role='button']";
    String STACK_ROW_XPATH = "//div[@data-stack-id='%s']";
    String MACHINE_NAME =
        "//span[contains(@class,'ram-settings-machine-item-item-name') and text()='%s']";
    String MACHINE_IMAGE = "//span[@class='ram-settings-machine-item-secondary-color']";
    String DECREMENT_MEMORY_BUTTON =
        "//*[@id='machine-%s-ram']//button[@aria-label='Decrement memory']";
    String INCREMENT_MEMORY_BUTTON =
        "//*[@id='machine-%s-ram']//button[@aria-label='Increment memory']";
    String MACHINE_RAM_VALUE = "//*[@id='machine-%s-ram']//input";
    String WORKSPACE_CREATED_DIALOG = "//md-dialog/che-popup[@title='Workspace Is Created']";
    String EDIT_WORKSPACE_DIALOG_BUTTON = "//che-button-primary//span[text()='Edit']";
    String OPEN_IN_IDE_DIALOG_BUTTON = "//che-button-default//span[text()='Open In IDE']";
    String ORGANIZATIONS_LIST_ID = "namespace-selector";
    String ORGANIZATION_ITEM = "//md-menu-item[text()='%s']";

    // buttons
    String TOP_CREATE_BUTTON_XPATH = "//button[@name='split-button']";
    String BOTTOM_CREATE_BUTTON_XPATH = "//che-button-save-flat/button[@name='saveButton']";
    String ALL_BUTTON_ID = "all-stacks-button";
    String QUICK_START_BUTTON_ID = "quick-start-button";
    String SINGLE_MACHINE_BUTTON_ID = "single-machine-button";
    String MULTI_MACHINE_BUTTON_ID = "multi-machine-button";
    String ADD_OR_IMPORT_PROJECT_BUTTON_ID = "ADD_PROJECT";
  }

  @FindBy(id = Locators.FILTERS_STACK_BUTTON)
  WebElement filtersStackButton;

  @FindBy(id = Locators.FILTER_STACK_INPUT)
  WebElement filterStackInput;

  @FindBy(id = TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(xpath = BOTTOM_CREATE_BUTTON_XPATH)
  WebElement bottomCreateWorkspaceButton;

  @FindBy(xpath = Locators.SEARCH_INPUT)
  WebElement searchInput;

  @FindBy(xpath = Locators.CLEAR_INPUT)
  WebElement clearInput;

  @FindBy(id = Locators.SELECT_ALL_STACKS_TAB)
  WebElement selectAllStacksTab;

  @FindBy(id = Locators.SELECT_QUICK_START_STACKS_TAB)
  WebElement selectQuickStartStacksTab;

  @FindBy(id = Locators.SELECT_SINGLE_MACHINE_STACKS_TAB)
  WebElement selectSingleMachineStacksTab;

  @FindBy(id = Locators.SELECT_MULTI_MACHINE_STACKS_TAB)
  WebElement selectMultiMachineStacksTab;

  public void typeWorkspaceName(String name) {
    seleniumWebDriverHelper.setValue(workspaceNameInput, name);
  }

  public String getWorkspaceNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(workspaceNameInput);
  }

  public void waitErrorMessage(String message) {
    seleniumWebDriverHelper.waitTextEqualsTo(By.id(ERROR_MESSAGE), message);
  }

  public boolean isMachineExists(String machineName) {
    return seleniumWebDriver
            .findElements(By.xpath(format(Locators.MACHINE_NAME, machineName)))
            .size()
        > 0;
  }

  public void clickOnIncrementMemoryButton(String machineName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(INCREMENT_MEMORY_BUTTON, machineName)));
  }

  public void clickOnDecrementMemoryButton(String machineName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.DECREMENT_MEMORY_BUTTON, machineName)));
  }

  public double getRAM(String machineName) {
    String amountOfRam =
        seleniumWebDriverHelper.waitVisibilityAndGetValue(
            By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)));
    return Double.parseDouble(amountOfRam);
  }

  public void setMachineRAM(String machineName, double value) {
    seleniumWebDriverHelper.setValue(
        By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)), Double.toString(value));
  }

  public void clickOnFiltersButton() {
    seleniumWebDriverHelper.waitAndClick(filtersStackButton);
  }

  public void typeToFiltersInput(String value) {
    seleniumWebDriverHelper.setValue(filterStackInput, value);
  }

  public String getTextFromFiltersInput() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(filterStackInput);
  }

  public void clearSuggestions() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(FILTER_SELECTED_SUGGESTION_BUTTON));
  }

  public void selectFilterSuggestion(String suggestion) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.FILTER_SUGGESTION_BUTTON, suggestion)));
  }

  public void waitToolbar() {
    waitToolbar(WIDGET_TIMEOUT_SEC);
  }

  public void waitToolbar(int timeout) {
    seleniumWebDriverHelper.waitVisibility(By.id(TOOLBAR_TITLE_ID), timeout);
  }

  public String getTextFromSearchInput() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(searchInput);
  }

  public void typeToSearchInput(String value) {
    seleniumWebDriverHelper.setValue(searchInput, value);
  }

  public void clearTextInSearchInput() {
    seleniumWebDriverHelper.waitAndClick(clearInput);
  }

  public boolean isStackVisible(String stackName) {
    return seleniumWebDriver
            .findElements(By.xpath(format(Locators.STACK_ROW_XPATH, stackName)))
            .size()
        > 0;
  }

  public void selectStack(String stackId) {
    seleniumWebDriverHelper.moveCursorToAndClick(
        By.xpath(format(Locators.STACK_ROW_XPATH, stackId)));
  }

  public boolean isCreateWorkspaceButtonEnabled() {
    return !Boolean.valueOf(
        seleniumWebDriverHelper.waitVisibilityAndGetAttribute(
            By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled"));
  }

  public void waitCreateWorkspaceButtonEnabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled", "false");
  }

  public void waitCreateWorkspaceButtonDisabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BOTTOM_CREATE_BUTTON_XPATH), "aria-disabled", "true");
  }

  public void clickOnAllStacksTab() {
    seleniumWebDriverHelper.waitAndClick(selectAllStacksTab);
  }

  public void clickOnQuickStartTab() {
    seleniumWebDriverHelper.waitAndClick(selectQuickStartStacksTab);
  }

  public void clickOnSingleMachineTab() {
    seleniumWebDriverHelper.waitAndClick(selectSingleMachineStacksTab);
  }

  public void clickOnMultiMachineTab() {
    seleniumWebDriverHelper.waitAndClick(selectMultiMachineStacksTab);
  }

  public void waitWorkspaceCreatedDialogIsVisible() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(WORKSPACE_CREATED_DIALOG), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnEditWorkspaceButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(EDIT_WORKSPACE_DIALOG_BUTTON), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnOpenInIDEButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OPEN_IN_IDE_DIALOG_BUTTON), ELEMENT_TIMEOUT_SEC);
  }

  public void clickOnCreateButtonAndOpenInIDE() {
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
    waitWorkspaceCreatedDialogIsVisible();
    clickOnOpenInIDEButton();
  }

  public void clickOnCreateButtonAndEditWorkspace() {
    waitCreateWorkspaceButtonEnabled();
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
    waitWorkspaceCreatedDialogIsVisible();
    clickOnEditWorkspaceButton();
  }

  public void clickOnBottomCreateButton() {
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
  }

  public void openOrganizationsList() {
    seleniumWebDriverHelper.waitAndClick(By.id(ORGANIZATIONS_LIST_ID));
  }

  public void selectOrganizationFromList(String organizationName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.ORGANIZATION_ITEM, organizationName)));
  }

  public WebElement waitTopCreateButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(TOP_CREATE_BUTTON_XPATH), timeout);
  }

  public WebElement waitTopCreateButton() {
    return waitTopCreateButton(DEFAULT_TIMEOUT);
  }

  public void clickOnTopCreateButton() {
    waitTopCreateButton().click();
  }

  public WebElement waitNameField(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(workspaceNameInput, timeout);
  }

  public WebElement waitNameField() {
    return waitNameField(DEFAULT_TIMEOUT);
  }

  public WebElement waitAllButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(ALL_BUTTON_ID), timeout);
  }

  public WebElement waitAllButton() {
    return waitAllButton(DEFAULT_TIMEOUT);
  }

  public void clickOnAllButton() {
    waitAllButton().click();
  }

  public WebElement waitQuickStartButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(QUICK_START_BUTTON_ID), timeout);
  }

  public WebElement waitQuickStartButton() {
    return waitQuickStartButton(DEFAULT_TIMEOUT);
  }

  public void clickOnQuickStartButton() {
    waitQuickStartButton().click();
  }

  public WebElement waitSingleMachineButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(SINGLE_MACHINE_BUTTON_ID), timeout);
  }

  public WebElement waitSingleMachineButton() {
    return waitSingleMachineButton(DEFAULT_TIMEOUT);
  }

  public void clickOnSingleMachineButton() {
    waitSingleMachineButton().click();
  }

  public WebElement waitMultiMachineButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(MULTI_MACHINE_BUTTON_ID), timeout);
  }

  public WebElement waitMultiMachineButton() {
    return waitMultiMachineButton(DEFAULT_TIMEOUT);
  }

  public void clickOnMultiMachineButton() {
    waitMultiMachineButton().click();
  }

  public WebElement waitAddOrImportProjectButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(ADD_OR_IMPORT_PROJECT_BUTTON_ID), timeout);
  }

  public WebElement waitAddOrImportProjectButton() {
    return waitAddOrImportProjectButton(DEFAULT_TIMEOUT);
  }

  public void clickOnAddOrImportProjectButton() {
    waitAddOrImportProjectButton().click();
  }

  public void waitBottomCreateButton(int timeout) {
    seleniumWebDriverHelper.waitVisibility(bottomCreateWorkspaceButton, timeout);
  }

  public void waitBottomCreateButton() {
    waitBottomCreateButton(DEFAULT_TIMEOUT);
  }

  public void waitMainActionButtons(int timeout) {
    waitTopCreateButton(timeout);
    waitAllButton(timeout);
    waitQuickStartButton(timeout);
    waitSingleMachineButton(timeout);
    waitMultiMachineButton(timeout);
    waitAddOrImportProjectButton(timeout);
    waitBottomCreateButton(timeout);
  }

  public void waitMainActionButtons() {
    waitMainActionButtons(DEFAULT_TIMEOUT);
  }

  public void waitPageLoad(int timeout) {
    waitToolbar(timeout);
    waitNameField(timeout);
    waitMainActionButtons(timeout);
  }

  public void waitPageLoad() {
    waitPageLoad(DEFAULT_TIMEOUT);
  }
}
