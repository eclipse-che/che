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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.utils.HttpUtil;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Aleksandr Shmaraev */
@Singleton
public class Consoles {

  private final WebDriverWait redrawDriverWait;
  private final WebDriverWait loadPageDriverWait;
  private final WebDriverWait updateProjDriverWait;
  private final ProjectExplorer projectExplorer;
  private final AskDialog askDialog;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  public static final String PROCESS_NAME_XPATH = "//span[text()='%s']";
  public static final String PROCESSES_MAIN_AREA = "gwt-debug-consolesPanel";
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
  public static final String PROCESSES_BUTTON = "gwt-debug-partButton-Processes";
  public static final String MAXIMIZE_PANEL_ICON =
      "//div[@id='gwt-debug-infoPanel']//div[@id='gwt-debug-maximizeButton']";
  public static final String HIDE_CONSOLES_ICON =
      "//div[@id='gwt-debug-infoPanel']//div[@id='gwt-debug-hideButton']";
  public static final String PREVIEW_URL = "//div[@active]//a[@href]";
  public static final String COMMAND_CONSOLE_ID =
      "//div[@active]//div[@id='gwt-debug-commandConsoleLines']";
  public static final String PLUS_ICON = "gwt-debug-plusPanel";
  public static final String TERMINAL_MENU_ITEM = "contextMenu/Terminal";
  public static final String SERVER_MENU_ITEM = "contextMenu/Servers";
  public static final String SERVER_INFO_TABLE_CAPTION = "gwt-debug-runtimeInfoCellTableCaption";
  public static final String SERVER_INFO_HIDE_INTERNAL_CHECK_BOX =
      "gwt-debug-runtimeInfoHideServersCheckBox";
  public static final String MACHINE_NAME =
      "//div[@id='gwt-debug-process-tree']//span[text()= '%s']";
  public static final String COMMANDS_MENU_ITEM = "gwt-debug-contextMenu/commandsActionGroup";
  public static final String SERVERS_MENU_ITEM = "contextMenu/Servers";
  public static final String COMMAND_NAME = "//tr[contains(@id,'command_%s')]";

  public interface CommandsGoal {
    String COMMON = "gwt-debug-contextMenu/Commands/goal_Common";
    String BUILD = "gwt-debug-contextMenu/Commands/goal_Build";
    String RUN = "gwt-debug-contextMenu/Commands/goal_Run";
  }

  protected final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private final Loader loader;
  private static final String CONSOLE_PANEL_DRUGGER_CSS = "div.gwt-SplitLayoutPanel-VDragger";
  private static final Logger LOG = LoggerFactory.getLogger(Consoles.class);

