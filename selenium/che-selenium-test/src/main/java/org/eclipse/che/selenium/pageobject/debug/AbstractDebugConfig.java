/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.debug;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Dmytro Nochevnov
 * @author Musienko Maxim
 */
@Singleton
public abstract class AbstractDebugConfig {

  protected final SeleniumWebDriver seleniumWebDriver;

  public AbstractDebugConfig(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
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
  }

  @FindBy(id = Locators.DEBUG_CONFIGURATION_WIDGET_ID)
  WebElement debugConfigurationsView;

  @FindBy(xpath = Locators.NAME_OF_DEBUG_CONFIG_FIELD_XPATH)
  WebElement nameFieldOfDebugConf;

  @FindBy(id = Locators.DEBUG_CONFIG_CLOSE_BTN_XPATH)
  WebElement configCloseBtn;

  @FindBy(xpath = Locators.DEBUG_CONFIG_SAVE_BTN_XPATH)
  WebElement configSaveBtn;

  @FindBy(xpath = Locators.DELETE_CONFIGURATION_DIALOG_TITLE_XPATH)
  WebElement deleteConfigurationDialogTitle;

  /** Wait opening of the Debug configuration widget */
  public void waitDebugConfigurationIsOpened() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(debugConfigurationsView));
  }

  /** Wait closing of the Debug configuration widget */
  public void waitDebugConfigurationIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.id(Locators.DEBUG_CONFIGURATION_WIDGET_ID)));
  }

  /**
   * Remove debug config with certain name and then close Debug Configuration form.
   *
   * @param configName name of configuration to remove.
   */
  public void removeConfig(String configName) {
    String configItemXpath = String.format(Locators.CONFIG_ITEM_XPATH_TEMPLATE, configName);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(configItemXpath)))
        .click();

    String removeConfigButtonXpath =
        String.format(Locators.REMOVE_CONFIG_BUTTON_XPATH_TEMPLATE, configName);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(removeConfigButtonXpath)))
        .click();

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(deleteConfigurationDialogTitle));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(AskDialog.OK_BTN_ID)))
        .click();

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(AskDialog.OK_BTN_ID)));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(configItemXpath)));

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
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nameFieldOfDebugConf));
    nameFieldOfDebugConf.clear();
    nameFieldOfDebugConf.sendKeys(configName);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(configSaveBtn))
        .click();
  }

  /** Expand the debugger type icon '+' */
  abstract void expandDebugCategory();

  void close() {
    configCloseBtn.click();
    waitDebugConfigurationIsClosed();
  }
}
