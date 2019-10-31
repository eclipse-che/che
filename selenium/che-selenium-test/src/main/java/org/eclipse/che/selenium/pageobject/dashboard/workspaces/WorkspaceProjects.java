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
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.ADD_NEW_PROJECT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.CHECKBOX_BY_PROJECT_NAME_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.DELETE_BUTTON_MESSAGE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.DELETE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.NOTIFICATION_CONTAINER_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.SEARCH_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.SELECT_ALL_CHECKBOX_XPATH;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Musienko Maxim
 * @author Ihor Okhrimenko
 */
@Singleton
public class WorkspaceProjects {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspaceProjects(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String PROJECT_BY_NAME = "//div[@aria-label='project-item']/span[text()='%s']";
    String DELETE_PROJECT = "//button/span[text()='Delete']";
    String ADD_NEW_PROJECT_BUTTON = "//che-button-primary[@che-button-title='Add Project']/button";
    String PROJECT_CHECKBOX = "//md-checkbox[contains(@aria-label, 'Project %s')]";
    String SEARCH_FIELD_XPATH = "//workspace-details-projects//input[@placeholder='Search']";
    String NOTIFICATION_CONTAINER_ID = "che-notification-container";
    String DELETE_BUTTON_XPATH = "//che-button-primary[@che-button-title='Delete']";
    String DELETE_BUTTON_MESSAGE_XPATH = "//che-list-header-delete-button/span";
    String SELECT_ALL_CHECKBOX_XPATH = "//md-checkbox[@aria-label='Project list']/div";
    String CHECKBOX_BY_PROJECT_NAME_XPATH_TEMPLATE = "//md-checkbox[@aria-label='Project %s']";
  }

  public enum BottomButton {
    SAVE_BUTTON("save-button"),
    APPLY_BUTTON("apply-button"),
    CANCEL_BUTTON("cancel-button");

    private final String buttonNameAttribute;

    BottomButton(String buttonNameAttribute) {
      this.buttonNameAttribute = buttonNameAttribute;
    }

    public By getLocator() {
      return By.className(this.buttonNameAttribute);
    }
  }

  public void clickOnSelectAllCheckbox() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(SELECT_ALL_CHECKBOX_XPATH));
  }

  public WebElement waitDeleteButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(DELETE_BUTTON_XPATH));
  }

  public void waitDeleteButtonDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(DELETE_BUTTON_XPATH));
  }

  public void clickOnDeleteButton() {
    waitDeleteButton().click();
  }

  public void waitDeleteButtonMessage(String expectedMessage) {
    seleniumWebDriverHelper.waitTextContains(
        By.xpath(DELETE_BUTTON_MESSAGE_XPATH), expectedMessage);
  }

  public void waitDeleteButtonMessageDisappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(DELETE_BUTTON_MESSAGE_XPATH));
  }

  public void waitDeleteButtonDisabling() {
    waitState(By.xpath(DELETE_BUTTON_XPATH), false);
  }

  public void waitDeleteButtonEnabling() {
    waitState(By.xpath(DELETE_BUTTON_XPATH), true);
  }

  public void typeToSearchField(String text) {
    seleniumWebDriverHelper.setValue(waitSearchField(), text);
  }

  public void waitNotification(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(By.id(NOTIFICATION_CONTAINER_ID), expectedText);
  }

  public WebElement wait(BottomButton button) {
    return seleniumWebDriverHelper.waitVisibility(button.getLocator());
  }

  public void waitInvisibility(BottomButton... bottomButtons) {
    asList(bottomButtons)
        .forEach(button -> seleniumWebDriverHelper.waitInvisibility(button.getLocator()));
  }

  public void waitAndClickOn(BottomButton button) {
    wait(button).click();
  }

  private void waitState(BottomButton button, boolean enabled) {
    waitState(button.getLocator(), enabled);
  }

  private void waitState(By locator, boolean enabled) {
    final String buttonStateAttribute = "aria-disabled";
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        locator, buttonStateAttribute, Boolean.toString(!enabled));
  }

  public void waitDisabled(BottomButton... buttons) {
    asList(buttons).forEach(button -> waitState(button, false));
  }

  public void waitEnabled(BottomButton... buttons) {
    asList(buttons).forEach(button -> waitState(button, true));
  }

  /**
   * wait the project is present in the 'All projects' tab
   *
   * @param projectName name of project
   */
  public void waitProjectIsPresent(String projectName) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_BY_NAME, projectName))));
  }

  /**
   * wait the project is not present in the 'All projects' tab
   *
   * @param projectName name of project
   */
  public void waitProjectIsNotPresent(String projectName) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_BY_NAME, projectName))));
  }

  /** click on the Add Project button */
  public void clickOnAddNewProjectButton() {
    waitAddNewProjectButton().click();
  }

  public WebElement waitAddNewProjectButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(ADD_NEW_PROJECT_BUTTON));
  }

  /** click on the Add Project button */
  public void selectProject(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_CHECKBOX, projectName))))
        .click();
  }

  public WebElement waitSearchField() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(SEARCH_FIELD_XPATH));
  }

  public void waitProjectDetailsPage() {
    waitSearchField();
    waitAddNewProjectButton();
  }

  public boolean isCheckboxEnabled(String projectName) {
    final String checkboxLocator = format(CHECKBOX_BY_PROJECT_NAME_XPATH_TEMPLATE, projectName);
    final String checkboxStateAttribute = "aria-checked";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(checkboxLocator), checkboxStateAttribute)
        .equals("true");
  }

  public void waitCheckboxEnabled(String... projectNames) {
    asList(projectNames)
        .forEach(
            projectName ->
                seleniumWebDriverHelper.waitSuccessCondition(
                    driver -> isCheckboxEnabled(projectName)));
  }

  public void waitCheckboxDisabled(String... projectNames) {
    asList(projectNames)
        .forEach(
            projectName ->
                seleniumWebDriverHelper.waitSuccessCondition(
                    driver -> !isCheckboxEnabled(projectName)));
  }

  public void clickOnCheckbox(String... projectNames) {
    asList(projectNames)
        .forEach(
            projectName -> {
              String checkboxLocator = format(CHECKBOX_BY_PROJECT_NAME_XPATH_TEMPLATE, projectName);
              seleniumWebDriverHelper.waitAndClick(By.xpath(checkboxLocator));
            });
  }
}
