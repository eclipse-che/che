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
package org.eclipse.che.selenium.pageobject.dashboard.stacks;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StackDetails {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWait redrawUiElementsTimeout;
  private final Dashboard dashboard;

  @Inject
  public StackDetails(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      Dashboard dashboard) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.dashboard = dashboard;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String TOOLBAR = "//div[@che-title='%s']";
    String NEW_STACK_NAME = "deskname";
    String SAVE_CHANGES_BUTTON = "saveButton";
    String BACK_TO_STACKS_LIST_BUTTON_XPATH = "//a[@title='All stacks']";
  }

  @FindBy(name = Locators.NEW_STACK_NAME)
  WebElement newStackName;

  public void waitToolbar(String stackName) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(String.format(Locators.TOOLBAR, stackName)));
  }

  public void setStackName(String stackName) {
    WebElement stackNameField =
        seleniumWebDriverHelper.waitVisibility(By.name(Locators.NEW_STACK_NAME));
    stackNameField.click();
    stackNameField.clear();
    stackNameField.sendKeys(stackName);
    WaitUtils.sleepQuietly(1);
    // seleniumWebDriverHelper.setText(newStackName, stackName);
  }

  public void clickOnSaveChangesButton() {
    seleniumWebDriverHelper.waitAndClick(By.name(Locators.SAVE_CHANGES_BUTTON));
  }

  public void backToStacksList() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.BACK_TO_STACKS_LIST_BUTTON_XPATH));
  }
}
