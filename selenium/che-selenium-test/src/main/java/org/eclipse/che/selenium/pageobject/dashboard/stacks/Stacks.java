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
package org.eclipse.che.selenium.pageobject.dashboard.stacks;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Stacks {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWait redrawUiElementsTimeout;
  private final Dashboard dashboard;

  @Inject
  public Stacks(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      Dashboard dashboard) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.dashboard = dashboard;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String TOOLBAR = "Stacks";
    String DOCUMENTATION_LINK = "//div[@che-link-title='Learn more.']/a";
    String ADD_STACK_BTN = "add-item-button";
    String BUILD_STACK_FROM_RECIPE_BUTTON_XPATH =
        "//che-button-primary[@che-button-title='Build Stack From Recipe']";
    String DELETE_STACK_BTN = "delete-item-button";
    String DELETE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Delete']";
    String SEARCH_STACK_FIELD = "//input[@ng-placeholder='Search']";
    String BULK_CHECKBOX = "//md-checkbox[@aria-label='Stack list']";

    String STACK_ITEM_CHECKBOX_XPATH_PATTERN = "//div[@id='stack-name-%s']//md-checkbox";
    String STACK_ITEM_NAME_XPATH_PATTERN = "//div[@id='stack-name-%s']";
    String STACK_ITEM_COMPONENTS_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//div[contains(@class,'stack-item-components')]";
    String STACK_ITEM_DESCRIPTION_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//div[contains(@class,'stack-item-description')]";

    String STACK_ITEM_DELETE_BUTTON_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//a[@uib-tooltip='Delete Stack']";
    String STACK_ITEM_DUPLICATE_STACK_BUTTON_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//a[@uib-tooltip='Duplicate stack']";

    String BUILD_STACK_FROM_RECIPE_DIALOG_XPATH = "//*[@title='Build stack from recipe']";
  }

  @FindBy(id = Locators.TOOLBAR)
  WebElement toolbar;

  @FindBy(id = Locators.ADD_STACK_BTN)
  WebElement addWorkspaceBtn;

  @FindBy(id = Locators.DELETE_STACK_BTN)
  WebElement deleteStackButton;

  @FindBy(xpath = Locators.BULK_CHECKBOX)
  WebElement bulkCheckbox;

  @FindBy(xpath = Locators.BUILD_STACK_FROM_RECIPE_BUTTON_XPATH)
  WebElement buildStackFromRecipeButton;

  @FindBy(xpath = Locators.DELETE_DIALOG_BUTTON)
  WebElement deleteDialogBtn;

  @FindBy(xpath = Locators.SEARCH_STACK_FIELD)
  WebElement searchWorkspaceField;

  public void waitToolbarTitleName() {
    seleniumWebDriverHelper.waitVisibility(toolbar);
  }

  public void clickOnAddStackButton() {
    seleniumWebDriverHelper.waitAndClick(addWorkspaceBtn);
  }

  public void clickOnBuildStackFromRecipeButton() {
    seleniumWebDriverHelper.waitAndClick(buildStackFromRecipeButton);
  }

  public void selectAllStacksByBulk() {
    seleniumWebDriverHelper.waitAndClick(bulkCheckbox);
  }

  public void selectStackByCheckbox(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.STACK_ITEM_CHECKBOX_XPATH_PATTERN, stackName)));
  }

  public void openStackDetails(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.STACK_ITEM_NAME_XPATH_PATTERN, stackName)));
  }

  public boolean isStackChecked(String workspaceName) {
    String attrValue =
        redrawUiElementsTimeout
            .until(
                visibilityOfElementLocated(
                    By.xpath(format(Locators.STACK_ITEM_CHECKBOX_XPATH_PATTERN, workspaceName))))
            .getAttribute("aria-checked");

    return Boolean.parseBoolean(attrValue);
  }

  public Boolean isStackItemExists(String stackName) {
    return seleniumWebDriverHelper.isVisible(
        By.xpath(format(Locators.STACK_ITEM_NAME_XPATH_PATTERN, stackName)));
  }

  public String getStackDescription(String stackName) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(format(Locators.STACK_ITEM_DESCRIPTION_XPATH_PATTERN, stackName)));
  }

  public String getStackComponents(String stackName) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(format(Locators.STACK_ITEM_COMPONENTS_XPATH_PATTERN, stackName)));
  }

  public void clickOnDeleteActionButton(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.STACK_ITEM_DELETE_BUTTON_XPATH_PATTERN, stackName)));
  }

  public Boolean isDeleteStackButtonEnabled(String stackName) {
    return seleniumWebDriverHelper
        .waitVisibility(
            By.xpath(format(Locators.STACK_ITEM_DELETE_BUTTON_XPATH_PATTERN, stackName)))
        .isEnabled();
  }

  public void clickOnDuplicateStackActionButton(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.STACK_ITEM_DUPLICATE_STACK_BUTTON_XPATH_PATTERN, stackName)));
  }

  public void waitBuildStackFromRecipeDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.BUILD_STACK_FROM_RECIPE_DIALOG_XPATH));
  }

  public void clickOnDeleteStackButton() {
    seleniumWebDriverHelper.waitAndClick(deleteStackButton);
  }

  public void clickOnDeleteDialogButton() {
    seleniumWebDriverHelper.waitAndClick(deleteDialogBtn);
  }
}
