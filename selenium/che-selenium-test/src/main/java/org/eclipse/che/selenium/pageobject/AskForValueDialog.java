/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
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
  private final Loader loader;

  @Inject
  public AskForValueDialog(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(ASK_FOR_VALUE_FORM)));
    loader.waitOnClosed();
  }

  /** click ok button */
  public void clickOkBtn() {
    okBtn.click();
    waitFormToClose();
  }

  /** click cancel button */
  public void clickCancelBtn() {
    cancelBtn.click();
    waitFormToClose();
  }

  public void clickCancelButtonJava() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(cancelButtonJava))
        .click();
  }

  public void typeAndWaitText(String text) {
    input.sendKeys(text);
    waitInputNameContains(text);
  }

  public void waitNewJavaClassOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(newJavaClass));
  }

  public void waitNewJavaClassClose() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(NEW_JAVA_CLASS_FORM)));
  }

  public void clickOkBtnNewJavaClass() {
    okBtnnewJavaClass.click();
    loader.waitOnClosed();
  }

  public void typeTextInFieldName(String text) {
    inputNameJavaClass.sendKeys(text);
  }

  public void deleteNameFromDialog() {
    input.clear();
  }

  /** select interface item from dropdown */
  public void selectInterfaceItemFromDropDown() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(type));
    type.click();
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(By.xpath(INTERFACE_ITEM)));
    interfaceItem.click();
  }

  /** select enum item from dropdown */
  public void selectEnumItemFromDropDown() {
    new WebDriverWait(seleniumWebDriver, 7).until(ExpectedConditions.visibilityOf(type));
    type.click();
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(By.xpath(ENUM_ITEM)));
    enumItem.click();
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
    new WebDriverWait(seleniumWebDriver, 3)
        .until(
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
    input.clear();
  }

  /** Wait for error message */
  public boolean waitErrorMessage() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.visibilityOf(errorLabel));
    return "Name is invalid".equals(errorLabel.getText());
  }

  public void waitErrorMessageInJavaClass() {
    new WebDriverWait(seleniumWebDriver, 5).until(ExpectedConditions.visibilityOf(errorLabelJava));
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
