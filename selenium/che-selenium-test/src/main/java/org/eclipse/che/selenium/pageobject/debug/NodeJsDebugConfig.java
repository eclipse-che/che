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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * @author Dmytro Nochevnov
 * @author Musienko Maxim
 */
@Singleton
public class NodeJsDebugConfig extends AbstractDebugConfig {

  private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public NodeJsDebugConfig(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    super(seleniumWebDriver, seleniumWebDriverHelper);
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String DEBUG_CATEGORY_EXPAND_ICON_XPATH =
        "//div[@id='gwt-debug-debugConfigurationTypesPanel']//span[text()='NodeJs']/following-sibling::span";

    String NODE_JS_SCRIPT_FIELD_CSS =
        "div#gwt-debug-nodeJsDebugConfigurationPageView-mainPanel>input";
  }

  @FindBy(xpath = Locators.DEBUG_CATEGORY_EXPAND_ICON_XPATH)
  WebElement debugCategoryExpandIcon;

  @FindBy(css = Locators.NODE_JS_SCRIPT_FIELD_CSS)
  WebElement scriptNodeJsField;

  /** Create debug configuration and close dialog. */
  public void createConfig(String configName) {
    createConfigWithoutClosingDialog(configName);
    close();
  }

  /**
   * Set path defined by user to js. file
   *
   * @param pathToNodeJsFile the defined path
   */
  public void setPathToScriptForNodeJs(String pathToNodeJsFile) {
    seleniumWebDriverHelper.waitVisibility(scriptNodeJsField).clear();
    scriptNodeJsField.sendKeys(pathToNodeJsFile);
  }

  @Override
  public void expandDebugCategory() {
    seleniumWebDriverHelper.waitAndClick(debugCategoryExpandIcon);
  }

  public void saveConfiguration() {
    clickOnSaveBtn();
  }

  public void saveConfigurationAndclose() {
    saveConfiguration();
    close();
  }

  public void clickOnDebugButtonAndWaitClosing() {
    clickOnDebugAndWaitClosing();
  }
}
