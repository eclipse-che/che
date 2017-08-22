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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Ann Shumilova */
@Singleton
public class CreateWorkspace {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public CreateWorkspace(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

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

  private interface Locators {
    String WORKSPACE_NAME_INPUT = "//input[@name='workspaceName']";
    String CREATE_BUTTON = "//che-button-save-flat//span[text()='Create']";
    String SEARCH_INPUT = "//input[@placeholder='Search']";
    String TOOLBAR_TITLE_ID = "New_Workspace";
    String STACK_ROW_XPATH = "//div[@data-stack-id='%s']";
    String RAM_INPUT_XPATH = "//input[@name='memory']";
    String MACHINE_RAM_ID = "machine-%s-ram";
    String SELECT_ALL_STACKS_TAB = "//che-toggle-joined-button[@che-title =  'All']";
    String SELECT_QUICK_START_STACKS_TAB =
        "//che-toggle-joined-button[@che-title =  'Quick Start']";
    String SELECT_SINGLE_MACHINE_STACKS_TAB =
        "//che-toggle-joined-button[@che-title =  'Single Machine']";
    String SELECT_MULTI_MACHINE_STACKS_TAB =
        "//che-toggle-joined-button[@che-title =  'Multi Machine']";
  }

  @FindBy(id = Locators.TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(xpath = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(xpath = Locators.CREATE_BUTTON)
  WebElement createWorkspaceButton;

  @FindBy(xpath = Locators.SEARCH_INPUT)
  WebElement searchInput;

  @FindBy(xpath = Locators.SELECT_ALL_STACKS_TAB)
  WebElement selectAllStacksTab;

  @FindBy(xpath = Locators.SELECT_QUICK_START_STACKS_TAB)
  WebElement selectQuickStartStacksTab;

  @FindBy(xpath = Locators.SELECT_SINGLE_MACHINE_STACKS_TAB)
  WebElement selectSingleMachineStacksTab;

  @FindBy(xpath = Locators.SELECT_MULTI_MACHINE_STACKS_TAB)
  WebElement selectMultiMachineStacksTab;

  public void waitToolbar() {
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.TOOLBAR_TITLE_ID)));
  }

  public void typeToSearchInput(String value) {
    searchInput.sendKeys(value);
  }

  public void selectStack(String stackId) {
    WebElement stack =
        seleniumWebDriver.findElement(By.xpath(String.format(Locators.STACK_ROW_XPATH, stackId)));
    new Actions(seleniumWebDriver).moveToElement(stack).perform();
    stack.click();
  }

  public void clickCreate() {
    createWorkspaceButton.click();
  }

  public void typeWorkspaceName(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(workspaceNameInput));
    workspaceNameInput.clear();
    workspaceNameInput.sendKeys(name);
  }

  public String getWorkspaceNameValue() {
    return workspaceNameInput.getAttribute("value");
  }

  public void setMachineRAM(String value) {
    WebElement ramInput = seleniumWebDriver.findElement(By.xpath(Locators.RAM_INPUT_XPATH));
    ramInput.clear();
    ramInput.sendKeys(value);
  }

  public void setMachineRAM(String machine, String value) {
    WebElement ramInput =
        seleniumWebDriver.findElement(By.id(String.format(Locators.MACHINE_RAM_ID, machine)));
    ramInput.clear();
    ramInput.sendKeys(value);
  }

  public void clickAllStacksTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(selectAllStacksTab))
        .click();
  }

  public void clickQuickStartTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(selectQuickStartStacksTab))
        .click();
  }

  public void clickSingleMachineTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(selectSingleMachineStacksTab))
        .click();
  }

  public void clickMultiMachineTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(selectMultiMachineStacksTab))
        .click();
  }
}
