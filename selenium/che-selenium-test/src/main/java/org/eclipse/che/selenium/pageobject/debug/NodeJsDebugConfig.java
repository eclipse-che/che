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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
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
public class NodeJsDebugConfig extends AbstractDebugConfig {

  @Inject
  public NodeJsDebugConfig(SeleniumWebDriver seleniumWebDriver) {
    super(seleniumWebDriver);
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
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(scriptNodeJsField))
        .clear();
    scriptNodeJsField.sendKeys(pathToNodeJsFile);
  }

  @Override
  void expandDebugCategory() {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(debugCategoryExpandIcon))
        .click();
  }
}
