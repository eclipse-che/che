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

import static java.util.Collections.singletonList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Confirmation dialog used in User Dashboard.
 *
 * @author Ann Shumilova
 */
@Singleton
public class ConfirmDialog {

  private interface Locators {
    String DIALOG_XPATH = "//che-popup";
    String DIALOG_TITLE_XPATH = "//che-popup//div[contains(@class, 'che-popup-header')]/div";
    String DIALOG_CLOSE_BUTTON_XPATH = "//che-popup//div[contains(@class, 'che-popup-header')]//i";
    String DIALOG_MESSAGE_XPATH =
        "//che-popup//div[contains(@class, 'che-confirm-dialog-notification')]/div";
    String DIALOG_CANCEL_BUTTON = "//che-popup//che-button-notice//span";
    String DIALOG_CONFIRM_BUTTON = "//che-popup//che-button-primary//span";
  }

  @FindBy(xpath = Locators.DIALOG_XPATH)
  WebElement dialog;

  @FindBy(xpath = Locators.DIALOG_TITLE_XPATH)
  WebElement title;

  @FindBy(xpath = Locators.DIALOG_MESSAGE_XPATH)
  WebElement message;

  @FindBy(xpath = Locators.DIALOG_CLOSE_BUTTON_XPATH)
  WebElement closeButton;

  @FindBy(xpath = Locators.DIALOG_CANCEL_BUTTON)
  WebElement cancelButton;

  @FindBy(xpath = Locators.DIALOG_CONFIRM_BUTTON)
  WebElement confirmButton;

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait webDriverWait;

  @Inject
  public ConfirmDialog(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.webDriverWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Wait confirm dialog opened. */
  public void waitOpened() {
    webDriverWait.until(visibilityOf(dialog));
  }

  /** Close confirm dialog closed. */
  public void closeDialog() {
    webDriverWait.until(visibilityOf(closeButton)).click();

    waitClosed();
  }

  /** Wait confirm dialog closed. */
  public void waitClosed() {
    webDriverWait.until(invisibilityOfAllElements(singletonList(dialog)));
  }

  /**
   * Returns confirm dialog title.
   *
   * @return title of the confirm dialog
   */
  public String getTitle() {
    return webDriverWait.until(visibilityOf(title)).getText();
  }

  /**
   * Returns content message of the confirm dialog.
   *
   * @return content message of the confirm dialog
   */
  public String getMessage() {
    return webDriverWait.until(visibilityOf(message)).getText();
  }

  /**
   * Returns confirm button title.
   *
   * @return confirm button's title
   */
  public String getConfirmButtonTitle() {
    return webDriverWait.until(visibilityOf(confirmButton)).getText();
  }

  /**
   * Return cancel button title.
   *
   * @return canel button's title
   */
  public String getCancelButtonTitle() {
    return webDriverWait.until(visibilityOf(cancelButton)).getText();
  }

  /** Performs click on Cancel button. */
  public void clickCancel() {
    webDriverWait.until(visibilityOf(cancelButton)).click();
  }

  /** Performs click on Confirm button. */
  public void clickConfirm() {
    webDriverWait.until(visibilityOf(confirmButton)).click();
  }
}
