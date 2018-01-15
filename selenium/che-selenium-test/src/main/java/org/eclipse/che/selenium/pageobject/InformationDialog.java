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

/**
 * Operations with information dialogs.
 *
 * @author Vitaliy Gulyy
 * @author Aleksandr Shmaraev
 */
@Singleton
public class InformationDialog {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public InformationDialog(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String INFORMATION_DIALOG_FORM = "//button[@id='info-window']/ancestor::div[3]";
    String OK_BUTTON_ID = "info-window";
    String MESSAGE_CONTAINER = "gwt-debug-info-window-message";
  }

  @FindBy(xpath = Locators.INFORMATION_DIALOG_FORM)
  private WebElement dialogForm;

  @FindBy(id = Locators.OK_BUTTON_ID)
  private WebElement okButton;

  @FindBy(id = Locators.MESSAGE_CONTAINER)
  private WebElement textContainer;

  /** Wait for information dialog is opened. */
  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(dialogForm));
  }

  /** Wait for information dialog is closed */
  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.INFORMATION_DIALOG_FORM)));
    loader.waitOnClosed();
  }

  /** Click Ok button at information dialog. */
  public void clickOkBtn() {
    okButton.click();
    waitFormToClose();
  }

  /**
   * dialog contains expected text
   *
   * @param expectedText expected text in widget
   */
  public void containsText(final String expectedText) {
    new WebDriverWait(seleniumWebDriver, 3).until(ExpectedConditions.visibilityOf(textContainer));
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            (ExpectedCondition<Boolean>) webDriver -> getTextFromWidget().contains(expectedText));
  }

  /**
   * Get the text from the information dialog
   *
   * @return {@link String} text
   */
  public String getTextFromWidget() {
    waitFormToOpen();
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(textContainer))
        .getText();
  }

  /**
   * check text on form, click on ok button and wait closing form
   *
   * @param expectedText
   */
  public void acceptInformDialogWithText(String expectedText) {
    waitFormToOpen();
    containsText(expectedText);
    clickOkBtn();
    waitFormToClose();
    loader.waitOnClosed();
  }
}
