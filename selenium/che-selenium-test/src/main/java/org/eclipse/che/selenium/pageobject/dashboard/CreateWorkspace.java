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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Ann Shumilova */
@Singleton
public class CreateWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;
  private final ActionsFactory actionsFactory;

  @Inject
  public CreateWorkspace(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String WORKSPACE_NAME_INPUT = "workspace-name-input";
    String ERROR_MESSAGE = "new-workspace-error-message";
    String TOOLBAR_TITLE_ID = "New_Workspace";
    String CREATE_BUTTON = "create-workspace-button";
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
  }

  @FindBy(id = Locators.FILTERS_STACK_BUTTON)
  WebElement filtersStackButton;

  @FindBy(id = Locators.FILTER_STACK_INPUT)
  WebElement filterStackInput;

  @FindBy(id = Locators.TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(id = Locators.CREATE_BUTTON)
  WebElement createWorkspaceButton;

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
    return workspaceNameInput.getAttribute("value");
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

  public void setMachineRAM(String value) {
    WebElement ramInput = seleniumWebDriver.findElement(By.xpath(Locators.MACHINE_RAM_VALUE));
    ramInput.clear();
    ramInput.sendKeys(value);
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
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.TOOLBAR_TITLE_ID)));
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
    return !Boolean.valueOf(createWorkspaceButton.getAttribute("aria-disabled"));
  }

  public void clickOnCreateWorkspaceButton() {
    createWorkspaceButton.click();
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
}
