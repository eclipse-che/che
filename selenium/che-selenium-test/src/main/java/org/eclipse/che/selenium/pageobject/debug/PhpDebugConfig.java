/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
public class PhpDebugConfig extends AbstractDebugConfig {

  @Inject
  public PhpDebugConfig(SeleniumWebDriver seleniumWebDriver) {
    super(seleniumWebDriver);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {

    String DEBUG_CATEGORY_EXPAND_ICON_XPATH =
        "//div[@id='gwt-debug-debugConfigurationTypesPanel']//span[text()='PHP']/following-sibling::span";

    String BREAK_AT_FIRST_LINE_LABEL_XPATH =
        "//div[@id='gwt-debug-zendDebugConfigurationPageView-mainPanel']//label[text()='Break at first line']";

    String BREAK_AT_FIRST_LINE_CHECKBOX_XPATH =
        "//div[@id='gwt-debug-zendDebugConfigurationPageView-mainPanel']//input[@type='checkbox' and following-sibling::label[text()='Break at first line']]";

    String DEBUG_PORT_INPUT_XPATH =
        "//div[@id='gwt-debug-zendDebugConfigurationPageView-mainPanel']//input[@type='text' and preceding-sibling::div[text()='Debug port']]";
  }

  @FindBy(xpath = Locators.DEBUG_CATEGORY_EXPAND_ICON_XPATH)
  WebElement debugCategoryExpandIcon;

  @FindBy(xpath = Locators.BREAK_AT_FIRST_LINE_LABEL_XPATH)
  WebElement breakAtFirstLineLabel;

  @FindBy(xpath = Locators.DEBUG_PORT_INPUT_XPATH)
  WebElement debugPortInput;

  /** Create debug configuration and close dialog. */
  public void createConfig(String configName) {
    createConfigWithoutClosingDialog(configName);
    close();
  }

  /** Create debug configuration and close dialog. */
  public void createConfig(String configName, boolean breakAtFirstLine, int debugPort) {
    createConfigWithoutClosingDialog(configName);

    // set debug port
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(debugPortInput));
    debugPortInput.clear();
    debugPortInput.sendKeys(String.valueOf(debugPort));

    // setup "break at first line" checkbox
    ensureBreakAtFirstLineCheckboxState(breakAtFirstLine);

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(configSaveBtn))
        .click();

    close();
  }

  /**
   * Check if state of "break at first line" checkbox equals to breakAtFirstLine parameter.
   *
   * @param breakAtFirstLine
   */
  private void ensureBreakAtFirstLineCheckboxState(boolean breakAtFirstLine) {
    boolean isSelected =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath(Locators.BREAK_AT_FIRST_LINE_CHECKBOX_XPATH)))
            .isSelected();

    if (isSelected != breakAtFirstLine) {
      breakAtFirstLineLabel.click();
    }
  }

  @Override
  void expandDebugCategory() {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(debugCategoryExpandIcon))
        .click();
  }
}
