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
package org.eclipse.che.selenium.pageobject.intelligent;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.DEPLOY_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.TEST_GOAL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Igor Ohrimenko */
@Singleton
public class CommandsExplorer {
  private final SeleniumWebDriver seleniumWebDriver;
  private final AskDialog askDialog;
  private final CommandsEditor commandsEditor;
  private final Loader loader;

  @Inject
  public CommandsExplorer(
      SeleniumWebDriver seleniumWebDriver,
      AskDialog askDialog,
      CommandsEditor commandsEditor,
      Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.askDialog = askDialog;
    this.commandsEditor = commandsEditor;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Describe Xpath locators for Commands Explore widget */
  private static class Locators {
    static final String COMMANDS_TAB_IN_THE_LEFT_PANEL_LOCATOR =
        "//div[@id='gwt-debug-partButton-Commands']";
    static final String MAIN_FORM = "//div[@id='gwt-debug-commands-explorer']";
    static final String ADD_COMMAND_TYPE =
        "//div[@class='popupContent']/select/option[text()='%s']";
    static final String CHOOSE_COMMAND_BY_NAME =
        "//div[@id='gwt-debug-commands-explorer']//div[text()='%s']";
    static final String COMMON_GOAL_LOCATOR = "//div[@id='goal_Common']";
    static final String DEPLOY_GOAL_LOCATOR_ = "//div[@id='goal_Deploy']";
    static final String DEBUG_GOAL_LOCATOR = "//div[@id='goal_Debug']";
    static final String RUN_GOAL_LOCATOR = "//div[@id='goal_Run']";
    static final String TEST_GOAL_LOCATOR = "//div[@id='goal_Test']";
    static final String BUILD_GOAL_LOCATOR = "//div[@id='goal_Build']";
    static final String ADD_COMMAND_BUTTON_LOCATOR =
        "//div[@id='goal_%s']/span[@id='commands_tree-button-add']";

    private Locators() {}
  }

  @FindBy(xpath = Locators.BUILD_GOAL_LOCATOR)
  WebElement buildGoal;

  @FindBy(xpath = Locators.TEST_GOAL_LOCATOR)
  WebElement testGoal;

  @FindBy(xpath = Locators.RUN_GOAL_LOCATOR)
  WebElement runGoal;

  @FindBy(xpath = Locators.DEBUG_GOAL_LOCATOR)
  WebElement debugGoal;

  @FindBy(xpath = Locators.DEPLOY_GOAL_LOCATOR_)
  WebElement deployGoal;

  @FindBy(xpath = Locators.COMMON_GOAL_LOCATOR)
  WebElement commonGoal;

  @FindBy(xpath = Locators.COMMANDS_TAB_IN_THE_LEFT_PANEL_LOCATOR)
  WebElement commandsTabInTheLeftPanel;

  @FindBy(xpath = Locators.MAIN_FORM)
  WebElement mainForm;

  private WebElement getCommandTypeElementInContextMenu(String commandType) {
    return seleniumWebDriver.findElement(
        By.xpath(String.format(Locators.ADD_COMMAND_TYPE, commandType)));
  }

  private WebElement getCommandByName(String commandlName) {
    return seleniumWebDriver.findElement(
        By.xpath(String.format(Locators.CHOOSE_COMMAND_BY_NAME, commandlName)));
  }

  public void openCommandsExplorer() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(commandsTabInTheLeftPanel))
        .click();
  }

  public void waitCommandExplorerIsOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));

    waitAddButtonIsClickable(BUILD_GOAL);
    waitAddButtonIsClickable(TEST_GOAL);
    waitAddButtonIsClickable(RUN_GOAL);
    waitAddButtonIsClickable(DEBUG_GOAL);
    waitAddButtonIsClickable(DEPLOY_GOAL);
    waitAddButtonIsClickable(COMMON_GOAL);
  }

  public void waitAddButtonIsClickable(String goalName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementToBeClickable(
                By.xpath(String.format(Locators.ADD_COMMAND_BUTTON_LOCATOR, goalName))));
  }

  public void waitCommandExplorerIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.MAIN_FORM)));
  }

  public void clickAddCommandButton(String goalName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.ADD_COMMAND_BUTTON_LOCATOR, goalName))))
        .click();
  }

  public void chooseCommandTypeInContextMenu(String type) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='popupContent']/select[contains(@class,'gwt-ListBox')]")));
    WebElement commandTypeElement =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.elementToBeClickable(getCommandTypeElementInContextMenu(type)));
    commandTypeElement.click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeSelected(commandTypeElement));
    // add timeout to be sure webelement ready to dbClick in some test got exception here
    WaitUtils.sleepQuietly(200, TimeUnit.MILLISECONDS);
    new Actions(seleniumWebDriver).doubleClick(commandTypeElement).perform();
  }

  /**
   * select command and open in editor
   *
   * @param commandName is a visible command name in command explorer
   */
  public void selectCommandByName(String commandName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(getCommandByName(commandName)));
    new Actions(seleniumWebDriver).doubleClick(getCommandByName(commandName)).build().perform();
    waitCommandIsSelected(commandName);
  }

  /**
   * wait until command will be selected
   *
   * @param commandName visible name of command in the command explorer
   */
  public void waitCommandIsSelected(String commandName) {
    String locator =
        String.format(
            "//div[@id='command_%s' and contains(normalize-space(@class), 'selected')]",
            commandName);

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }

  public void waitCommandInExplorerByName(String commandName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='gwt-debug-navPanel']//div[text()='" + commandName + "']")));
  }

  public void cloneCommandByName(String commandName) {
    selectCommandByName(commandName);
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//div[@id='command_"
                        + commandName
                        + "']//span[@id='commands_tree-button-duplicate']/*")))
        .click();
  }

  public void clickOnRemoveButtonInExplorerByName(String commandName) {
    selectCommandByName(commandName);
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='command_" + commandName + "']")))
        .click();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//div[@id='command_"
                        + commandName
                        + "']//span[@id='commands_tree-button-remove']")))
        .click();
  }

  public void runCommandByName(String commandName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(getCommandByName(commandName)));
    new Actions(seleniumWebDriver).doubleClick(getCommandByName(commandName)).build().perform();
    commandsEditor.waitActiveEditor();
    commandsEditor.clickOnRunButton();
    commandsEditor.clickOnCancelCommandEditorButton();
  }

  public void deleteCommandByName(String commandName) {
    selectCommandByName(commandName);
    loader.waitOnClosed();

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='command_" + commandName + "']")))
        .click();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//div[@id='command_"
                        + commandName
                        + "']//span[@id='commands_tree-button-remove']")))
        .click();
    askDialog.waitFormToOpen();
    askDialog.confirmAndWaitClosed();
  }

  public void waitRemoveCommandFromExplorerByName(String commandName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[@id='command_" + commandName + "']")));
  }

  public void checkCommandIsPresentInGoal(String goalName, String commandName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//div[@id='goal_"
                        + goalName
                        + "']/parent::div//div[@id='command_"
                        + commandName
                        + "']")));
  }

  public void checkCommandIsNotPresentInGoal(String goalName, String commandName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(
                    "//div[@id='goal_"
                        + goalName
                        + "']/parent::div//div[@id='command_"
                        + commandName
                        + "']")));
  }
}
