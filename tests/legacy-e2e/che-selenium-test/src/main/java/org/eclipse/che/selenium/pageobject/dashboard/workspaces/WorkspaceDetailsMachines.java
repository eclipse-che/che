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
import static java.lang.String.valueOf;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.CHECKBOX_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.DECREMENT_RAM_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.DELETE_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.EDIT_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.ENVIRONMENT_VARIABLES_LINK_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.IMAGE_FIELD_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.INCREMENT_RAM_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.INSTALLERS_LINK_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.MACHINE_LIST_ITEM_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.NOTIFICATION_MESSAGE_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.RAM_AMOUNT_TEXT_FIELD_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.SERVERS_LINK_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.SETTINGS_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetailsMachines.Locators.SETTINGS_CONTAINER_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

@Singleton
public class WorkspaceDetailsMachines {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private WorkspaceDetailsMachines(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String MACHINE_LIST_ITEM_XPATH_TEMPLATE =
        "//workspace-machines//ng-transclude[@class='che-list-content' and //span[@machine-name='%s']]";
    String CHECKBOX_XPATH_TEMPLATE = "//md-checkbox[@aria-label= 'Machine-%s']";
    String DECREMENT_RAM_BUTTON_XPATH_TEMPLATE =
        MACHINE_LIST_ITEM_XPATH_TEMPLATE
            + "//ng-form[@name='ramAmountForm']//button[@data-action='decrement']";
    String INCREMENT_RAM_BUTTON_XPATH_TEMPLATE =
        MACHINE_LIST_ITEM_XPATH_TEMPLATE
            + "//ng-form[@name='ramAmountForm']//button[@data-action='increment']";
    String RAM_AMOUNT_TEXT_FIELD_XPATH_TEMPLATE =
        MACHINE_LIST_ITEM_XPATH_TEMPLATE + "//input[@id='machine--ram']";
    String EDIT_BUTTON_XPATH_TEMPLATE = "//div[@edit-machine='%s']";
    String SETTINGS_BUTTON_XPATH_TEMPLATE =
        MACHINE_LIST_ITEM_XPATH_TEMPLATE + "//span[@uib-tooltip='Settings']";
    String DELETE_BUTTON_XPATH_TEMPLATE = "//div[@delete-machine='%s']";
    String SETTINGS_CONTAINER_XPATH =
        "//div[contains(@class, 'workspace-machine-actions-popover') and @is-open='isOpen']";
    String INSTALLERS_LINK_XPATH = SETTINGS_CONTAINER_XPATH + "//a[text()='Installers']";
    String SERVERS_LINK_XPATH = SETTINGS_CONTAINER_XPATH + "//a[text()='Servers']";
    String ENVIRONMENT_VARIABLES_LINK_XPATH =
        SETTINGS_CONTAINER_XPATH + "//a[text()='Environment Variables']";
    String IMAGE_FIELD_XPATH_TEMPLATE =
        MACHINE_LIST_ITEM_XPATH_TEMPLATE + "//span[@machine-image ='%s']";
    String NOTIFICATION_MESSAGE_XPATH_TEMPLATE = "//md-toast[@che-info-text='%s']";
  }

  private By getImageFieldLocator(String machineName, String imageName) {
    final String imageFieldXpath = format(IMAGE_FIELD_XPATH_TEMPLATE, machineName, imageName);
    return By.xpath(imageFieldXpath);
  }

  private By getEditButtonLocator(String machineName) {
    final String editButtonXpath = format(EDIT_BUTTON_XPATH_TEMPLATE, machineName);
    return By.xpath(editButtonXpath);
  }

  private By getSettingsButtonLocator(String machineName) {
    final String settingsButtonXpath = format(SETTINGS_BUTTON_XPATH_TEMPLATE, machineName);
    return By.xpath(settingsButtonXpath);
  }

  private By getDeleteButtonLocator(String machineName) {
    final String deleteButtonXpath = format(DELETE_BUTTON_XPATH_TEMPLATE, machineName);
    return By.xpath(deleteButtonXpath);
  }

  private By getMachineListItemLocator(String machineName) {
    final String machineListItemXpath = format(MACHINE_LIST_ITEM_XPATH_TEMPLATE, machineName);
    return By.xpath(machineListItemXpath);
  }

  private By getCheckboxLocator(String machineName) {
    final String checkboxXpath = format(CHECKBOX_XPATH_TEMPLATE, machineName);
    return By.xpath(checkboxXpath);
  }

  private By getDecrementRamButtonLocator(String machineName) {
    final String decrementButtonXpath = format(DECREMENT_RAM_BUTTON_XPATH_TEMPLATE, machineName);
    return By.xpath(decrementButtonXpath);
  }

  private By getIncrementRamButtonLocator(String machineName) {
    final String incrementButtonXpath = format(INCREMENT_RAM_BUTTON_XPATH_TEMPLATE, machineName);
    return By.xpath(incrementButtonXpath);
  }

  private By getRamAmountTextFieldLocator(String machineName) {
    final String ramAmountTextFieldXpath =
        format(RAM_AMOUNT_TEXT_FIELD_XPATH_TEMPLATE, machineName);
    return By.xpath(ramAmountTextFieldXpath);
  }

