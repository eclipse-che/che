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
package org.eclipse.che.selenium.pageobject.intelligent;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Sergey Skorik */
@Singleton
public class CommandsPalette {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final WebDriverWait redrawUiElementTimeout;
  private final Actions actions;

  @Inject
  public CommandsPalette(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.redrawUiElementTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.actions = new Actions(seleniumWebDriver);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public enum MeasuresMemory {
    MB,
    GB
  }

  public enum MoveTypes {
    UP,
    DOWN
  }

  /** Class introduce Xpath locators for DOM navigation inside CommandsPalette widget */
  private static final class Locators {
    static final String COMMAND_PALETTE = "//div[@id='gwt-debug-commands_palette']";
    static final String COMMAND_PALETTE_BUTTON =
        "//div[@id='gwt-debug-button-open_command_palette']";
    static final String CLOSE_COMMAND_PALETTE =
        "//div[contains(text(), 'Commands Palette')]//parent::div//parent::div//div[2]";
    static final String SEARCH_FIELD = "//input[@id='gwt-debug-commands_palette-filter']";
    static final String COMMANDS_LIST =
        "//div[@id = 'gwt-debug-commands_palette']//div[contains(@id,'gwt-uid')]";
    static final String COMMANDS =
        "//div[@id = 'gwt-debug-commands_palette']//div[contains(text(),'%s')]";
  }

  @FindBy(xpath = Locators.COMMAND_PALETTE)
  WebElement commandPalette;

  @FindBy(xpath = Locators.COMMAND_PALETTE_BUTTON)
  WebElement commandPaletteButton;

  @FindBy(xpath = Locators.CLOSE_COMMAND_PALETTE)
  WebElement closeCommandPalette;

  @FindBy(xpath = Locators.SEARCH_FIELD)
  WebElement searchField;

  /** Start CommandPalette widget by clicking on button */
  public void openCommandPalette() {
    loader.waitOnClosed();
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(commandPaletteButton)).click();
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(commandPalette));
  }

  /** Start CommandPalette widget by Shift+F10 hot keys */
  public void openCommandPaletteByHotKeys() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(Keys.SHIFT.toString(), Keys.F10.toString())
        .perform();
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(commandPalette));
  }

  /** Check the CommandsPalette widget is started */
  public void waitCommandPalette() {
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(commandPalette));
  }

  public void closeCommandPalette() {
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(closeCommandPalette)).click();
    redrawUiElementTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.COMMAND_PALETTE)));
    loader.waitOnClosed();
  }

  // TODO Get a commands list from the CommandsPalette
  private ArrayList commandsList() {
    ArrayList list = new ArrayList();
    list = (ArrayList) seleniumWebDriver.findElements(By.xpath(Locators.COMMANDS_LIST));

    return list;
  }

  /**
   * Search and start command by name
   *
   * @param commandName name of the command
   */
  public void searchAndStartCommand(String commandName) {
    redrawUiElementTimeout
        .until(ExpectedConditions.visibilityOf(searchField))
        .sendKeys(commandName);
    loader.waitOnClosed();
  }

  public void clearSearchField() {
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(searchField)).clear();
    redrawUiElementTimeout.until(ExpectedConditions.visibilityOf(searchField)).sendKeys(Keys.ENTER);
    loader.waitOnClosed();
  }

  public void commandIsExists(String commandName) {
    redrawUiElementTimeout.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(format(Locators.COMMANDS, commandName))));
  }

  public void commandIsNotExists(String commandName) {
    redrawUiElementTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(format(Locators.COMMANDS, commandName))));
  }

  /**
   * Start the command by doubleClick
   *
   * @param commandName name of the command
   */
  public void startCommandByDoubleClick(String commandName) {
    String locatorToCurrentCommand = format(Locators.COMMANDS, commandName);
    WebElement currentCommand =
        redrawUiElementTimeout.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorToCurrentCommand)));
    actions.doubleClick(currentCommand).perform();
    redrawUiElementTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.xpath(locatorToCurrentCommand)));
  }

  /**
   * Start the command by pressing Enter key
   *
   * @param commandName name of the command
   */
  public void startCommandByEnterKey(String commandName) {
    String locatorToCurrentCommand = format(Locators.COMMANDS, commandName);
    WebElement currentCommand =
        redrawUiElementTimeout.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorToCurrentCommand)));
    seleniumWebDriver.findElement(By.xpath(locatorToCurrentCommand)).click();
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(currentCommand, Keys.ENTER.toString())
        .perform();
    redrawUiElementTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.xpath(locatorToCurrentCommand)));
  }

  /**
   * Search and start command by name
   *
   * @param mt where to move(down or up)
   * @param steps
   */
  public void moveAndStartCommand(MoveTypes mt, int steps) {
    Keys k = mt.equals(MoveTypes.UP) ? Keys.ARROW_UP : Keys.ARROW_DOWN;

    for (int i = 0; i < steps; i++) {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(k.toString()).perform();
      loader.waitOnClosed();
    }
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER.toString()).perform();
  }
}
