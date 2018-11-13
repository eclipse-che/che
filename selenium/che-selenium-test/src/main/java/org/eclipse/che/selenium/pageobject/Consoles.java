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
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response.Status;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.utils.HttpUtil;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Aleksandr Shmaraev */
@Singleton
public class Consoles {

  private final WebDriverWait redrawDriverWait;
  private final WebDriverWait loadPageDriverWait;
  private final WebDriverWait updateProjDriverWait;
  private final WebDriverWait appStartDriverWait;
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
  public static final String COMMAND_CONSOLE_XPATH =
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
  public static final Pattern LANGUAGE_SERVER_UPDATE_MESSAGE =
      Pattern.compile(
          "Workspace updated. Result code: '0', message: 'OK'. Added projects: '\\[([^\\n\\r\\]]*)\\]', removed projects: ");
  public static final String JAVA_LANGUAGE_SERVER_STARTED =
      "Starting: 100% Starting Java Language Server";

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
    appStartDriverWait = new WebDriverWait(seleniumWebDriver, APPLICATION_START_TIMEOUT_SEC);
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

  @FindBy(xpath = COMMAND_CONSOLE_XPATH)
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
    seleniumWebDriverHelper.waitAndClick(processesBtn);
  }

  /** click on consoles tab in bottom and wait opening console area (terminal on other console ) */
  public void clickOnDebugTab() {
    seleniumWebDriverHelper.waitAndClick(debugTab);
  }

  /** click on preview url link */
  public void clickOnPreviewUrl() {
    seleniumWebDriverHelper.waitAndClick(previewUrl);
  }

  public void clickOnServerItemInContextMenu() {
    seleniumWebDriverHelper.waitAndClick(serverMenuItem);
  }

  public void clickOnTerminalItemInContextMenu() {
    seleniumWebDriverHelper.waitAndClick(terminalMenuItem);
  }

  public void clickOnPlusMenuButton() {
    seleniumWebDriverHelper.waitVisibility(plusMenuBtn);
    seleniumWebDriverHelper.moveCursorToAndClick(plusMenuBtn);
  }

  public void clickOnHideInternalServers() {
    seleniumWebDriverHelper.waitAndClick(serverInfoHideInternalCheckBox);
  }

  public void waitExpectedTextIntoServerTableCation(String expectedText) {

    seleniumWebDriverHelper.waitTextContains(
        serverInfoTableCaption, expectedText, UPDATING_PROJECT_TIMEOUT_SEC);
  }

  public void checkReferenceList(String id, String expectedText) {
    seleniumWebDriverHelper.waitTextContains(By.id(id), expectedText);
  }

  public void waitReferenceIsNotPresent(String referenceId) {
    seleniumWebDriverHelper.waitInvisibility(By.id(referenceId));
  }

  public boolean checkThatServerExists(String serverName) {
    final String serversXpath = "//div[contains(@id, 'runtime-info-reference-')]";
    final String particularServerXpathTemplate = "(" + serversXpath + ")[%s]";
    final int serversCount =
        seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(serversXpath)).size();

    // finding each element directly before using helps avoid "StaleElementReferenceException"
    for (int i = 1; i <= serversCount; i++) {
      String particularServerXpath = String.format(particularServerXpathTemplate, i);
      String serverText =
          seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(particularServerXpath));

      if (serverName.equals(serverText)) {
        return true;
      }
    }

    return false;
  }

  public void waitExpectedTextIntoPreviewUrl(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(previewUrl, expectedText);
  }

  /** Get url for preview. */
  public String getPreviewUrl() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(previewUrl);
  }

  /** Wait until url for preview is responsive, that is requesting by url returns Status.OK. */
  public void waitPreviewUrlIsResponsive(int timeoutInSec) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          try {
            return (HttpUtil.getUrlResponseCode(getPreviewUrl()) == Status.OK.getStatusCode());
          } catch (IOException ex) {
            LOG.warn(format("Preview URL %s is still inaccessible.", getPreviewUrl()));
            return false;
          }
        },
        timeoutInSec);
  }

  /** click on the 'Close' icon of the terminal into "Consoles' area */
  public void closeTerminalIntoConsoles() {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(closeTerminal),
        StaleElementReferenceException.class);
  }

  /** open new terminal */
  public void openNewTerminalIntoProcesses() {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(openNewTerminal),
        StaleElementReferenceException.class);
  }

  /**
   * click on the 'Close' icon of the process into 'Processes' area
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void closeProcessInProcessConsoleTreeByName(String nameProcess) {
    final String closeIconXpath = format(CLOSE_PROCESS_ICON, nameProcess);
    seleniumWebDriverHelper.waitAndClick(By.xpath(closeIconXpath));
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessInProcessConsoleTree(String nameProcess) {
    waitProcessInProcessConsoleTree(nameProcess, LOAD_PAGE_TIMEOUT_SEC);
  }

  /**
   * wait that process is present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   * @param timeout is timeout in seconds defined by user
   */
  public void waitProcessInProcessConsoleTree(String nameProcess, int timeout) {
    final String processXpath = format(PROCESS_NAME_XPATH, nameProcess);

    seleniumWebDriverHelper.waitVisibility(By.xpath(processXpath), timeout);
  }

  /**
   * wait process name is not present into console 'Processes' tree
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void waitProcessIsNotPresentInProcessConsoleTree(String nameProcess) {
    final String processXpath = format(PROCESS_NAME_XPATH, nameProcess);

    seleniumWebDriverHelper.waitInvisibility(By.xpath(processXpath));
  }

  /**
   * select process into 'Process' console tree by name
   *
   * @param nameProcess is process into console 'Processes' tree
   */
  public void selectProcessInProcessConsoleTreeByName(String nameProcess) {
    final String processXpath = format(PROCESS_NAME_XPATH, nameProcess);

    seleniumWebDriverHelper.waitAndClick(By.xpath(processXpath));
  }

  /**
   * select process into console by tab name
   *
   * @param tabNameProcess is tab name of process
   */
  public void selectProcessByTabName(String tabNameProcess) {
    final String processTabXpath = format(TAB_PROCESS_NAME, tabNameProcess);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(By.xpath(processTabXpath)),
        StaleElementReferenceException.class);
  }

  /**
   * wait tab name of process is present
   *
   * @param tabNameProcess is tab name of process
   */
  public void waitTabNameProcessIsPresent(String tabNameProcess) {
    final String processTabXpath = format(TAB_PROCESS_NAME, tabNameProcess);

    seleniumWebDriverHelper.waitVisibility(By.xpath(processTabXpath));
  }

  /**
   * wait tab name of process is not present
   *
   * @param tabNameProcess is tab name of process
   */
  public void waitTabNameProcessIsNotPresent(String tabNameProcess) {
    final String processTabXpath = format(TAB_PROCESS_NAME, tabNameProcess);

    seleniumWebDriverHelper.waitInvisibility(By.xpath(processTabXpath));
  }

  /**
   * close process by tab name
   *
   * @param tabNameProcess is tab name of process
   */
  public void closeProcessByTabName(String tabNameProcess) {
    final String tabProcessCloseIconXpath = format(TAB_PROCESS_CLOSE_ICON, tabNameProcess);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(By.xpath(tabProcessCloseIconXpath)),
        StaleElementReferenceException.class);
  }

  /** click on the 'Hide' icon of the 'Processes' */
  public void closeProcessesArea() {
    seleniumWebDriverHelper.waitAndClick(hideConsolesIcon);
  }

  /** click on open ssh terminal button in the consoles widget and wait opening terminal */
  public void clickOnOpenSshTerminalBtn() {
    seleniumWebDriverHelper.waitAndClick(sshTerminalBtn);
    waitIsCommandConsoleOpened();
  }

  /** wait opening 'Command console widget' */
  public void waitIsCommandConsoleOpened() {
    seleniumWebDriverHelper.waitVisibility(consoleContainer);
  }

  /**
   * wait opening 'Command console widget'
   *
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitIsCommandConsoleOpened(int definedTimeout) {
    seleniumWebDriverHelper.waitVisibility(consoleContainer, definedTimeout);
  }

  /** wait expected text into 'Command console' */
  public void waitExpectedTextIntoConsole(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(
        consoleContainer, expectedText, UPDATING_PROJECT_TIMEOUT_SEC);

    updateProjDriverWait.until(textToBePresentInElement(consoleContainer, expectedText));
  }

  /** wait on "Starting: 100% Starting Java Language Server" message appears in console */
  public void waitJDTLSStartedMessage() {
    try {
      waitExpectedTextIntoConsole(JAVA_LANGUAGE_SERVER_STARTED);
    } catch (TimeoutException e) {
      LOG.warn("timed out waiting for java language server to start");
    }
  }

  /** wait JDT LS message about project is updated */
  public void waitJDTLSProjectResolveFinishedMessage(String... projects) {
    List<String> projectStrings = new ArrayList<>(projects.length);
    for (String project : projects) {
      projectStrings.add("file:///projects/" + project);
    }
    try {

      appStartDriverWait.until(
          (Predicate<WebDriver>)
              webdriver -> {
                ArrayList<String> projectsCopy = new ArrayList<>(projectStrings);
                String text = consoleContainer.getText();
                Matcher matcher = LANGUAGE_SERVER_UPDATE_MESSAGE.matcher(text);
                while (matcher.find()) {
                  String addedProjects = matcher.group(1);
                  Iterator<String> iter = projectsCopy.iterator();
                  while (iter.hasNext()) {
                    if (addedProjects.contains(iter.next())) {
                      iter.remove();
                    }
                  }
                  if (projectsCopy.isEmpty()) {
                    return true;
                  }
                }
                return false;
              });
    } catch (TimeoutException e) {
      LOG.warn("timed out waiting for resolving projects {}", (Object[]) projects);
    }
    // once a new project has been added to the workspace, stuff in the project
    // explorer will be updated, for example showing packages instead of folders
    // wait for this to be finished
    loader.waitOnClosed();
  }

  /** get visible text from command console */
  public String getVisibleTextFromCommandConsole() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(consoleContainer);
  }

  /**
   * wait expected text into 'Command console'
   *
   * @param expectedText expected message in console
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitExpectedTextIntoConsole(String expectedText, int definedTimeout) {
    seleniumWebDriverHelper.waitTextContains(consoleContainer, expectedText, definedTimeout);
  }

  public void waitEmptyConsole(int timeout) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> {
          String currentText =
              seleniumWebDriverHelper.waitPresence(By.xpath(COMMAND_CONSOLE_XPATH)).getText();

          return "".equals(currentText);
        });
  }

  /** wait a preview url into 'Consoles' */
  public void waitPreviewUrlIsPresent() {
    seleniumWebDriverHelper.waitVisibility(previewUrl, ELEMENT_TIMEOUT_SEC);
  }

  /** wait a preview url is not present into 'Consoles' */
  public void waitPreviewUrlIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PREVIEW_URL));
  }

  /** return state open/or close of processes main widget */
  public boolean processesMainAreaIsOpen() {
    return seleniumWebDriverHelper.isVisible(processesMainArea);
  }

  /** click on the maximize panel icon */
  public void clickOnMaximizePanelIcon() {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(By.xpath(MAXIMIZE_PANEL_ICON)),
        StaleElementReferenceException.class);
  }

  public void dragConsolesInDefinePosition(int verticalShiftInPixels) {
    seleniumWebDriverHelper
        .getAction()
        .dragAndDropBy(
            seleniumWebDriverHelper.waitVisibility(consolesPanelDrag),
            verticalShiftInPixels,
            verticalShiftInPixels)
        .perform();
  }

  public void openServersTabFromContextMenu(String machineName) {
    final String machineXpath = format(MACHINE_NAME, machineName);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> {
          seleniumWebDriverHelper.waitAndClick(By.xpath(machineXpath));
        },
        WebDriverException.class);

    seleniumWebDriverHelper.moveCursorToAndContextClick(By.xpath(machineXpath));

    seleniumWebDriverHelper.waitNoExceptions(
        () -> {
          seleniumWebDriverHelper.waitAndClick(serversMenuItem);
        },
        StaleElementReferenceException.class);
  }

  public void startCommandFromProcessesArea(
      String machineName, ContextMenuCommandGoals commandGoal, String commandName) {

    final String machineXpath = format(MACHINE_NAME, machineName);
    final String commandXpath = format(COMMAND_NAME, commandName);

    seleniumWebDriverHelper.waitAndClick(By.xpath(machineXpath));
    seleniumWebDriverHelper.moveCursorToAndContextClick(By.xpath(machineXpath));
    seleniumWebDriverHelper.waitAndClick(commandsMenuItem);
    seleniumWebDriverHelper.waitAndClick(By.id(commandGoal.get()));
    seleniumWebDriverHelper.waitAndClick(By.xpath(commandXpath));
  }

  public void executeCommandFromProcessesArea(
      String machineName,
      ContextMenuCommandGoals commandGoal,
      String commandName,
      String expectedMessageInConsole) {
    startCommandFromProcessesArea(machineName, commandGoal, commandName);
    waitExpectedTextIntoConsole(expectedMessageInConsole);
  }

  public void startTerminalFromProcessesArea(String machineName) {
    final String machineXpath = format(MACHINE_NAME, machineName);

    seleniumWebDriverHelper.moveCursorToAndContextClick(By.xpath(machineXpath));
    clickOnTerminalItemInContextMenu();
  }

  public void clickOnClearOutputButton() {
    seleniumWebDriverHelper.waitAndClick(By.id("gwt-debug-terminal_clear_output"));
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

    // wait for 5 sec to prevent "Application is not available" error
    WaitUtils.sleepQuietly(5);
    clickOnPreviewUrl();

    seleniumWebDriverHelper.switchToNextWindow(currentWindow);

    try {
      seleniumWebDriverHelper.waitVisibility(webElement, LOADER_TIMEOUT_SEC);
    } finally {
      seleniumWebDriver.close();
      seleniumWebDriver.switchTo().window(currentWindow);
      seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    }
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
    waitExpectedTextIntoConsole(expectedMessageInTerminal, APPLICATION_START_TIMEOUT_SEC);
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
