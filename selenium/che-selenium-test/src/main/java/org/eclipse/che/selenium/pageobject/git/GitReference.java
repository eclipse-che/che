/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class GitReference {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitReference(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_FORM_ID = "gwt-debug-git-checkoutReference-mainForm";
    String CHECKOUT_BTN_ID = "git-checkoutReference-checkout";
    String CANCEL_BTN_ID = "git-checkoutReference-cancel";
    String FIELD_REFERENCES_ID = "gwt-debug-git-checkoutReference-reference";
  }

  @FindBy(id = Locators.MAIN_FORM_ID)
  WebElement mainForm;

  @FindBy(id = Locators.CHECKOUT_BTN_ID)
  WebElement checkOutBtn;

  @FindBy(id = Locators.CANCEL_BTN_ID)
  WebElement cancelBtn;

  @FindBy(id = Locators.FIELD_REFERENCES_ID)
  WebElement field;

  /** wait opening of the reference widget */
  public void waitOpeningMainForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));
  }

  /** wait opening of the reference widget */
  public void waitClosingMainForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM_ID)));
  }

  /** wait type the value into reference field */
  public void typeReference(String reference) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(field))
        .clear();
    field.sendKeys(reference);
  }

  /** click on checkout button */
  public void clickOnCheckoutBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(checkOutBtn))
        .click();
  }

  /** click on cancel button */
  public void clickOnCancelBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(cancelBtn))
        .click();
  }
}
