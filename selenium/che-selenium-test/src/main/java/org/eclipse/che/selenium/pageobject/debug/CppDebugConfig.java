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
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
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
public class CppDebugConfig extends AbstractDebugConfig {

  @Inject
  public CppDebugConfig(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    super(seleniumWebDriver, seleniumWebDriverHelper);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {

    String DEBUG_CATEGORY_EXPAND_ICON_XPATH =
        "//div[@id='gwt-debug-debugConfigurationTypesPanel']//span[text()='GDB']/following-sibling::span";

    String DEBUG_PORT_INPUT_XPATH =
        "//div[@id='gwt-debug-gdbDebugConfigurationPageView-mainPanel']//input[@type='text' and preceding-sibling::div[text()='Port']]";
  }

  @FindBy(xpath = Locators.DEBUG_CATEGORY_EXPAND_ICON_XPATH)
  WebElement debugCategoryExpandIcon;

  @FindBy(xpath = Locators.DEBUG_PORT_INPUT_XPATH)
  WebElement debugPortInput;

  /** Create debug configuration and close dialog. */
  public void createConfig(String configName) {
    createConfigWithoutClosingDialog(configName);
    close();
  }

  /** Create debug configuration and close dialog. */
  public void createConfig(String configName, int debugPort) {
    createConfigWithoutClosingDialog(configName);

    // set debug port
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(debugPortInput));
    debugPortInput.clear();
    debugPortInput.sendKeys(String.valueOf(debugPort));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(configSaveBtn))
        .click();

    close();
  }

  @Override
  public void expandDebugCategory() {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(debugCategoryExpandIcon))
        .click();
  }
}