  @Inject
  public Consoles(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      ProjectExplorer projectExplorer,
      AskDialog askDialog,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.projectExplorer = projectExplorer;
    this.askDialog = askDialog;
    this.webDriverWaitFactory = webDriverWaitFactory;
    redrawDriverWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    loadPageDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    updateProjDriverWait = new WebDriverWait(seleniumWebDriver, UPDATING_PROJECT_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(css = CONSOLE_PANEL_DRUGGER_CSS)
  WebElement consolesPanelDrag;

  @FindBy(id = PROCESSES_BUTTON)
  WebElement processesBtn;

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

  @FindBy(id = PROCESSES_MAIN_AREA)
  WebElement processesMainArea;

  @FindBy(id = PLUS_ICON)
  WebElement plusMenuBtn;

  @FindBy(id = SERVER_MENU_ITEM)
  WebElement serverMenuItem;

  @FindBy(id = TERMINAL_MENU_ITEM)
  WebElement terminalMenuItem;

  @FindBy(id = SERVER_INFO_TABLE_CAPTION)
  WebElement serverInfoTableCaption;

  @FindBy(id = SERVER_INFO_HIDE_INTERNAL_CHECK_BOX)
  WebElement serverInfoHideInternalCheckBox;

  @FindBy(id = COMMANDS_MENU_ITEM)
  WebElement commandsMenuItem;

  @FindBy(id = SERVERS_MENU_ITEM)
  WebElement serversMenuItem;

  /**
   * click on consoles icon in side line and wait opening console area (terminal on other console )
   */
  public void clickOnProcessesButton() {
    redrawDriverWait.until(visibilityOf(processesBtn)).click();
  }

  /** click on consoles tab in bottom and wait opening console area (terminal on other console ) */
  public void clickOnDebugTab() {
    redrawDriverWait.until(visibilityOf(debugTab)).click();
  }

  /** click on preview url link */
  public void clickOnPreviewUrl() {
    loadPageDriverWait.until(visibilityOf(previewUrl)).click();
  }

  public void clickOnServerItemInContextMenu() {
    redrawDriverWait.until(visibilityOf(serverMenuItem)).click();
  }

  public void clickOnTerminalItemInContextMenu() {
    redrawDriverWait.until(visibilityOf(terminalMenuItem)).click();
  }

  public void clickOnPlusMenuButton() {
    redrawDriverWait.until(visibilityOf(plusMenuBtn));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(plusMenuBtn).click().perform();
  }

  public void clickOnHideInternalServers() {
    redrawDriverWait.until(visibilityOf(serverInfoHideInternalCheckBox)).click();
  }

  public void waitExpectedTextIntoServerTableCation(String expectedText) {
    updateProjDriverWait.until(textToBePresentInElement(serverInfoTableCaption, expectedText));
  }

  public void checkReferenceList(String id, String expectedText) {
    redrawDriverWait.until(textToBePresentInElementLocated(By.id(id), expectedText));
  }

  public void waitReferenceIsNotPresent(String referenceId) {
    redrawDriverWait.until(invisibilityOfElementLocated(By.id(referenceId)));
  }

  public boolean checkThatServerExists(String serverName) {
    List<WebElement> webElements =
        loadPageDriverWait.until(
            visibilityOfAllElementsLocatedBy(
                By.xpath("//div[contains(@id, 'runtime-info-reference-')]")));

    for (WebElement we : webElements) {
      if (we.getText().equals(serverName)) {
        return true;
      }
    }

    return false;
  }

  public void waitExpectedTextIntoPreviewUrl(String expectedText) {
    redrawDriverWait.until(textToBePresentInElement(previewUrl, expectedText));
  }

  /** Get url for preview. */
  public String getPreviewUrl() {
    return loadPageDriverWait.until(visibilityOf(previewUrl)).getText();
  }

  /** Wait until url for preview is responsive, that is requesting by url returns Status.OK. */
  public void waitPreviewUrlIsResponsive(int timeoutInSec) {
    webDriverWaitFactory
        .get(timeoutInSec)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  try {
                    return (HttpUtil.getUrlResponseCode(getPreviewUrl())
                        == Status.OK.getStatusCode());
                  } catch (IOException ex) {
                    LOG.warn(format("Preview URL %s is still inaccessible.", getPreviewUrl()));
                    return false;
                  }
                });
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
        .until(visibilityOfElementLocated(By.xpath(format(CLOSE_PROCESS_ICON, nameProcess))))
        .click();
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessInProcessConsoleTree(String nameProcess) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(By.xpath(format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   * @param timeout is timeout in seconds defined by user
   */
  public void waitProcessInProcessConsoleTree(String nameProcess, int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(visibilityOfElementLocated(By.xpath(format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * wait process name is not present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessIsNotPresentInProcessConsoleTree(String nameProcess) {
    loadPageDriverWait.until(
        invisibilityOfElementLocated(By.xpath(format(PROCESS_NAME_XPATH, nameProcess))));
  }

  /**
   * select process into 'Process' console tree by name
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void selectProcessInProcessConsoleTreeByName(String nameProcess) {
    loadPageDriverWait
        .until(visibilityOfElementLocated(By.xpath(format(PROCESS_NAME_XPATH, nameProcess))))
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
        .until(visibilityOfElementLocated(By.xpath(format(TAB_PROCESS_NAME, tabNameProcess))))
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
        visibilityOfElementLocated(By.xpath(format(TAB_PROCESS_NAME, tabNameProcess))));
  }

  /**
   * wait tab name of process is not present
   *
   * @param tabNameProcess is tab name of process
   */
  public void waitTabNameProcessIsNotPresent(String tabNameProcess) {
    redrawDriverWait.until(
        invisibilityOfElementLocated(By.xpath(format(TAB_PROCESS_NAME, tabNameProcess))));
  }

  /**
   * close process by tab name
   *
   * @param tabNameProcess is tab name of process
   */
  public void closeProcessByTabName(String tabNameProcess) {
    redrawDriverWait
        .until(elementToBeClickable(By.xpath(format(TAB_PROCESS_CLOSE_ICON, tabNameProcess))))
        .click();
  }

  /** click on the 'Hide' icon of the 'Processes' */
  public void closeProcessesArea() {
    loadPageDriverWait.until(visibilityOf(hideConsolesIcon)).click();
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
    new WebDriverWait(seleniumWebDriver, definedTimeout).until(visibilityOf(consoleContainer));
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
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(visibilityOf(previewUrl));
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

  public void dragConsolesInDefinePosition(int verticalShiftInPixels) {
    WebElement drag = redrawDriverWait.until(visibilityOf(consolesPanelDrag));
    new Actions(seleniumWebDriver)
        .dragAndDropBy(drag, verticalShiftInPixels, verticalShiftInPixels)
        .perform();
  }

  public void openServersTabFromContextMenu(String machineName) {
    Wait<WebDriver> wait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(ELEMENT_TIMEOUT_SEC, SECONDS)
            .pollingEvery(500, MILLISECONDS)
            .ignoring(WebDriverException.class);

    WebElement machine =
        wait.until(visibilityOfElementLocated(By.xpath(format(MACHINE_NAME, machineName))));
    wait.until(visibilityOf(machine)).click();

    actionsFactory.createAction(seleniumWebDriver).moveToElement(machine).contextClick().perform();
    redrawDriverWait.until(visibilityOf(serversMenuItem)).click();
  }

  public void startCommandFromProcessesArea(
      String machineName, ContextMenuCommandGoals commandGoal, String commandName) {
    WebElement machine =
        redrawDriverWait.until(
            visibilityOfElementLocated(By.xpath(format(MACHINE_NAME, machineName))));
    machine.click();

    actionsFactory.createAction(seleniumWebDriver).moveToElement(machine).contextClick().perform();
    redrawDriverWait.until(visibilityOf(commandsMenuItem)).click();

    redrawDriverWait.until(visibilityOfElementLocated(By.id(commandGoal.get()))).click();
    redrawDriverWait
        .until(visibilityOfElementLocated(By.xpath(format(COMMAND_NAME, commandName))))
        .click();
  }

  public void startTerminalFromProcessesArea(String machineName) {
    WebElement machine =
        redrawDriverWait.until(
            visibilityOfElementLocated(By.xpath(format(MACHINE_NAME, machineName))));

    actionsFactory.createAction(seleniumWebDriver).moveToElement(machine).contextClick().perform();
    clickOnTerminalItemInContextMenu();
  }

  public void clickOnClearOutputButton() {
    loadPageDriverWait
        .until(visibilityOfElementLocated(By.id("gwt-debug-terminal_clear_output")))
        .click();
  }

  public void executeCommandInsideProcessesArea(
      String machineName,
      ContextMenuCommandGoals goal,
      String commandName,
      String expectedMessageInTerminal) {
    startCommandFromProcessesArea(machineName, goal, commandName);
    waitTabNameProcessIsPresent(commandName);
    waitProcessInProcessConsoleTree(commandName);
    waitExpectedTextIntoConsole(expectedMessageInTerminal, PREPARING_WS_TIMEOUT_SEC);
  }

  // Click on preview url and check visibility of web element on opened page
  public void checkWebElementVisibilityAtPreviewPage(By webElement) {
    String currentWindow = seleniumWebDriver.getWindowHandle();

    waitPreviewUrlIsPresent();
    waitPreviewUrlIsResponsive(10);
    clickOnPreviewUrl();

    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    seleniumWebDriverHelper.waitVisibility(webElement, LOADER_TIMEOUT_SEC);

    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(currentWindow);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
  }

  // Start command from project context menu and check expected message in Console
  public void executeCommandFromProjectExplorer(
      String projectName,
      ContextMenuCommandGoals goal,
      String commandName,
      String expectedMessageInTerminal) {
    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.invokeCommandWithContextMenu(goal, projectName, commandName);

    waitTabNameProcessIsPresent(commandName);
    waitProcessInProcessConsoleTree(commandName);
    waitExpectedTextIntoConsole(expectedMessageInTerminal, PREPARING_WS_TIMEOUT_SEC);
  }

  public void closeProcessTabWithAskDialog(String tabName) {
    String message =
        format(
            "The process %s will be terminated after closing console. Do you want to continue?",
            tabName);
    waitProcessInProcessConsoleTree(tabName);
    closeProcessByTabName(tabName);
    askDialog.acceptDialogWithText(message);
    waitProcessIsNotPresentInProcessConsoleTree(tabName);
  }
}
