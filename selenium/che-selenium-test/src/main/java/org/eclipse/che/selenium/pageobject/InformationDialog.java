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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Operations with information dialogs.
 *
 * @author Vitaliy Gulyy
 * @author Aleksandr Shmaraev
 */
@Singleton
public class InformationDialog {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final Loader loader;

  @Inject
  public InformationDialog(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      Loader loader) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String INFORMATION_DIALOG_FORM = "//button[@id='info-window']/ancestor::table";
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
    seleniumWebDriverHelper.waitVisibility(dialogForm);
  }

  public boolean isFormOpened() {
    return seleniumWebDriverHelper.isVisible(dialogForm);
  }

  /**
   * Wait for information dialog is opened.
   *
   * @param expectedTimeout timeout defined by user
   */
  public void waitFormToOpen(int expectedTimeout) {
    seleniumWebDriverHelper.waitVisibility(dialogForm, expectedTimeout);
  }

  /** Wait for information dialog is closed */
  public void waitFormToClose() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.INFORMATION_DIALOG_FORM));
  }

  /** Click Ok button at information dialog. */
  public void clickOkBtn() {
    seleniumWebDriverHelper.waitAndClick(okButton);
    waitFormToClose();
  }

  /**
   * dialog contains expected text
   *
   * @param expectedText expected text in widget
   */
  public void containsText(final String expectedText) {
    seleniumWebDriverHelper.waitTextContains(textContainer, expectedText);
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
