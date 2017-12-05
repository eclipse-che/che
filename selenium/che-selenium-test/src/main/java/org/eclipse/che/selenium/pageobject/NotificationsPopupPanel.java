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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Zaryana Dombrovskaya */
@Singleton
public class NotificationsPopupPanel {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationsPopupPanel.class);

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public NotificationsPopupPanel(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
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
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.visibilityOf(progressPopupPanel));
  }

  /** wait progress Popup panel appear after timeout defined by user */
  public void waitProgressBarControl(int userTimeout) {
    new WebDriverWait(seleniumWebDriver, userTimeout)
        .until(ExpectedConditions.visibilityOf(progressPopupPanel));
  }

  /** wait progress Popup panel disappear */
  public void waitProgressPopupPanelClose() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(PROGRESS_POPUP_PANEL_ID)));
  }

  /** wait progress Popup panel disappear after timeout defined by user */
  public void waitProgressPopupPanelClose(int userTimeout) {
    new WebDriverWait(seleniumWebDriver, userTimeout)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(PROGRESS_POPUP_PANEL_ID)));
  }

  /**
   * get all text from progress popup panel
   *
   * @return text from popup panel
   */
  public String getAllMessagesFromProgressPopupPanel() {
    waitProgressBarControl();
    return progressPopupPanel.getText();
  }

  /**
   * get all text from progress popup panel after appearance this with timeout defined by user
   *
   * @return text from popup panel
   */
  public String getAllMessagesFromProgressPopupPanel(int userTimeout) {
    waitProgressBarControl(userTimeout);
    return progressPopupPanel.getText();
  }

  /**
   * wait expected text on progress popup panel and then close it
   *
   * @param message expected text
   */
  public void waitExpectedMessageOnProgressPanelAndClosed(final String message) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                input -> getAllMessagesFromProgressPopupPanel().contains(message));
    waitProgressPopupPanelClose();
  }

  /**
   * wait expected text on progress popup panel and then close it
   *
   * @param message expected text
   * @param timeout timeout defined by user
   */
  public void waitExpectedMessageOnProgressPanelAndClosed(final String message, final int timeout) {
    try {
      new WebDriverWait(seleniumWebDriver, timeout)
          .until(
              (ExpectedCondition<Boolean>)
                  input -> getAllMessagesFromProgressPopupPanel(timeout).contains(message));
    } catch (StaleElementReferenceException ex) {
      LOG.debug(ex.getLocalizedMessage(), ex);
      WaitUtils.sleepQuietly(500, TimeUnit.MILLISECONDS);
      new WebDriverWait(seleniumWebDriver, timeout)
          .until(ExpectedConditions.textToBePresentInElement(progressPopupPanel, message));
    }
    waitProgressPopupPanelClose(timeout);
  }

  /** wait disappearance of notification popups */
  public void waitPopUpPanelsIsClosed() {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(PROGRESS_POPUP_PANEL_ID)));
  }
}
