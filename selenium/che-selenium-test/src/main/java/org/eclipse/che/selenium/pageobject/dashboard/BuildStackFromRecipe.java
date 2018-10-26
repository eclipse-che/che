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
package org.eclipse.che.selenium.pageobject.dashboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class BuildStackFromRecipe {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public BuildStackFromRecipe(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String SELECT_TAB_XPATH = "//md-tab-item/span[text()='%s']";
    String ACTIVE_TAB_XPATH = "//md-tab-content[contains(@class,'md-active')]";
    String EDITOR_XPATH = ACTIVE_TAB_XPATH + "//div[@class='CodeMirror-code']";
    String OK_BUTTON_XPATH = ACTIVE_TAB_XPATH + "//*[@che-button-title='Ok']";
    String CANCEL_BUTTON_XPATH = ACTIVE_TAB_XPATH + "//*[@che-button-title='Cancel']";
    String CLOSE_DIALOG_BUTTON_XPATH = "//md-dialog//i";
    String CREATE_STACK_DIALOG_FORM_XPATH =
        "//div[contains(@class, 'md-dialog-container ng-scope')]/md-dialog";
    String ERROR_MESSAGE_XPATH = "//div[@ng-message='customValidator']";
  }

  public void selectTabByName(String tabName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(Locators.SELECT_TAB_XPATH, tabName)));
  }

  public void enterRecipe(String recipe) {
    WebElement element = seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.EDITOR_XPATH));
    seleniumWebDriverHelper
        .getAction(seleniumWebDriver)
        .moveToElement(element)
        .click()
        .sendKeys(recipe)
        .perform();
  }

  public String getRecipeFromActiveTab() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(Locators.EDITOR_XPATH));
  }

  public void waitCreateStackDialogVisible() {
    // wait should be changed to "TestWebElementRenderChecker" after resolving issue
    // https://github.com/eclipse/che/issues/10087
    WaitUtils.sleepQuietly(2);

    seleniumWebDriverHelper.waitVisibility(By.xpath(Locators.CREATE_STACK_DIALOG_FORM_XPATH));
    seleniumWebDriverHelper.waitVisibility(By.xpath("//div[text()='Build stack from recipe']"));
  }

  public void closeCreateStackDialogByCloseButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.CLOSE_DIALOG_BUTTON_XPATH));
  }

  public void waitCreateStackDialogClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.CREATE_STACK_DIALOG_FORM_XPATH));
  }

  public void waitOkButtonEnabled() {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(Locators.OK_BUTTON_XPATH), "aria-disabled", "false");
  }

  public void clickOnOkButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.OK_BUTTON_XPATH));
  }

  public void clickOnCancelButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.CANCEL_BUTTON_XPATH));
  }

  public void checkRecipeIsCorrect() {
    waitNoErrorMessage();
    waitOkButtonEnabled();
  }

  public void waitNoErrorMessage() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.ERROR_MESSAGE_XPATH));
  }
}
