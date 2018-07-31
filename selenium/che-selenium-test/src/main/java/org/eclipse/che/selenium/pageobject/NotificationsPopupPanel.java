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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Zaryana Dombrovskaya */
@Singleton
public class NotificationsPopupPanel {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationsPopupPanel.class);

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public NotificationsPopupPanel(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final String PROGRESS_POPUP_PANEL_ID = "gwt-debug-popup-container";
  private static final String CLOSE_POPUP_IMG_XPATH =
      "//div[@id='gwt-debug-popup-container']/descendant::*[local-name()='svg'][2]";

  @FindBy(id = PROGRESS_POPUP_PANEL_ID)
  private WebElement progressPopupPanel;

  @FindBy(xpath = CLOSE_POPUP_IMG_XPATH)
  private WebElement closePopupButton;

  /** wait progress Popup panel appear */
  public void waitProgressBarControl() {
    waitProgressBarControl(EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /** wait progress Popup panel appear after timeout defined by user */
  public void waitProgressBarControl(int userTimeout) {
    seleniumWebDriverHelper.waitVisibility(progressPopupPanel, userTimeout);
  }

  /** wait progress Popup panel disappear */
  public void waitProgressPopupPanelClose() {
    waitProgressPopupPanelClose(EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /** wait progress Popup panel disappear after timeout defined by user */
  public void waitProgressPopupPanelClose(int userTimeout) {
    seleniumWebDriverHelper.waitInvisibility(progressPopupPanel, userTimeout);
  }

  /**
   * wait expected text on progress popup panel and then close it
   *
   * @param message expected text
   */
  public void waitExpectedMessageOnProgressPanelAndClosed(final String message) {
    waitExpectedMessageOnProgressPanelAndClosed(message, ELEMENT_TIMEOUT_SEC);
  }

  /**
   * wait expected text on progress popup panel and then close it
   *
   * @param message expected text
   * @param timeout timeout defined by user
   */
  public void waitExpectedMessageOnProgressPanelAndClosed(final String message, final int timeout) {
    seleniumWebDriverHelper.waitTextContains(progressPopupPanel, message, timeout);
    waitProgressPopupPanelClose(timeout);
  }

  /** wait disappearance of notification popups */
  public void waitPopupPanelsAreClosed() {
    seleniumWebDriverHelper.waitInvisibility(progressPopupPanel, WIDGET_TIMEOUT_SEC);
  }
}
