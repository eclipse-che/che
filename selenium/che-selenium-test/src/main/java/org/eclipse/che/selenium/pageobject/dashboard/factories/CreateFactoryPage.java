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
package org.eclipse.che.selenium.pageobject.dashboard.factories;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.DELETE;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateFactoryPage {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWait redrawUiElementsTimeout;
  protected final ActionsFactory actionsFactory;

  @Inject
  public CreateFactoryPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface TabNames {
    String WORKSPACE_TAB_ID = "new-factory-workspace-tab";
    String GIT_TAB_ID = "new-factory-git-tab";
    String CONFIG_TAB_ID = "new-factory-config-tab";
    String TEMPLATE_TAB_ID = "new-factory-template-tab";
  }

  private interface Locators {
    String NEW_FACTORY_PAGE_TITLE_ID = "New_Factory_From";
    String FACTORY_NAME_ID = "new-factory-name-input";
    String NEW_FACTORY_ERROR_MESSAGE = "//div[@id='new-workspace-error-message']/div";
    String SEARCH_WORKSPACE_BUTTON = "//div[@id='search-factory']//md-icon";
    String SEARCH_WORKSPACE_FIELD = "//div[@id='search-factory']//input";
    String WORKSPACE_NAME_XPATH = "//a[@id='workspace-name' and text()='%s']";
    String GIT_URL_ID = "git-url-input";
    String UPLOAD_FILE_BUTTON_ID = "upload-file-button";
    String HIDDEN_UPLOAD_INPUT_BUTTON_XPATH = "//input[@uploader='factoryFromFileCtrl.uploader']";
    String MINIMAL_FACTORY_TEMPLATE_ID = "minimal-template-button";
    String COMPLETE_FACTORY_TEMPLATE_ID = "complete-template-button";
    String CREATE_FACTORY_BUTTON_ID = "create-factory-next-button";
    String EDITOR_TEMPLATE_XPATH = "//div[@class='CodeMirror-code']";
  }

  @FindBy(id = Locators.NEW_FACTORY_PAGE_TITLE_ID)
  WebElement newFactoryPageTitle;

  @FindBy(id = Locators.FACTORY_NAME_ID)
  WebElement factoryName;

  @FindBy(id = Locators.CREATE_FACTORY_BUTTON_ID)
  WebElement createFactoryButton;

  @FindBy(id = Locators.MINIMAL_FACTORY_TEMPLATE_ID)
  WebElement minimalTemplateButton;

  @FindBy(id = Locators.UPLOAD_FILE_BUTTON_ID)
  WebElement uploadFileButton;

  @FindBy(xpath = Locators.HIDDEN_UPLOAD_INPUT_BUTTON_XPATH)
  WebElement hiddenUploadInputButton;

  @FindBy(id = Locators.COMPLETE_FACTORY_TEMPLATE_ID)
  WebElement completeTemplateButton;

  @FindBy(xpath = Locators.SEARCH_WORKSPACE_BUTTON)
  WebElement searchWorkspaceButton;

  @FindBy(xpath = Locators.SEARCH_WORKSPACE_FIELD)
  WebElement searchWorkspaceField;

  @FindBy(id = Locators.GIT_URL_ID)
  WebElement gitRepoUrlField;

  @FindBy(xpath = Locators.EDITOR_TEMPLATE_XPATH)
  WebElement editorTemplate;

  public void waitToolbarTitle() {
    seleniumWebDriverHelper.waitVisibility(newFactoryPageTitle);
  }

  public void typeFactoryName(String name) {
    seleniumWebDriverHelper.setValue(factoryName, name);
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnSourceTab(String tabName) {
    seleniumWebDriverHelper.waitAndClick(By.id(tabName));
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnMinimalTemplateButton() {
    seleniumWebDriverHelper.waitAndClick(minimalTemplateButton);
  }

  public void clickOnCompleteTemplateButton() {
    seleniumWebDriverHelper.waitAndClick(completeTemplateButton);
  }

  public void waitTemplateButtons() {
    seleniumWebDriverHelper.waitVisibility(minimalTemplateButton);
    seleniumWebDriverHelper.waitVisibility(completeTemplateButton);
  }

  public Boolean isCreateFactoryButtonEnabled() {
    WaitUtils.sleepQuietly(1);
    String attrValue =
        seleniumWebDriverHelper.waitVisibility(createFactoryButton).getAttribute("aria-disabled");
    return !(Boolean.parseBoolean(attrValue));
  }

  public void clickOnCreateFactoryButton() {
    seleniumWebDriverHelper.waitAndClick(createFactoryButton);
  }

  public void clickOnWorkspaceFromList(String workspaceName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.WORKSPACE_NAME_XPATH, workspaceName)));
  }

  public void waitWorkspaceNameInList(String workspaceName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.WORKSPACE_NAME_XPATH, workspaceName)));
  }

  public void waitErrorMessage(String expectedText) {
    seleniumWebDriverHelper.waitTextEqualsTo(
        By.xpath(Locators.NEW_FACTORY_ERROR_MESSAGE), expectedText);
  }

  public void waitErrorMessageNotVisible() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.NEW_FACTORY_ERROR_MESSAGE));
  }

  public boolean isUploadFileButtonEnabled() {
    return seleniumWebDriverHelper.waitVisibilityAndGetEnableState(uploadFileButton);
  }

  // we can't use 'uploadFileButton' element directly here because it is a DIV which just calls
  // hidden input of file type 'hiddenUploadInputButton'
  public void uploadSelectedConfigFile(Path resourcesUploadFile) throws IOException {
    seleniumWebDriverHelper.selectResourceToUpload(hiddenUploadInputButton, resourcesUploadFile);
  }

  public void typeGitRepositoryUrl(String gitRepoUrl) {
    seleniumWebDriverHelper.setValue(gitRepoUrlField, gitRepoUrl);
  }

  public void clickOnSearchFactoryButton() {
    seleniumWebDriverHelper.waitAndClick(searchWorkspaceButton);
  }

  public void waitSearchFactoryField() {
    seleniumWebDriverHelper.waitVisibility(searchWorkspaceField);
  }

  public void typeTextToSearchFactoryField(String expectedText) {
    seleniumWebDriverHelper.setValue(searchWorkspaceField, expectedText);
  }

  public void waitWorkspacesListIsEmpty() {
    seleniumWebDriverHelper.waitInvisibility(By.id("workspace-name"));
  }

  public void setFocusInEditorTemplate() {
    seleniumWebDriverHelper.waitAndClick(editorTemplate);
  }

  public void typeConfigFileToTemplateEditor(String pathToFile)
      throws IOException, URISyntaxException {
    URL resourcesFile = getClass().getResource(pathToFile);

    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(resourcesFile.toURI()), Charset.forName("UTF-8"));
    for (String line : listWithAllLines) {
      seleniumWebDriverHelper.sendKeys(line);
    }
  }

  public void deleteAllContentFromTemplateEditor() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("a")
        .keyUp(CONTROL)
        .sendKeys(DELETE)
        .perform();
  }
}
