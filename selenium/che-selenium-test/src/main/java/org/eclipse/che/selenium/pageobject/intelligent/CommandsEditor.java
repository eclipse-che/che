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
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.eclipse.che.selenium.pageobject.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraiev */
@Singleton
public class CommandsEditor {

  private final WebDriverWait redrawWait;
  private final WebDriverWait elemDriverWait;
  private final CodenvyEditor editor;
  private final ActionsFactory actionsFactory;
  private final SeleniumWebDriver seleniumWebDriver;
  private final AskForValueDialog askForValueDialog;
  private final Loader loader;

  @Inject
  public CommandsEditor(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskForValueDialog askForValueDialog,
      TestWebElementRenderChecker testWebElementRenderChecker,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory,
      CodenvyEditor editor) {
    redrawWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    elemDriverWait = new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC);
    this.editor = editor;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriver = seleniumWebDriver;
    this.askForValueDialog = askForValueDialog;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
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

  /** Waits until current editor's tab is ready to work. */
  public void waitActive() {
    editor.waitActive();
  }

  /**
   * Waits during {@code timeout} until current editor's tab is ready to work.
   *
   * @param timeout waiting time in seconds
   */
  public void waitActive(int timeout) {
    editor.waitActive(timeout);
  }

  /**
   * Performs "Escape" button pushing. Mainly, may be used for closing forms in the commands editor.
   */
  public void cancelFormInEditorByEscape() {
    editor.cancelFormInEditorByEscape();
  }

  /**
   * Types {@code text} into commands editor with pause 1 sec.
   *
   * @param text text which should be typed
   */
  public void typeTextIntoEditor(String text) {
    editor.typeTextIntoEditor(text);
  }

  /**
   * Waits until specified {@code expectedText} is present in the commands editor.
   *
   * @param expectedText text which should be present in the commands editor
   */
  public void waitTextIntoEditor(final String expectedText) {
    editor.waitTextIntoEditor(expectedText);
  }

  /**
   * Waits during {@code timeout} until specified {@code expectedText} is present in commands
   * editor.
   *
   * @param expectedText text which should be present in the commands editor
   * @param timeout waiting time in seconds
   */
  public void waitTextIntoEditor(final String expectedText, final int timeout) {
    editor.waitTextIntoEditor(expectedText, timeout);
  }

  /** Deletes current editor's line. */
  public void selectLineAndDelete() {
    editor.selectLineAndDelete();
  }

  /**
   * Waits until editor's tab with specified {@code nameOfFile} is present in the commands editor.
   *
   * @param nameOfFile title of the commands editor's tab which should be checked
   */
  public void waitTabIsPresent(String nameOfFile) {
    editor.waitTabIsPresent(nameOfFile);
  }

  /**
   * Waits during {@code timeout} until commands editor's tab with specified {@code nameOfFile} is
   * present in editor.
   *
   * @param nameOfFile title of the commands editor's tab which should be checked
   * @param timeout waiting time in seconds
   */
  public void waitTabIsPresent(String nameOfFile, int timeout) {
    editor.waitTabIsPresent(nameOfFile, timeout);
  }

  /**
   * Waits until open file tab with specified {@code nameOfFile} is without '*' unsaved status.
   *
   * @param nameOfFile title of the tab which should be checked
   */
  public void waitTabFileWithSavedStatus(String nameOfFile) {
    editor.waitTabFileWithSavedStatus(nameOfFile);
  }

  /**
   * Selects commands editor's tab with specified {@code nameOfFile}.
   *
   * @param nameOfFile title of the tab which should be selected
   */
  public void selectTabByName(String nameOfFile) {
    editor.selectTabByName(nameOfFile);
  }

  /**
   * Waits active tab with specified {@code nameOfFile}.
   *
   * @param nameOfFile title of the tab which should be checked
   */
  public void waitActiveTabFileName(String nameOfFile) {
    editor.waitActiveTabFileName(nameOfFile);
  }

  /**
   * Launches code assistant by "ctrl" + "space" keys pressing and waits until container is opened.
   */
  public void launchAutocompleteAndWaitContainer() {
    editor.launchAutocompleteAndWaitContainer();
  }

  /**
   * Waits specified {@code expectedText} in autocomplete container.
   *
   * @param expectedText text which should be present in the container
   */
  public void waitTextIntoAutocompleteContainer(final String expectedText) {
    editor.waitTextIntoAutocompleteContainer(expectedText);
  }

  /**
   * Selects specified {@code item} in the autocomplete container.
   *
   * @param item item from autocomplete container.
   */
  public void selectAutocompleteProposal(String item) {
    editor.selectAutocompleteProposal(item);
  }

  /**
   * Selects specified {@code item} in the autocomplete proposal container, and presses "ENTER".
   *
   * @param item item in the autocomplete proposal container.
   */
  public void enterAutocompleteProposal(String item) {
    editor.enterAutocompleteProposal(item);
  }

  /** Waits until autocomplete form is closed. */
  public void waitAutocompleteContainerIsClosed() {
    editor.waitAutocompleteContainerIsClosed();
  }

  /**
   * Selects specified {@code item} in the autocomplete container and sends double click to item.
   *
   * @param item item in the autocomplete proposal container.
   */
  public void selectItemIntoAutocompleteAndPerformDoubleClick(String item) {
    editor.selectItemIntoAutocompleteAndPerformDoubleClick(item);
  }

  /** Closes autocomplete container by "Escape" button and checks that container is closed. */
  public void closeAutocomplete() {
    editor.closeAutocomplete();
  }

  /** Launches code assistant by "ctrl" + "space" keys pressing. */
  public void launchAutocomplete() {
    editor.launchAutocomplete();
  }

  /**
   * Waits until commands editor's tab with specified {@code nameOfFile} is not presented.
   *
   * @param nameOfFile title of editor's tab which should be checked
   */
  public void waitTabIsNotPresent(String nameOfFile) {
    editor.waitTabIsNotPresent(nameOfFile);
  }

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
    editor.waitActive();
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

  public void deleteAllContent() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("a")
        .keyUp(CONTROL)
        .sendKeys(DELETE)
        .perform();
  }

  public void setCursorToLine(int positionLine) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.chord(CONTROL, "l")).perform();
    askForValueDialog.waitFormToOpen();
    loader.waitOnClosed();
    askForValueDialog.typeAndWaitText(String.valueOf(positionLine));
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitActive();
  }
}
