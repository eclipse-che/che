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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceOverview {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WorkspaceOverview(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
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
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.WORKSPACE_TITLE, nameWorkspace))));
  }

  public Boolean isWorkspaceNameErrorMessageEquals(String message) {
    return errorMessages.getText().equals(message);
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
