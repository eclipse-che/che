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
package org.eclipse.che.selenium.pageobject.debug;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.debug.AbstractDebugConfig.Locators.DEBUG_CONFIGURATION_WIDGET_ID;

import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * @author Dmytro Nochevnov
 * @author Musienko Maxim
 */
@Singleton
public abstract class AbstractDebugConfig {
  private SeleniumWebDriverHelper seleniumWebDriverHelper;

  protected final SeleniumWebDriver seleniumWebDriver;

  public AbstractDebugConfig(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String DEBUG_CONFIGURATION_WIDGET_ID = "editDebugConfigurationsView";

    String NAME_OF_DEBUG_CONFIG_FIELD_XPATH =
        "//div[@id='editDebugConfigurationsView']//div[text()='Name']/following-sibling::input";

    String DEBUG_CONFIG_SAVE_BTN_XPATH =
        "//button[@id='window-edit-debug-configurations-save' and not(@disabled)]";
    String DEBUG_CONFIG_CLOSE_BTN_XPATH = "window-edit-debug-configurations-close";

    String CONFIG_ITEM_XPATH_TEMPLATE =
        "//div[@id='editDebugConfigurationsView']//div[contains(@id, 'gwt-debug-debug-configuration') and text()='%s']";
    String REMOVE_CONFIG_BUTTON_XPATH_TEMPLATE = CONFIG_ITEM_XPATH_TEMPLATE + "/span/span[1]";

    String DELETE_CONFIGURATION_DIALOG_TITLE_XPATH = "//div[text()='Delete Configuration']";

    String DEBUG_BUTTON_ID = "window-edit-debug-configurations-debug";
  }

  @FindBy(id = DEBUG_CONFIGURATION_WIDGET_ID)
  WebElement debugConfigurationsView;

  @FindBy(xpath = Locators.NAME_OF_DEBUG_CONFIG_FIELD_XPATH)
  WebElement nameFieldOfDebugConf;

  @FindBy(id = Locators.DEBUG_CONFIG_CLOSE_BTN_XPATH)
  WebElement configCloseBtn;

  @FindBy(id = Locators.DEBUG_BUTTON_ID)
  WebElement debugButton;

  @FindBy(xpath = Locators.DEBUG_CONFIG_SAVE_BTN_XPATH)
  WebElement configSaveBtn;

  @FindBy(xpath = Locators.DELETE_CONFIGURATION_DIALOG_TITLE_XPATH)
  WebElement deleteConfigurationDialogTitle;

  /** Wait opening of the Debug configuration widget */
  public void waitDebugConfigurationIsOpened() {
    seleniumWebDriverHelper.waitVisibility(debugConfigurationsView, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Wait closing of the Debug configuration widget */
  public void waitDebugConfigurationIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(
        By.id(DEBUG_CONFIGURATION_WIDGET_ID), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * Remove debug config with certain name and then close Debug Configuration form.
   *
   * @param configName name of configuration to remove.
   */
  public void removeConfig(String configName) {
    String configItemXpath = String.format(Locators.CONFIG_ITEM_XPATH_TEMPLATE, configName);
    seleniumWebDriverHelper.waitAndClick(By.xpath(configItemXpath));
    String removeConfigButtonXpath =
        String.format(Locators.REMOVE_CONFIG_BUTTON_XPATH_TEMPLATE, configName);
    seleniumWebDriverHelper.waitAndClick(By.xpath(removeConfigButtonXpath));
    seleniumWebDriverHelper.waitVisibility(deleteConfigurationDialogTitle);
    seleniumWebDriverHelper.waitAndClick(By.id(AskDialog.OK_BTN_ID));
    seleniumWebDriverHelper.waitInvisibility(By.id(AskDialog.OK_BTN_ID));
    seleniumWebDriverHelper.waitInvisibility(By.xpath(configItemXpath));
    close();
  }

  /**
   * Return xpath to submenu of "Run > Debug" command
   *
   * @param projectName
   * @return
   */
  public String getXpathToІRunDebugCommand(String projectName) {
    return String.format(
        "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
        TestMenuCommandsConstants.Run.DEBUG, projectName);
  }

  /**
   * Create debug configuration with default parameters and certain name.
   *
   * @param configName name of configuration
   */
  void createConfigWithoutClosingDialog(String configName) {
    waitDebugConfigurationIsOpened();
    expandDebugCategory();
    seleniumWebDriverHelper.waitVisibility(nameFieldOfDebugConf);
    nameFieldOfDebugConf.clear();
    nameFieldOfDebugConf.sendKeys(configName);
    clickOnSaveBtn();
  }

  /** Expand the debugger type icon '+' */
  public abstract void expandDebugCategory();

  protected void clickOnSaveBtn() {
    seleniumWebDriverHelper.waitAndClick(configSaveBtn);
  }

  protected void close() {
    seleniumWebDriverHelper.waitAndClick(configCloseBtn);
    waitDebugConfigurationIsClosed();
  }

  protected void clickOnDebugAndWaitClosing() {
    seleniumWebDriverHelper.waitAndClick(debugButton);
    waitDebugConfigurationIsClosed();
  }
}
