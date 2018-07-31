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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by aleksandr shmaraiev */
@Singleton
public class DialogAbout {

  private static final String ABOUT_DIALOG = "gwt-debug-aboutView-window";
  private static final String ABOUT_DIALOG_OK_BUTTON = "help-about-ok";
  private static final String BUILD_DETAILS_OK_BUTTON = "build-details-ok";
  private static final String BUILD_DETAILS_ANCHOR = "gwt-debug-build-details-anchor";
  private static final String BUILD_DETAILS_WINDOW = "gwt-debug-build-details";
  private static final String TEXT_VERSION =
      "//div[@id='gwt-debug-aboutView-window']//div[text()='Version :']/following-sibling::div[1]";

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public DialogAbout(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = ABOUT_DIALOG)
  private WebElement aboutDialog;

  @FindBy(id = ABOUT_DIALOG_OK_BUTTON)
  private WebElement aboutDialogOkButton;

  @FindBy(id = BUILD_DETAILS_OK_BUTTON)
  private WebElement buildDetailsOkButton;

  @FindBy(xpath = TEXT_VERSION)
  private WebElement textVersion;

  @FindBy(id = BUILD_DETAILS_ANCHOR)
  WebElement buildDetailsAnchor;

  @FindBy(id = BUILD_DETAILS_WINDOW)
  WebElement buildDetailsWindow;

  public void closeAboutDialog() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(aboutDialogOkButton))
        .click();
    new WebDriverWait(seleniumWebDriver, 3)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(ABOUT_DIALOG)));
  }

  /** wait 'About Codenvy' dialog is open */
  public void waitAboutDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(ABOUT_DIALOG)));
  }

  /**
   * Verifies text elements in the About Dialog
   *
   * @param text is a certain text of element
   */
  public void waitVerifyTextElements(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(aboutDialog, text));
  }

  public void clickOnBuildDetailsAnchor() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buildDetailsAnchor))
        .click();
  }

  public void waitBuildDetailsDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(BUILD_DETAILS_WINDOW)));
  }

  public void closeBuildDetailsDialog() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buildDetailsOkButton))
        .click();
    new WebDriverWait(seleniumWebDriver, 3)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(BUILD_DETAILS_WINDOW)));
  }
}
