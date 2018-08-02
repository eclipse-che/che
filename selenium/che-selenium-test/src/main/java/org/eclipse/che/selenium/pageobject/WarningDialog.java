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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WarningDialog {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WarningDialog(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String WARNING_DIALOG_ID = "gwt-debug-info-window-message";
    String OK_BUTTON = "info-window";
  }

  @FindBy(id = Locators.WARNING_DIALOG_ID)
  private WebElement warningDialog;

  @FindBy(id = Locators.OK_BUTTON)
  private WebElement okBtn;

  /** wait closing a warning dialog */
  public void waitWaitClosingWarnDialogWindow() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.WARNING_DIALOG_ID)));
  }

  /** wait ok btn, click it and wait closing of the form */
  public void clickOkBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(okBtn));
    okBtn.click();
    waitWaitClosingWarnDialogWindow();
  }

  /** wait appearance a warning dialog */
  public void waitWaitWarnDialogWindowWithSpecifiedTextMess(String mess) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(warningDialog, mess));
  }

  public boolean isPresent() {
    try {
      return warningDialog.isDisplayed();
    } catch (NoSuchElementException e) {
      return false;
    }
  }
}
