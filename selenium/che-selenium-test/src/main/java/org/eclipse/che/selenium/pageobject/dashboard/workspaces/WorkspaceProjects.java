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

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.ADD_NEW_PROJECT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.NOTIFICATION_CONTAINER_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects.Locators.SEARCH_FIELD_XPATH;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
    String PROJECT_BY_NAME = "//div[contains(@ng-click, 'projectItem')]/span[text()='%s']";
    String DELETE_PROJECT = "//button/span[text()='Delete']";
    String DELETE_SELECTED_PROJECTS = "//che-button-primary[@che-button-title='Delete']/button";
    String DELETE_IT_PROJECT = "//md-dialog//che-button-primary[@che-button-title='Delete']/button";
    String ADD_NEW_PROJECT_BUTTON = "//che-button-primary[@che-button-title='Add Project']/button";
    String PROJECT_CHECKBOX = "//md-checkbox[contains(@aria-label, 'Project %s')]";
    String SEARCH_FIELD_XPATH = "//workspace-details-projects//input[@placeholder='Search']";
    String NOTIFICATION_CONTAINER_ID = "che-notification-container";
  }

  public enum BottomButton {
    SAVE_BUTTON("save-button"),
    APPLY_BUTTON("apply-button"),
    CANCEL_BUTTON("cancel-button");

    private final String buttonNameAttribute;

    BottomButton(String buttonNameAttribute) {
      this.buttonNameAttribute = buttonNameAttribute;
    }

    public String get() {
      return this.buttonNameAttribute;
    }
  }

  public void waitNotification(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(By.id(NOTIFICATION_CONTAINER_ID), expectedText);
  }

  public WebElement waitBottomButton(BottomButton button) {
    return seleniumWebDriverHelper.waitVisibility(By.name(button.get()));
  }

  public void clickOnBottomButton(BottomButton button) {
    waitBottomButton(button).click();
  }

  private void waitBottomButtonState(BottomButton button, boolean state) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.name(button.get()), "aria-disabled", Boolean.toString(!state));
  }

  public void waitBottomButtonDisabled(BottomButton... buttons) {
    asList(buttons).forEach(button -> waitBottomButtonState(button, false));
  }

  public void waitBottomButtonEnabled(BottomButton... buttons) {
    asList(buttons).forEach(button -> waitBottomButtonState(button, true));
  }

  /**
   * wait the project is present in the 'All projects' tab
   *
   * @param projectName name of project
   */
  public void waitProjectIsPresent(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
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
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_BY_NAME, projectName))));
  }

  /**
   * open settings for project by name in the 'All projects' tab
   *
   * @param projectName name of project
   */
  public void openSettingsForProjectByName(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_BY_NAME, projectName))))
        .click();
    waitProjectDetailsPage();
  }

  /** click on 'DELETE' button in settings of project */
  public void clickOnDeleteProject() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.DELETE_PROJECT)))
        .click();
  }

  /** click on 'DELETE IT!' button in the confirming window */
  public void clickOnDeleteItInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.DELETE_IT_PROJECT)))
        .click();
  }

  public void clickOnDeleteProjectButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.DELETE_SELECTED_PROJECTS)))
        .click();
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
    try {
      waitSearchField();
      waitAddNewProjectButton();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8931");
    }
  }
}
