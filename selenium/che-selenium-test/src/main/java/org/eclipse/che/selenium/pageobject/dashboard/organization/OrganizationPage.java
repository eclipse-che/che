/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.organization;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Sergey Skorik */
@Singleton
public class OrganizationPage {

  private final WebDriverWait redrawUiElementsTimeout;
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  private interface Locators {

    String ORGANIZATION_TITLE_ID = "__organizationDetailsController_organizationName__";
    String ORGANIZATION_NAME = "//input[@name = 'name']";
    String BACK_ICON = "//a[contains(@class, 'che-toolbar-breadcrumb')]/md-icon";
    String SAVE_BUTTON = "//div[contains(@class, 'save-button-placeholder')]//button";
    String SETTINGS_TAB = "//md-tab-item//span[contains(text(), 'Settings')]";
    String WORKSPACE_CAP = "//input[@name = 'workspaceCap']";
    String RUNNING_WORKSPACE_CAP = "//input[@name = 'runtimeCap']";
    String WORKSPACE_RAM_CAP = "//input[@name = 'workspaceRamCap']";
    String DELETE_ORG_BUTTON = "//button[contains(@class, 'che-button')]//span[text() = 'Delete']";
    String DELETE_ORG_WIDGET_BUTTON =
        "//div[contains(@class,'che-confirm-dialog-notification')]//span";
    String SUB_ORGANIZATIONS_TAB = "//md-tab-item//span[contains(text(), 'Sub-Organizations')]";
    String ADD_SUB_ORGANIZATION_BUTTON =
        "//che-button-primary[@che-button-title =  'Add Sub-Organization']/a";
    String MEMBERS_TAB = "//md-tab-item//span[contains(text(), 'Members')]";
    String MEMBERS_LIST = "//list-organization-members";
    String ADD_MEMBER_BUTTON = "//che-button-primary[@ng-click = 'onAdd()']";
    String DELETE_MEMBER_BUTTON_ID = "delete-item-button";
    String MEMBERS_HEADER_XPATH =
        "//md-content[contains(@class, 'organization-member-list')]//div[contains(@class, 'che-list-header-column')]//span";
    String MEMBERS_LIST_ITEM_XPATH =
        "//md-content[contains(@class, 'organization-member-list')]//span[text() = '%s']";
    String MEMBER_CHECKBOX = "//md-checkbox[contains(@aria-label,'member')]";
    String DELETE_MEMBER_WIDGET_BUTTON = "//che-popup//button";
    String MEMBERS_SEARCH_FIELD = "//div[@class = 'che-list-search']//input";
    String WORKSPACES_TAB_XPATH = "//md-tab-item//span[contains(text(), 'Workspaces')]";
    String ADD_WORKSPACE_BTN_XPATH = "//*[@che-button-title='Add Workspace']";
  }

  @FindBy(id = Locators.ORGANIZATION_TITLE_ID)
  WebElement organizationTitle;

  @FindBy(xpath = Locators.ORGANIZATION_NAME)
  WebElement organizationName;

  @FindBy(xpath = Locators.BACK_ICON)
  WebElement backIcon;

  @FindBy(xpath = Locators.SAVE_BUTTON)
  WebElement saveButton;

  @FindBy(xpath = Locators.WORKSPACE_CAP)
  WebElement workspaceCap;

  @FindBy(xpath = Locators.RUNNING_WORKSPACE_CAP)
  WebElement runningWorkspaceCap;

  @FindBy(xpath = Locators.WORKSPACE_RAM_CAP)
  WebElement workspaceRamCap;

  @FindBy(xpath = Locators.DELETE_ORG_BUTTON)
  WebElement deleteOrgButton;

  @FindBy(xpath = Locators.DELETE_ORG_WIDGET_BUTTON)
  WebElement deleteOrgWidgetButton;

  @FindBy(xpath = Locators.SETTINGS_TAB)
  WebElement settingsTab;

  @FindBy(xpath = Locators.MEMBERS_TAB)
  WebElement membersTab;

  @FindBy(xpath = Locators.MEMBERS_LIST)
  WebElement membersList;

  @FindBy(xpath = Locators.SUB_ORGANIZATIONS_TAB)
  WebElement subOrganizationsTab;

  @FindBy(xpath = Locators.ADD_SUB_ORGANIZATION_BUTTON)
  WebElement addSubOrganizationsButton;

  @FindBy(xpath = Locators.ADD_MEMBER_BUTTON)
  WebElement addMemberButton;

  @FindBy(id = Locators.DELETE_MEMBER_BUTTON_ID)
  WebElement deleteMemberButton;

  @FindBy(xpath = Locators.DELETE_MEMBER_WIDGET_BUTTON)
  WebElement widgetDeleteMemberButton;

  @FindBy(xpath = Locators.MEMBERS_SEARCH_FIELD)
  WebElement membersSearchField;

  @FindBy(xpath = Locators.WORKSPACES_TAB_XPATH)
  WebElement workspacesTab;

