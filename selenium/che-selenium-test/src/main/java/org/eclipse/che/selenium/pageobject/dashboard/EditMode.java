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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The edit/save mode with SAVE and CANCEL buttons.
 *
 * @author Ann Shumilova
 */
@Singleton
public class EditMode {

  private interface Locators {
    String MODE_BAR_XPATH = "//div[contains(@class, 'workspace-edit-mode-overlay')]";
    String MODE_SAVE_BUTTON_XPATH =
        MODE_BAR_XPATH + "//che-button-save-flat[@che-button-title='Save']";
    String MODE_CANCEL_BUTTON_XPATH =
        MODE_BAR_XPATH + "//che-button-cancel-flat[@che-button-title='Cancel']";
  }

  private final SeleniumWebDriver seleniumWebDriver;

  @FindBy(xpath = Locators.MODE_BAR_XPATH)
  WebElement modeBar;

  @FindBy(xpath = Locators.MODE_SAVE_BUTTON_XPATH)
  WebElement saveButton;

  @FindBy(xpath = Locators.MODE_CANCEL_BUTTON_XPATH)
  WebElement cancelButton;

  @Inject
  public EditMode(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Wait edit mode is displayed. */
  public void waitDisplayed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(modeBar));
  }

  /** Wait edit mode is hidden. */
  public void waitHidden() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(Locators.MODE_BAR_XPATH)));
  }

  /** Performs click on Cancel button. */
  public void clickCancel() {
    cancelButton.click();
  }

  /** Performs click on Confirm button. */
  public void clickSave() {
    saveButton.click();
  }

  /** Performs click on Confirm button. */
  public boolean isSaveEnabled() {
    // animation duration:
    WaitUtils.sleepQuietly(1);
    return !Boolean.valueOf(saveButton.getAttribute("aria-disabled"));
  }
}