  private By getRamAmountTextFieldContainerLocator(String machineName) {
    final String ramAmountTextFieldXpath =
        format(RAM_AMOUNT_TEXT_FIELD_XPATH_TEMPLATE + "//parent::div", machineName);
    return By.xpath(ramAmountTextFieldXpath);
  }

  public void waitNotificationMessage(String expectedMessage) {
    final String notificationMessageXpath =
        format(NOTIFICATION_MESSAGE_XPATH_TEMPLATE, expectedMessage);
    seleniumWebDriverHelper.waitVisibility(By.xpath(notificationMessageXpath));
  }

  public void waitMachineListItem(String machineName) {
    seleniumWebDriverHelper.waitVisibility(getMachineListItemLocator(machineName));
  }

  public void waitMachineListItemWithAttributes(
      String machineName, String imageName, String expectedRamValue) {
    waitMachineListItem(machineName);
    waitEditButton(machineName);
    waitSettindsButton(machineName);
    waitDeleteButton(machineName);
    waitImageNameInMachineListItem(machineName, imageName);
    waitIncrementRamButton(machineName);
    waitDecrementRamButton(machineName);
    waitRamAmount(machineName, expectedRamValue);
  }

  public void waitImageNameInMachineListItem(String machineName, String imageName) {
    seleniumWebDriverHelper.waitVisibility(getImageFieldLocator(machineName, imageName));
  }

  public void clickOnInstallersLink() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(INSTALLERS_LINK_XPATH));
  }

  public void clickOnServersLink() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(SERVERS_LINK_XPATH));
  }

  public void clickOnEnvironmentVariablesLink() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(ENVIRONMENT_VARIABLES_LINK_XPATH));
  }

  public void waitSettingsPopover() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(SETTINGS_CONTAINER_XPATH));
  }

  public boolean isSettingsPopoverOpened() {
    return seleniumWebDriverHelper.isVisible(By.xpath(SETTINGS_CONTAINER_XPATH));
  }

  public void waitSettingsPopoverInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(SETTINGS_CONTAINER_XPATH));
  }

  public boolean isRamValueValid(String machineName) {
    final String ramValidationAttribute = "aria-invalid";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(
            getRamAmountTextFieldLocator(machineName), ramValidationAttribute)
        .equals("false");
  }

  public void waitValidRamHighlighting(String machineName) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isRamValueValid(machineName));
  }

  public void waitInvalidRamHighlighting(String machineName) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isRamValueValid(machineName));
  }

  public String getRamAmount(String machineName) {
    String ramAmount =
        seleniumWebDriverHelper.waitVisibilityAndGetValue(
            getRamAmountTextFieldLocator(machineName));
    return ramAmount;
  }

  public void waitRamAmount(String machineName, String ramAmount) {
    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            seleniumWebDriverHelper.waitSuccessCondition(
                driver -> getRamAmount(machineName).equals(ramAmount)),
        ELEMENT_TIMEOUT_SEC,
        StaleElementReferenceException.class);
  }

  public void typeRamAmount(String machineName, String ramAmount) {
    final String emptyValue = "";

    seleniumWebDriverHelper.setValue(getRamAmountTextFieldLocator(machineName), emptyValue);
    if (ramAmount.isEmpty()) {
      return;
    }

    for (int i = 0; i < ramAmount.length(); i++) {
      seleniumWebDriverHelper.waitAndSendKeysTo(
          getRamAmountTextFieldLocator(machineName), valueOf(ramAmount.charAt(i)));
    }

    seleniumWebDriverHelper.waitValueEqualsTo(getRamAmountTextFieldLocator(machineName), ramAmount);
  }

  public WebElement waitDecrementRamButton(String machineName) {
    return seleniumWebDriverHelper.waitVisibility(getDecrementRamButtonLocator(machineName));
  }

  public WebElement waitIncrementRamButton(String machineName) {
    return seleniumWebDriverHelper.waitVisibility(getIncrementRamButtonLocator(machineName));
  }

  public void clickOnDecrementRamButton(String machineName) {
    waitDecrementRamButton(machineName).click();
    WaitUtils.sleepQuietly(2);
  }

  public void clickOnIncrementRamButton(String machineName) {
    waitIncrementRamButton(machineName).click();
    WaitUtils.sleepQuietly(2);
  }

  public boolean isCheckboxEnabled(String machineName) {
    final String checkboxStateAttribute = "aria-checked";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(getCheckboxLocator(machineName), checkboxStateAttribute)
        .equals("true");
  }

  public void waitCheckboxEnabled(String machineName) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isCheckboxEnabled(machineName));
  }

  public void waitCheckboxDisabled(String machineName) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isCheckboxEnabled(machineName));
  }

  public WebElement waitEditButton(String machineName) {
    return seleniumWebDriverHelper.waitVisibility(getEditButtonLocator(machineName));
  }

  public WebElement waitSettindsButton(String machineName) {
    return seleniumWebDriverHelper.waitVisibility(getSettingsButtonLocator(machineName));
  }

  public WebElement waitDeleteButton(String machineName) {
    return seleniumWebDriverHelper.waitVisibility(getDeleteButtonLocator(machineName));
  }

  public void clickOnEditButton(String machineName) {
    waitEditButton(machineName).click();
  }

  public void clickOnSettingsButton(String machineName) {
    waitSettindsButton(machineName).click();
  }

  public void clickOnDeleteButton(String machineName) {
    waitDeleteButton(machineName).click();
  }
}
