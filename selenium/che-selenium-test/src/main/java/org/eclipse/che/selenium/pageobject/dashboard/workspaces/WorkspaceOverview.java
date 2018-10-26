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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview.Locators.AS_FILE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview.Locators.AS_FILE_CONFIG_BODY_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview.Locators.EXPORT_WS_FORM_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview.Locators.PRIVATE_CLOUD_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview.Locators.SAVE_BUTTON_XPATH;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WorkspaceOverview {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  public WorkspaceOverview(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String NAME_WORKSPACE_INPUT = "//input[@placeholder='Name of the workspace *']";
    String DELETE_WORKSPACE_BTN = "//button/span[text()='Delete']";
    String WORKSPACE_TITLE = "//div[contains(@class,'toolbar-info')]/span[text()='%s']";
    String EXPORT_WORKSPACE_BTN =
        "//button[contains(@class, 'che-button')]/span[text()='Export as a file']";
    String DOWNLOAD_WORKSPACE_BTN = "//che-button-default[@che-button-title='download']";
    String CLIPBOARD_JSON_WS_BTN = "//che-button-default[@che-button-title='clipboard']";
    String HIDE_JSON_WS_BTN = "//span[text()='Close']";
    String WORKSPACE_JSON_CONTENT = "//div[@class='CodeMirror-code']";
    String WS_NAME_ERROR_MESSAGES = "//che-error-messages";
    String EXPORT_WS_FORM_XPATH = "//che-popup[@title='Export Workspace']";
    String AS_FILE_CONFIG_BODY_XPATH =
        "//che-popup[@title='Export Workspace']//div[@class='CodeMirror-code']";

    String SAVE_BUTTON_XPATH = "//button[@name='save-button' ]";
    String AS_FILE_BUTTON_XPATH = "//md-tab-item/span[text()='As a File']";
    String PRIVATE_CLOUD_BUTTON_XPATH = "//md-tab-item/span[text()='To Private Cloud']";
  }

  @FindBy(xpath = Locators.NAME_WORKSPACE_INPUT)
  WebElement nameWorkspaceInput;

  @FindBy(xpath = Locators.DELETE_WORKSPACE_BTN)
  WebElement deleteWorkspaceBtn;

  @FindBy(xpath = Locators.EXPORT_WORKSPACE_BTN)
  WebElement exportWsButton;

  @FindBy(xpath = Locators.DOWNLOAD_WORKSPACE_BTN)
  WebElement downloadWsJsonBtn;

  @FindBy(xpath = Locators.CLIPBOARD_JSON_WS_BTN)
  WebElement clipboardWsJsonBtn;

  @FindBy(xpath = Locators.HIDE_JSON_WS_BTN)
  WebElement hideJsonWsBtn;

  @FindBy(xpath = Locators.WORKSPACE_JSON_CONTENT)
  WebElement workspaceJsonContent;

  @FindBy(xpath = Locators.WS_NAME_ERROR_MESSAGES)
  WebElement errorMessages;

  /**
   * Check name of workspace in 'Overview' tab
   *
   * @param nameWorkspace expected name of workspace
   */
  public void checkNameWorkspace(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.WORKSPACE_TITLE, nameWorkspace))));
  }

  public void waitNameFieldValue(String workspaceName) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver ->
            seleniumWebDriverHelper
                .waitVisibilityAndGetValue(nameWorkspaceInput)
                .equals(workspaceName));
  }

  public void waitUntilNoErrorsDisplayed() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(nameWorkspaceInput, "aria-invalid", "false");
  }

  public void waitErrorBorderOfNameField() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(nameWorkspaceInput, "aria-invalid", "true");
  }

  public void waitSaveButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(SAVE_BUTTON_XPATH));
  }

  public void waitDisabledSaveButton() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(SAVE_BUTTON_XPATH), "aria-disabled", "true");
  }

  public void waitEnabledSaveButton() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(SAVE_BUTTON_XPATH), "aria-disabled", "false");
  }

  public void waitExportWsFormOpened() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(EXPORT_WS_FORM_XPATH));
  }

  public String getAsFileFormText() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(AS_FILE_CONFIG_BODY_XPATH));
  }

  public void waitConfiguration(String... expectedConfiguration) {
    asList(expectedConfiguration).forEach(config -> getAsFileFormText().contains(config));
  }

  public void clickOnAsFileButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(AS_FILE_BUTTON_XPATH));
  }

  public void waitExportWorkspaceFormOpened() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(EXPORT_WS_FORM_XPATH));
    testWebElementRenderChecker.waitElementIsRendered(By.xpath(EXPORT_WS_FORM_XPATH));
  }

  public void waitExportWorkspaceFormClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(EXPORT_WS_FORM_XPATH));
  }

  public boolean isAsFileTabOpened() {
    final String asFileButtonParentContainer = AS_FILE_BUTTON_XPATH + "/parent::md-tab-item";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(asFileButtonParentContainer), "aria-selected")
        .equals("true");
  }

  public void waitAsFileTabOpened() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isAsFileTabOpened());
  }

  public void waitAsFileTabClosed() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isAsFileTabOpened());
  }

  public void clickOnToPrivateCloudButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(PRIVATE_CLOUD_BUTTON_XPATH));
  }

  public boolean isToPrivateCloudTabOpened() {
    final String privateCloudButtonParentContainer =
        PRIVATE_CLOUD_BUTTON_XPATH + "/parent::md-tab-item";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(privateCloudButtonParentContainer), "aria-selected")
        .equals("true");
  }

  public void waitToPrivateCloudTabOpened() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isToPrivateCloudTabOpened());
  }

  public void waitToPrivateCloudTabClosed() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isToPrivateCloudTabOpened());
  }

  public void clickOnDownloadButton() {
    final String downloadButtonXpath = "//che-button-default [@che-button-title='download']";
    seleniumWebDriverHelper.waitAndClick(By.xpath(downloadButtonXpath));
  }

  public void clickOnClipboardButton() {
    final String clipboardButtonXpath = "//che-button-default [@che-button-title='clipboard']";
    seleniumWebDriverHelper.waitAndClick(By.xpath(clipboardButtonXpath));
  }

  public void clickOnCloseExportWorkspaceFormButton() {
    final String closeButtonXpath = "//che-button-notice [@che-button-title='Close']";
    seleniumWebDriverHelper.waitAndClick(By.xpath(closeButtonXpath));
  }

  public void clickOnCloseExportWorkspaceFormIcon() {
    final String closeFormIcon = EXPORT_WS_FORM_XPATH + "//i";
    seleniumWebDriverHelper.waitAndClick(By.xpath(closeFormIcon));
  }

  public void waitNameErrorMessage(String expectedMessage) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> isWorkspaceNameErrorMessageEquals(expectedMessage));
  }

  public Boolean isWorkspaceNameErrorMessageEquals(String message) {
    return errorMessages.getText().equals(message);
  }

  public void checkOnWorkspaceNameErrorAbsence() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath("//che-error-messages/div")));
  }

  public void enterNameWorkspace(String nameWorkspace) {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(visibilityOf(nameWorkspaceInput))
        .clear();
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(visibilityOf(nameWorkspaceInput))
        .sendKeys(nameWorkspace);
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(textToBePresentInElementValue(nameWorkspaceInput, nameWorkspace));
  }

  public void clickExportWorkspaceBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(exportWsButton))
        .click();
  }

  /** click on 'DELETE' button in 'Delete workspace' */
  public void clickOnDeleteWorkspace() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(deleteWorkspaceBtn))
        .click();
  }

  public void isDeleteWorkspaceButtonExists() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(deleteWorkspaceBtn));
  }

  public void waitDownloadWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(downloadWsJsonBtn));
  }

  public void waitClipboardWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(clipboardWsJsonBtn));
  }

  public void clickOnHideWorkspaceJsonFileBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(hideJsonWsBtn))
        .click();
  }

  public void clickIntoWorkspaceJsonContent() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(workspaceJsonContent))
        .click();
  }
}
