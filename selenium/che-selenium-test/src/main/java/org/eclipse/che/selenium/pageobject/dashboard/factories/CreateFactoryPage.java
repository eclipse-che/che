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
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CreateFactoryPage {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final WebDriverWait redrawUiElementsTimeout;

  @Inject
  public CreateFactoryPage(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
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
    String MINIMAL_FACTORY_TEMPLATE_ID = "minimal-template-button";
    String COMPLETE_FACTORY_TEMPLATE_ID = "complete-template-button";
    String CREATE_FACTORY_BUTTON_ID = "create-factory-next-button";
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

  @FindBy(id = Locators.COMPLETE_FACTORY_TEMPLATE_ID)
  WebElement completeTemplateButton;

  @FindBy(xpath = Locators.SEARCH_WORKSPACE_BUTTON)
  WebElement searchWorkspaceButton;

  @FindBy(xpath = Locators.SEARCH_WORKSPACE_FIELD)
  WebElement searchWorkspaceField;

  public void waitToolbarTitle() {
    redrawUiElementsTimeout.until(visibilityOf(newFactoryPageTitle));
  }

  public void typeFactoryName(String name) {
    redrawUiElementsTimeout.until(visibilityOf(factoryName));
    factoryName.clear();
    factoryName.sendKeys(name);
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnSourceTab(String tabName) {
    redrawUiElementsTimeout.until(visibilityOfElementLocated(By.id(tabName))).click();
    WaitUtils.sleepQuietly(1);
  }

  public void clickOnMinimalTemplateButton() {
    redrawUiElementsTimeout.until(visibilityOf(minimalTemplateButton)).click();
  }

  public void clickOnCompleteTemplateButton() {
    redrawUiElementsTimeout.until(visibilityOf(completeTemplateButton)).click();
  }

  public void waitTemplateButtons() {
    redrawUiElementsTimeout.until(visibilityOf(minimalTemplateButton));
    redrawUiElementsTimeout.until(visibilityOf(completeTemplateButton));
  }

  public Boolean isCreateFactoryButtonDisabled() {
    String attrValue =
        redrawUiElementsTimeout
            .until(visibilityOf(createFactoryButton))
            .getAttribute("aria-disabled");
    return Boolean.parseBoolean(attrValue);
  }

  public void clickOnCreateFactoryButton() {
    redrawUiElementsTimeout.until(visibilityOf(createFactoryButton)).click();
  }

  public void clickOnWorkspaceFromList(String workspaceName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_NAME_XPATH, workspaceName))))
        .click();
  }

  public void waitWorkspaceNameInList(String workspaceName) {
    redrawUiElementsTimeout.until(
        visibilityOfElementLocated(By.xpath(format(Locators.WORKSPACE_NAME_XPATH, workspaceName))));
  }

  public String getErrorMessage() {
    return redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.xpath(Locators.NEW_FACTORY_ERROR_MESSAGE)))
        .getText();
  }

  public void waitErrorMessageNotVisible() {
    redrawUiElementsTimeout.until(
        invisibilityOfElementLocated(By.xpath(Locators.NEW_FACTORY_ERROR_MESSAGE)));
  }

  public void waitUploadFileButton() {
    redrawUiElementsTimeout.until(visibilityOf(uploadFileButton));
  }

  public void waitGitUrlField() {
    redrawUiElementsTimeout.until(visibilityOfElementLocated(By.id(Locators.GIT_URL_ID)));
  }

  public void clickOnSearchFactoryButton() {
    redrawUiElementsTimeout.until(visibilityOf(searchWorkspaceButton)).click();
  }

  public void waitSearchFactoryField() {
    redrawUiElementsTimeout.until(visibilityOf(searchWorkspaceField));
  }

  public void typeTextToSearchFactoryField(String text) {
    redrawUiElementsTimeout.until(visibilityOf(searchWorkspaceField));
    searchWorkspaceField.clear();
    searchWorkspaceField.sendKeys(text);
    WaitUtils.sleepQuietly(1);
  }

  public void waitWorkspacesListIsEmpty() {
    redrawUiElementsTimeout.until(invisibilityOfElementLocated(By.id("workspace-name")));
  }
}
