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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Vitaliy Gulyy */
@Singleton
public class AskForValueDialog {
  private static final String OK_BTN_ID = "askValue-dialog-ok";
  private static final String CANCEL_BTN_ID = "askValue-dialog-cancel";
  private static final String CANCEL_BTN_JAVA = "newJavaClass-dialog-cancel";
  private static final String ASK_FOR_VALUE_FORM = "gwt-debug-askValueDialog-window";
  private static final String INPUT = "gwt-debug-askValueDialog-textBox";
  private static final String NEW_JAVA_CLASS_FORM = "gwt-debug-newJavaSourceFileView-window";
  private static final String INPUT_NAME_JAVA_CLASS = "gwt-debug-newJavaSourceFileView-nameField";
  private static final String OK_BTN_NEW_JAVA_CLASS = "newJavaClass-dialog-ok";
  private static final String ERROR_LABEL =
      "//*[@id='gwt-debug-askValueDialog-window']//"
          + "div[@style='position: absolute; left: 0px; top: 0px; right: 0px; bottom: 0px;']";
  private static final String ERROR_LABEL_JAVA =
      "//input[@id='gwt-debug-newJavaSourceFileView-nameField']/following-sibling::div[text()='Name is invalid']";
  private static final String TYPE_ID = "gwt-debug-newJavaSourceFileView-typeField";
  private static final String INTERFACE_ITEM =
      "//*[@id='gwt-debug-newJavaSourceFileView-typeField' ]//option[@value='Interface']";
  private static final String ENUM_ITEM =
      "//*[@id='gwt-debug-newJavaSourceFileView-typeField' ]//option[@value='Enum']";

  private final ActionsFactory actionsFactory;

  public enum JavaFiles {
    CLASS,
    INTERFACE,
    ENUM
  }

  @FindBy(id = OK_BTN_ID)
  WebElement okBtn;

  @FindBy(id = CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(id = ASK_FOR_VALUE_FORM)
  WebElement form;

  @FindBy(id = INPUT)
  WebElement input;

  @FindBy(id = NEW_JAVA_CLASS_FORM)
  WebElement newJavaClass;

  @FindBy(id = INPUT_NAME_JAVA_CLASS)
  WebElement inputNameJavaClass;

  @FindBy(id = OK_BTN_NEW_JAVA_CLASS)
  WebElement okBtnnewJavaClass;

  @FindBy(xpath = ERROR_LABEL)
  WebElement errorLabel;

  @FindBy(id = TYPE_ID)
  WebElement type;

  @FindBy(xpath = INTERFACE_ITEM)
  WebElement interfaceItem;

  @FindBy(xpath = ENUM_ITEM)
  WebElement enumItem;

  @FindBy(id = CANCEL_BTN_JAVA)
  WebElement cancelButtonJava;

  @FindBy(xpath = ERROR_LABEL_JAVA)
  WebElement errorLabelJava;

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait loadPageWait;
  private final WebDriverWait redrawElementWait;
  private final Loader loader;

  @Inject
  public AskForValueDialog(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    this.redrawElementWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitFormToOpen() {
    redrawElementWait.until(visibilityOf(form));
  }

  public void waitFormToClose() {
    redrawElementWait.until(invisibilityOfElementLocated(By.id(ASK_FOR_VALUE_FORM)));
    loader.waitOnClosed();
  }

  /** click ok button */
  public void clickOkBtn() {
    loadPageWait.until(visibilityOf(okBtn)).click();
    waitFormToClose();
  }

  /** click cancel button */
  public void clickCancelBtn() {
    loadPageWait.until(visibilityOf(cancelBtn)).click();
    waitFormToClose();
  }

  public void clickCancelButtonJava() {
    redrawElementWait.until(visibilityOf(cancelButtonJava)).click();
  }

  public void typeAndWaitText(String text) {
    loadPageWait.until(visibilityOf(input)).sendKeys(text);
    waitInputNameContains(text);
  }

  public void waitNewJavaClassOpen() {
    redrawElementWait.until(visibilityOf(newJavaClass));
  }

  public void waitNewJavaClassClose() {
    redrawElementWait.until(invisibilityOfElementLocated(By.id(NEW_JAVA_CLASS_FORM)));
  }

  public void clickOkBtnNewJavaClass() {
    loadPageWait.until(visibilityOf(okBtnnewJavaClass)).click();
    loader.waitOnClosed();
  }

  public void typeTextInFieldName(String text) {
    loadPageWait.until(visibilityOf(inputNameJavaClass)).sendKeys(text);
    waitTextInFieldName(text);
  }

  public void waitTextInFieldName(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                loadPageWait
                    .until(visibilityOf(inputNameJavaClass))
                    .getAttribute("value")
                    .contains(expectedText));
  }

  public void deleteNameFromDialog() {
    loadPageWait.until(visibilityOf(input)).clear();
  }

  /** select interface item from dropdown */
  public void selectInterfaceItemFromDropDown() {
    loadPageWait.until(visibilityOf(type)).click();
    loadPageWait.until(visibilityOf(interfaceItem)).click();
  }

  /** select enum item from dropdown */
  public void selectEnumItemFromDropDown() {
    loadPageWait.until(visibilityOf(type)).click();
    loadPageWait.until(visibilityOf(enumItem)).click();
  }

  public void createJavaFileByNameAndType(String name, JavaFiles javaFileType) {
    waitNewJavaClassOpen();
    typeTextInFieldName(name);

    if (javaFileType == JavaFiles.INTERFACE) {
      selectInterfaceItemFromDropDown();
    }
    if (javaFileType == JavaFiles.ENUM) {
      selectEnumItemFromDropDown();
    }

    clickOkBtnNewJavaClass();
    waitNewJavaClassClose();
  }

  /** wait expected text */
  public void waitInputNameContains(final String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            webDriver ->
                seleniumWebDriver
                    .findElement(By.id(INPUT))
                    .getAttribute("value")
                    .contains(expectedText));
  }

  public void createNotJavaFileByName(String name) {
    typeAndWaitText(name);
    clickOkBtn();
  }

  /** clear the input box */
  public void clearInput() {
    waitFormToOpen();
    loadPageWait.until(visibilityOf(input)).clear();
  }

  /** Wait for error message */
  public boolean waitErrorMessage() {
    loadPageWait.until(visibilityOf(errorLabel));
    return "Name is invalid".equals(errorLabel.getText());
  }

  public void waitErrorMessageInJavaClass() {
    loadPageWait.until(visibilityOf(errorLabelJava));
  }

  public boolean isWidgetNewJavaClassIsOpened() {
    try {
      return newJavaClass.isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  /** launch the 'Rename project' form by keyboard */
  public void launchFindFormByKeyboard() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.SHIFT, Keys.F6).perform();
  }
}
