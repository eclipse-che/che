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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceShare {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspaceShare(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String ADD_DEVELOPER_BTN = "//che-button-primary[@che-button-title='Add Developer']";
    String BULK_SELECTION = "share-workspace-bulk-selection";
    String DELETE_MEMBERS_BUTTON = "delete-item-button";

    String MEMBER_ITEM = "//div[@id='member-name-%s']";
    String MEMBER_NAME_XPATH = MEMBER_ITEM + "//span[@name='member-name']";
    String MEMBER_CHECKBOX_XPATH = MEMBER_ITEM + "//md-checkbox";
    String MEMBER_PERMISSIONS_XPATH = MEMBER_ITEM + "//span[@name='member-permissions']";;
    String REMOVE_MEMBER_ICON = MEMBER_ITEM + "//div[@name='remove-member']";

    String INVITE_MEMBER_DIALOG = "add-members-dialog";
    String CLOSE_DIALOG_BUTTON_NAME = "close-dialog-button";
    String SELECT_ALL_MEMBERS_BY_BULK_DIALOG = "//md-checkbox[@aria-label='Member list']";
    String SHARE_WORKSPACE_DIALOG_BUTTON = "shareButton";

    String NO_MEMBERS_IN_ORGANIZATION_DIALOG = "//md-dialog";

    String FILTER_MEMBERS_BY_NAME_FIELD = "";
  }

  /**
   * Wait the email of developer is present in 'Share' tab
   *
   * @param name the email of developer
   */
  public void waitMemberNameInShareList(String name) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.MEMBER_NAME_XPATH, name)));
  }

  public void waitMemberNameNotExistsInShareList(String name) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(Locators.MEMBER_NAME_XPATH, name)));
  }

  public void clickOnAddDeveloperButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_DEVELOPER_BTN));
  }

  public String getMemberPermissions(String name) {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.xpath(format(Locators.MEMBER_PERMISSIONS_XPATH, name)));
  }

  public void clickOnMemberCheckbox(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.MEMBER_CHECKBOX_XPATH, name)));
  }

  public String isMemberCheckedInList(String name) {
    return seleniumWebDriverHelper
        .waitVisibility(By.xpath(format(Locators.MEMBER_CHECKBOX_XPATH, name)))
        .getAttribute("aria-checked");
  }

  public void clickOnRemoveMemberButton(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.REMOVE_MEMBER_ICON, name)));
  }

  public void waitInviteMemberDialog() {
    seleniumWebDriverHelper.waitVisibility(By.id(Locators.INVITE_MEMBER_DIALOG));
  }

  public void selectAllMembersInDialogByBulk() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.SELECT_ALL_MEMBERS_BY_BULK_DIALOG));
  }

  public void clickOnShareWorkspaceButton() {
    seleniumWebDriverHelper.waitAndClick(By.name(Locators.SHARE_WORKSPACE_DIALOG_BUTTON));
  }

  public void closeInviteMemberDialog() {
    seleniumWebDriverHelper.waitAndClick(By.id(Locators.INVITE_MEMBER_DIALOG));
  }

  public void waitNoMembersDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG));
  }

  public void closeNoMembersDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG));
  }

  /**
   * Delete a developer from sharing list
   *
   * @param email is an email of developer into sharing list
   */
  public void deleteDeveloperFromShareList(String email) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(String.format(Locators.REMOVE_MEMBER_ICON, email))))
        .click();
  }
}
