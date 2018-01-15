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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class AskDialog {

  public static final String OK_BTN_ID = "ask-dialog-ok";
  public static final String CANCEL_BTN_ID = "ask-dialog-cancel";
  public static final String ASK_DIALOG_FORM_XPATH =
      "//button[@id='ask-dialog-ok']/ancestor::div[3]";
  public static final String WARNING_TEXT_XPATH =
      "//button[@id='ask-dialog-ok']/ancestor::div[3]//span";

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public AskDialog(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = OK_BTN_ID)
  WebElement okBtn;

  @FindBy(id = CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(xpath = ASK_DIALOG_FORM_XPATH)
  WebElement form;

  @FindBy(xpath = WARNING_TEXT_XPATH)
  WebElement warning_Text;

  public void clickOkBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(okBtn))
        .click();
  }

  public void clickCancelBtn() {
    waitFormToOpen();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(cancelBtn))
        .click();
  }

  /** wait opening the confirmation form */
  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(form));
  }

  /**
   * wait opening the confirmation form with timeout defined by user
   *
   * @param timeOut user timeout
   */
  public void waitFormToOpen(int timeOut) {
    new WebDriverWait(seleniumWebDriver, timeOut).until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(ASK_DIALOG_FORM_XPATH)));
    loader.waitOnClosed();
  }

  /**
   * dialog contains expected text
   *
   * @param expectedText expected text in widget
   */
  public void containsText(final String expectedText) {
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.visibilityOf(warning_Text));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> warning_Text.getText().contains(expectedText));
  }

  /**
   * wait dialog contains expected text click on ok button and wait closing form
   *
   * @param expectedText expected text in widget
   */
  public void acceptDialogWithText(String expectedText) {
    waitFormToOpen();
    containsText(expectedText);
    clickOkBtn();
    waitFormToClose();
    loader.waitOnClosed();
  }

  /**
   * wait dialog contains expected text click on ok button and wait closing form
   *
   * @param expectedText expected text in widget
   * @param definedTimeout timeout in seconds defined with user
   */
  public void acceptDialogWithText(String expectedText, int definedTimeout) {
    waitFormToOpen(definedTimeout);
    containsText(expectedText);
    clickOkBtn();
    waitFormToClose();
    loader.waitOnClosed();
  }

  /** click on ok button and wait closing form (text on form is not checked) */
  public void confirmAndWaitClosed() {
    waitFormToOpen();
    clickOkBtn();
    waitFormToClose();
    loader.waitOnClosed();
  }

  /**
   * wait dialog contains expected text click on cancel button and wait closing form
   *
   * @param expectedText expected text in widget
   */
  public void refuseDialogWithText(String expectedText) {
    waitFormToOpen();
    containsText(expectedText);
    clickCancelBtn();
    waitFormToClose();
    loader.waitOnClosed();
  }
}
