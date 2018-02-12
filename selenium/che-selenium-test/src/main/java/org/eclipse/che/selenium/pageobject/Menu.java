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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Musienko Maxim */
@Singleton
public class Menu {
  private static final Logger LOG = LoggerFactory.getLogger(Menu.class);

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private WebDriverWait redrawMenuItemsWait;

  @Inject
  public Menu(SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
    redrawMenuItemsWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  private static String ENABLED_CSS_VALUE = "rgba(255, 255, 255, 1)";

  private interface Locators {
    String DISABLED_ITEM = "//tr[@id='%s' and @item-enabled='false']";
    String ENABLED_ITEM = "//tr[@id='%s' and @item-enabled='true']";
  }

  /**
   * Run command from toolbar
   *
   * @param idCommand
   */
  public void runCommand(String idCommand) {
    redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommand)));
    clickOnCommand(idCommand);
    loader.waitOnClosed();
  }

  /**
   * Run command from toolbar with user delay for active state of menu
   *
   * @param idCommand
   * @param userDelay delay for waiting active state menu defined by user
   */
  public void runCommand(String idCommand, int userDelay) {
    redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommand)));
    clickOnCommand(idCommand);
    loader.waitOnClosed();
  }

  /**
   * Run command for disabled elements of menu
   *
   * @param idTopMenuCommand
   * @param idCommandName
   */
  public void runAndWaitCommand(final String idTopMenuCommand, final String idCommandName) {

    redrawMenuItemsWait.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            driver.findElement(By.id(idTopMenuCommand)).click();
            return driver.findElement(By.id(idCommandName)).isDisplayed();
          }
        });
    seleniumWebDriver.findElement(By.id(idCommandName)).click();
    redrawMenuItemsWait.until(invisibilityOfElementLocated(By.id(idCommandName)));
  }

  /**
   * Run command from sub menu.
   *
   * @param idTopMenuCommand
   * @param idCommandName
   */
  public void runCommand(final String idTopMenuCommand, final String idCommandName) {
    loader.waitOnClosed();
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idTopMenuCommand))).click();

    } catch (WebDriverException ex) {
      LOG.warn(ex.getLocalizedMessage());
      WaitUtils.sleepQuietly(REDRAW_UI_ELEMENTS_TIMEOUT_SEC, TimeUnit.MILLISECONDS);
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idTopMenuCommand))).click();
    }
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommandName))).click();
    } catch (TimeoutException e) {
      seleniumWebDriver.findElement(By.id(idTopMenuCommand)).click();
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommandName))).click();
    }
    redrawMenuItemsWait.until(invisibilityOfElementLocated(By.id(idCommandName)));
    loader.waitOnClosed();
  }

  /**
   * Run command from sub menu.
   *
   * @param idTopMenuCommand
   * @param idCommandName
   * @param idSubCommandName
   */
  public void runCommand(String idTopMenuCommand, String idCommandName, String idSubCommandName) {
    loader.waitOnClosed();
    clickOnCommand(idTopMenuCommand);
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommandName)));
    } catch (TimeoutException e) {
      LOG.error(e.getLocalizedMessage(), e);
      clickOnCommand(idTopMenuCommand);
    }
    redrawMenuItemsWait.until(elementToBeClickable(By.id(idCommandName)));
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(seleniumWebDriver.findElement(By.id(idCommandName)))
        .perform();
    // if element If the element is not drawn in time, in the catch block call submenu again
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idSubCommandName)));
    } catch (TimeoutException e) {
      WaitUtils.sleepQuietly(1);
      seleniumWebDriver.findElement(By.id(idCommandName)).click();
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idSubCommandName)));
    }
    seleniumWebDriver.findElement(By.id(idSubCommandName)).click();
    redrawMenuItemsWait.until(invisibilityOfElementLocated(By.id(idCommandName)));

    loader.waitOnClosed();
  }

  /**
   * Run command from sub menu.
   *
   * @param idTopMenuCommand
   * @param idCommandName
   * @param xpathSubCommandName
   */
  public void runCommandByXpath(
      String idTopMenuCommand, String idCommandName, String xpathSubCommandName) {

    loader.waitOnClosed();
    clickOnCommand(idTopMenuCommand);
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.id(idCommandName)));
    } catch (TimeoutException e) {
      LOG.error(e.getLocalizedMessage(), e);
      clickOnCommand(idTopMenuCommand);
    }
    redrawMenuItemsWait.until(elementToBeClickable(By.id(idCommandName)));
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(seleniumWebDriver.findElement(By.id(idCommandName)))
        .perform();
    // if element If the element is not drawn in time, in the catch block call submenu again
    try {
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.xpath(xpathSubCommandName)));
    } catch (TimeoutException e) {
      LOG.error(e.getLocalizedMessage(), e);
      WaitUtils.sleepQuietly(MINIMUM_SEC);
      seleniumWebDriver.findElement(By.id(idCommandName)).click();
      redrawMenuItemsWait.until(visibilityOfElementLocated(By.xpath(xpathSubCommandName)));
    }
    seleniumWebDriver.findElement(By.xpath(xpathSubCommandName)).click();
    redrawMenuItemsWait.until(invisibilityOfElementLocated(By.id(idCommandName)));

    loader.waitOnClosed();
  }

  /**
   * Run command from top menu.
   *
   * @param idTopMenuCommand name of menu
   */
  public void clickOnCommand(final String idTopMenuCommand) {
    // TODO Use attributes enabled/disabled instead of css values
    redrawMenuItemsWait.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            WebElement element = driver.findElement(By.id(idTopMenuCommand));
            boolean isTrue = element.getCssValue("color").equals(ENABLED_CSS_VALUE);
            Actions actions = actionsFactory.createAction(seleniumWebDriver);
            actions.moveToElement(element).click().perform();
            return isTrue;
          }
        });
  }

  /**
   * Run command from menu using Web elements with xpath
   *
   * @param command is name of command
   */
  public void runCommandByXpath(String command) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.ENABLED_ITEM, command))))
        .click();

    loader.waitOnClosed();
  }

  /** wait a command is not present in the menu */
  public void waitCommandIsNotPresentInMenu(String menuCommand) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(menuCommand)));
  }

  /**
   * wait a command is disabled in the menu
   *
   * @param idCommand is name of command in the menu
   */
  public void waitCommandIsDisabledInMenu(String idCommand) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.DISABLED_ITEM, idCommand))));
  }

  /**
   * wait a menu item is enabled
   *
   * @param idCommand is name of command in the menu
   */
  public void waitMenuItemIsEnabled(String idCommand) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(idCommand)));
  }
}
