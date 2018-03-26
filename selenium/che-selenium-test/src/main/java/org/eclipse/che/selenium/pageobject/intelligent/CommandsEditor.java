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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_EDITOR_ENTRY_POINT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_TAB_UNSAVED_FILE_NAME;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsLocators.COMMAND_MACROS_FORM;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsLocators.COMMAND_MACROS_INPUT_FIELD;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsLocators.DESCRIPTION_MACROS;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsLocators.NAME_IN_GOAL_DROP_DOWN;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.ENTER;

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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * @author Ihor Okhrimenko
 * @author Aleksandr Shmaraiev
 */
@Singleton
public class CommandsEditor {
  private final CodenvyEditor editor;
  private final ActionsFactory actionsFactory;
  private final SeleniumWebDriver seleniumWebDriver;
  private final AskForValueDialog askForValueDialog;
  private final Loader loader;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

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
    this.editor = editor;
    this.actionsFactory = actionsFactory;
    this.seleniumWebDriver = seleniumWebDriver;
    this.askForValueDialog = askForValueDialog;
    this.loader = loader;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;

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
  public static final class CommandsLocators {
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

  @FindBy(xpath = COMMAND_MACROS_FORM)
  WebElement macrosContainer;

  @FindBy(xpath = COMMAND_MACROS_INPUT_FIELD)
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
    seleniumWebDriverHelper.waitAndClick(runButton, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void clickOnCancelCommandEditorButton() {
    seleniumWebDriverHelper.waitAndClick(cancelCommandEditorButton, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void clickOnSaveButtonInTheEditCommand() {
    seleniumWebDriverHelper.waitAndClick(saveButtonInCommandEditor, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * wait the command tab with specified name with unsaved status
   *
   * @param nameCommand name of tab command
   */
  public void waitTabCommandWithUnsavedStatus(String nameCommand) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(ACTIVE_TAB_UNSAVED_FILE_NAME, nameCommand)), ELEMENT_TIMEOUT_SEC);
  }

  public void typeTextIntoNameCommandField(String nameCommand) {
    seleniumWebDriverHelper.setValue(nameCommandField, nameCommand);
  }

  public void waitTextIntoNameCommandField(String expectedText) {
    seleniumWebDriverHelper.waitValue(
        nameCommandField, expectedText, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void waitTextIntoGoalField(String expectedText) {
    seleniumWebDriverHelper.waitText(nameGoalField, expectedText, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void selectGoalNameIntoCommandEditor(String goalName) {
    seleniumWebDriverHelper.waitAndClick(iconDropDownGoal, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    seleniumWebDriverHelper.waitVisibility(goalDropDown, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(NAME_IN_GOAL_DROP_DOWN, goalName)));
  }

  public void setFocusIntoTypeCommandsEditor(String commandsEditorType) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(commandsEditorType), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    editor.waitActive();
  }

  public void waitTextIntoDescriptionMacrosForm(String expectedText) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DESCRIPTION_MACROS, expectedText)), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void selectMacrosLinkInCommandsEditor(String macrosLinkType) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(macrosLinkType), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    waitCommandsMacrosIsOpen();
  }

  public void waitCommandsMacrosIsOpen() {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(COMMAND_MACROS_FORM), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void waitCommandsMacrosIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(COMMAND_MACROS_FORM), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void typeTextIntoSearchMacroField(String text) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(COMMAND_MACROS_INPUT_FIELD), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);

    seleniumWebDriverHelper.setValue(By.xpath(COMMAND_MACROS_INPUT_FIELD), text);
  }

  public void waitTextIntoSearchMacroField(String expectedText) {
    seleniumWebDriverHelper.waitValue(
        macrosInputField, expectedText, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void waitTextIntoMacrosContainer(final String expectedText) {
    webDriverWaitFactory
        .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getAllVisibleTextFromMacrosContainer().contains(expectedText));
  }

  public String getAllVisibleTextFromMacrosContainer() {
    waitCommandsMacrosIsOpen();
    return macrosContainer.getText();
  }

  public void enterMacroCommandByEnter(String item) {
    selectMacroCommand(item);
    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  public void enterMacroCommandByDoubleClick(String item) {
    selectMacroCommand(item);
    seleniumWebDriverHelper.doubleClick();
  }

  public void selectMacroCommand(String item) {
    waitVisibilityAndGetMacro(item).click();

    waitMacroCommandIsSelected(item);
  }

  private void waitMacroCommandIsSelected(String item) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  return waitVisibilityAndGetMacro(item)
                      .getCssValue("background-color")
                      .contains("rgba(215, 215, 215, 0.2)");
                });
  }

  public WebElement waitVisibilityAndGetMacro(String item) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(COMMAND_MACROS_FORM + "//div[text()='%s']/parent::td/parent::tr", item)),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
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
    seleniumWebDriverHelper.sendKeys(Keys.chord(CONTROL, "l"));
    askForValueDialog.waitFormToOpen();
    loader.waitOnClosed();
    askForValueDialog.typeAndWaitText(String.valueOf(positionLine));
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitActive();
  }
}
