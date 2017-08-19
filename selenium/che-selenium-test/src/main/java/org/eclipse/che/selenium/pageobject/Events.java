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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Zaryana Dombrovskaya */
@Singleton
public class Events {

  public static final String EVENTS_AREA = "gwt-debug-notificationManager-mainPanel";
  public static final String PROJECT_EVENTS_TAB = "gwt-debug-partButton-Events";
  public static final String CLOSE_EVENT_ICON =
      "//*[local-name()='svg' and contains(@id,'gwt-debug-close-icongwt')]";
  public static final String MINIMIZE_BUTTON = "gwt-debug-notification-minimizeBut";

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final NotificationsPopupPanel notificationsPopupPanel;
  private final ActionsFactory actionsFactory;

  @Inject
  public Events(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      NotificationsPopupPanel notificationsPopupPanel,
      ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.notificationsPopupPanel = notificationsPopupPanel;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = PROJECT_EVENTS_TAB)
  WebElement projectEventsTab;

  @FindBy(id = EVENTS_AREA)
  WebElement eventsArea;

  /** wait project events tab */
  public void waitProjectEventsTab() {
    new WebDriverWait(seleniumWebDriver, 30)
        .until(ExpectedConditions.visibilityOf(projectEventsTab));
  }

  /** click on project events tab */
  public void clickProjectEventsTab() {
    notificationsPopupPanel.waitProgressPopupPanelClose();
    waitProjectEventsTab();
    loader.waitOnClosed();
    projectEventsTab.click();
  }

  /** wait project events area */
  public void waitOpened() {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(ExpectedConditions.presenceOfElementLocated(By.id(EVENTS_AREA)));
  }

  /** wait 'Events' panel is closed */
  public void waitEventsPanelIsClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(EVENTS_AREA)));
  }

  public String getAllMessagesFromOutput() {
    waitOpened();
    return eventsArea.getText();
  }

  /**
   * wait expected message into events panel
   *
   * @param message the expected message;
   * @param userTimeout timeout wor waiting
   */
  public void waitExpectedMessage(final String message, final int userTimeout) {
    new WebDriverWait(seleniumWebDriver, UPDATING_PROJECT_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) input -> getAllMessagesFromOutput().contains(message));
  }

  /**
   * wait expected message into events panel
   *
   * @param message the expected message;
   */
  public void waitExpectedMessage(final String message) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) input -> getAllMessagesFromOutput().contains(message));
  }

  /**
   * wait expected message into events panel
   *
   * @param message the expected message;
   */
  public void waitMessageIsNotPresent(final String message) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver input) {
                return !getAllMessagesFromOutput().contains(message);
              }
            });
  }

  /** wait while event message panel will be empty */
  public void waitEmptyEventsPanel() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                List<WebElement> messageCloseIcons =
                    seleniumWebDriver.findElements(By.id(CLOSE_EVENT_ICON));
                return messageCloseIcons.size() == 0;
              }
            });
  }

  /** Clear opened and closed panel */
  public void clearAllMessages() {
    List<WebElement> messageCloseIcons = seleniumWebDriver.findElements(By.xpath(CLOSE_EVENT_ICON));
    for (WebElement messageCloseIcon : messageCloseIcons) {
      new WebDriverWait(seleniumWebDriver, 5)
          .until(ExpectedConditions.visibilityOf(messageCloseIcon));
      messageCloseIcon.click();
    }
    waitEmptyEventsPanel();
  }

  /** Double click on on project events tab */
  public void doubleClickProjectEventsTab() {
    notificationsPopupPanel.waitProgressPopupPanelClose();
    waitProjectEventsTab();
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick()
        .doubleClick(projectEventsTab)
        .perform();
  }

  /** click on the minimize button of 'Events' panel */
  public void clickMinimizeButtonEventsPanel() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(MINIMIZE_BUTTON)))
        .click();
    waitEventsPanelIsClosed();
  }
}
