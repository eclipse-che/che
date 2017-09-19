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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev */
@Singleton
public class Consoles {

  private final WebDriverWait redrawDriverWait;
  private final WebDriverWait loadPageDriverWait;
  private final WebDriverWait updateProjDriverWait;

  public static final String PROCESS_NAME_XPATH = "//span[text()='%s']";
  public static final String PROCESSES_MAIN_AREA =
      "//div[@role='toolbar-header']//div[text()='Processes']";
  public static final String CLOSE_PROCESS_ICON =
      "//div[@id='gwt-debug-consolesPanel']//ul//span[text()='%s']/parent::span/span";
  public static final String TAB_PROCESS_NAME =
      "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']";
  public static final String TAB_PROCESS_CLOSE_ICON =
      "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']/following-sibling::div";
  public static final String DEBUG_TAB = "gwt-debug-partButton-Debug";
  public static final String OPEN_SSH_TERMINAL = "//span[text()='SSH']";
  public static final String OPEN_NEW_TERMINAL = "//div[@id='gwt-debug-process-tree']//span/span";
  public static final String CLOSE_TERMINAL_CONSOLES_ICON =
      "//span[text()='Terminal']/preceding::span[2]";
  public static final String PROCESSES_TAB = "gwt-debug-partButton-Processes";
  public static final String MAXIMIZE_PANEL_ICON =
      "//div[@id='gwt-debug-infoPanel']//div[text()='Processes']/parent::div/following-sibling::div//*[local-name() = 'svg' and @name='workBenchIconMaximize']";
  public static final String HIDE_CONSOLES_ICON =
      "//div[@id='gwt-debug-infoPanel']//div[text()='Processes']/parent::div/following-sibling::div//*[local-name() = 'svg' and @name='workBenchIconMinimize']";
  public static final String PREVIEW_URL = "//div[@active]//a[@href]";
  public static final String COMMAND_CONSOLE_ID =
      "//div[@active]//div[@id='gwt-debug-commandConsoleLines']";
  protected final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private static final String CONSOLE_PANEL_DRUGGER_CSS = "div.gwt-SplitLayoutPanel-VDragger";

