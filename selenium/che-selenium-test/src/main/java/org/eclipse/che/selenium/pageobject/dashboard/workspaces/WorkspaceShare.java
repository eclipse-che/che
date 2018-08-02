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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class WorkspaceShare {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspaceShare(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String ADD_DEVELOPER_BUTTON_XPATH = "//che-button-primary[@che-button-title='Add Developer']";
    String BULK_SELECTION_ID = "share-workspace-bulk-selection";
    String FILTER_MEMBERS_BY_NAME_FIELD_XPATH = "//input[@ng-placeholder='Search']";
    String MEMBER_ITEM_XPATH_PATTERN = "//div[@id='member-email-%s']";
    String MEMBER_NAME_XPATH = MEMBER_ITEM_XPATH_PATTERN + "//span[@data-user-email]";
    String MEMBER_CHECKBOX_XPATH = MEMBER_ITEM_XPATH_PATTERN + "//md-checkbox";
    String MEMBER_PERMISSIONS_XPATH = MEMBER_ITEM_XPATH_PATTERN + "//span[@data-user-permissions]";;
    String REMOVE_MEMBER_ICON_XPATH = MEMBER_ITEM_XPATH_PATTERN + "//div[@data-user-action]";
    String INVITE_MEMBER_DIALOG_ID = "add-members-dialog";
    String CLOSE_DIALOG_BUTTON_NAME_ID = "close-dialog-button";
    String SELECT_ALL_MEMBERS_BY_BULK_DIALOG_XPATH = "//md-checkbox[@aria-label='Member list']";
    String SHARE_WORKSPACE_DIALOG_BUTTON_NAME = "shareButton";
    String NO_MEMBERS_IN_ORGANIZATION_DIALOG_XPATH = "//che-popup[@title='No members in team']";
  }

  @FindBy(xpath = Locators.FILTER_MEMBERS_BY_NAME_FIELD_XPATH)
  WebElement filterMembersField;

  @FindBy(id = Locators.BULK_SELECTION_ID)
  WebElement bulkSelection;

  public void clickOnBulkSelection() {
    seleniumWebDriverHelper.waitAndClick(bulkSelection);
  }

  public void filterMembers(String value) {
    filterMembersField.clear();
    filterMembersField.sendKeys(value);
  }

  public void waitMemberNameInShareList(String name) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.MEMBER_NAME_XPATH, name)));
  }

  public void waitMemberNameNotExistsInShareList(String name) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(Locators.MEMBER_NAME_XPATH, name)));
  }

  public void clickOnAddDeveloperButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_DEVELOPER_BUTTON_XPATH));
  }

  public String getMemberPermissions(String name) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(format(Locators.MEMBER_PERMISSIONS_XPATH, name)));
  }

  public void clickOnMemberCheckbox(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.MEMBER_CHECKBOX_XPATH, name)));
  }

  public Boolean isMemberCheckedInList(String name) {
    return Boolean.parseBoolean(
        seleniumWebDriverHelper
            .waitVisibility(By.xpath(format(Locators.MEMBER_CHECKBOX_XPATH, name)))
            .getAttribute("aria-checked"));
  }

  public void clickOnRemoveMemberButton(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.REMOVE_MEMBER_ICON_XPATH, name)));
  }

  public void waitInviteMemberDialog() {
    seleniumWebDriverHelper.waitVisibility(By.id(Locators.INVITE_MEMBER_DIALOG_ID));
  }

  public void waitInviteMemberDialogClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.INVITE_MEMBER_DIALOG_ID));
  }

  public void selectAllMembersInDialogByBulk() {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(Locators.SELECT_ALL_MEMBERS_BY_BULK_DIALOG_XPATH));
  }

  public void clickOnShareWorkspaceButton() {
    seleniumWebDriverHelper.waitAndClick(By.name(Locators.SHARE_WORKSPACE_DIALOG_BUTTON_NAME));
  }

  public void closeInviteMemberDialog() {
    seleniumWebDriverHelper.waitAndClick(By.id(Locators.INVITE_MEMBER_DIALOG_ID));
  }

  public void waitNoMembersDialog() {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG_XPATH));
  }

  public void closeNoMembersDialog() {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG_XPATH));
  }
}
