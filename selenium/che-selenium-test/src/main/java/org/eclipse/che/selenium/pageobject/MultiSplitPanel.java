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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev */
@Singleton
public class MultiSplitPanel {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public MultiSplitPanel(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface SplitPaneCommands {
    String CLOSE_PANE = "Close Pane";
    String CLOSE_ALL_TABS = "Close All Tabs In Pane";
    String SPLIT_PANE_IN_ROWS = "Split Pane In Two Rows";
    String SPLIT_PANE_IN_COLUMNS = "Split Pane In Two Columns";
  }

  private interface Locators {
    String TABS_PANEL_ID = "gwt-debug-multiSplitPanel-tabsPanel";
    String SPLIT_PANEL_AREA = "(//div[@id='gwt-debug-process-output-panel-holder'])[position()=%s]";
    String SPLIT_PANE_MENU_ICON = "(//div[@id='gwt-debug-menuSplitPanel'])[position()=%s]";
    String SPLIT_PANE_MENU = "//div[@class='popupContent']";
    String SPLIT_PANE_COMMAND = "//div[@class='popupContent']//following::div[text()='%s']";
    String PROCESS_IN_MENU_CLOSE_ICON =
        "//div[@class='popupContent']//following::div[text()='%s']/following-sibling::div";
    String TAB_PROCESS_NAME =
        "(//div[@id='gwt-debug-multiSplitPanel-tabsPanel'])[position()=%s]//div[text()='%s']";
    String TAB_PROCESS_CLOSE_ICON =
        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']/following-sibling::div";
    String TAB_NAME_FOCUS = "//div[@focused]/div[text()='%s']";
    String GIT_INFO_PANEL = "(//div[@id='gwt-debug-gitOutputConsoleLines'])[position()=%s]";
  }

  /**
   * click on the icon of the multi split panel
   *
   * @param iconPosition is a position of split pane icon; the most right and lowest open pane has
   *     position number one
   */
  public void clickOnIconMultiSplitPanel(int iconPosition) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.SPLIT_PANE_MENU_ICON, iconPosition))))
        .click();
  }

  /** wait opening menu of the multi split panel */
  public void waitSplitPanelMenuIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SPLIT_PANE_MENU)));
  }

  /** wait closing menu of the multi split panel */
  public void waitSplitPanelMenuIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.SPLIT_PANE_MENU)));
  }

  /**
   * wait a number of the open split panels
   *
   * @param numberPanels is a number of open split panels
   */
  public void waitNumberOpenSplitPanels(int numberPanels) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.numberOfElementsToBe(By.id(Locators.TABS_PANEL_ID), numberPanels));
  }

  /**
   * click on the split panel
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   */
  public void selectSplitPanel(int numberPanel) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SPLIT_PANEL_AREA, numberPanel))))
        .click();
  }

  /**
   * select command into split pane widget
   *
   * @param paneCommands is a name command
   */
  public void selectCommandSplitPane(String paneCommands) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.SPLIT_PANE_COMMAND, paneCommands))))
        .click();
  }

  /**
   * wait process is present into split pane widget
   *
   * @param nameProcess is name of process into the split pane widget
   */
  public void waitProcessIsPresentIntoPaneMenu(String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.SPLIT_PANE_COMMAND, nameProcess))));
  }

  /**
   * wait process is not present into split pane widget
   *
   * @param nameProcess is name of process into the split pane widget
   */
  public void waitProcessIsNotPresentIntoPaneMenu(String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(String.format(Locators.SPLIT_PANE_COMMAND, nameProcess))));
  }

  /**
   * close a process into the split pane widget
   *
   * @param nameProcess is name of process into the split pane widget
   */
  public void closeProcessIntoPaneMenu(String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.PROCESS_IN_MENU_CLOSE_ICON, nameProcess))))
        .click();
  }

  /**
   * select command by tab name
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   * @param nameProcess is name of tab process for select
   */
  public void selectProcessByTabName(int numberPanel, String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_PROCESS_NAME, numberPanel, nameProcess))))
        .click();
  }

  /**
   * wait a tab name of process is focused
   *
   * @param nameProcess is tab name of process
   */
  public void waitTabNameProcessIsFocused(String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_NAME_FOCUS, nameProcess))));
  }

  /**
   * wait tab name of process is present
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   * @param nameProcess is tab name of process
   */
  public void waitTabProcessIsPresent(int numberPanel, String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_PROCESS_NAME, numberPanel, nameProcess))));
  }

  /**
   * wait tab name of process is not present
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   * @param nameProcess is tab name of process
   */
  public void waitTabProcessIsNotPresent(int numberPanel, String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_PROCESS_NAME, numberPanel, nameProcess))));
  }

  /**
   * close by click on the tab name of process
   *
   * @param nameProcess is name tab name of process
   */
  public void closeProcessByTabName(String nameProcess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.TAB_PROCESS_CLOSE_ICON, nameProcess))))
        .click();
  }

  /**
   * wait expected message into the split git info
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   * @param message is expected message
   */
  public void waitMesageIntoSplitGitPanel(int numberPanel, String message) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver webDriver) -> {
              return getMessFromSplitGitPanel(numberPanel).contains(message);
            });
  }

  /**
   * get text from split git info panel
   *
   * @param numberPanel is a position of split pane; the most right and lowest open pane has
   *     position number one
   */
  public String getMessFromSplitGitPanel(int numberPanel) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.GIT_INFO_PANEL, numberPanel))))
        .getText();
  }
}