  @Inject
  public Consoles(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    redrawDriverWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    loadPageDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    updateProjDriverWait = new WebDriverWait(seleniumWebDriver, UPDATING_PROJECT_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }
  @FindBy(css = CONSOLE_PANEL_DRUGGER_CSS)
  WebElement consolesPanelDrugger;

  @FindBy(id = PROCESSES_TAB)
  WebElement processesTab;

  @FindBy(id = DEBUG_TAB)
  WebElement debugTab;

  @FindBy(xpath = OPEN_SSH_TERMINAL)
  WebElement sshTerminalBtn;

  @FindBy(xpath = OPEN_NEW_TERMINAL)
  WebElement openNewTerminal;

  @FindBy(xpath = CLOSE_TERMINAL_CONSOLES_ICON)
  WebElement closeTerminal;

  @FindBy(xpath = HIDE_CONSOLES_ICON)
  WebElement hideConsolesIcon;

  @FindBy(xpath = COMMAND_CONSOLE_ID)
  WebElement consoleContainer;

  @FindBy(xpath = PREVIEW_URL)
  WebElement previewUrl;

  @FindBy(xpath = PROCESSES_MAIN_AREA)
  WebElement processesMainArea;

  /** click on consoles tab in bottom and wait opening console area (terminal on other console ) */
  public void clickOnProcessesTab() {
    redrawDriverWait.until(visibilityOf(processesTab)).click();
  }

  /** click on consoles tab in bottom and wait opening console area (terminal on other console ) */
  public void clickOnDebugTab() {
    redrawDriverWait.until(visibilityOf(debugTab)).click();
  }

  /** click on preview url link */
  public void clickOnPreviewUrl() {
    loadPageDriverWait.until(visibilityOf(previewUrl)).click();
  }

  public void waitExpectedTextIntoPreviewUrl(String expectedText) {
    redrawDriverWait.until(textToBePresentInElement(previewUrl, expectedText));
  }

  /** Get url for preview. */
  public String getPreviewUrl() {
    return loadPageDriverWait.until(visibilityOf(previewUrl)).getText();
  }

  /** click on the 'Close' icon of the terminal into "Consoles' area */
  public void closeTerminalIntoConsoles() {
    loadPageDriverWait.until(elementToBeClickable(closeTerminal)).click();
  }

  /** open new terminal */
  public void openNewTerminalIntoProcesses() {
    loadPageDriverWait.until(elementToBeClickable(openNewTerminal)).click();
  }

  /**
   * click on the 'Close' icon of the process into 'Processes' area
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void closeProcessInProcessConsoleTreeByName(String nameProcess) {
    loadPageDriverWait
        .until(visibilityOfElementLocated(By.xpath(String.format(CLOSE_PROCESS_ICON, nameProcess))))
        .click();
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessInProcessConsoleTree(String nameProcess) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(By.xpath(String.format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   * @param timeout is timeout in seconds defined by user
   */
  public void waitProcessInProcessConsoleTree(String nameProcess, int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(
            visibilityOfElementLocated(By.xpath(String.format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * wait process name is not present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessIsNotPresentInProcessConsoleTree(String nameProcess) {
    loadPageDriverWait.until(
        invisibilityOfElementLocated(By.xpath(String.format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * select process into 'Process' console tree by name
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void selectProcessInProcessConsoleTreeByName(String nameProcess) {
    loadPageDriverWait
        .until(visibilityOfElementLocated(By.xpath(String.format(PROCESS_NAME_XPATH, nameProcess))))
        .click();
  }

  /**
   * select process into console by tab name
   *
   * @param tabNameProcess is tab name of process
   */
  public void selectProcessByTabName(String tabNameProcess) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(String.format(TAB_PROCESS_NAME, tabNameProcess))))
        .click();
    loader.waitOnClosed();
  }

  /**
   * wait tab name of process is present
   *
   * @param tabNameProcess is tab name of process
   */
  public void waitTabNameProcessIsPresent(String tabNameProcess) {
    redrawDriverWait.until(
        visibilityOfElementLocated(By.xpath(String.format(TAB_PROCESS_NAME, tabNameProcess))));
  }

  /**
   * wait tab name of process is not present
   *
   * @param tabNameProcess is tab name of process
   */
  public void waitTabNameProcessIsNotPresent(String tabNameProcess) {
    redrawDriverWait.until(
        invisibilityOfElementLocated(By.xpath(String.format(TAB_PROCESS_NAME, tabNameProcess))));
  }

  /**
   * close process by tab name
   *
   * @param tabNameProcess is tab name of process
   */
  public void closeProcessByTabName(String tabNameProcess) {
    redrawDriverWait
        .until(
            elementToBeClickable(By.xpath(String.format(TAB_PROCESS_CLOSE_ICON, tabNameProcess))))
        .click();
  }

  /** click on the 'Hide' icon of the 'Processes' */
  public void closeProcessesArea() {
    redrawDriverWait.until(visibilityOf(hideConsolesIcon)).click();
  }

  /** click on open ssh terminal button in the consoles widget and wait opening terminal */
  public void clickOnOpenSshTerminalBtn() {
    redrawDriverWait.until(visibilityOf(sshTerminalBtn)).click();
    waitIsCommandConsoleOpened();
  }

  /** wait opening 'Command console widget' */
  public void waitIsCommandConsoleOpened() {
    redrawDriverWait.until(visibilityOf(consoleContainer));
  }

  /**
   * wait opening 'Command console widget'
   *
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitIsCommandConsoleOpened(int definedTimeout) {
    new WebDriverWait(seleniumWebDriver, definedTimeout)
        .until(ExpectedConditions.visibilityOf(consoleContainer));
  }

  /** wait expected text into 'Command console' */
  public void waitExpectedTextIntoConsole(String expectedText) {
    updateProjDriverWait.until(textToBePresentInElement(consoleContainer, expectedText));
  }

  /** get visible text from command console */
  public String getVisibleTextFromCommandConsole() {
    return redrawDriverWait.until(visibilityOf(consoleContainer)).getText();
  }

  /**
   * wait expected text into 'Command console'
   *
   * @param expectedText expected messaje in concole
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitExpectedTextIntoConsole(String expectedText, int definedTimeout) {
    new WebDriverWait(seleniumWebDriver, definedTimeout)
        .until(textToBePresentInElement(consoleContainer, expectedText));
  }

  /** wait a preview url into 'Consoles' */
  public void waitPreviewUrlIsPresent() {
    redrawDriverWait.until(visibilityOf(previewUrl));
  }

  /** wait a preview url is not present into 'Consoles' */
  public void waitPreviewUrlIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(PREVIEW_URL)));
  }

  /** return state open/or close of processes main widget */
  public boolean processesMainAreaIsOpen() {
    return processesMainArea.isDisplayed();
  }

  /** click on the maximize panel icon */
  public void clickOnMaximizePanelIcon() {
    redrawDriverWait.until(elementToBeClickable(By.xpath(MAXIMIZE_PANEL_ICON))).click();
  }

  /**
   * * apply drug and drop feature for consoles feature, shift the work bench panel up or down
   *
   * @param xoffset offset in pixels for shifting
   */
  public void drugConsolesInDefinePosition(int xoffset) {
    WebElement drugger =
            redrawDriverWait.until(ExpectedConditions.visibilityOf(consolesPanelDrugger));
    new Actions(seleniumWebDriver).dragAndDropBy(drugger, xoffset, xoffset).perform();
  }
}
