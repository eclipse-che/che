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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
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

    String MEMBER_ITEM = "//div[@id='member-name-%s']";
    String MEMBER_NAME_XPATH = MEMBER_ITEM + "//span[@name='member-name']";
    String MEMBER_CHECKBOX_XPATH = MEMBER_ITEM + "//span[@name='remove-member']";
    String MEMBER_PERMISSIONS_XPATH = MEMBER_ITEM + "//span[@name='member-permissions']";;
    String REMOVE_MEMBER_ICON = MEMBER_ITEM + "//div[@name='remove-member']";

    String INVITE_MEMBER_DIALOG = "//che-popup[@id='add-members-dialog']";
    String CLOSE_DIALOG_BUTTON_NAME = "close-dialog-button";
    String SELECT_MEMBER_DIALOG_BUTTON = "//che-button-primary[@che-button-title='Share']";
    String SHARE_WORKSPACE_DIALOG_BUTTON_NAME = "shareButton";

    String NO_MEMBERS_IN_ORGANIZATION_DIALOG = "//md-dialog";

    String FILTER_MEMBERS_BY_NAME_FIELD = "";

    String INPUT_SHARE_DIALOG = "//md-chips[contains(@class,'share-user-input')]//input";
    String SHARE_BTN_DIALOG = "//che-button-primary[@aria-disabled='false']//span[text()='Share']";
    String DEVELOPER_SHARE_ITEM = "//span[text()='%s']";
    String WARNING_DIALOG_DELETE = "//div[@class='ng-binding' and text()=\"%s\"]";
  }

  public void waitMemberNameInShareList(String name) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.MEMBER_NAME_XPATH, name)));
  }

  public void clickOnAddDeveloperButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_DEVELOPER_BTN));
  }

  public void waitInviteMemberDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.INVITE_MEMBER_DIALOG));
  }

  public void closeInviteMemberDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.INVITE_MEMBER_DIALOG));
  }

  public void waitNoMembersDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG));
  }

  public void closeNoMembersDialog() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.NO_MEMBERS_IN_ORGANIZATION_DIALOG));
  }

  /**
   * Adds developer to share list
   *
   * @param email is an email of developer into sharing list
   */
  public void addDeveloperToShareList(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_DEVELOPER_BTN)))
        .click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.INPUT_SHARE_DIALOG)))
        .sendKeys(email + ",");
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.SHARE_BTN_DIALOG)))
        .click();
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

  /** Wait the text into warning dialog delete or remove */
  public void waitTextInWarningDialogDelete(String expText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.WARNING_DIALOG_DELETE, expText))));
  }

  /**
   * Wait the email of developer is present in 'Share' tab
   *
   * @param email the email of developer
   */
  public void waitDeveloperIsPresentInShareTab(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.DEVELOPER_SHARE_ITEM, email))));
  }

  /**
   * Wait the email of developer is not present in 'Share' tab
   *
   * @param email the email of developer
   */
  public void waitDeveloperIsNotPresentInShareTab(String email) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(By.xpath(format(Locators.DEVELOPER_SHARE_ITEM, email))));
  }
}
