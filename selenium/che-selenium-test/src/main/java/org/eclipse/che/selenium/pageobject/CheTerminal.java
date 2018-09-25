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
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
public class CheTerminal {
  private static final Logger LOG = LoggerFactory.getLogger(CheTerminal.class);
  public static final String TERMINAL = "Terminal";
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final CommandsPalette commandsPalette;
  private final TestCommandServiceClient commandServiceClient;
  private final Consoles consoles;

  @Inject
  public CheTerminal(
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
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='"
            + TERMINAL
            + "']";
    String TERMINAL_TAB_XPATH_TEMPLATE =
        "//div[contains(@id,'gwt-debug-Terminal')]"
            + "/preceding::div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[contains(text(),'%s')]";
    String TERMINAL_FOCUS_XPATH =
        "//div[contains(@id,'gwt-debug-Terminal') and @active]"
            + "//div[contains(@class, 'terminal xterm enable-mouse-events focus')]";
  }

  @FindBy(xpath = Locators.TERMINAL_DEFAULT_TAB_XPATH)
  WebElement defaultTerminalTab;

  @FindBy(xpath = Locators.TERMINAL_CONSOLE_CONTAINER_XPATH)
  WebElement defaultTerminalContainer;

  /** waits default terminal tab */
  public void waitFirstTerminalTab() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOf(defaultTerminalTab));
  }

  /**
   * waits default terminal tab
   *
   * @param timeWait time of waiting terminal container in seconds
   */
  public void waitFirstTerminalTab(int timeWait) {
    new WebDriverWait(seleniumWebDriver, timeWait).until(visibilityOf(defaultTerminalTab));
  }

  /**
   * waits terminal tab with number
   *
   * @param terminalNumber number of terminal
   */
  public void waitTerminalTab(int terminalNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(getTerminalTabXPath(terminalNumber))));
  }

  /** waits appearance the main terminal container */
  public void waitTerminalConsole() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(defaultTerminalContainer));
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
    new WebDriverWait(seleniumWebDriver, timeWait).until(visibilityOf(defaultTerminalContainer));
  }

  /**
   * gets visible text from terminal container where lines divided by "/n" delimiter
   *
   * @param terminalNumber name of terminal to get text from
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public String getVisibleTextFromTerminal(int terminalNumber) {
    String jsCommandToReadVisibleTextFromTerminal =
        String.format(
            "return IDE.TerminalContentProvider.getVisibleText('%s', 'dev-machine')",
            getTerminalTitle(terminalNumber));
    List<String> lines =
        (List<String>) seleniumWebDriver.executeScript(jsCommandToReadVisibleTextFromTerminal);

    if (lines == null) {
      return "";
    }

    return String.join("/n", lines);
  }

  private String getTerminalTitle(int terminalNumber) {
    return terminalNumber == 1 ? TERMINAL : format("%s-%s", TERMINAL, terminalNumber);
  }

  /** gets visible text from first terminal container where lines divided by "/n" delimiter */
  public String getVisibleTextFromFirstTerminal() {
    return getVisibleTextFromTerminal(1);
  }

  /**
   * waits text into the terminal
   *
   * @param terminalNumber
   * @param expectedText expected text into terminal
   */
  public void waitTextInTerminal(int terminalNumber, String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver input) -> getVisibleTextFromTerminal(terminalNumber).contains(expectedText));
  }

  /**
   * waits text in the first terminal
   *
   * @param expectedText expected text into terminal
   */
  public void waitTextInFirstTerminal(String expectedText) {
    waitTextInTerminal(1, expectedText);
  }

  /**
   * waits expected text is not present in the terminal
   *
   * @param expectedText expected text
   */
  public void waitNoTextInFirstTerminal(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> !(getVisibleTextFromFirstTerminal().contains(expectedText)));
  }

  /**
   * waits text into the terminal
   *
   * @param expectedText expected text into terminal
   * @param definedTimeout timeout in seconds defined with user
   */
  public void waitTextInTerminal(String expectedText, int definedTimeout) {
    new WebDriverWait(seleniumWebDriver, definedTimeout)
        .until((WebDriver input) -> getVisibleTextFromFirstTerminal().contains(expectedText));
  }

  public void waitFirstTerminalIsNotEmpty() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromFirstTerminal().length() > 0);
  }

  public void waitTerminalIsNotEmpty(int terminalNumber) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver input) -> getVisibleTextFromTerminal(terminalNumber).length() > 0);
  }

  public void selectFocusToActiveTerminal() {
    waitTerminalConsole();

    if (seleniumWebDriver.findElements(By.xpath(Locators.TERMINAL_FOCUS_XPATH)).size() == 0) {
      defaultTerminalContainer.click();
    }
  }

  /**
   * send user information into active terminal console
   *
   * @param command the user info.
   */
  public void typeIntoActiveTerminal(String command) {
    selectFocusToActiveTerminal();
    defaultTerminalContainer.findElement(By.tagName("textarea")).sendKeys(command);
    loader.waitOnClosed();
  }

  /**
   * Types and executes given command in terminal.
   *
   * @param command text to run in terminal
   */
  public void sendCommandIntoTerminal(String command) {
    selectFocusToActiveTerminal();
    defaultTerminalContainer
        .findElement(By.tagName("textarea"))
        .sendKeys(command + Keys.ENTER.toString());
    loader.waitOnClosed();
  }

  /** select first terminal tab */
  public void selectFirstTerminalTab() {
    waitFirstTerminalTab();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(elementToBeClickable(defaultTerminalTab))
        .click();
  }

  /**
   * scroll terminal by pressing key 'End'
   *
   * @param item is the name of the highlighted item
   */
  public void moveDownListTerminal(String item) {
    loader.waitOnClosed();
    typeIntoActiveTerminal(Keys.END.toString());
    WaitUtils.sleepQuietly(2);

    WebElement element =
        seleniumWebDriver.findElement(
            By.xpath(format("(//span[contains(text(), '%s')])[position()=1]", item)));

    if (!element.getCssValue("background-color").equals(LINE_HIGHLIGHTED_GREEN)) {
      typeIntoActiveTerminal(Keys.ARROW_UP.toString());
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
    typeIntoActiveTerminal(Keys.PAGE_DOWN.toString());
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
    typeIntoActiveTerminal(Keys.PAGE_UP.toString());
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
    return format(Locators.TERMINAL_TAB_XPATH_TEMPLATE, getTerminalTitle(terminalNumber));
  }

  public void logApplicationInfo(String projectName, TestWorkspace ws) {
    try {
      String getApplicationInfoCommand = format("ps -up $(pgrep -f \"%s\" | head -1)", projectName);
      String getApplicationInfoCommandName = "getApplicationInfo";

      commandServiceClient.createCommand(
          getApplicationInfoCommand, getApplicationInfoCommandName, CUSTOM, ws.getId());
      seleniumWebDriver.navigate().refresh();
      commandsPalette.openCommandPalette();
      commandsPalette.startCommandByDoubleClick(getApplicationInfoCommandName);

      String applicationInfo = consoles.getVisibleTextFromCommandConsole();
      LOG.warn("@@@ Web application info: {}", applicationInfo);
    } catch (Exception ex) {
      LOG.error("@@@ Cannot catch the info about application.", ex);
    }
  }
}
