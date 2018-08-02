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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class ChoiceDialog {

  private static final String ASK_DIALOG_FORM_XPATH =
      "//div[@id='gwt-debug-dialog-choice']/ancestor::table";
  private static final String WARNING_TEXT_XPATH = "//div[@id='gwt-debug-dialog-choice']//span";
  private static final String FIRST_BUTTON_ID = "ask-dialog-first";
  private static final String SECOND_BUTTON_ID = "ask-dialog-second";
  private static final String THIRD_BUTTON_ID = "ask-dialog-third";

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public ChoiceDialog(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(xpath = ASK_DIALOG_FORM_XPATH)
  WebElement form;

  @FindBy(id = FIRST_BUTTON_ID)
  WebElement firstButton;

  @FindBy(id = SECOND_BUTTON_ID)
  WebElement secondButton;

  @FindBy(id = THIRD_BUTTON_ID)
  WebElement thirdButton;

  @FindBy(xpath = WARNING_TEXT_XPATH)
  WebElement warning_Text;

  /** Wait until the dialog opens. */
  public void waitFormToOpen() {
    seleniumWebDriverHelper.waitVisibility(form);
  }

  /**
   * Wait until the dialog opens with timeout defined by user.
   *
   * @param timeOut user timeout
   */
  public void waitFormToOpen(int timeOut) {
    new WebDriverWait(seleniumWebDriver, timeOut).until(ExpectedConditions.visibilityOf(form));
  }

  /** Wait until the dialog closes. */
  public void waitFormToClose() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(ASK_DIALOG_FORM_XPATH));
    loader.waitOnClosed();
  }

  /** Check if the dialog contains expected text. */
  public void containsText(final String expectedText) {
    seleniumWebDriverHelper.waitTextContains(
        warning_Text, expectedText, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Click the first button. */
  public void clickFirstButton() {
    seleniumWebDriverHelper.waitAndClick(firstButton);
  }

  /** Click the second button. */
  public void clickSecondButton() {
    seleniumWebDriverHelper.waitAndClick(secondButton);
  }

  /** Click the third button. */
  public void clickThirdButton() {
    seleniumWebDriverHelper.waitAndClick(thirdButton);
  }
}
