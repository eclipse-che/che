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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WorkspaceEnvVariables {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public WorkspaceEnvVariables(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
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
  }

  public void clickOnAddEnvVariableButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_ENV_VARIABLE_BUTTON)))
        .click();
  }

  public void checkAddNewEnvVarialbleDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_ENV_VARIABLE_DIALOG_NAME)));
  }

  public void enterEnvVariableName(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.NEW_ENV_VARIABLE_NAME)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.NEW_ENV_VARIABLE_NAME)))
        .sendKeys(name);
  }

  public void enterEnvVariableValue(String value) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.NEW_ENV_VARIABLE_VALUE)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.NEW_ENV_VARIABLE_VALUE)))
        .sendKeys(value);
  }

  public void addNewEnvironmentVariable(String name, String value) {
    enterEnvVariableName(name);
    enterEnvVariableValue(value);
  }

  public Boolean checkEnvVariableExists(String varName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.VARIABLE_NAME, varName))))
        .getText()
        .equals(varName);
  }

  public Boolean checkValueExists(String varName, String varValue) {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_VALUE, varName, varValue))))
        .isDisplayed();
  }

  public void checkValueIsNotExists(String varName, String varValue) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(format(Locators.VARIABLE_VALUE, varName, varValue))));
    loader.waitOnClosed();
  }

  public void clickOnDeleteEnvVariableButton(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.DELETE_ENV_VARIABLE, varName))))
        .click();
  }

  public void clickOnEditEnvVariableButton(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.EDIT_ENV_VARIABLE, varName))))
        .click();
  }

  public void clickOnEnvVariableCheckbox(String varName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.VARIABLE_CHECKBOX, varName))))
        .click();
  }
}
