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
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectOptions.Locators.CANCEL_PROJECT_OPTIONS_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectOptions.Locators.PROJECT_NAME_ERROR_MESSAGE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectOptions.Locators.REPOSITORY_URL_ERROR_MESSAGE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectOptions.Locators.SAVE_PROJECT_OPTIONS_BUTTON_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/** @author Ihor Okhrimenko */
@Singleton
public class ProjectOptions {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  ProjectOptions(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String CANCEL_PROJECT_OPTIONS_BUTTON_XPATH = "//button[@name='cancelButton']";
    String SAVE_PROJECT_OPTIONS_BUTTON_XPATH = "//che-button-primary//button[@name='saveButton']";
    String PROJECT_NAME_ERROR_MESSAGE_XPATH =
        "//div[@che-name='projectName']//div[@id='new-workspace-error-message']/div";
    String REPOSITORY_URL_ERROR_MESSAGE_XPATH =
        "//div[@che-name='projectGitURL']//div[@id='new-workspace-error-message']/div";
  }

  public void waitProjectOptionsForm() {
    waitProjectNameField();
    waitDescriptionField();
    waitRepositoryUrlField();
  }

  public WebElement waitProjectNameField() {
    return waitElementByNameAttribute("projectName");
  }

  public void waitProjectNameFieldValue(String expectedValue) {
    seleniumWebDriverHelper.waitValueEqualsTo(waitProjectNameField(), expectedValue);
  }

  public void setValueOfNameField(String value) {
    seleniumWebDriverHelper.setValue(waitProjectNameField(), value);
  }

  private WebElement waitElementByNameAttribute(String nameAttribute) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//input[@name='%s']", nameAttribute)));
  }

  public WebElement waitDescriptionField() {
    return waitElementByNameAttribute("projectDescription");
  }

  public void waitDescriptionFieldValue(String expectedValue) {
    seleniumWebDriverHelper.waitValueEqualsTo(waitDescriptionField(), expectedValue);
  }

  public void typeTextInDescriptionField(String text) {
    seleniumWebDriverHelper.setValue(waitDescriptionField(), text);
  }

  public WebElement waitRepositoryUrlField() {
    return waitElementByNameAttribute("projectGitURL");
  }

  public void typeTextInRepositoryUrlField(String value) {
    seleniumWebDriverHelper.setValue(waitRepositoryUrlField(), value);
  }

  public void waitRepositoryUrlFieldValue(String expectedValue) {
    seleniumWebDriverHelper.waitValueEqualsTo(waitRepositoryUrlField(), expectedValue);
  }

  public WebElement waitRemoveButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath("//button[@name='removeButton']"));
  }

  public void clickOnRemoveButton() {
    seleniumWebDriverHelper.waitAndClick(waitRemoveButton());
  }

  public WebElement waitSaveButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(SAVE_PROJECT_OPTIONS_BUTTON_XPATH));
  }

  public WebElement waitCancelButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(CANCEL_PROJECT_OPTIONS_BUTTON_XPATH));
  }

  public void clickOnSaveButton() {
    waitSaveButton().click();
  }

  public void clickOnCancelButton() {
    waitCancelButton().click();
  }

  private void waitButtonDisableState(WebElement button, boolean state) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(button, "aria-disabled", Boolean.toString(state));
  }

  public void waitSaveButtonEnabling() {
    waitButtonDisableState(waitSaveButton(), false);
  }

  public void waitSaveButtonDisabling() {
    waitButtonDisableState(waitSaveButton(), true);
  }

  public void waitCancelButtonEnabling() {
    waitButtonDisableState(waitCancelButton(), false);
  }

  public void waitCancelButtonDisabling() {
    waitButtonDisableState(waitCancelButton(), true);
  }

  public void waitProjectNameErrorMessage(String expectedMessage) {
    seleniumWebDriverHelper.waitTextEqualsTo(
        By.xpath(PROJECT_NAME_ERROR_MESSAGE_XPATH), expectedMessage);
  }

  public void waitProjectNameErrorDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PROJECT_NAME_ERROR_MESSAGE_XPATH));
  }

  public void waitRepositoryUrlErrorMessage(String expectedMessage) {
    seleniumWebDriverHelper.waitTextEqualsTo(
        By.xpath(REPOSITORY_URL_ERROR_MESSAGE_XPATH), expectedMessage);
  }

  public void waitRepositoryUrlErrorDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(REPOSITORY_URL_ERROR_MESSAGE_XPATH));
  }
}
