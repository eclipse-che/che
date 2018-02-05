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
package org.eclipse.che.selenium.pageobject.machineperspective;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MachineTerminal {
  private static final Logger LOG = LoggerFactory.getLogger(MachineTerminal.class);
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final CommandsPalette commandsPalette;
  private final TestCommandServiceClient commandServiceClient;
  private final Consoles consoles;

  @Inject
  public MachineTerminal(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      CommandsPalette commandsPalette,
      TestCommandServiceClient commandServiceClient,
      Consoles consoles) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.commandsPalette = commandsPalette;
    this.commandServiceClient = commandServiceClient;
    this.consoles = consoles;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static String LINE_HIGHLIGHTED_GREEN = "rgba(6, 152, 154, 1)";

  private interface Locators {
    String TERMINAL_CONSOLE_CONTAINER_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal') and @active]/div[2]";
    String TERMINAL_DEFAULT_TAB_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal')]"
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='Terminal']";
    String TERMINAL_TAB_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal')]"
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[contains(text(),'Terminal%s')]";
    String TERMINAL_FOCUS_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal') and @active]"
            + "//div[contains(@class, 'terminal xterm xterm-theme-default focus')]";
  }

  @FindBy(xpath = Locators.TERMINAL_DEFAULT_TAB_XPATH)
  WebElement defaultTermTab;

  @FindBy(xpath = Locators.TERMINAL_CONSOLE_CONTAINER_XPATH)
  WebElement defaultTermContainer;

  /** waits default terminal tab */
  public void waitTerminalTab() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC).until(visibilityOf(defaultTermTab));
  }

  /**
   * waits default terminal tab
   *
   * @param timeWait time of waiting terminal container in seconds
   */
  public void waitTerminalTab(int timeWait) {
    new WebDriverWait(seleniumWebDriver, timeWait).until(visibilityOf(defaultTermTab));
  }

  /**
   * waits terminal tab with number
   *
   * @param termNumber number of terminal
   */
  public void waitNumberTerminalTab(int termNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(getTerminalTabXPath(termNumber))));
  }

  /** waits appearance the main terminal container */
  public void waitTerminalConsole() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTermContainer));
  }

  public boolean terminalIsPresent() {
    return seleniumWebDriver.findElements(By.xpath(Locators.TERMINAL_DEFAULT_TAB_XPATH)).size() > 0;
  }

  public void waitTerminalIsNotPresent(int termNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(getTerminalTabXPath(termNumber))));
  }

  /**
   * waits appearance the main terminal container
   *
   * @param timeWait time of waiting terminal container in seconds
   */
  public void waitTerminalConsole(int timeWait) {
    new WebDriverWait(seleniumWebDriver, timeWait).until(visibilityOf(defaultTermContainer));
  }

  /** gets visible text from terminal container */
  public String getVisibleTextFromTerminal() {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTermContainer))
        .getText();
  }

  /**
   * waits text into the terminal
   *
   * @param expectedText expected text into terminal
   */
  public void waitExpectedTextIntoTerminal(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromTerminal().contains(expectedText));
  }

  /**
   * waits expected text is not present in the terminal
   *
   * @param expectedText expected text
   */
  public void waitExpectedTextNotPresentTerminal(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> !(getVisibleTextFromTerminal().contains(expectedText)));
  }

  /**
   * waits text into the terminal
   *
   * @param expectedText expected text into terminal
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitExpectedTextIntoTerminal(String expectedText, int definedTimeout) {
    new WebDriverWait(seleniumWebDriver, definedTimeout)
        .until((WebDriver input) -> getVisibleTextFromTerminal().contains(expectedText));
  }

  public void waitTerminalIsNotEmpty() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromTerminal().length() > 0);
  }

  public void selectFocusToActiveTerminal() {
    waitTerminalConsole();

    if (seleniumWebDriver.findElements(By.xpath(Locators.TERMINAL_FOCUS_XPATH)).size() == 0) {
      defaultTermContainer.click();
    }
  }

  /**
   * send user information into active terminal console
   *
   * @param command the user info.
   */
  public void typeIntoTerminal(String command) {
    selectFocusToActiveTerminal();
    defaultTermContainer.findElement(By.tagName("textarea")).sendKeys(command);
    loader.waitOnClosed();
  }

  /**
   * Types and executes given command in terminal.
   *
   * @param command text to run in terminal
   */
  public void sendCommandIntoTerminal(String command) {
    selectFocusToActiveTerminal();
    defaultTermContainer
        .findElement(By.tagName("textarea"))
        .sendKeys(command + Keys.ENTER.toString());
    loader.waitOnClosed();
  }

  /** select default terminal tab */
  public void selectTerminalTab() {
    waitTerminalTab();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(defaultTermTab))
        .click();
  }

  /**
   * scroll terminal by pressing key 'End'
   *
   * @param item is the name of the highlighted item
   */
  public void moveDownListTerminal(String item) {
    loader.waitOnClosed();
    typeIntoTerminal(Keys.END.toString());
    WaitUtils.sleepQuietly(2);

    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));

    if (!element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN)) {
      typeIntoTerminal(Keys.ARROW_UP.toString());
      element =
          seleniumWebDriver.findElement(
              By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
      WaitUtils.sleepQuietly(2);
    }
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    WebElement finalElement = element;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                finalElement.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  /**
   * scroll terminal by pressing key 'PageDown'
   *
   * @param item is the name of the highlighted item
   */
  public void movePageDownListTerminal(String item) {
    loader.waitOnClosed();
    typeIntoTerminal(Keys.PAGE_DOWN.toString());
    WaitUtils.sleepQuietly(2);
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  /**
   * scroll terminal by pressing key 'PageUp'
   *
   * @param item is the name of the highlighted item
   */
  public void movePageUpListTerminal(String item) {
    loader.waitOnClosed();
    typeIntoTerminal(Keys.PAGE_UP.toString());
    WaitUtils.sleepQuietly(2);
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  /**
   * scroll terminal by pressing key 'Home'
   *
   * @param item is the name of the highlighted item
   */
  public void moveUpListTerminal(String item) {
    loader.waitOnClosed();
    typeIntoTerminal(Keys.HOME.toString());
    WaitUtils.sleepQuietly(2);
    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(element));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver input) ->
                element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN));
  }

  private String getTerminalTabXPath(int terminalNumber) {
    return terminalNumber == 1
        ? format(Locators.TERMINAL_TAB_XPATH, "")
        : format(Locators.TERMINAL_TAB_XPATH, "-" + terminalNumber);
  }

  // TODO should be removed after fixing: https://github.com/eclipse/che/issues/8105
  // this auxiliary method for investigate problem that was described in the issue:
  // https://github.com/eclipse/che/issues/8105
  public void launchScriptAndGetInfo(
      TestWorkspace ws, String currentProject, TestProjectServiceClient testProjectServiceClient) {
    try {
      String ideCommnandName = "checkApp";
      String bashFileName = "check-app-state.sh";
      String terminalCommandForCheckResult =
          String.format(
              "cd /projects/%s && chmod +x %s && ./%s", currentProject, bashFileName, bashFileName);
      commandServiceClient.createCommand(
          terminalCommandForCheckResult, ideCommnandName, CUSTOM, ws.getId());
      String bashScript =
          "#!/bin/bash\n"
              + "\n"
              + "URL=$1\n"
              + "pid=$(pgrep -f \"user/tomcat8\")\n"
              + "echo \"PID: $pid\"\n"
              + "\n"
              + "test() {\n"
              + "    app_content=$(curl -s $1)\n"
              + "    if [[ $app_content == *\"hello\"* ]];then\n"
              + "        echo \"test passed with $1\"\n"
              + "    else\n"
              + "        echo \"test failed with $1\"\n"
              + "    fi\n"
              + "}";

      testProjectServiceClient.createFileInProject(
          ws.getId(), currentProject, bashFileName, bashScript);
      seleniumWebDriver.navigate().refresh();
      commandsPalette.openCommandPalette();
      commandsPalette.startCommandByDoubleClick(ideCommnandName);
      consoles.waitExpectedTextIntoConsole("PID");
      String webAppPIDs = consoles.getVisibleTextFromCommandConsole();
      LOG.warn("@@@ The PID list from run Web application is: " + webAppPIDs);
    } catch (Exception ex) {
      LOG.error("@@@ Cannot catch the info about PID", ex);
    }
  }
}
