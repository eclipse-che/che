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
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class TheiaIde {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  TheiaIde(SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String THEIA_IDE_ID = "theia-app-shell";
    String THEIA_IDE_TOP_PANEL_ID = "theia-top-panel";
    String LOADER_XPATH = "//div[@class='theia-preload theia-hidden']";
    String MAIN_MENU_ITEM_XPATH = "//div[@id='theia:menubar']//li/div[text()='%s']";
    String SUBMENU_ITEM_XPATH = "//li[@class='p-Menu-item']/div[text()='%s']";

    String DIALOG_ABOUT_XPATH = "//div[@class='dialogBlock']";
    String DIALOG_ABOUT_TITLE_XPATH = DIALOG_ABOUT_XPATH + "//div[@class='dialogTitle']";
    String DIALOG_ABOUT_CONTENT_XPATH = DIALOG_ABOUT_XPATH + "//div[@class='dialogContent']";
    String CLOSE_DIALOG_ABOUT_BUTTON_XPATH = DIALOG_ABOUT_XPATH + "//button";
  }

  @FindBy(id = Locators.THEIA_IDE_ID)
  WebElement theiaIde;

  @FindBy(id = Locators.THEIA_IDE_TOP_PANEL_ID)
  WebElement theiaIdeTopPanel;

  @FindBy(xpath = Locators.LOADER_XPATH)
  WebElement loader;

  @FindBy(xpath = Locators.DIALOG_ABOUT_XPATH)
  WebElement dialogAbout;

  @FindBy(xpath = Locators.DIALOG_ABOUT_TITLE_XPATH)
  WebElement dialogAboutTitle;

  @FindBy(xpath = Locators.DIALOG_ABOUT_CONTENT_XPATH)
  WebElement dialogAboutContent;

  @FindBy(xpath = Locators.CLOSE_DIALOG_ABOUT_BUTTON_XPATH)
  WebElement dialogAboutOkButton;

  public void waitTheiaIde() {
    seleniumWebDriverHelper.waitVisibility(theiaIde, PREPARING_WS_TIMEOUT_SEC);
  }

  public void waitTheiaIdeTopPanel() {
    seleniumWebDriverHelper.waitVisibility(theiaIdeTopPanel);
  }

  public void waitLoaderInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(loader);
  }

  public void clickOnItemInMainMenu(String itemName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.MAIN_MENU_ITEM_XPATH, itemName)));
  }

  public void clickOnSubmenuItem(String itemName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.SUBMENU_ITEM_XPATH, itemName)));
  }

  public void waitDialogAboutForm() {
    seleniumWebDriverHelper.waitVisibility(dialogAbout, PREPARING_WS_TIMEOUT_SEC);
  }

  public void waitDialogAboutFormClosed() {
    seleniumWebDriverHelper.waitInvisibility(dialogAbout);
  }

  public void closeDialogAboutForm() {
    seleniumWebDriverHelper.waitAndClick(dialogAboutOkButton);
  }

  public String getDialogAboutTitle() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(dialogAboutTitle);
  }

  public void waitDialogAboutFormContains(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(dialogAboutContent, expectedText);
  }
}
