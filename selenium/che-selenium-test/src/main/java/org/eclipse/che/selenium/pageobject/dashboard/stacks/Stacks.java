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
package org.eclipse.che.selenium.pageobject.dashboard.stacks;

import static java.lang.String.format;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Stacks {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public Stacks(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String TOOLBAR_ID = "Stacks";
    String DOCUMENTATION_LINK_XPATH = "//div[@che-link-title='Learn more.']/a";
    String ADD_STACK_BUTTON_ID = "import-item-button";
    String BUILD_STACK_FROM_RECIPE_BUTTON_ID = "import-item-button";
    String DELETE_STACK_BUTTON_ID = "delete-item-button";
    String DELETE_DIALOG_BUTTON_ID = "ok-dialog-button";
    String SEARCH_STACK_FIELD_XPATH = "//input[@ng-placeholder='Search']";
    String STACKS_LIST_HEADERS_XPATH = "//div[@che-column-title]";
    String SORT_STACKS_BY_NAME_ID = "sort-stacks-by-name";
    String BULK_CHECKBOX_XPATH = "//md-checkbox[@aria-label='Stack list']";
    String NO_STACKS_FOUND_XPATH = "//span[text()='No stacks found.']";
    String STACK_ITEM_XPATH = "//div[contains(@class, 'stack-item-name')]";
    String STACK_ITEM_CHECKBOX_XPATH_PATTERN = "//div[@id='stack-name-%s']//md-checkbox";
    String STACK_ITEM_NAME_XPATH_PATTERN = "//div[@id='stack-name-%s']";
    String STACK_ITEM_NAME_CONTAINS_XPATH_PATTERN = "//div[contains(@id,'stack-name-%s')]";
    String STACK_ITEM_COMPONENTS_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//div[contains(@class,'stack-item-components')]";
    String STACK_ITEM_DESCRIPTION_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//div[contains(@class,'stack-item-description')]";
    String STACK_ITEM_DELETE_BUTTON_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//a[@uib-tooltip='Delete Stack']";
    String DUPLICATE_STACK_BUTTON_XPATH_PATTERN =
        "//div[@id='stack-name-%s']//a[@uib-tooltip='Duplicate stack']";
    String BUILD_STACK_FROM_RECIPE_DIALOG_XPATH = "//*[@title='Build stack from recipe']";
  }

  @FindBy(id = Locators.TOOLBAR_ID)
  WebElement toolbar;

  @FindBy(id = Locators.ADD_STACK_BUTTON_ID)
  WebElement addWorkspaceBtn;

  @FindBy(id = Locators.DELETE_STACK_BUTTON_ID)
  WebElement deleteStackButton;

  @FindBy(xpath = Locators.BULK_CHECKBOX_XPATH)
  WebElement bulkCheckbox;

  @FindBy(id = Locators.BUILD_STACK_FROM_RECIPE_BUTTON_ID)
  WebElement buildStackFromRecipeButton;

  @FindBy(id = Locators.DELETE_DIALOG_BUTTON_ID)
  WebElement deleteDialogBtn;

  @FindBy(xpath = Locators.SEARCH_STACK_FIELD_XPATH)
  WebElement searchStackField;

  public void waitToolbarTitleName() {
    seleniumWebDriverHelper.waitVisibility(toolbar);
  }

  public void waitAddStackButton() {
    seleniumWebDriverHelper.waitVisibility(addWorkspaceBtn);
  }

  public void clickOnAddStackButton() {
    seleniumWebDriverHelper.waitAndClick(addWorkspaceBtn);
  }

  public void waitBuildStackFromRecipeButton() {
    seleniumWebDriverHelper.waitVisibility(buildStackFromRecipeButton);
  }

  public void clickOnBuildStackFromRecipeButton() {
    seleniumWebDriverHelper.waitAndClick(buildStackFromRecipeButton);
  }

  public void waitBuildStackFromRecipeDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.BUILD_STACK_FROM_RECIPE_DIALOG_XPATH));
  }

  public void selectAllStacksByBulk() {
    seleniumWebDriverHelper.waitAndClick(bulkCheckbox);
  }

  public void selectStackByCheckbox(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.STACK_ITEM_CHECKBOX_XPATH_PATTERN, stackName)));
  }

  public boolean isStackChecked(String workspaceName) {
    String attrValue =
        seleniumWebDriverHelper.waitVisibilityAndGetAttribute(
            By.xpath(format(Locators.STACK_ITEM_CHECKBOX_XPATH_PATTERN, workspaceName)),
            "aria-checked");

    return Boolean.parseBoolean(attrValue);
  }

  public void openStackDetails(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.STACK_ITEM_NAME_XPATH_PATTERN, stackName)));
  }

  public void waitStackItem(String stackName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.STACK_ITEM_NAME_XPATH_PATTERN, stackName)));
  }

  public Boolean isStackItemExisted(String stackName) {
    return seleniumWebDriverHelper.isVisible(
        By.xpath(format(Locators.STACK_ITEM_NAME_XPATH_PATTERN, stackName)));
  }

  public Boolean isDuplicatedStackExisted(String stackName) {
    return seleniumWebDriverHelper.isVisible(
        By.xpath(format(Locators.STACK_ITEM_NAME_CONTAINS_XPATH_PATTERN, stackName)));
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

  public void clickOnDuplicateStackButton(String stackName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.DUPLICATE_STACK_BUTTON_XPATH_PATTERN, stackName)));
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnDeleteStackButton() {
    seleniumWebDriverHelper.waitAndClick(deleteStackButton);
  }

  public void clickOnDeleteDialogButton() {
    seleniumWebDriverHelper.waitAndClick(deleteDialogBtn);
  }

  public void waitFilterStacksField() {
    seleniumWebDriverHelper.waitVisibility(searchStackField);
  }

  public void typeToSearchInput(String value) {
    seleniumWebDriverHelper.setValue(searchStackField, value);
  }

  public void waitNoStacksFound() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.NO_STACKS_FOUND_XPATH));
  }

  public ArrayList<String> getStacksListHeaders() {
    ArrayList<String> titles = new ArrayList<>();
    List<WebElement> headers =
        seleniumWebDriver.findElements(By.xpath(Locators.STACKS_LIST_HEADERS_XPATH));
    headers.forEach(
        header -> {
          titles.add(header.getText());
        });

    return titles;
  }

  public ArrayList<String> getStacksNamesList() {
    ArrayList<String> stacksNames = new ArrayList<>();
    List<WebElement> names = seleniumWebDriver.findElements(By.xpath(Locators.STACK_ITEM_XPATH));
    names.forEach(
        name -> {
          stacksNames.add(name.getText());
        });

    return stacksNames;
  }

  public void clickOnSortStacksByNameButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(Locators.SORT_STACKS_BY_NAME_ID));
  }

  public void waitDocumentationLink() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.DOCUMENTATION_LINK_XPATH));
  }
}
