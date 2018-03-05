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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_EDITOR_ENTRY_POINT;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.eclipse.che.selenium.pageobject.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraiev */
@Singleton
public class CommandsEditor extends CodenvyEditor {

  private final WebDriverWait redrawWait;
  private final WebDriverWait elemDriverWait;

  @Inject
  public CommandsEditor(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskForValueDialog askForValueDialog,
      TestWebElementRenderChecker testWebElementRenderChecker,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    super(
        seleniumWebDriver,
        loader,
        actionsFactory,
        askForValueDialog,
        testWebElementRenderChecker,
        seleniumWebDriverHelper,
        webDriverWaitFactory);
    PageFactory.initElements(seleniumWebDriver, this);
    redrawWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    elemDriverWait = new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC);
  }

  public static final class CommandsEditorType {
    public static final String COMMAND_LINE_EDITOR =
        "(" + Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH + ")[1]";
    public static final String PREVIEW_URL_EDITOR =
        "(" + Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH + ")[2]";

    private CommandsEditorType() {}
  }

  public static final class CommandsMacrosLinkType {
    public static final String EDITOR_MACROS_LINK =
        "(" + ACTIVE_EDITOR_ENTRY_POINT + "//a[@id='gwt-debug-link-explore_macros'])[1]";
    public static final String PREVIEW_MACROS_LINK =
        "(" + ACTIVE_EDITOR_ENTRY_POINT + "//a[@id='gwt-debug-link-explore_macros'])[2]";

    private CommandsMacrosLinkType() {}
  }

  /** Class introduce Xpath locators for DOM navigation inside Commands Editor widget */
  private static final class CommandsLocators {
    static final String RUN_BUTTON =
        ACTIVE_EDITOR_ENTRY_POINT + "//button[@id='gwt-debug-command-editor-button-run']";

    static final String CANCEL_COMMAND_EDITOR_BUTTON =
        ACTIVE_EDITOR_ENTRY_POINT + "//button[@id='gwt-debug-command-editor-button-cancel']";

    static final String SAVE_BUTTON_IN_COMMAND_EDITOR =
        ACTIVE_EDITOR_ENTRY_POINT + "//button[@id='gwt-debug-command-editor-button-save']";

    static final String NAME_COMMAND_FIELD = RUN_BUTTON + "/following::input[1]";

    static final String NAME_GOAL_FIELD =
        ACTIVE_EDITOR_ENTRY_POINT + "//div[@id='gwt-debug-command_editor-goal']/label[1]";

    static final String DROP_DOWN_GOAL_ICON =
        ACTIVE_EDITOR_ENTRY_POINT
            + "//div[@id='gwt-debug-command_editor-goal']//following::*[local-name()='svg']";

    static final String GOAL_DROP_DOWN =
        ACTIVE_EDITOR_ENTRY_POINT + "//div[@id='gwt-debug-command_editor-goal']";

    static final String NAME_IN_GOAL_DROP_DOWN = GOAL_DROP_DOWN + "//label[text()='%s']";

    static final String DESCRIPTION_MACROS = "//div[text()='%s']";

    static final String COMMAND_MACROS_FORM = "//div[@id='gwt-debug-macro_chooser']";

    static final String COMMAND_MACROS_INPUT_FIELD = COMMAND_MACROS_FORM + "/descendant::input[1]";

    private CommandsLocators() {}
  }

  @FindBy(xpath = CommandsLocators.RUN_BUTTON)
  WebElement runButton;

  @FindBy(xpath = CommandsLocators.CANCEL_COMMAND_EDITOR_BUTTON)
  WebElement cancelCommandEditorButton;

  @FindBy(xpath = CommandsLocators.SAVE_BUTTON_IN_COMMAND_EDITOR)
  WebElement saveButtonInCommandEditor;

  @FindBy(xpath = CommandsLocators.NAME_COMMAND_FIELD)
  WebElement nameCommandField;

  @FindBy(xpath = CommandsLocators.NAME_GOAL_FIELD)
  WebElement nameGoalField;

  @FindBy(xpath = CommandsLocators.DROP_DOWN_GOAL_ICON)
  WebElement iconDropDownGoal;

  @FindBy(xpath = CommandsLocators.GOAL_DROP_DOWN)
  WebElement goalDropDown;

  @FindBy(xpath = CommandsLocators.COMMAND_MACROS_FORM)
  WebElement macrosContainer;

  @FindBy(xpath = CommandsLocators.COMMAND_MACROS_INPUT_FIELD)
  WebElement macrosInputField;

  public void clickOnRunButton() {
    redrawWait.until(visibilityOf(runButton)).click();
  }

  public void clickOnCancelCommandEditorButton() {
    redrawWait.until(visibilityOf(cancelCommandEditorButton)).click();
  }

  public void clickOnSaveButtonInTheEditCommand() {
    redrawWait.until(visibilityOf(saveButtonInCommandEditor)).click();
  }

  /**
   * wait the command tab with specified name with unsaved status
   *
   * @param nameCommand name of tab command
   */
  public void waitTabCommandWithUnsavedStatus(String nameCommand) {
    elemDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.ACTIVE_TAB_UNSAVED_FILE_NAME, nameCommand))));
  }

  public void typeTextIntoNameCommandField(String nameCommand) {
    redrawWait.until(visibilityOf(nameCommandField)).clear();
    redrawWait.until(visibilityOf(nameCommandField)).sendKeys(nameCommand);
  }

  public void waitTextIntoNameCommandField(String expText) {
    redrawWait.until(
        (ExpectedCondition<Boolean>)
            webDriver -> nameCommandField.getAttribute("value").equals(expText));
  }

  public void waitTextIntoGoalField(String expText) {
    redrawWait.until(
        (ExpectedCondition<Boolean>) webDriver -> nameGoalField.getText().equals(expText));
  }

  public void selectGoalNameIntoCommandEditor(String goalName) {
    redrawWait.until(visibilityOf(iconDropDownGoal)).click();
    redrawWait.until(visibilityOf(goalDropDown));
    redrawWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(CommandsLocators.NAME_IN_GOAL_DROP_DOWN, goalName))))
        .click();
  }

  public void setFocusIntoTypeCommandsEditor(String commandsEditorType) {
    redrawWait.until(visibilityOfElementLocated(By.xpath(commandsEditorType))).click();
    waitActive();
  }

  public void waitTextIntoDescriptionMacrosForm(String expText) {
    redrawWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(CommandsLocators.DESCRIPTION_MACROS, expText))));
  }

  public void selectMacrosLinkInCommandsEditor(String macrosLinkType) {
    redrawWait.until(visibilityOfElementLocated(By.xpath(macrosLinkType))).click();
    waitCommandsMacrosIsOpen();
  }

  public void waitCommandsMacrosIsOpen() {
    redrawWait.until(visibilityOfElementLocated(By.xpath(CommandsLocators.COMMAND_MACROS_FORM)));
  }

  public void waitCommandsMacrosIsClosed() {
    redrawWait.until(invisibilityOfElementLocated(By.xpath(CommandsLocators.COMMAND_MACROS_FORM)));
  }

  public void typeTextIntoSearchMacroField(String text) {
    redrawWait
        .until(visibilityOfElementLocated(By.xpath(CommandsLocators.COMMAND_MACROS_INPUT_FIELD)))
        .click();
    redrawWait
        .until(visibilityOfElementLocated(By.xpath(CommandsLocators.COMMAND_MACROS_INPUT_FIELD)))
        .clear();
    redrawWait
        .until(visibilityOfElementLocated(By.xpath(CommandsLocators.COMMAND_MACROS_INPUT_FIELD)))
        .sendKeys(text);
  }

  public void waitTextIntoSearchMacroField(String expText) {
    redrawWait.until(
        (ExpectedCondition<Boolean>)
            webDriver -> macrosInputField.getAttribute("value").equals(expText));
  }

  public void waitTextIntoMacrosContainer(final String expText) {
    redrawWait.until(
        (ExpectedCondition<Boolean>)
            (WebDriver webDriver) -> getAllVisibleTextFromMacrosContainer().contains(expText));
  }

  public String getAllVisibleTextFromMacrosContainer() {
    waitCommandsMacrosIsOpen();
    return macrosContainer.getText();
  }

  public void enterMacroCommandByEnter(String item) {
    selectMacroCommand(item);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER.toString()).perform();
  }

  public void enterMacroCommandByDoubleClick(String item) {
    selectMacroCommand(item);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  public void selectMacroCommand(String item) {
    String locator =
        String.format(CommandsLocators.COMMAND_MACROS_FORM + "//div[text()='%s']", item);
    redrawWait.until(visibilityOfElementLocated(By.xpath(locator))).click();
    WebElement highlightEItem = seleniumWebDriver.findElement(By.xpath(locator));
    redrawWait
        .until(visibilityOf(highlightEItem))
        .getCssValue("background-color")
        .contains("rgb(37, 108, 159)");
  }

  public void waitMacroCommandIsSelected(String item) {
    String locator =
        String.format(CommandsLocators.COMMAND_MACROS_FORM + "//div[text()='%s']", item);
    WebElement highlightEItem = seleniumWebDriver.findElement(By.xpath(locator));
    redrawWait
        .until(visibilityOf(highlightEItem))
        .getCssValue("background-color")
        .contains("rgb(37, 108, 159)");
  }

  @Override
  public void deleteAllContent() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    action.sendKeys("a").perform();
    action.keyUp(Keys.CONTROL).perform();
    action.sendKeys(Keys.DELETE.toString()).perform();
  }

  @Override
  public void setCursorToLine(int positionLine) {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(Keys.chord(Keys.CONTROL, "l"))
        .perform();
    askForValueDialog.waitFormToOpen();
    loader.waitOnClosed();
    askForValueDialog.typeAndWaitText(String.valueOf(positionLine));
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    waitActive();
  }
}
