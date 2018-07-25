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
package org.eclipse.che.selenium.pageobject.dashboard;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class DashboardPopup {

  private interface Locators {
    String POPUP_XPATH = "//che-popup";
    String POPUP_OK_BUTTON_XPATH = "//che-popup//button//span[text()='OK']";
    String POPUP_TITLE_XPATH = "//che-popup//div[contains(@class, 'che-popup-header')]/div";
    String POPUP_MESSAGE_XPATH = "//che-popup//div[contains(@class, 'license-issue-message')]/span";
  }

  @FindBy(xpath = Locators.POPUP_XPATH)
  WebElement popup;

  @FindBy(xpath = Locators.POPUP_OK_BUTTON_XPATH)
  WebElement popupOkButton;

  @FindBy(xpath = Locators.POPUP_TITLE_XPATH)
  WebElement popupTitle;

  @FindBy(xpath = Locators.POPUP_MESSAGE_XPATH)
  WebElement popupMessage;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public DashboardPopup(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitPopupDisplays() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC).until(visibilityOf(popup));
  }

  public void closePopup() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(popupOkButton))
        .click();

    waitPopupClosed();
  }

  public void waitPopupClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(Locators.POPUP_XPATH)));
  }

  public String getPopupTitle() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(popupTitle));

    return popupTitle.getText();
  }

  public String getPopupMessage() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(popupMessage));

    return popupMessage.getText();
  }
}
