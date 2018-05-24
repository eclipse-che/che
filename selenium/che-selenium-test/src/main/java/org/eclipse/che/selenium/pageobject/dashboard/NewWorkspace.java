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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Locators.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Ann Shumilova */
@Singleton
public class NewWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;
  private final ActionsFactory actionsFactory;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private static final int DEFAULT_TIMEOUT = TestTimeoutsConstants.DEFAULT_TIMEOUT;

  @Inject
  public NewWorkspace(
      SeleniumWebDriver seleniumWebDriver,
      ActionsFactory actionsFactory,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
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

  @FindBy(id = Locators.TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(xpath = Locators.BOTTOM_CREATE_BUTTON_XPATH)
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
    redrawUiElementsTimeout.until(visibilityOf(workspaceNameInput));
    workspaceNameInput.clear();
    workspaceNameInput.sendKeys(name);
    WaitUtils.sleepQuietly(1);
  }

  public String getWorkspaceNameValue() {
    return waitNameField().getAttribute("value");
  }

  public boolean isWorkspaceNameErrorMessageEquals(String message) {
    return redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.id(Locators.ERROR_MESSAGE)))
        .getText()
        .equals(message);
  }

  public boolean isMachineExists(String machineName) {
    return seleniumWebDriver
            .findElements(By.xpath(format(Locators.MACHINE_NAME, machineName)))
            .size()
        > 0;
  }

  public void clickOnIncrementMemoryButton(String machineName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.INCREMENT_MEMORY_BUTTON, machineName))))
        .click();
  }

  public void clickOnDecrementMemoryButton(String machineName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.DECREMENT_MEMORY_BUTTON, machineName))))
        .click();
  }

  public double getRAM(String machineName) {
    String s =
        seleniumWebDriver
            .findElement(By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)))
            .getAttribute("value");
    return Double.parseDouble(s);
  }

  public void setMachineRAM(String machineName, double value) {
    WebElement ramInput =
        seleniumWebDriver.findElement(By.xpath(format(Locators.MACHINE_RAM_VALUE, machineName)));
    ramInput.clear();
    ramInput.sendKeys(Double.toString(value));
  }

  public void clickOnFiltersButton() {
    redrawUiElementsTimeout.until(visibilityOf(filtersStackButton)).click();
  }

  public void typeToFiltersInput(String value) {
    redrawUiElementsTimeout.until(visibilityOf(filterStackInput)).sendKeys(value);
  }

  public String getTextFromFiltersInput() {
    return filterStackInput.getAttribute("value");
  }

  public void clearSuggestions() {
    WebElement webElement =
        redrawUiElementsTimeout.until(
            visibilityOfElementLocated(By.xpath(Locators.FILTER_SELECTED_SUGGESTION_BUTTON)));
    webElement.click();
  }

  public void selectFilterSuggestion(String suggestion) {
    WebElement webElement =
        redrawUiElementsTimeout.until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.FILTER_SUGGESTION_BUTTON, suggestion))));
    webElement.click();
  }

  public void waitToolbar() {
    waitToolbar(WIDGET_TIMEOUT_SEC);
  }

  public void waitToolbar(int timeout) {
    seleniumWebDriverHelper.waitVisibility(By.id(Locators.TOOLBAR_TITLE_ID), timeout);
  }

  public String getTextFromSearchInput() {
    return searchInput.getAttribute("value");
  }

  public void typeToSearchInput(String value) {
    redrawUiElementsTimeout.until(visibilityOf(searchInput)).clear();
    searchInput.sendKeys(value);
  }

  public void clearTextInSearchInput() {
    redrawUiElementsTimeout.until(visibilityOf(clearInput)).click();
  }

  public boolean isStackVisible(String stackName) {
    return seleniumWebDriver
            .findElements(By.xpath(format(Locators.STACK_ROW_XPATH, stackName)))
            .size()
        > 0;
  }

  public void selectStack(String stackId) {
    WebElement stack =
        redrawUiElementsTimeout.until(
            visibilityOfElementLocated(By.xpath(format(Locators.STACK_ROW_XPATH, stackId))));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(stack).perform();
    stack.click();
  }

  public boolean isCreateWorkspaceButtonEnabled() {
    return !Boolean.valueOf(bottomCreateWorkspaceButton.getAttribute("aria-disabled"));
  }

  public void clickOnAllStacksTab() {
    redrawUiElementsTimeout.until(visibilityOf(selectAllStacksTab)).click();
  }

  public void clickOnQuickStartTab() {
    redrawUiElementsTimeout.until(visibilityOf(selectQuickStartStacksTab)).click();
  }

  public void clickOnSingleMachineTab() {
    redrawUiElementsTimeout.until(visibilityOf(selectSingleMachineStacksTab)).click();
  }

  public void clickOnMultiMachineTab() {
    redrawUiElementsTimeout.until(visibilityOf(selectMultiMachineStacksTab)).click();
  }

  public void waitWorkspaceIsCreatedDialogIsVisible() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.WORKSPACE_CREATED_DIALOG)));
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnEditWorkspaceButton() {
    seleniumWebDriverHelper
            .waitAndClick(By.xpath(EDIT_WORKSPACE_DIALOG_BUTTON), ELEMENT_TIMEOUT_SEC);


    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(elementToBeClickable(By.xpath(Locators.EDIT_WORKSPACE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnOpenInIDEButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(elementToBeClickable(By.xpath(Locators.OPEN_IN_IDE_DIALOG_BUTTON)))
        .click();
  }

  public void clickOnCreateButtonAndOpenInIDE() {
    WaitUtils.sleepQuietly(1);
    redrawUiElementsTimeout.until(visibilityOf(bottomCreateWorkspaceButton)).click();
    waitWorkspaceIsCreatedDialogIsVisible();
    clickOnOpenInIDEButton();
  }

  public void clickOnCreateButtonAndEditWorkspace() {
    WaitUtils.sleepQuietly(1);
    redrawUiElementsTimeout.until(visibilityOf(bottomCreateWorkspaceButton)).click();
    waitWorkspaceIsCreatedDialogIsVisible();
    clickOnEditWorkspaceButton();
  }

  public void clickOnBottomCreateButton() {
    seleniumWebDriverHelper.waitAndClick(bottomCreateWorkspaceButton);
  }

  public void openOrganizationsList() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ORGANIZATIONS_LIST_ID)))
        .click();
  }

  public void selectOrganizationFromList(String organizationName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.ORGANIZATION_ITEM, organizationName))))
        .click();
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
