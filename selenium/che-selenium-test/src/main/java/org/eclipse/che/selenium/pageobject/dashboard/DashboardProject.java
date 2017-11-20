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
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class DashboardProject {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public DashboardProject(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum Template {
    WEB_JAVA_SPRING("web-java-spring"),
    CONSOLE_JAVA_SIMPLE("console-java-simple"),
    WEB_JAVA_PETCLINIC("web-java-petclinic");

    private final String value;

    Template(String value) {
      this.value = value;
    }

    /** @return name of template displayed in dashboard. */
    public String value() {
      return value;
    }
  }

  private interface Locators {
    String NAME_WORKSPACE_FIELD = "input[placeholder='Name of the workspace']";
    String CREATE_PROJECT_BUTTON = "//button/span[contains(text(), 'Create')]";
    String NAME_OF_PROJECT_FIELD = "input[placeholder='Name of the project']";
    String CREATE_NEW_WS = "//md-radio-button[@aria-label='Create new workspace from stack']";
    String SELECT_EXISTING_WS = "//div[@class='list-item-name ng-binding' and text()='%s']";
    String SELECT_TEMPLATE_PROJECT = "//div[text()='%s']";
    String NEW_PROJECT_LINK = "//a[@ng-href='#/create-project']/span[text()='Create Workspace']";
    String SELECT_SOURCE_ITEM = "//md-radio-button[@aria-label='%s']";
    String GIT_URL_INPUT = "//input[@placeholder='Url of the git repository']";
    String ZIP_URL_INPUT = "//input[@placeholder='Url of the zip file']";
    String LOCATION_TAB = "//span[text()='%s']//parent::md-tab-item";
    String EXISTING_WS_RADIO_BUTTON = "//md-radio-button[@value='existing-workspace']";
    String PROJECT_BY_NAME = "//div[contains(@ng-click, 'projectItem')]/span[text()='%s']";
    String DELETE_PROJECT = "//button/span[text()='Delete']";
    String DELETE_IT_PROJECT = "//che-button-primary[@che-button-title='Delete']/button";
    String SKIP_ROOT_FOLDER = "//span[text()='Skip the root folder of the archive']";
  }

  @FindBy(css = Locators.NAME_WORKSPACE_FIELD)
  WebElement nameOfWorkspaceField;

  @FindBy(css = Locators.NAME_OF_PROJECT_FIELD)
  WebElement nameOfProjectField;

  @FindBy(xpath = Locators.CREATE_PROJECT_BUTTON)
  WebElement createProjectButton;

  @FindBy(xpath = Locators.CREATE_NEW_WS)
  WebElement createWsFromStack;

  @FindBy(xpath = Locators.NEW_PROJECT_LINK)
  WebElement newProject;

  @FindBy(xpath = Locators.GIT_URL_INPUT)
  WebElement gitUrlInput;

  @FindBy(xpath = Locators.ZIP_URL_INPUT)
  WebElement zipUrlInput;

  public void waitNameOfWorkspaceField() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nameOfWorkspaceField));
  }

  public void waitNameOfProjectField() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nameOfProjectField));
  }

  public void typeWorkspaceName(String nameOfWs) {
    waitNameOfWorkspaceField();
    nameOfWorkspaceField.clear();
    nameOfWorkspaceField.sendKeys(nameOfWs);
  }

  public void typeProjectName(String nameOfProject) {
    waitNameOfProjectField();
    nameOfProjectField.clear();
    nameOfProjectField.sendKeys(nameOfProject);
  }

  /** click on the radio button 'Create new workspace from stack' */
  public void selectCreateNewWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(createWsFromStack))
        .click();
  }

  /** click on create new project button on New project page */
  public void clickOnCreateProject() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(createProjectButton))
        .click();
  }

  /**
   * select existing workspace in the 'Select Workspace' tab when create new project
   *
   * @param wsName is name of the existing workspace
   */
  public void selectExistingWs(String wsName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.EXISTING_WS_RADIO_BUTTON)))
        .click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SELECT_EXISTING_WS, wsName))))
        .click();
  }

  /**
   * select template project in the 'STACK LIBRARY' tab
   *
   * @param projectName is name of the existing template project
   */
  public void selectTemplateProject(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SELECT_TEMPLATE_PROJECT, projectName))))
        .click();
  }

  /**
   * Select source in the 'New Project' tab
   *
   * @param nameSource name os source
   */
  public void selectSource(String nameSource) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SELECT_SOURCE_ITEM, nameSource))))
        .click();
  }

  /** Click on '+' button in the 'New Project' tab */
  public void clickOnNewProjectButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(newProject))
        .click();
  }

  /**
   * Enter Git URL for project in the 'New Project' tab
   *
   * @param url URL for project on git repository
   */
  public void enterGitURL(String url) {
    WebElement input =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOf(gitUrlInput));
    input.clear();
    input.sendKeys(url);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(gitUrlInput, url));
  }

  /**
   * Enter ZIP URL for project in the 'New Project' tab
   *
   * @param url URL for zip archive
   */
  public void enterZipURL(String url) {
    WebElement input =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOf(zipUrlInput));
    input.clear();
    input.sendKeys(url);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(zipUrlInput, url));
  }

  /**
   * Select existing location for import project in the 'New Project' tab
   *
   * @param location name of location
   */
  public void selectExistingLocation(String location) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.LOCATION_TAB, location))))
        .click();
  }

  /**
   * wait the project is present in the 'All projects' tab
   *
   * @param projectName name of project
   */
  public void waitProjectIsPresent(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
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
            ExpectedConditions.invisibilityOfElementLocated(
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
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.PROJECT_BY_NAME, projectName))))
        .click();
  }

  /** click on 'DELETE' button in settings of project */
  public void clickOnDeleteProject() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.DELETE_PROJECT)))
        .click();
  }

  /** click on 'DELETE IT!' button in the confirming window */
  public void clickOnDeleteItInDialogWindow() {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.DELETE_IT_PROJECT)))
        .click();
  }

  /** Click on 'Skip the root folder of the archive' */
  public void clickOnSkipRootFolder() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SKIP_ROOT_FOLDER)))
        .click();
  }

  /**
   * Create workspace with new project inside.
   *
   * @param wsName name of workspace to place the project
   * @param template name of project template
   * @param projectName name of project
   */
  public void createProjectFromTemplate(String wsName, Template template, String projectName) {
    clickOnNewProjectButton();
    selectExistingWs(wsName);
    selectSource("New from blank, template, or sample project");
    selectTemplateProject(template.value());
    typeProjectName(projectName);
    clickOnCreateProject();
  }
}
