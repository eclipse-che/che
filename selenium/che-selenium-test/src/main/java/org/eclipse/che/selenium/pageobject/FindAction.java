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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class FindAction {

  private static final String ENTER_ACTION_FORM = "//div[contains(text(),'Enter action name:')]";
  private static final String TEXT_BOX_ACTION_FORM =
      "//div[text()='Enter action name:']/following::input[@class='gwt-TextBox']";
  private static final String INCLUDE_NON_MENU_ACTIONS = "//span[@class='gwt-CheckBox']";
  private static final String RESULT_TEXT_BOX = "//div[@class='gwt-PopupPanel']//div[3]//table";
  private static final String FOUND_ACTION_BY_NAME = "//td[contains(text(),'%s')]/parent::tr";

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private WebDriverWait waitWithRedrawTimeout;

  @Inject
  public FindAction(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    waitWithRedrawTimeout = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(xpath = ENTER_ACTION_FORM)
  WebElement enterActionForm;

  @FindBy(xpath = TEXT_BOX_ACTION_FORM)
  WebElement textBoxActionForm;

  @FindBy(xpath = RESULT_TEXT_BOX)
  WebElement resultTextBox;

  @FindBy(xpath = INCLUDE_NON_MENU_ACTIONS)
  WebElement includeNonMenuActions;

  /** wait the 'Enter Action' form is open */
  private void waitEnterActionFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(enterActionForm));
  }

  /** type text into 'Find Action' form */
  public void typeTextIntoFindActionForm(String text) {
    waitEnterActionFormIsOpen();
    clearTextBoxActionForm();
    for (char symbol : text.toCharArray()) {
      waitWithRedrawTimeout
          .until(ExpectedConditions.visibilityOf(textBoxActionForm))
          .sendKeys(Character.toString(symbol));
    }
    waitWithRedrawTimeout.until(
        ExpectedConditions.textToBePresentInElementValue(textBoxActionForm, text));
  }

  /** clear text into 'Enter action name:' input */
  public void clearTextBoxActionForm() {
    waitWithRedrawTimeout.until(ExpectedConditions.visibilityOf(textBoxActionForm)).clear();
  }

  /**
   * wait text in the 'Find Action' form
   *
   * @param expectedText expected text
   */
  public void waitTextInFormFindAction(String expectedText) {
    waitWithRedrawTimeout.until(
        ExpectedConditions.textToBePresentInElement(resultTextBox, expectedText));
  }

  /** click on the 'Include non menu actions' checkbox */
  public void clickOnIncludeNonMenuActions() {
    waitWithRedrawTimeout
        .until(ExpectedConditions.elementToBeClickable(includeNonMenuActions))
        .click();
  }

  /**
   * click on the found action
   *
   * @param action name of action
   */
  public void clickOnFoundAction(String action) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(
            waitWithRedrawTimeout.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath(String.format(FOUND_ACTION_BY_NAME, action)))))
        .perform();
  }

  /**
   * try to get attribute checked from checkbox
   *
   * @return selected state checkbox of widget
   */
  public boolean isCheckBoxSelected() {
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    Object chkBoxObject =
        js.executeScript("return arguments[0].getAttribute(\"checked\");", includeNonMenuActions);
    return (chkBoxObject == null) ? false : Boolean.parseBoolean(chkBoxObject.toString());
  }

  public void setCheckBoxInSelectedPosition() {
    waitEnterActionFormIsOpen();
    if (!isCheckBoxSelected()) clickOnIncludeNonMenuActions();
  }

  public void setCheckBoxInNotSelectedPosition() {
    waitEnterActionFormIsOpen();
    if (isCheckBoxSelected()) clickOnIncludeNonMenuActions();
  }
}
