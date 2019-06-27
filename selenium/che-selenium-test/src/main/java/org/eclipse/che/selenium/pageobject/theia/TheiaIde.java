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
package org.eclipse.che.selenium.pageobject.theia;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.theia.TheiaIde.Locators.NOTIFICATION_CLOSE_BUTTON;
import static org.eclipse.che.selenium.pageobject.theia.TheiaIde.Locators.NOTIFICATION_MESSAGE_CONTAINS_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaIde.Locators.NOTIFICATION_MESSAGE_EQUALS_TO_XPATH_TEMPLATE;
import static org.openqa.selenium.Keys.chord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class TheiaIde {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  TheiaIde(SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String THEIA_CHE_DASHBOARD_XPATH = "//div[@class='che-dashboard']";
    String THEIA_IDE_ID = "theia-app-shell";
    String THEIA_IDE_TOP_PANEL_ID = "theia-top-panel";
    String LOADER_XPATH = "//div[@class='theia-preload theia-hidden']";
    String MAIN_MENU_ITEM_XPATH_PATTERN = "//div[@id='theia:menubar']//li/div[text()='%s']";
    String SUBMENU_ITEM_XPATH_PATTERN = "//li[@class='p-Menu-item']/div[text()='%s']";
    String ABOUT_DIALOG_XPATH = "//div[@class='dialogBlock']";
    String ABOUT_DIALOG_TITLE_XPATH = ABOUT_DIALOG_XPATH + "//div[@class='dialogTitle']";
    String ABOUT_DIALOG_CONTENT_XPATH = ABOUT_DIALOG_XPATH + "//div[@class='dialogContent']";
    String ABOUT_DIALOG_OK_BUTTON_XPATH = ABOUT_DIALOG_XPATH + "//button";
    String NOTIFICATION_MESSAGE_EQUALS_TO_XPATH_TEMPLATE =
        "//div[@class='theia-NotificationsContainer']//p[text()='%s']";
    String NOTIFICATION_MESSAGE_CONTAINS_XPATH_TEMPLATE =
        "//div[@class='theia-NotificationsContainer']//p[contains(text(), '%s')]";
    String NOTIFICATION_CLOSE_BUTTON =
        "//div[@class='theia-NotificationsContainer']//button[text()='Close']";
  }

  @FindBy(xpath = Locators.THEIA_CHE_DASHBOARD_XPATH)
  WebElement theiaCheDashboard;

  @FindBy(id = Locators.THEIA_IDE_ID)
  WebElement theiaIde;

  @FindBy(id = Locators.THEIA_IDE_TOP_PANEL_ID)
  WebElement theiaIdeTopPanel;

  @FindBy(xpath = Locators.LOADER_XPATH)
  WebElement loader;

  @FindBy(xpath = Locators.ABOUT_DIALOG_XPATH)
  WebElement aboutDialog;

  @FindBy(xpath = Locators.ABOUT_DIALOG_TITLE_XPATH)
  WebElement aboutDialogTitle;

  @FindBy(xpath = Locators.ABOUT_DIALOG_CONTENT_XPATH)
  WebElement aboutDialogContent;

  @FindBy(xpath = Locators.ABOUT_DIALOG_OK_BUTTON_XPATH)
  WebElement aboutDialogOkButton;

  public void openNavbarMenu() {
    seleniumWebDriverHelper.waitAndClick(theiaCheDashboard);
  }

  private String getNotificationEqualsToXpath(String messageText) {
    return format(NOTIFICATION_MESSAGE_EQUALS_TO_XPATH_TEMPLATE, messageText);
  }

  private String getNotificationContainsXpath(String messageText) {
    return format(NOTIFICATION_MESSAGE_CONTAINS_XPATH_TEMPLATE, messageText);
  }

  public void clickOnNotificationCloseButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(NOTIFICATION_CLOSE_BUTTON));
  }

  public void waitNotificationEqualsTo(String expectedText) {
    final String notificationXpath = getNotificationEqualsToXpath(expectedText);

    seleniumWebDriverHelper.waitVisibility(By.xpath(notificationXpath), ELEMENT_TIMEOUT_SEC);
  }

  public boolean isNotificationEqualsTo(String notificationText) {
    final String notificationXpath = getNotificationEqualsToXpath(notificationText);

    return seleniumWebDriverHelper.isVisible(By.xpath(notificationXpath));
  }

  public boolean isNotificationContains(String notificationText) {
    final String notificationXpath = getNotificationContainsXpath(notificationText);

    return seleniumWebDriverHelper.isVisible(By.xpath(notificationXpath));
  }

  public void waitNotificationMessageContains(String expectedText) {
    waitNotificationMessageContains(expectedText, LOAD_PAGE_TIMEOUT_SEC);
  }

  public void waitNotificationMessageContains(String expectedText, int timeout) {
    final String notificationMessage = getNotificationContainsXpath(expectedText);

    seleniumWebDriverHelper.waitVisibility(By.xpath(notificationMessage), timeout);
  }

  public void waitNotificationDisappearance(String notificationText) {
    waitNotificationDisappearance(notificationText, LOAD_PAGE_TIMEOUT_SEC);
  }

  public void waitNotificationDisappearance(String notificationText, int timeout) {
    final String notificationMessage = getNotificationContainsXpath(notificationText);

    seleniumWebDriverHelper.waitInvisibility(By.xpath(notificationMessage), timeout);
  }

  public void waitNotificationPanelClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.className("theia-Notification"));
  }

  public void waitTheiaIde() {
    seleniumWebDriverHelper.waitVisibility(theiaIde, PREPARING_WS_TIMEOUT_SEC);
  }

  public void waitTheiaIdeTopPanel() {
    seleniumWebDriverHelper.waitVisibility(theiaIdeTopPanel);
  }

  public void waitLoaderInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(loader);
  }

  public void clickOnMenuItemInMainMenu(String itemName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.MAIN_MENU_ITEM_XPATH_PATTERN, itemName)));
  }

  public void clickOnSubmenuItem(String itemName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.SUBMENU_ITEM_XPATH_PATTERN, itemName)));
  }

  /**
   * Run command from sub menu.
   *
   * @param topMenuCommand
   * @param commandName
   */
  public void runMenuCommand(String topMenuCommand, String commandName) {
    clickOnMenuItemInMainMenu(topMenuCommand);
    clickOnSubmenuItem(commandName);
  }

  public void waitAboutDialogIsOpen() {
    seleniumWebDriverHelper.waitVisibility(aboutDialog, PREPARING_WS_TIMEOUT_SEC);
    seleniumWebDriverHelper.waitTextContains(aboutDialogTitle, "Theia");
  }

  public void waitAboutDialogIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(aboutDialog);
  }

  public void closeAboutDialog() {
    seleniumWebDriverHelper.waitAndClick(aboutDialogOkButton);
  }

  public void waitAboutDialogContains(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(aboutDialogContent, expectedText);
  }

  public void switchToIdeFrame() {
    final String ideWindowXpath = "//ide-iframe[@id='ide-iframe-window' and @aria-hidden='false']";
    final String ideFrameId = "ide-application-iframe";

    // wait until IDE window is opened
    seleniumWebDriverHelper.waitVisibility(By.xpath(ideWindowXpath), PREPARING_WS_TIMEOUT_SEC);

    // switch to IDE frame
    seleniumWebDriverHelper.waitAndSwitchToFrame(By.id(ideFrameId), PREPARING_WS_TIMEOUT_SEC);
  }

  public void pressKeyCombination(CharSequence... combination) {
    final String keyCombination = chord(asList(combination));

    // to ensure that events are ended before pressing keys combination
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.sendKeys(keyCombination);
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