  @Inject
  public OrganizationPage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitOrganizationTitle(String name) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBe(By.id(Locators.ORGANIZATION_TITLE_ID), name));
  }

  public void clickSettingsTab() {
    WaitUtils.sleepQuietly(1);
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(settingsTab)).click();
  }

  public void clickBackButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(backIcon)).click();
  }

  public void clickMembersTab() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersTab)).click();
  }

  public void clickOnWorkspacesTab() {
    seleniumWebDriverHelper.waitAndClick(workspacesTab);
  }

  public void waitMembersList() {
    redrawUiElementsTimeout.until(visibilityOf(membersList));
  }

  public boolean isAddMemberButtonVisible() {
    List<WebElement> elements =
        seleniumWebDriver.findElements(By.xpath(Locators.ADD_MEMBER_BUTTON));
    return elements.isEmpty() ? false : elements.get(0).isDisplayed();
  }

  public void clickSubOrganizationsTab() {
    WaitUtils.sleepQuietly(1);
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(subOrganizationsTab)).click();
  }

  public void clickAddSuborganizationButton() {
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(addSubOrganizationsButton))
        .click();
  }

  public void waitOrganizationName(String name) {
    redrawUiElementsTimeout.until(
        ExpectedConditions.attributeToBeNotEmpty(organizationName, "value"));
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.ORGANIZATION_NAME))
                .getAttribute("value")
                .equals(name));
  }

  public String getOrganizationTitle() {
    return redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(organizationTitle))
        .getText();
  }

  public String getOrganizationName() {
    return organizationName.getAttribute("value");
  }

  public void setOrganizationName(String name) {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(organizationName)).clear();
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(organizationName)).sendKeys(name);
    waitOrganizationName(name);
  }

  public void clickSaveButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(saveButton)).click();
  }

  public void clickDeleteOrganizationButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(deleteOrgButton)).click();
  }

  public int getWorkspaceCap() {
    return Integer.parseInt(
        redrawUiElementsTimeout
            .until(ExpectedConditions.visibilityOf(workspaceCap))
            .getAttribute("value"));
  }

  public int getRunningWorkspaceCap() {
    return Integer.parseInt(
        redrawUiElementsTimeout
            .until(ExpectedConditions.visibilityOf(runningWorkspaceCap))
            .getAttribute("value"));
  }

  public int getWorkspaceRamCap() {
    return Integer.parseInt(
        redrawUiElementsTimeout
            .until(ExpectedConditions.visibilityOf(workspaceRamCap))
            .getAttribute("value"));
  }

  public void setWorkspaceCap(int capSize) {
    String size = String.valueOf(capSize);
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(workspaceCap)).clear();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(workspaceCap))
        .sendKeys(String.valueOf(size));
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.WORKSPACE_CAP))
                .getAttribute("value")
                .equals(size));
  }

  public void setRunningWorkspaceCap(int capSize) {
    String size = String.valueOf(capSize);
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(runningWorkspaceCap)).clear();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(runningWorkspaceCap))
        .sendKeys(String.valueOf(size));
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.RUNNING_WORKSPACE_CAP))
                .getAttribute("value")
                .equals(size));
  }

  public void setWorkspaceRamCap(int capSize) {
    String size = String.valueOf(capSize);
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(workspaceRamCap)).clear();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(workspaceRamCap))
        .sendKeys(String.valueOf(size));
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.DELETE_ORG_BUTTON))
                .getAttribute("value")
                .equals(size));
  }

  public void clickAddMemberButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(addMemberButton)).click();
  }

  public void checkMemberExistsInMembersList(String email) {
    redrawUiElementsTimeout.until(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath(String.format(Locators.MEMBERS_LIST_ITEM_XPATH, email))));
  }

  public void checkMemberIsNotExistsInMembersList(String email) {
    redrawUiElementsTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(String.format(Locators.MEMBERS_LIST_ITEM_XPATH, email))));
  }

  public void deleteMember(String email) {
    String s = String.format(Locators.MEMBERS_LIST_ITEM_XPATH, email);
    s = s + "/ancestor::div[3]" + Locators.MEMBER_CHECKBOX;
    WaitUtils.sleepQuietly(1);
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(s)))
        .click();
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(deleteMemberButton)).click();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(widgetDeleteMemberButton))
        .click();
    redrawUiElementsTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(String.format(Locators.MEMBERS_LIST_ITEM_XPATH, email))));
  }

  public String getMembersNameByEmail(String email) {
    String s = String.format(Locators.MEMBERS_LIST_ITEM_XPATH, email);
    s = s + "/ancestor::div[2]//span[contains(@class, 'member-email')]";
    return redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(s)))
        .getText();
  }

  public void searchMembers(String name) {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersSearchField)).clear();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(membersSearchField))
        .sendKeys(name);
  }

  public void clearSearchField() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersSearchField)).clear();
  }

  public boolean isDeleteButtonVisible() {
    List<WebElement> elements =
        seleniumWebDriver.findElements(By.xpath(Locators.DELETE_ORG_BUTTON));
    return elements.isEmpty() ? false : elements.get(0).isDisplayed();
  }

  public boolean isOrganizationNameReadonly() {
    return isReadonly(organizationName);
  }

  public boolean isWorkspaceCapReadonly() {
    return isReadonly(workspaceCap);
  }

  public boolean isRunningCapReadonly() {
    return isReadonly(runningWorkspaceCap);
  }

  public boolean isRAMCapReadonly() {
    return isReadonly(workspaceRamCap);
  }

  private boolean isReadonly(WebElement element) {
    String readonly = element.getAttribute("readonly");
    return readonly == null ? false : readonly.equalsIgnoreCase("true");
  }

  public void clickOnAddWorkspaceBtn() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_WORKSPACE_BTN_XPATH));
  }
}
