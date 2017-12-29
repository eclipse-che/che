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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Ann Shumilova */
@Singleton
public class ProjectSourcePage {

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;

  @Inject
  public ProjectSourcePage(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum Sources {
    SAMPLES("samples"),
    BLANK("blank"),
    GIT("git"),
    GITHUB("github"),
    ZIP("zip");

    private final String title;

    Sources(String title) {
      this.title = title;
    }
  }

  private interface Locators {
    String ADD_OR_IMPORT_PROJECT_BUTTON = "ADD_PROJECT";
    String PROJECT_SOURCE_SELECTOR = "project-source-selector";
    String SAMPLES_BUTTON = "%s-button";
    String SAMPLE_CHECKBOX = "//div[@id='sample-%s']/md-checkbox";
    String TEMPLATE_NAME = "//span[contains(@class,'template-selector-item-name')]";
    String TEMPLATE_DESCRIPTION = "//span[contains(@class,'template-selector-item-description')]";
    String GIT_REPO_XPATH = "remote-git-url-input";
    String ZIP_XPATH = "remote-zip-url-input";
    String ZIP_SKIP_ROOT_XPATH = "zip-skip-root-folder-checkbox";
    String ADD_PROJECT_BUTTON = "add-project-button";
    String CANCEL_BUTTON = "cancelButton";
    String SAVE_BUTTON = "saveButton";
    String REMOVE_BUTTON = "removeButton";

    String PROJECT_NAME = "//input[@name='projectName']";
    String PROJECT_DESCRIPTION = "//input[@name='projectDescription']";
    String CONNECT_GITHUB_ACCOUNT_BUTTON = "//span[text()='Connect your github account']";
    String GITHUB_PROJECTS_LIST =
        "//md-list[contains(@class,'import-github-project-repositories-list')]";
    String GITHUB_PROJECT_CHECKBOX = "//md-checkbox[@aria-label='GitHub repository %s']";
  }

  public interface Template {
    String WEB_JAVA_SPRING = "web-java-spring";
    String CONSOLE_JAVA_SIMPLE = "console-java-simple";
    String WEB_JAVA_PETCLINIC = "web-java-petclinic";
  }

  @FindBy(id = Locators.ADD_OR_IMPORT_PROJECT_BUTTON)
  WebElement addOrImportProjectButton;

  @FindBy(id = Locators.PROJECT_SOURCE_SELECTOR)
  WebElement projectSourceSelector;

  @FindBy(id = Locators.GIT_REPO_XPATH)
  WebElement gitRepositoryInput;

  @FindBy(id = Locators.ZIP_XPATH)
  WebElement zipLocationInput;

  @FindBy(id = Locators.ZIP_SKIP_ROOT_XPATH)
  WebElement zipSkipRoot;

  @FindBy(name = Locators.CANCEL_BUTTON)
  WebElement cancelButton;

  @FindBy(name = Locators.SAVE_BUTTON)
  WebElement saveButton;

  @FindBy(name = Locators.REMOVE_BUTTON)
  WebElement removeButton;

  @FindBy(id = Locators.ADD_PROJECT_BUTTON)
  WebElement addProjectButton;

  public void clickOnAddOrImportProjectButton() {
    addOrImportProjectButton.click();
  }

  // wait that the Project Source Selector visible
  public void waitOpened() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOf(projectSourceSelector));
  }

  /**
   * select type of created project
   *
   * @param source is type of the existing templates
   */
  public void selectSourceTab(Sources source) {
    WebElement sourceTab =
        seleniumWebDriver.findElement(By.id(String.format(Locators.SAMPLES_BUTTON, source.title)));
    sourceTab.click();
  }

  /**
   * Select project sample
   *
   * @param name name of sample
   */
  public void selectSample(String name) {
    WebElement sample =
        seleniumWebDriver.findElement(By.xpath(format(Locators.SAMPLE_CHECKBOX, name)));
    sample.click();
  }

  /**
   * Enter Git URL for project in the 'New Project' tab
   *
   * @param url URL for project on git repository
   */
  public void typeGitRepositoryLocation(String url) {
    gitRepositoryInput.clear();
    gitRepositoryInput.sendKeys(url);
  }

  /**
   * Enter ZIP URL for project in the 'New Project' tab
   *
   * @param url URL for zip archive
   */
  public void typeZipLocation(String url) {
    zipLocationInput.clear();
    zipLocationInput.sendKeys(url);
  }

  public void skipRootFolder() {
    zipSkipRoot.click();
  }

  public void clickOnAddProjectButton() {
    addProjectButton.click();
  }

  public void clickOnCancelChangesButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(cancelButton))
        .click();
  }

  public void clickOnSaveChangesButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(saveButton))
        .click();
  }

  public void clickOnRemoveProjectButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(removeButton))
        .click();
  }

  public void waitCreatedProjectButton(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(projectName)));
  }

  public boolean isProjectNotExists(String projectName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(projectName)));
  }

  public void clickOnCreateProjectButton(String projectName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(projectName)))
        .click();
  }

  public String getProjectName() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.PROJECT_NAME)))
        .getAttribute("value");
  }

  public void changeProjectName(String name) {
    WebElement webElement = seleniumWebDriver.findElement(By.xpath(Locators.PROJECT_NAME));
    webElement.clear();
    webElement.sendKeys(name);
  }

  public String getProjectDescription() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.PROJECT_DESCRIPTION)))
        .getAttribute("value");
  }

  public void changeProjectDescription(String description) {
    WebElement webElement = seleniumWebDriver.findElement(By.xpath(Locators.PROJECT_DESCRIPTION));
    webElement.clear();
    webElement.sendKeys(description);
  }

  public boolean isConnectGitHubAccountButtonVisible() {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.CONNECT_GITHUB_ACCOUNT_BUTTON)))
        .isDisplayed();
  }

  public void clickOnConnectGithubAccountButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.CONNECT_GITHUB_ACCOUNT_BUTTON)))
        .click();
  }

  public void waitGithubProjectsList() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.GITHUB_PROJECTS_LIST)));
  }

  public void selectProjectFromList(String projectName) {
    WebElement project =
        seleniumWebDriver.findElement(
            By.xpath(format(Locators.GITHUB_PROJECT_CHECKBOX, projectName)));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(project).perform();
    project.click();
  }
}
