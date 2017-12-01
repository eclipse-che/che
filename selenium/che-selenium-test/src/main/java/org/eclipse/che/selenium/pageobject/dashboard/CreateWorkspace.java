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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
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

  private interface Locators {
    String TOOLBAR_TITLE_ID = "New_Workspace";
    String WORKSPACE_NAME_INPUT = "workspace-name-input";
    String CREATE_BUTTON = "create-workspace-button";
    String SELECT_ALL_STACKS_TAB = "all-stacks-button";
    String SELECT_QUICK_START_STACKS_TAB = "quick-start-button";
    String SELECT_SINGLE_MACHINE_STACKS_TAB = "single-machine-button";
    String SELECT_MULTI_MACHINE_STACKS_TAB = "multi-machine-button";
    String ADD_STACK_BUTTON = "search-stack-input";
    String FILTER_STACK_BUTTON = "search-stack-input";
    String SEARCH_INPUT = "search-stack-input";
    String STACK_ROW_XPATH = "//div[@data-stack-id='%s']";
    String RAM_INPUT_XPATH = "//input[@name='memory']";
    String MACHINE_RAM_ID = "machine-%s-ram";
  }

  @FindBy(id = Locators.TOOLBAR_TITLE_ID)
  WebElement toolbarTitle;

  @FindBy(id = Locators.WORKSPACE_NAME_INPUT)
  WebElement workspaceNameInput;

  @FindBy(id = Locators.CREATE_BUTTON)
  WebElement createWorkspaceButton;

  @FindBy(id = Locators.SEARCH_INPUT)
  WebElement searchInput;

  @FindBy(id = Locators.SELECT_ALL_STACKS_TAB)
  WebElement selectAllStacksTab;

  @FindBy(id = Locators.SELECT_QUICK_START_STACKS_TAB)
  WebElement selectQuickStartStacksTab;

  @FindBy(id = Locators.SELECT_SINGLE_MACHINE_STACKS_TAB)
  WebElement selectSingleMachineStacksTab;

  @FindBy(id = Locators.SELECT_MULTI_MACHINE_STACKS_TAB)
  WebElement selectMultiMachineStacksTab;

  public void waitToolbar() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.TOOLBAR_TITLE_ID)));
  }

  public void typeToSearchInput(String value) {
    searchInput.sendKeys(value);
  }

  public void selectStack(String stackId) {
    WebElement stack =
        seleniumWebDriver.findElement(By.xpath(format(Locators.STACK_ROW_XPATH, stackId)));
    new Actions(seleniumWebDriver).moveToElement(stack).perform();
    stack.click();
  }

  public void clickOnCreateWorkspaceButton() {
    createWorkspaceButton.click();
  }

  public void typeWorkspaceName(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(workspaceNameInput));
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
        seleniumWebDriver.findElement(By.id(format(Locators.MACHINE_RAM_ID, machine)));
    ramInput.clear();
    ramInput.sendKeys(value);
  }

  public void clickOnAllStacksTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(selectAllStacksTab))
        .click();
  }

  public void clickOnQuickStartTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(selectQuickStartStacksTab))
        .click();
  }

  public void clickOnSingleMachineTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(selectSingleMachineStacksTab))
        .click();
  }

  public void clickOnMultiMachineTab() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(selectMultiMachineStacksTab))
        .click();
  }
}
