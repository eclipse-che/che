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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_LINES_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_TAB_FILE_NAME;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ALL_TABS_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ASSIST_CONTENT_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.AUTOCOMPLETE_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.AUTOCOMPLETE_PROPOSAL_DOC_ID;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.CONTEXT_MENU;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAKPOINT_CONDITION;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAKPOINT_DISABLED;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAK_POINT_ACTIVE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAK_POINT_INACTIVE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_PREFIX_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.EDITOR_TABS_PANEL;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.HIGHLIGHT_ITEM_PATTERN;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.HOVER_POPUP_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.IMPLEMENTATIONS_ITEM;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.IMPLEMENTATION_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ITEM_TAB_LIST;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.JAVA_DOC_POPUP;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.LANGUAGE_SERVER_REFACTORING_RENAME_FIELD_CSS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ORION_CONTENT_ACTIVE_EDITOR_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.POSITION_CURSOR_NUMBER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.PROPOSITION_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.PUNCTUATION_SEPARATOR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_ANNOTATIONS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_FOLDING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_LINES;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_OVERVIEW;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.SELECTED_ITEM_IN_EDITOR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_CONTEXT_MENU_BODY;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_CLOSE_ICON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_NAME_AND_STYLE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_NAME_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_LIST_BUTTON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_WITH_UNSAVED_STATUS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TEXT_VIEW_RULER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TOOLTIP_TITLE_CSS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabColor.BLUE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabColor.FOCUSED_DEFAULT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabColor.GREEN;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabColor.UNFOCUSED_DEFAULT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.TabColor.YELLOW;
import static org.openqa.selenium.Keys.ALT;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.END;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.Keys.F6;
import static org.openqa.selenium.Keys.HOME;
import static org.openqa.selenium.Keys.LEFT_CONTROL;
import static org.openqa.selenium.Keys.SHIFT;
import static org.openqa.selenium.Keys.SPACE;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfNestedElementLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;

@Singleton
public class CodenvyEditor {
  public static final String CLOSE_ALL_TABS = "gwt-debug-contextMenu/closeAllEditors";
  public static final String VCS_RULER = "//div[@class='ruler vcs']/div";
  public static final Logger LOG = getLogger(CodenvyEditor.class);

  protected final SeleniumWebDriver seleniumWebDriver;
  protected final Loader loader;
  protected final ActionsFactory actionsFactory;
  protected final AskForValueDialog askForValueDialog;

  private final TestWebElementRenderChecker testWebElementRenderChecker;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  @Inject
  public CodenvyEditor(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskForValueDialog askForValueDialog,
      TestWebElementRenderChecker testWebElementRenderChecker,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.askForValueDialog = askForValueDialog;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Interface introduce base locators for DOM navigation inside editor */
  public interface Locators {
    String CONTEXT_MENU = "//div[@id='menu-lock-layer-id']/div[2]";
    String EDITOR_TABS_PANEL = "gwt-debug-multiSplitPanel-tabsPanel";
    String ACTIVE_LINE_NUMBER = "gwt-debug-cursorPosition";
    String TAB_CONTEXT_MENU_BODY = "//*[@id='gwt-debug-contextMenu/closeAllEditors']/parent::tbody";
    String POSITION_CURSOR_NUMBER =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[text()='%s']";
    String ACTIVE_EDITOR_ENTRY_POINT =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[@active]";
    String ORION_ACTIVE_EDITOR_CONTAINER_XPATH =
        ACTIVE_EDITOR_ENTRY_POINT + "//div[@class='textviewContent' and @contenteditable='true']";
    String ORION_CONTENT_ACTIVE_EDITOR_XPATH = ORION_ACTIVE_EDITOR_CONTAINER_XPATH + "/div";
    String ACTIVE_LINES_XPATH =
        "//div[@class='textviewSelection']/preceding::div[@class='annotationLine currentLine'][1]";
    String ACTIVE_LINE_HIGHLIGHT_CLASSNAME = "annotationLine currentLine";
    String ACTIVE_TAB_FILE_NAME = "//div[@active]/descendant::div[text()='%s']";
    String ACTIVE_TAB_UNSAVED_FILE_NAME = "//div[@active and @unsaved]//div[text()='%s']";
    String TAB_FILE_NAME_XPATH =
        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']";
    String TAB_FILE_NAME_AND_STYLE =
        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s' and @style='%s']";
    String TAB_FILE_CLOSE_ICON =
        "//div[@id='gwt-debug-editorMultiPartStack-contentPanel']//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']/following::div[1]";
    String ALL_TABS_XPATH =
        "//div[@id='gwt-debug-editorMultiPartStack-contentPanel']//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[string-length(text())>0]";
    String TAB_WITH_UNSAVED_STATUS =
        "//div[@id='gwt-debug-editor-tab' and @unsaved]//div[text()='%s']";
    String SELECTED_ITEM_IN_EDITOR = "//div[@contenteditable='true']//span[contains(text(), '%s')]";
    String ASSIST_CONTENT_CONTAINER = "//div[@class='contentassist']/following-sibling::div";
    String AUTOCOMPLETE_CONTAINER = "//div[text()='Proposals:']//following::div/ulist";
    String PROPOSITION_CONTAINER = "//div[@id='gwt_root']/following::div/ulist";
    String SHOW_HINTS_POP_UP = "//div[@class='popupContent']/div[1]";
    String RULER_ANNOTATIONS = "//div[@class='ruler annotations']";
    String RULER_OVERVIEW = "//div[@class='ruler overview']";
    String RULER_LINES = "//div[@class='ruler lines']";
    String RULER_FOLDING = "//div[@class='ruler folding']";
    String IMPLEMENTATION_CONTAINER =
        "//div[contains(text(), 'Choose Implementation of %s')]/parent::div";
    String IMPLEMENTATION_CONTENT =
        "//div[contains(text(), 'Choose Implementation of')]/following::div";
    String IMPLEMENTATIONS_ITEM =
        "//div[contains(text(), 'Choose Implementation of')]/following::span[text()='%s']";
    String PUNCTUATION_SEPARATOR = "//span[contains(@class,'punctuation separator space')]";
    String TEXT_VIEW_RULER = "//div[@class='textviewInnerRightRuler']";
    String DOWNLOAD_SOURCES_LINK = "//anchor[text()='Download sources']";
    String TAB_LIST_BUTTON = "gwt-debug-editorMenu";
    String ITEM_TAB_LIST = "//div[@class='popupContent']//div[text()='%s']/parent::div";
    String NOTIFICATION_PANEL_ID = "gwt-debug-leftNotificationGutter";
    String DEBUGGER_PREFIX_XPATH = "//div[@class[contains(., 'rulerLines')] and text()='%d']";
    String DEBUGGER_BREAK_POINT_INACTIVE = "//div[@class='breakpoint inactive' and text()='%d']";
    String DEBUGGER_BREAK_POINT_ACTIVE = "//div[@class='breakpoint active' and text()='%d']";
    String DEBUGGER_BREAKPOINT_CONDITION =
        "//div[@class='breakpoint %s condition' and text()='%d']";
    String DEBUGGER_BREAKPOINT_DISABLED = "//div[@class='breakpoint disabled' and text()='%d']";
    String JAVA_DOC_POPUP = "//div[@class='gwt-PopupPanel']//iframe";
    String AUTOCOMPLETE_PROPOSAL_JAVA_DOC_POPUP =
        "//div//iframe[contains(@src, 'api/java/code-assist/compute/info?')]";
    String HIGHLIGHT_ITEM_PATTERN = "//li[@selected='true']//span[text()='%s']";
    String TOOLTIP_TITLE_CSS = "span.tooltipTitle";
    String TEXT_TO_MOVE_CURSOR_XPATH =
        ORION_ACTIVE_EDITOR_CONTAINER_XPATH + "//span[contains(text(),'%s')]";
    String HOVER_POPUP_XPATH =
        "//div[@class='textviewTooltip' and contains(@style,'visibility: visible')]";
    String AUTOCOMPLETE_PROPOSAL_DOC_ID = "gwt-debug-content-assistant-doc-popup";
    String LANGUAGE_SERVER_REFACTORING_RENAME_FIELD_CSS = "input.orionCodenvy";
  }

  public enum TabActionLocator {
    CLOSE(By.id("contextMenu/Close")),
    CLOSE_ALL(By.id("contextMenu/Close All")),
    CLOSE_OTHER(By.id("contextMenu/Close Other")),
    CLOSE_ALL_BUT_PINNED(By.id("contextMenu/Close All But Pinned")),
    REOPEN_CLOSED_TAB(By.id("contextMenu/Reopen Closed Tab")),
    PIN_UNPIN_TAB(By.id("contextMenu/Pin/Unpin Tab")),
    SPLIT_VERTICALLY(By.id("contextMenu/Split Pane In Two Columns")),
    SPIT_HORISONTALLY(By.id("contextMenu/Split Pane In Two Rows"));

    @SuppressWarnings("ImmutableEnumChecker")
    private final By id;

    TabActionLocator(By id) {
      this.id = id;
    }

    private By get() {
      return this.id;
    }
  }

  /** Editor`s markers types */
  public enum MarkerLocator {
    ERROR_OVERVIEW("//div[@class='ruler annotations']/div[@class='annotation error']"),
    WARNING_OVERVIEW("//div[@class='ruler annotations']/div[@class='annotation warning']"),
    TASK_OVERVIEW("//div[@class='ruler annotations']/div[@class='annotation task']"),
    ERROR("//div[@class='ruler overview']/div[@class='annotationOverview error']"),
    WARNING("//div[@class='ruler overview']/div[@class='annotationOverview warning']"),
    INFO("//div[@class='annotationHTML info']");

    private final String locator;

    MarkerLocator(String locator) {
      this.locator = locator;
    }

    private String get() {
      return this.locator;
    }
  }

  /** Editor's context menu items */
  public enum ContextMenuLocator {
    REFACTORING(By.id("contextMenu/Refactoring")),
    REFACTORING_MOVE(By.id("contextMenu/Refactoring/Move")),
    REFACTORING_RENAME(By.id("contextMenu/Refactoring/Rename")),
    UNDO(By.id("contextMenu/Undo")),
    REDO(By.id("contextMenu/Redo")),
    FORMAT(By.id("contextMenu/Format")),
    QUICK_DOC(By.id("contextMenu/Quick Documentation")),
    QUICK_FIX(By.id("contextMenu/Quick Fix")),
    OPEN_DECLARATION(By.id("contextMenu/Open Declaration")),
    NAVIGATE_FILE_STRUCTURE(By.id("contextMenu/Navigate File Structure")),
    FIND(By.id("contextMenu/Find")),
    OPEN_ON_GITHUB(By.id("contextMenu/Open on GitHub")),
    CLOSE(By.id("contextMenu/Close"));

    @SuppressWarnings("ImmutableEnumChecker")
    private final By itemLocator;

    ContextMenuLocator(By itemLocator) {
      this.itemLocator = itemLocator;
    }

    private By get() {
      return this.itemLocator;
    }
  }

  public enum TabColor {
    YELLOW("color: rgb(224, 185, 29);"),
    GREEN("color: rgb(114, 173, 66);"),
    BLUE("color: rgb(49, 147, 212);"),
    FOCUSED_DEFAULT("rgba(255, 255, 255, 1)"),
    UNFOCUSED_DEFAULT("rgba(170, 170, 170, 1)");

    private final String color;

    TabColor(String color) {
      this.color = color;
    }

    private String get() {
      return this.color;
    }
  }

  @FindAll({@FindBy(id = Locators.ACTIVE_LINE_NUMBER)})
  private List<WebElement> activeLineNumbers;

  @FindBy(xpath = AUTOCOMPLETE_CONTAINER)
  private WebElement autocompleteContainer;

  @FindBy(xpath = PROPOSITION_CONTAINER)
  private WebElement propositionContainer;

  @FindBy(xpath = JAVA_DOC_POPUP)
  private WebElement javaDocPopUp;

  @FindBy(xpath = ASSIST_CONTENT_CONTAINER)
  private WebElement assistContentContainer;

  @FindBy(xpath = Locators.IMPLEMENTATION_CONTENT)
  private WebElement implementationContent;

  @FindBy(xpath = ACTIVE_LINES_XPATH)
  private WebElement activeLineXpath;

  @FindBy(xpath = Locators.SHOW_HINTS_POP_UP)
  private WebElement showHintsPopUp;

  @FindBy(xpath = ORION_ACTIVE_EDITOR_CONTAINER_XPATH)
  private WebElement activeEditorContainer;

  @FindBy(xpath = Locators.AUTOCOMPLETE_PROPOSAL_JAVA_DOC_POPUP)
  private WebElement autocompleteProposalJavaDocPopup;

  @FindBy(xpath = ALL_TABS_XPATH)
  private WebElement someOpenedTab;

  @FindBy(css = TOOLTIP_TITLE_CSS)
  private WebElement tooltipTitle;

  @FindBy(xpath = HOVER_POPUP_XPATH)
  private WebElement hoverPopup;

  @FindBy(id = AUTOCOMPLETE_PROPOSAL_DOC_ID)
  private WebElement proposalDoc;

  @FindBy(css = LANGUAGE_SERVER_REFACTORING_RENAME_FIELD_CSS)
  private WebElement languageServerRenameField;

  /**
   * Waits during {@code timeout} until current editor's tab is ready to work.
   *
   * @param timeout waiting time in seconds
   */
  public void waitActive(int timeout) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.waitVisibility(activeEditorContainer, timeout);
  }

  /** Waits until current editor's tab is ready to work. */
  public void waitActive() {
    waitActive(ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Gets visible text from active editor's tab
   *
   * @return visible text from editor's tab
   */
  public String getVisibleTextFromEditor() {
    waitActive();
    List<WebElement> lines =
        seleniumWebDriverHelper.waitPresenceOfAllElements(
            By.xpath(ORION_CONTENT_ACTIVE_EDITOR_XPATH), ELEMENT_TIMEOUT_SEC);
    return getTextFromOrionLines(lines);
  }

  /**
   * Gets visible text from split editor's tab with specified {@code indexOfEditor}.
   *
   * @param indexOfEditor index of editor's tab which should be read, starting from "1"
   * @return visible text from chosen editor's tab
   */
  public String getTextFromSplitEditor(int indexOfEditor) {
    waitActive();
    List<WebElement> inner =
        seleniumWebDriverHelper
            .waitPresenceOfAllElements(
                By.xpath(ORION_ACTIVE_EDITOR_CONTAINER_XPATH), ELEMENT_TIMEOUT_SEC)
            .get(indexOfEditor - 1)
            .findElements(By.tagName("div"));

    return getTextFromOrionLines(inner);
  }

  /**
   * Waits during {@code timeout} until specified {@code expectedText} is present in editor.
   *
   * @param expectedText text which should be present in the editor
   * @param timeout waiting time in seconds
   */
  public void waitTextIntoEditor(final String expectedText, final int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getVisibleTextFromEditor().contains(expectedText));
  }

  /**
   * Waits until specified {@code expectedText} is present in editor.
   *
   * @param expectedText text which should be present in the editor
   */
  public void waitTextIntoEditor(final String expectedText) {
    waitTextIntoEditor(expectedText, ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits during {@code timeout} until specified {@code expectedText} is present in the editor's
   * tab with defined {@code indexOfEditor}.
   *
   * @param indexOfEditor index of editor's tab, text from which should be checked, numeration
   *     starts from "1"
   * @param timeout waiting time in seconds
   */
  public void waitTextInDefinedSplitEditor(
      int indexOfEditor, final int timeout, String expectedText) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromSplitEditor(indexOfEditor).contains(expectedText));
  }

  /**
   * wait text in tooltip pop-up (after clicking or hovering warning/error marker)
   *
   * @param expectedText the expected text into tooltip pop-up
   */
  public void waitTextInToolTipPopup(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(tooltipTitle, expectedText);
  }

  /** Get text from hover popup */
  public String getTextFromHoverPopup() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(hoverPopup);
  }

  /**
   * wait text in hover pop-up (after hovering on text)
   *
   * @param expectedText the expected text into hover pop-up
   */
  public void waitTextInHoverPopup(String expectedText) {
    try {
      seleniumWebDriverHelper.waitTextContains(hoverPopup, expectedText);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10674", ex);
    }
  }

  /**
   * Waits during {@code timeout} until specified {@code text} is not present in the editor's tab
   * with defined {@code indexOfEditor}.
   *
   * @param indexOfEditor index of editor's tab, text from which should be checked, numeration
   *     starts from "1"
   * @param timeout waiting time in seconds
   * @param text text which should not be present in the chosen editor's tab
   */
  public void waitTextIsNotPresentInDefinedSplitEditor(
      int indexOfEditor, final int timeout, String text) {
    webDriverWaitFactory
        .get(timeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> !getTextFromSplitEditor(indexOfEditor).contains(text));
  }

  /**
   * Waits until {@code text} is not present in editor.
   *
   * @param text text which should not be present in the editor
   */
  public void waitTextNotPresentIntoEditor(final String text) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>) webDriver -> !(getVisibleTextFromEditor().contains(text)));
  }

  /**
   * Waits until editor's tab with specified {@code nameOfFile} is closed.
   *
   * @param nameOfFile title of the editor's tab
   */
  public void waitWhileFileIsClosed(String nameOfFile) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits until changes in the editor's tab with specified {@code nameFile} is saved and closes
   * this tab.
   *
   * @param nameFile title of the editor's tab which should be checked
   */
  public void closeFileByNameWithSaving(String nameFile) {
    loader.waitOnClosed();
    selectTabByName(nameFile);
    waitTabFileWithSavedStatus(nameFile);
    clickOnCloseFileIcon(nameFile);
    waitWhileFileIsClosed(nameFile);
    loader.waitOnClosed();
  }

  /**
   * Waits visibility of the "Close" icon in the editor's tab with specified {@code fileName} and
   * clicks on it.
   *
   * @param fileName title of the editor's tab which should be closed
   */
  public void clickOnCloseFileIcon(String fileName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(TAB_FILE_CLOSE_ICON, fileName)));
  }

  /**
   * Checks that at list one editor's tab is opened.
   *
   * @return true - if at list one tab is opened in the editor, false - if not
   */
  public boolean isAnyTabsOpened() {
    return seleniumWebDriverHelper.isVisible(someOpenedTab);
  }

  /** Gets all open editor's tabs and closes they with checking the files saving */
  public void closeAllTabs() {
    loader.waitOnClosed();

    seleniumWebDriverHelper
        .waitVisibilityOfAllElements(By.xpath(ALL_TABS_XPATH), REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .forEach(tab -> closeFileByNameWithSaving(tab.getText()));

    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(ALL_TABS_XPATH), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Closes all tabs by using context menu */
  public void closeAllTabsByContextMenu() {
    List<WebElement> tabs =
        seleniumWebDriverHelper.waitVisibilityOfAllElements(
            By.xpath(ALL_TABS_XPATH), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);

    WebElement tab =
        seleniumWebDriverHelper.waitVisibility(
            By.xpath(format(TAB_FILE_CLOSE_ICON, tabs.get(tabs.size() - 1).getText())));

    seleniumWebDriverHelper.moveCursorToAndContextClick(tab);

    testWebElementRenderChecker.waitElementIsRendered(By.xpath(TAB_CONTEXT_MENU_BODY));
    seleniumWebDriverHelper.waitAndClick(By.id(CLOSE_ALL_TABS));
    seleniumWebDriverHelper.waitInvisibility(By.xpath(ALL_TABS_XPATH));
  }

  /**
   * Opens context menu for editor's tab with specified {@code tabName}.
   *
   * @param tabName title of the editor's tab
   */
  public void openAndWaitContextMenuForTabByName(String tabName) {
    WebElement tab =
        seleniumWebDriverHelper.waitVisibility(By.xpath(format(TAB_FILE_CLOSE_ICON, tabName)));

    seleniumWebDriverHelper.moveCursorToAndContextClick(tab);
    testWebElementRenderChecker.waitElementIsRendered(By.xpath(TAB_CONTEXT_MENU_BODY));
  }

  /**
   * Runs action for tab from the context menu.
   *
   * @param tabAction item from tab's context menu
   */
  public void runActionForTabFromContextMenu(TabActionLocator tabAction) {
    seleniumWebDriverHelper.waitAndClick(tabAction.get());
  }

  /**
   * Types {@code text} into orion editor with pause 1 sec.
   *
   * @param text text which should be typed
   */
  public void typeTextIntoEditor(String text) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.sendKeys(text);
    loader.waitOnClosed();
  }

  /**
   * Types specified {@code text} into editor without pause for saving.
   *
   * @param text text which should be typed
   */
  public void typeTextIntoEditorWithoutDelayForSaving(String text) {
    seleniumWebDriverHelper.sendKeys(text);
  }

  /**
   * Types specified {@code text} into defined editor's {@code line}.
   *
   * @param text text which should be typed
   * @param line the line's number where text should be typed
   */
  public void typeTextIntoEditor(String text, int line) {
    setCursorToLine(line);
    typeTextIntoEditor(text);
  }

  /**
   * Sets cursor to specified {@code positionLine}.
   *
   * @param positionLine line's number where cursor should be placed
   */
  public void setCursorToLine(int positionLine) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.sendKeys(Keys.chord(CONTROL, "l"));
    askForValueDialog.waitFormToOpen();
    loader.waitOnClosed();
    askForValueDialog.typeAndWaitText(String.valueOf(positionLine));
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    waitActive();
    expectedNumberOfActiveLine(positionLine);
  }

  /**
   * Sets cursor to specified {@code positionLine} and {@code positionChar} and checks result.
   *
   * @param positionLine line's number where cursor should be placed
   * @param positionChar char's number where cursor should be placed
   */
  public void goToCursorPositionVisible(int positionLine, int positionChar) {
    openGoToLineFormAndSetCursorToPosition(positionLine, positionChar);
    waitActive();
    waitSpecifiedValueForLineAndChar(positionLine, positionChar);
  }

  /**
   * Sets cursor to specified {@code positionLine} and {@code positionChar} and checks result.
   *
   * @param positionLine line's number where cursor should be placed
   * @param positionChar char's number where cursor should be placed
   */
  public void goToPosition(int positionLine, int positionChar) {
    openGoToLineFormAndSetCursorToPosition(positionLine, positionChar);
    waitActive();
    waitCursorPosition(positionLine, positionChar);
  }

  /**
   * Select text in defined interval
   *
   * @param fromLine beginning of first line for selection
   * @param numberOfLine end of first line for selection
   */
  public void selectLines(int fromLine, int numberOfLine) {
    Actions action = seleniumWebDriverHelper.getAction(seleniumWebDriver);
    setCursorToLine(fromLine);
    action.keyDown(SHIFT).perform();
    for (int i = 0; i < numberOfLine; i++) {
      typeTextIntoEditor(Keys.ARROW_DOWN.toString());
    }
    action.keyUp(SHIFT).perform();
    action.sendKeys(Keys.END.toString()).keyUp(SHIFT).perform();
  }

  /**
   * Sets cursor to specified {@code positionLine} and {@code positionChar}.
   *
   * @param positionLine line's number where cursor should be placed
   * @param positionChar char's number where cursor should be placed
   */
  private void openGoToLineFormAndSetCursorToPosition(int positionLine, int positionChar) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.sendKeys(Keys.chord(CONTROL, "l"));
    askForValueDialog.waitFormToOpen();
    loader.waitOnClosed();
    askForValueDialog.typeAndWaitText(
        String.valueOf(positionLine) + ":" + String.valueOf(positionChar));
    loader.waitOnClosed();
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  /**
   * Launches code assistant by "ctrl" + "space" keys combination and waits until container is
   * opened.
   */
  public void launchAutocompleteAndWaitContainer() {
    launchAutocomplete();
    waitAutocompleteContainer();
  }

  /** Launches code assistant by "ctrl" + "space" keys pressing. */
  public void launchAutocomplete() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    typeTextIntoEditor(SPACE.toString());
    action.keyUp(CONTROL).perform();
  }

  /** Closes autocomplete container by "Escape" button and checks that container is closed. */
  public void closeAutocomplete() {
    typeTextIntoEditor(ESCAPE.toString());
    waitAutocompleteContainerIsClosed();
  }

  /** Waits until autocomplete form is opened. */
  public void waitAutocompleteContainer() {
    seleniumWebDriverHelper.waitVisibility(autocompleteContainer, ELEMENT_TIMEOUT_SEC);
  }

  /** Waits until autocomplete form is closed. */
  public void waitAutocompleteContainerIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(AUTOCOMPLETE_CONTAINER));
  }

  /**
   * Waits specified {@code expectedProposal} in autocomplete container.
   *
   * @param expectedProposal text which should be present in the container
   */
  public void waitProposalIntoAutocompleteContainer(final String expectedProposal) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getAllVisibleTextFromAutocomplete().contains(expectedProposal));
  }

  /**
   * Waits specified {@code proposals} in autocomplete container.
   *
   * @param expectedProposals text which should be present in the container
   */
  public void waitProposalsIntoAutocompleteContainer(List<String> expectedProposals) {
    expectedProposals.forEach(
        proposal -> {
          waitProposalIntoAutocompleteContainer(proposal);
        });
  }

  /**
   * Waits a marker with specified {@code markerLocator} on the defined {@code position}
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @param position line`s number where marker is expected
   */
  public void waitMarkerInPosition(MarkerLocator markerLocator, int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(markerLocator.get(), position)), ELEMENT_TIMEOUT_SEC);
    setCursorToLine(position);
    expectedNumberOfActiveLine(position);
  }

  /** Waits for "no Git change" markers in the opened editor. */
  public void waitNoGitChangeMarkers() {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    getListGitMarkers()
                        .stream()
                        .allMatch(element -> "".equals(element.getAttribute("class"))));
  }

  /**
   * Waits for "Git insertion" marker between {@code startLine} and {@code endLine}, inclusive.
   *
   * @param startLine line number of the markers start
   * @param endLine line number of the markers end
   */
  public void waitGitInsertionMarkerInPosition(int startLine, int endLine) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  for (int i = startLine; i <= endLine; i++) {
                    if (!"git-change-marker insertion"
                        .equals(getListGitMarkers().get(i).getAttribute("class"))) {
                      return false;
                    }
                  }
                  return true;
                });
  }

  /**
   * Waits for "Git modification" marker between {@code startLine} and {@code endLine} including
   * this two lines.
   *
   * @param startLine line number of the markers start
   * @param endLine line number of the markers end
   */
  public void waitGitModificationMarkerInPosition(int startLine, int endLine) {
    webDriverWaitFactory
        .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  for (int i = startLine; i <= endLine; i++) {
                    WebElement webElement = getListGitMarkers().get(i);
                    webDriverWaitFactory.get().until(visibilityOf(webElement));
                    if (!"git-change-marker modification"
                        .equals(webElement.getAttribute("class"))) {
                      return false;
                    }
                  }
                  return true;
                });
  }

  /**
   * Waits for "Git deletion" marker in the specified {@code line}.
   *
   * @param line line's number where the marker should be displayed
   */
  public void waitGitDeletionMarkerInPosition(int line) {
    webDriverWaitFactory
        .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    "git-change-marker deletion"
                        .equals(getListGitMarkers().get(line).getAttribute("class")));
  }

  /**
   * get the list of git markers web-elements in the editor
   *
   * @return the list of git markers web-elements
   */
  private List<WebElement> getListGitMarkers() {
    List<WebElement> rulerVcsElements =
        seleniumWebDriverHelper.waitPresenceOfAllElements(By.xpath(VCS_RULER));
    List<WebElement> subList = rulerVcsElements.subList(1, rulerVcsElements.size() - 1);
    webDriverWaitFactory.get().until(visibilityOfAllElements(subList));
    return rulerVcsElements;
  }

  /**
   * Waits marker with specified {@code markerLocator} on the defined {@code line} and click on it
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @param line line's number, where marker is expected
   */
  public void clickOnMarker(MarkerLocator markerLocator, int line) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(markerLocator.get(), line)));
  }

  /**
   * Waits marker with specified {@code markerLocator} on the defined {@code line number} and move
   * cursor to it.
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @param line line's number, where marker is expected
   */
  public void moveToMarker(MarkerLocator markerLocator, int line) {
    WebElement marker =
        seleniumWebDriverHelper.waitVisibility(By.xpath(format(markerLocator.get(), line)));
    seleniumWebDriverHelper.moveCursorTo(marker);
  }

  /**
   * Waits until marker with specified {@code markerLocator} be invisible on the defined {@code
   * position}
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @param position line's number, where marker should not be displayed
   */
  public void waitMarkerInvisibility(MarkerLocator markerLocator, int position) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(markerLocator.get(), position)), ELEMENT_TIMEOUT_SEC);
    expectedNumberOfActiveLine(position);
  }

  /**
   * Waits until all markers with specified {@code markerLocator} be invisible
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void waitAllMarkersInvisibility(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(markerLocator.get()));
  }

  /**
   * Waits until at list one marker with specified {@code markerLocator} be visible
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void waitCodeAssistMarkers(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath(markerLocator.get()), ELEMENT_TIMEOUT_SEC);
  }

  /** @return text from autocomplete */
  public String getAllVisibleTextFromAutocomplete() {
    waitAutocompleteContainer();
    return autocompleteContainer.getText();
  }

  /**
   * Selects specified {@code item} in the autocomplete proposal container, and presses "ENTER".
   *
   * @param item item in the autocomplete proposal container.
   */
  public void enterAutocompleteProposal(String item) {
    selectAutocompleteProposal(item);
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(HIGHLIGHT_ITEM_PATTERN, item)));
    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * Selects specified {@code item} in the autocomplete container and sends double click to item.
   *
   * @param item item in the autocomplete proposal container.
   */
  public void selectItemIntoAutocompleteAndPerformDoubleClick(String item) {
    selectAutocompleteProposal(item);

    seleniumWebDriverHelper.waitVisibility(By.xpath(format(HIGHLIGHT_ITEM_PATTERN, item)));
    seleniumWebDriverHelper.doubleClick();
  }

  /**
   * Selects specified {@code item} in the autocomplete container.
   *
   * @param item item from autocomplete container.
   */
  public void selectAutocompleteProposal(String item) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(AUTOCOMPLETE_CONTAINER + "/li/span[text()='%s']", item)));
  }

  /**
   * Moves mouse to the marker with specified {@code markerLocator} and waits until "assist content
   * container" is visible.
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void moveToMarkerAndWaitAssistContent(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.moveCursorTo(By.xpath(markerLocator.get()));
    waitAnnotationCodeAssistIsOpen();
  }

  /** Waits until annotations code assist is opened. */
  public void waitAnnotationCodeAssistIsOpen() {
    seleniumWebDriverHelper.waitVisibility(assistContentContainer, ELEMENT_TIMEOUT_SEC);
  }

  /** Waits until annotations code assist is closed. */
  public void waitAnnotationCodeAssistIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(ASSIST_CONTENT_CONTAINER));
  }

  /** Waits until specified {@code expectedText} is present in annotation code assist. */
  public void waitTextIntoAnnotationAssist(final String expectedText) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  return seleniumWebDriverHelper
                      .waitVisibilityOfAllElements(By.xpath(ASSIST_CONTENT_CONTAINER + "//span"))
                      .stream()
                      .map(item -> item.getText().contains(expectedText))
                      .collect(toList())
                      .contains(true);
                });
  }

  /**
   * Waits until specified {@code expectedText} is present in proposition assist panel.
   *
   * @param expectedText expected text in error proposition
   */
  public void waitTextIntoFixErrorProposition(final String expectedText) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getAllVisibleTextFromProposition().contains(expectedText));
  }

  /**
   * Gets all visible text from proposition container.
   *
   * @return text from proposition assist container
   */
  public String getAllVisibleTextFromProposition() {
    waitPropositionAssistContainer();
    return seleniumWebDriverHelper.waitVisibilityAndGetText(propositionContainer);
  }

  /** Waits until assist proposition container is opened. */
  public void waitPropositionAssistContainer() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(PROPOSITION_CONTAINER), ELEMENT_TIMEOUT_SEC);
  }

  /** Waits until assist proposition container is closed */
  public void waitErrorPropositionPanelClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PROPOSITION_CONTAINER));
  }

  /** Launches the "code assist proposition" container */
  public void launchPropositionAssistPanel() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(ALT).perform();
    action.sendKeys(ENTER).perform();
    action.keyUp(ALT).perform();
    waitPropositionAssistContainer();
  }

  /** Launches the "code assist proposition" container in JS files */
  public void launchPropositionAssistPanelForJSFiles() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(LEFT_CONTROL).perform();
    action.sendKeys(SPACE).perform();
    action.keyUp(LEFT_CONTROL).perform();
  }

  /**
   * Applies the first item in the "assist proposition" container by single click and performs
   * double click to it.
   */
  public void selectFirstItemIntoFixErrorPropByDoubleClick() {
    String tmpLocator = PROPOSITION_CONTAINER + "/li";
    seleniumWebDriverHelper.waitAndClick(By.xpath(tmpLocator));
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(tmpLocator));

    waitErrorPropositionPanelClosed();
  }

  /**
   * Applies the first item in the "assist proposition" container by single click and sends "enter"
   * key to it.
   */
  public void selectFirstItemIntoFixErrorPropByEnter() {
    String tmpLocator = PROPOSITION_CONTAINER + "/li";
    seleniumWebDriverHelper.waitAndClick(By.xpath(tmpLocator));
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(tmpLocator));

    waitErrorPropositionPanelClosed();
  }

  /**
   * Applies specified {@code item} in the assist proposition container by moving cursor and
   * performs double click on item.
   *
   * @param item visible name of the item which should be applied
   */
  public void enterTextIntoFixErrorPropByDoubleClick(String item) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(PROPOSITION_CONTAINER + "/li/span[text()=\"%s\"]", item)));
  }

  /**
   * Applies specified {@code item} in the proposition container by single click and sends "Enter"
   * key to it.
   *
   * @param item visible name of the item which should be applied
   */
  public void enterTextIntoFixErrorPropByEnter(String item) {
    seleniumWebDriverHelper.waitAndClick(
        propositionContainer.findElement(By.xpath(format("//li//span[text()=\"%s\"]", item))));

    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * Applies specified {@code item} with defined {@code description} in Js file editor by single
   * click and sends "Enter" key to it.
   *
   * @param item visible name of the item which should be applied
   */
  public void enterTextIntoFixErrorPropByEnterForJsFiles(String item, String description) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(
            format("//span[text()='%s']/span[text()='%s']/ancestor::div[1]", description, item)));
  }

  /** Invokes the 'Show hints' popup panel. */
  public void callShowHintsPopUp() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("p")
        .keyUp(CONTROL)
        .perform();
  }

  /** Waits until the 'Show hints' popup panel is opened. */
  public void waitShowHintsPopUpOpened() {
    seleniumWebDriverHelper.waitVisibility(showHintsPopUp);
  }

  /** Waits until the 'Show hints' popup panel is closed. */
  public void waitShowHintsPopUpClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.SHOW_HINTS_POP_UP));
  }

  /**
   * Waits expected text in the 'Show hints' popup panel.
   *
   * @param expText text which should be present in the popup panel
   */
  public void waitExpTextIntoShowHintsPopUp(String expText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getTextFromShowHintsPopUp().contains(expText));
  }

  /** Gets visible text from the 'Show hints' popup panel. */
  public String getTextFromShowHintsPopUp() {
    testWebElementRenderChecker.waitElementIsRendered(By.xpath("//div[@class='gwt-PopupPanel']"));
    return seleniumWebDriverHelper.waitVisibilityAndGetText(showHintsPopUp);
  }

  /**
   * Waits while open file tab with specified name becomes without '*' unsaved status.
   *
   * @param nameOfFile title of the tab which should be checked
   */
  public void waitTabFileWithSavedStatus(String nameOfFile) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(TAB_WITH_UNSAVED_STATUS, nameOfFile)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits active tab with specified {@code nameOfFile}.
   *
   * @param nameOfFile title of the tab which should be checked
   */
  public void waitActiveTabFileName(String nameOfFile) {
    seleniumWebDriverHelper.waitPresence(By.xpath(format(ACTIVE_TAB_FILE_NAME, nameOfFile)));
  }

  /** Checks that all files have been closed. (Checks disappearance of all text areas and tabs) */
  public void waitWhileAllFilesWillClosed() {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath("//div[@id='gwt-debug-editorPartStack-tabsPanel']//div[text()]"),
        ELEMENT_TIMEOUT_SEC);

    seleniumWebDriverHelper.waitInvisibility(By.xpath(ORION_ACTIVE_EDITOR_CONTAINER_XPATH));
  }

  /**
   * Selects editor's tab with specified {@code nameOfFile}.
   *
   * @param nameOfFile title of the tab which should be selected
   */
  public void selectTabByName(String nameOfFile) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)));
  }

  /**
   * Waits until editor's tab with specified {@code fileName} and {@code tabColor} is visible.
   *
   * @param fileName title of the tab which should be checked
   * @param tabColor color which editor's tab should be colored by
   */
  public void waitTabWithNameAndColor(String fileName, TabColor tabColor) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(TAB_FILE_NAME_AND_STYLE, fileName, tabColor.get())));
  }

  /**
   * Waits until editor's tab with specified {@code fileName} is in yellow color.
   *
   * @param fileName title of editor's tab which should be checked
   */
  public void waitYellowTab(String fileName) {
    waitTabWithNameAndColor(fileName, YELLOW);
  }

  /**
   * Waits until editor's tab with specified {@code fileName} is in green color.
   *
   * @param fileName title of editor's tab which should be checked
   */
  public void waitGreenTab(String fileName) {
    waitTabWithNameAndColor(fileName, GREEN);
  }

  /**
   * Waits until editor's tab with specified {@code fileName} is in blue color.
   *
   * @param fileName title of editor's tab which should be checked
   */
  public void waitBlueTab(String fileName) {
    waitTabWithNameAndColor(fileName, BLUE);
  }

  /**
   * Waits until editor's tab with specified {@code fileName} is in default color.
   *
   * <p>Note! Default color depends on tab's focus.
   *
   * @param fileName title of editor's tab which should be checked
   */
  public void waitDefaultColorTab(final String fileName) {
    waitActive();

    final String expectedColor =
        waitTabVisibilityAndCheckFocus(fileName) ? FOCUSED_DEFAULT.get() : UNFOCUSED_DEFAULT.get();

    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriverHelper
                        .waitVisibility(By.xpath(format(TAB_FILE_NAME_XPATH, fileName)))
                        .getCssValue("color")
                        .equals(expectedColor));
  }

  /**
   * Waits until editor's tab with specified {@code fileName} is visible and focused.
   *
   * @param fileName title of editor's tab which should be checked
   * @return true - if tab is focused, false - if not
   */
  public boolean waitTabVisibilityAndCheckFocus(String fileName) {
    return null
        != seleniumWebDriverHelper
            .waitVisibility(By.xpath(format(TAB_FILE_NAME_XPATH + "/parent::div", fileName)))
            .getAttribute("focused");
  }

  /**
   * Waits until editor's tab with specified {@code nameOfFile} is not presented.
   *
   * @param nameOfFile title of editor's tab which should be checked
   */
  public void waitTabIsNotPresent(String nameOfFile) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Gets value of the "path" attribute from {@link WebElement} which defined by {@code
   * nameOfOpenedFile}.
   *
   * @param nameOfOpenedFile title of file from which "path" attribute should be got
   * @return value of the "path" attribute
   */
  public String getAssociatedPathFromTheTab(String nameOfOpenedFile) {
    return seleniumWebDriverHelper
        .waitVisibility(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfOpenedFile)))
        .getAttribute("path");
  }

  /**
   * Waits until editor's tab with specified {@code nameOfFile} is present in editor.
   *
   * @param nameOfFile name of editor's tab which should be checked
   */
  public void waitTabIsPresent(String nameOfFile) {
    waitTabIsPresent(nameOfFile, LOAD_PAGE_TIMEOUT_SEC);
  }

  /**
   * Waits during {@code timeout} until editor's tab with specified {@code nameOfFile} is present in
   * editor.
   *
   * @param nameOfFile title of editor's tab which should be checked
   * @param timeout waiting time in seconds
   */
  public void waitTabIsPresent(String nameOfFile, int timeout) {
    seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)), timeout);

    loader.waitOnClosed();
  }

  /**
   * Deletes line with specified {@code numberOfLine}.
   *
   * @param numberOfLine number of line which should be deleted
   */
  public void selectLineAndDelete(int numberOfLine) {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    setCursorToLine(numberOfLine);
    typeTextIntoEditor(HOME.toString());
    action.keyDown(SHIFT).perform();
    typeTextIntoEditor(END.toString());
    action.keyUp(SHIFT).perform();
    typeTextIntoEditor(DELETE.toString());
    loader.waitOnClosed();
  }

  /** Deletes current editor's line. */
  public void selectLineAndDelete() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    typeTextIntoEditor(HOME.toString());
    action.keyDown(SHIFT).perform();
    typeTextIntoEditor(END.toString());
    action.keyUp(SHIFT).perform();
    typeTextIntoEditor(DELETE.toString());
  }

  /** Deletes current editor's line by "Ctrl"+"D" keys pressing. */
  public void deleteCurrentLine() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("d")
        .keyUp(CONTROL)
        .perform();

    loader.waitOnClosed();
  }

  /** Deletes current line by "Ctrl"+"D" keys pressing and inserts new line instead. */
  public void deleteCurrentLineAndInsertNew() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("d")
        .keyUp(CONTROL)
        .sendKeys(ARROW_UP)
        .sendKeys(END)
        .sendKeys(ENTER)
        .perform();
  }

  /** Deletes all editor's content by "Ctrl"+"A" keys pressing and "Delete" key pressing. */
  public void deleteAllContent() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("a")
        .keyUp(CONTROL)
        .sendKeys(DELETE)
        .perform();

    waitEditorIsEmpty();
  }

  /** Waits until the IDE line panel with numbers of lines is visible. */
  public void waitDebugerLineIsVisible(int line) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(DEBUGGER_PREFIX_XPATH, line)));
  }

  /**
   * Waits until breakpoint with inactive state is in defined {@code position}.
   *
   * @param position number of line where breakpoint should be present
   */
  public void waitBreakPointWithInactiveState(int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DEBUGGER_BREAK_POINT_INACTIVE, position)));
  }

  /**
   * Sets breakpoint on specified {@code position} in the IDE breakpoint panel.
   *
   * @param position number of line where breakpoint should be placed
   */
  public void setInactiveBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));

    waitBreakPointWithInactiveState(position);
  }

  /**
   * Sets breakpoint on specified {@code position} in the IDE breakpoint panel and waits on active
   * state of it.
   *
   * @param position number of line where breakpoint should be placed
   */
  public void setBreakPointAndWaitActiveState(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));

    waitActiveBreakpoint(position);
  }

  /**
   * Sets breakpoint to the specified {@code position} in the editor.
   *
   * @param position number of line where breakpoint should be placed
   */
  public void setBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));
  }

  /**
   * Waits until breakpoint with active state is in defined {@code position}.
   *
   * @param position number of line where breakpoint should be placed
   */
  public void waitActiveBreakpoint(int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DEBUGGER_BREAK_POINT_ACTIVE, position)), LOADER_TIMEOUT_SEC);
  }

  /**
   * Waits until breakpoint with inactive state is in defined {@code position}.
   *
   * @param position number of line where breakpoint should be placed
   */
  public void waitInactiveBreakpoint(int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DEBUGGER_BREAK_POINT_INACTIVE, position)), LOADER_TIMEOUT_SEC);
  }

  /**
   * Waits until breakpoint with specified {@code activeState} is present in defined {@code
   * lineNumber}.
   *
   * @param lineNumber number of line where breakpoint should be placed
   * @param activeState state of breakpoint
   */
  public void waitConditionalBreakpoint(int lineNumber, boolean activeState) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(
            format(DEBUGGER_BREAKPOINT_CONDITION, activeState ? "active" : "inactive", lineNumber)),
        LOADER_TIMEOUT_SEC);
  }

  /**
   * Waits until breakpoint with "disabled" status is present in the line with specified {@code
   * lineNumber}.
   *
   * @param lineNumber number of line where breakpoint should be placed
   */
  public void waitDisabledBreakpoint(int lineNumber) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DEBUGGER_BREAKPOINT_DISABLED, lineNumber)), LOADER_TIMEOUT_SEC);
  }

  /** Waits until current editor's tab is without visible text. */
  public void waitEditorIsEmpty() {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().isEmpty());
  }

  /** Waits until javadoc popup is opened. */
  public void waitJavaDocPopUpOpened() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(JAVA_DOC_POPUP), ELEMENT_TIMEOUT_SEC);
  }

  /** Waits until javadoc popup is closed */
  public void waitJavaDocPopUpClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(JAVA_DOC_POPUP));
  }

  /**
   * Waits until {@code expectedText} is present in javadoc's popup body, and switches to parent
   * frame.
   */
  public void checkTextToBePresentInJavaDocPopUp(String expectedText) {
    waitTextInJavaDoc(expectedText);
    seleniumWebDriver.switchTo().parentFrame();
  }

  /**
   * Waits until {@code expectedText} is present in javadoc's body.
   *
   * <p>Note! Sometimes javadoc displays with delays, in this case empty frame displaying first, and
   * method waits {@code expectedText} in this empty frame even if javadoc with {@code expectedText}
   * was loaded successfully. That's why frame switching is used in the wait logic.
   *
   * @param expectedText visible text which should be present in javadoc
   */
  private void waitTextInJavaDoc(String expectedText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  waitAvailabilityAndSwitchToJavaDocFrame();

                  if (waitAndCheckTextPresenceInJavaDoc(expectedText)) {
                    return true;
                  }

                  seleniumWebDriver.switchTo().parentFrame();
                  return false;
                });
  }

  /**
   * Waits until javadoc body is visible and checks presence of {@code expectedText}.
   *
   * <p>Note! {@link SeleniumWebDriver} should be switched to the javadoc frame.
   *
   * <p>Please use {@link CodenvyEditor#waitAvailabilityAndSwitchToJavaDocFrame()} method for
   * switching to javadoc frame.
   *
   * @param expectedText text which should be present in javadoc body
   * @return true - if {@code expectedText} is present in javadoc body, false - if not
   */
  public boolean waitAndCheckTextPresenceInJavaDoc(String expectedText) {
    return seleniumWebDriverHelper
        .waitVisibility(By.tagName("body"))
        .getText()
        .contains(expectedText);
  }

  /**
   * Waits until frame, which contains javadoc popup, is available and switches the {@link
   * SeleniumWebDriver} to it.
   */
  public void waitAvailabilityAndSwitchToJavaDocFrame() {
    seleniumWebDriverHelper.waitAndSwitchToFrame(By.xpath(JAVA_DOC_POPUP));
  }

  /**
   * Checks text after clicking on link in javadoc's popup
   *
   * @param text text which should be present in the page which opened after clicking on the link
   * @param textLink visible link's text
   */
  public void checkTextAfterGoToLinkInJavaDocPopUp(String text, String textLink) {
    seleniumWebDriverHelper.waitAndSwitchToFrame(By.xpath(JAVA_DOC_POPUP));

    WebElement link =
        seleniumWebDriverHelper.waitVisibility(By.xpath(format("//a[text()='%s']", textLink)));

    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> !link.getAttribute("href").isEmpty());

    seleniumWebDriver.get(link.getAttribute("href"));

    webDriverWaitFactory.get().until(textToBePresentInElementLocated(By.tagName("body"), text));

    seleniumWebDriver.navigate().back();
    seleniumWebDriver.switchTo().parentFrame();
  }

  /** Opens javadoc's popup */
  public void openJavaDocPopUp() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys(Keys.chord("q"))
        .keyUp(CONTROL)
        .perform();
  }

  /**
   * Waits until {@code expectedText} is present in the current active line of the current tab.
   *
   * @param expectedText text which should be present in the checking line
   */
  public void waitTextElementsActiveLine(final String expectedText) {
    waitActive();

    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    seleniumWebDriverHelper
                        .waitVisibilityAndGetText(By.xpath(ACTIVE_LINES_XPATH))
                        .contains(expectedText));
  }

  /**
   * Gets current cursor position, it means line's number and char's number.
   *
   * @return cursor position which defined in line's number and char's number.
   */
  public Pair<Integer, Integer> getCurrentCursorPositionsFromVisible() {
    waitActive();
    WebElement currentActiveElement =
        activeLineNumbers.stream().filter(webElement -> webElement.isDisplayed()).findFirst().get();
    return getCursorPositionFromWebElement(currentActiveElement);
  }

  /**
   * Waits active and focused editor's tab and gets cursor position.
   *
   * <p>Note! Before usage, ensure the checking editor's tab is active.
   *
   * @return cursor position which defined in line's number and char's number.
   */
  public Pair<Integer, Integer> getCursorPositionsFromActive() {
    String xpathToCurrentActiveCursorPosition =
        "//div[@active and @focused]/parent::div[@id='gwt-debug-multiSplitPanel-tabsPanel']/div[@active and @focused]/parent::div[@id='gwt-debug-multiSplitPanel-tabsPanel']/parent::div/parent::div/following-sibling::div//div[@active]//div[@id='gwt-debug-cursorPosition']";
    waitActive();
    return getCursorPositionFromWebElement(
        seleniumWebDriverHelper.waitVisibility(By.xpath(xpathToCurrentActiveCursorPosition)));
  }

  /**
   * Waits until cursor is placed in the position which specified {@code linePosition} and {@code
   * charPosition}.
   *
   * @param linePosition line's number where cursor should be placed
   * @param charPosition char's position where cursor should be placed
   */
  public void waitCursorPosition(final int linePosition, final int charPosition) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    (getCursorPositionsFromActive().first == linePosition)
                        && (getCursorPositionsFromActive().second == charPosition));
  }

  private Pair<Integer, Integer> getCursorPositionFromWebElement(WebElement webElement) {
    int[] currentCursorPositions =
        Arrays.asList(webElement.getText().split(":"))
            .stream()
            .mapToInt(Integer::parseInt)
            .toArray();
    return new Pair<Integer, Integer>(currentCursorPositions[0], currentCursorPositions[1]);
  }

  /** Gets number of the current line where cursor is placed */
  public int getPositionVisible() {
    waitActive();
    return getCurrentCursorPositionsFromVisible().first;
  }

  /**
   * Checks that active line has specified {@code lineNumber}.
   *
   * @param lineNumber line's number which should be active
   */
  public void expectedNumberOfActiveLine(final int lineNumber) {
    waitActive();
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> lineNumber == getPositionVisible());
  }

  /**
   * Gets number of the char's position on which cursor is placed.
   *
   * @return number of the char's position on which cursor is placed
   */
  public int getPositionOfChar() {
    return getCurrentCursorPositionsFromVisible().second;
  }

  /**
   * Waits until cursor is placed in specified {@code linePosition} and {@code charPosition}.
   *
   * @param linePosition line's number where cursor is expected
   * @param charPosition char's number where cursor is expected
   */
  public void waitSpecifiedValueForLineAndChar(final int linePosition, final int charPosition) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    (getPositionVisible() == linePosition)
                        && (getPositionOfChar() == charPosition));
  }

  /**
   * Waits until cursor is placed in specified {@code lineAndChar} position.
   *
   * @param lineAndChar expected line and char position in format "1:25"
   */
  public void waitSpecifiedValueForLineAndChar(String lineAndChar) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(POSITION_CURSOR_NUMBER, lineAndChar)));
  }

  /** Launches refactor for local variables by keyboard */
  public void launchLocalRefactor() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(SHIFT)
        .sendKeys(F6)
        .keyUp(SHIFT)
        .perform();

    loader.waitOnClosed();
  }

  /**
   * The first invocation of launchLocalRefactor() runs local refactoring, the second invocation
   * opens "Refactor" form.
   */
  public void launchRefactorForm() {
    launchLocalRefactor();
    launchLocalRefactor();
  }

  /**
   * Clicks on the selected element in the editor.
   *
   * @param nameElement visible name of the element
   */
  public void clickOnSelectedElementInEditor(String nameElement) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(SELECTED_ITEM_IN_EDITOR, nameElement)));

    waitActive();
  }

  /**
   * Waits until the 'Implementation(s)' form is opened.
   *
   * @param fileName is name of the selected file
   */
  public void waitImplementationFormIsOpen(String fileName) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(IMPLEMENTATION_CONTAINER, fileName)));
  }

  /** Waits until the 'Implementation(s)' form is closed */
  public void waitImplementationFormIsClosed(String fileName) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(IMPLEMENTATION_CONTAINER, fileName)));
  }

  /** Launches the 'Implementation(s)' form by keyboard */
  public void launchImplementationFormByKeyboard() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .keyDown(ALT)
        .sendKeys("b")
        .keyUp(ALT)
        .keyUp(CONTROL)
        .perform();
  }

  /** Performs "Escape" button pushing. Mainly, may be used for closing forms in the editor. */
  public void cancelFormInEditorByEscape() {
    typeTextIntoEditor(ESCAPE.toString());
  }

  /**
   * Waits until {@code expectedText} text is present in the 'Implementation' form.
   *
   * @param expectedText text which should be present in the form
   */
  public void waitTextInImplementationForm(String expectedText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromImplementationForm().contains(expectedText));
  }

  /** Gets visible text from the 'Implementation(s)' form. */
  public String getTextFromImplementationForm() {
    return seleniumWebDriverHelper.waitVisibility(implementationContent).getText();
  }

  /**
   * Performs double click on the specified {@code itemName} in the 'Implementation' form.
   *
   * @param itemName visible name of the element which should be clicked
   */
  public void chooseImplementationByDoubleClick(String itemName) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(IMPLEMENTATIONS_ITEM, itemName)));
  }

  /**
   * Selects specified {@code itemName} in the 'Implementation' form.
   *
   * @param itemName visible name of the element which should be clicked
   */
  public void selectImplementationByClick(String itemName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(IMPLEMENTATIONS_ITEM, itemName)));
  }

  /**
   * Checks that specified {@code text} is present only once.
   *
   * @param text text which should be checked
   * @return true - if text is present only once, false - if not
   */
  public boolean checkWhatTextLinePresentOnce(String text) {
    String visibleTextFromEditor = getVisibleTextFromEditor();
    int index = visibleTextFromEditor.indexOf(text);
    if (index > 0) {
      return !visibleTextFromEditor
          .substring(index + 1, visibleTextFromEditor.length())
          .contains(text);
    }
    return false;
  }

  /**
   * Clicks on web element by Xpath
   *
   * @param xPath is Xpath of web element
   */
  public void clickOnElementByXpath(String xPath) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(xPath));
  }

  /**
   * Gets quantity of visible markers with specified {@code markerLocator}
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @return markers quantity
   */
  public int getMarkersQuantity(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_OVERVIEW));

    return seleniumWebDriverHelper
        .waitVisibilityOfAllElements(By.xpath(markerLocator.get()))
        .size();
  }

  /**
   * Waits until annotations with specified {@code markerLocator} visible
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void waitAnnotationsAreNotPresent(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(markerLocator.get()));
  }

  /**
   * Removes all file's content since specified {@code numberOfLine} to the end of file.
   *
   * @param numberOfLine number of line since which the file's content should be removed
   */
  public void removeLineAndAllAfterIt(int numberOfLine) {
    setCursorToLine(numberOfLine);
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .keyDown(SHIFT)
        .sendKeys(END)
        .keyUp(CONTROL)
        .keyUp(SHIFT)
        .sendKeys(DELETE)
        .perform();
  }

  /** Waits until the ruler overview is present. */
  public void waitRulerOverviewIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_OVERVIEW));
  }

  /** Waits until the ruler overview is not present. */
  public void waitRulerOverviewIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_OVERVIEW));
  }

  /** Waits until the ruler annotation is present. */
  public void waitRulerAnnotationsIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_ANNOTATIONS));
  }

  /** Waits until the ruler annotation is not present. */
  public void waitRulerAnnotationsIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_ANNOTATIONS));
  }

  /** Waits until the ruler line is present. */
  public void waitRulerLineIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_LINES));
  }

  /** Waits until the ruler line is not present. */
  public void waitRulerLineIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_LINES));
  }

  /** Waits until the ruler folding is present. */
  public void waitRulerFoldingIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_FOLDING));
  }

  /** Waits until the ruler folding is not present. */
  public void waitRulerFoldingIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_FOLDING));
  }

  /** Waits until the all punctuation separators are present. */
  public void waitAllPunctuationSeparatorsArePresent() {
    seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(PUNCTUATION_SEPARATOR));
  }

  /** Waits until the all punctuation separators are not present. */
  public void waitAllPunctuationSeparatorsAreNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PUNCTUATION_SEPARATOR));
  }

  /** Waits until the text view ruler is present. */
  public void waitTextViewRulerIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TEXT_VIEW_RULER));
  }

  /** Waits until the text view ruler is not present. */
  public void waitTextViewRulerIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(TEXT_VIEW_RULER));
  }

  /** Clicks on 'Download sources' link in the editor. */
  public void clickOnDownloadSourcesLink() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.DOWNLOAD_SOURCES_LINK));
  }

  /**
   * Changes width of the editor's window.
   *
   * @param xOffset horizontal move offset.
   */
  public void changeWidthWindowForEditor(int xOffset) {
    WebElement moveElement =
        seleniumWebDriverHelper.waitVisibility(
            By.xpath("//div[@id='gwt-debug-navPanel']/parent::div/following::div[1]"));

    actionsFactory.createAction(seleniumWebDriver).dragAndDropBy(moveElement, xOffset, 0).perform();
  }

  /**
   * Opens list of the tabs.
   *
   * <p>Note! This is possible if opened tabs don't fit in the tab bar.
   */
  public void openTabList() {
    seleniumWebDriverHelper.waitAndClick(By.id(TAB_LIST_BUTTON));
  }

  /**
   * Waits until the editor's tab with specified {@code tabName} is present in the tab list.
   *
   * @param tabName title of the editor's tab which should be checked
   */
  public void waitTabIsPresentInTabList(String tabName) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(ITEM_TAB_LIST, tabName)));
  }

  /**
   * Waits until the tab with specified {@code tabName} is not present in the tab list.
   *
   * @param tabName title of the editor's tab which should be checked
   */
  public void waitTabIsNotPresentInTabList(String tabName) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(ITEM_TAB_LIST, tabName)));
  }

  public void waitCountTabsWithProvidedName(int countTabs, String tabName) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> countTabs == getAllTabsWithProvidedName(tabName).size());
  }

  /**
   * Clicks on the tab with specified {@code tabName} in the tab list.
   *
   * @param tabName title of the editor's tab which should be checked
   */
  public void clickOnTabInTabList(String tabName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(ITEM_TAB_LIST, tabName)));
  }

  /**
   * Selects window of certain {@code index} of split editor's tab with specified {@code tabName}.
   *
   * @param index index of the window of split tab which should be selected, starting from "0"
   * @param tabName title of the editor's tab which is split
   */
  public void selectTabByIndexEditorWindow(int index, String tabName) {
    seleniumWebDriverHelper
        .waitPresenceOfAllElements(By.xpath(format(TAB_FILE_NAME_XPATH, tabName)))
        .get(index)
        .click();
  }

  /**
   * Selects window of certain {@code index} of split editor's tab with specified {@code tabName}
   * and opens context menu for this tab.
   *
   * @param index index of the window which should be selected, starting from "0"
   * @param tabName title of the editor's tab for which menu should be opened
   */
  public void selectTabByIndexEditorWindowAndOpenMenu(int index, String tabName) {
    List<WebElement> windowList =
        seleniumWebDriverHelper.waitPresenceOfAllElements(
            By.xpath(format(TAB_FILE_CLOSE_ICON, tabName)));

    actionsFactory.createAction(seleniumWebDriver).contextClick(windowList.get(index)).perform();
  }

  /**
   * Checks the editor's tab with specified {@code tabName} is present only once in the editor.
   *
   * @param tabName title of the editor's tab for which menu should be opened
   */
  public boolean tabIsPresentOnce(String tabName) {
    return 1
        == seleniumWebDriverHelper
            .waitPresenceOfAllElements(By.id(EDITOR_TABS_PANEL))
            .stream()
            .filter(element -> element.getText().contains(tabName))
            .collect(toList())
            .size();
  }

  /**
   * Auxiliary method for reading text from Orion editor.
   *
   * @param lines List of WebElements that contain text (mostly <div> tags after
   *     ORION_ACTIVE_EDITOR_CONTAINER_XPATH locator)
   * @return the normalized text
   */
  private String getTextFromOrionLines(List<WebElement> lines) {
    StringBuilder stringBuilder = new StringBuilder();

    try {
      stringBuilder = waitLinesElementsPresenceAndGetText(lines);
    }
    // If an editor do not attached to the DOM (we will have state element exception). We wait
    // attaching 2 second and try to read text again.
    catch (WebDriverException ex) {
      WaitUtils.sleepQuietly(2);

      stringBuilder.setLength(0);
      stringBuilder = waitLinesElementsPresenceAndGetText(lines);
    }
    return stringBuilder.toString();
  }

  private StringBuilder waitLinesElementsPresenceAndGetText(List<WebElement> lines) {
    StringBuilder stringBuilder = new StringBuilder();

    lines.forEach(
        line -> {
          List<WebElement> nestedElements = line.findElements(By.tagName("span"));

          for (int a = 0; a < nestedElements.size(); a++) {
            webDriverWaitFactory
                .get()
                .until(presenceOfNestedElementLocatedBy(line, By.xpath(format("span[%s]", a + 1))));
          }

          nestedElements.remove(nestedElements.size() - 1);
          nestedElements.forEach(elem -> stringBuilder.append(elem.getText()));
          stringBuilder.append("\n");
        });

    return stringBuilder.deleteCharAt(stringBuilder.length() - 1);
  }

  /** Opens context menu into editor */
  public void openContextMenuInEditor() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .contextClick(seleniumWebDriverHelper.waitVisibility(activeEditorContainer))
        .perform();

    waitContextMenu();
  }

  /**
   * Opens context menu for the specified {@code selectedItem} in the editor.
   *
   * @param selectedElement visible text in the editor on which context click should be performed
   */
  public void openContextMenuOnElementInEditor(String selectedElement) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .contextClick(
            seleniumWebDriverHelper.waitVisibility(
                By.xpath(format(SELECTED_ITEM_IN_EDITOR, selectedElement))))
        .perform();

    waitContextMenu();
  }

  /** Waits until context menu form is opened. */
  public void waitContextMenu() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(CONTEXT_MENU));
  }

  /** Waits until context menu form is not present. */
  public void waitContextMenuIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(CONTEXT_MENU));
  }

  /** Check if context menu is visible. */
  public boolean isContextMenuPresent() {
    return seleniumWebDriverHelper.isVisible(By.xpath(CONTEXT_MENU));
  }

  /**
   * Clicks on {@code item} in context menu.
   *
   * @param item editor context menu item which defined in {@link ContextMenuLocator}
   */
  public void clickOnItemInContextMenu(ContextMenuLocator item) {
    seleniumWebDriverHelper.waitAndClick(item.get());
    loader.waitOnClosed();
  }

  /**
   * Get html code of java doc of autocomplete proposal.
   *
   * @return html code of JavaDoc of already opened autocomplete proposal by making request on 'src'
   *     attribute of iframe of JavaDoc popup.
   */
  public String getAutocompleteProposalJavaDocHtml() throws IOException {
    waitJavaDocPopupSrcAttributeIsNotEmpty();
    return getJavaDocPopupText();
  }

  /**
   * Waits until specified {@code expectedText} is present in javadoc.
   *
   * @param expectedText text which should be present in javadoc
   */
  public void waitContextMenuJavaDocText(String expectedText) {
    waitJavaDocPopupSrcAttributeIsNotEmpty();

    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  String javaDocPopupHtmlText = "";
                  try {
                    javaDocPopupHtmlText = getJavaDocPopupText();
                  } catch (StaleElementReferenceException e) {
                    LOG.warn(
                        "Can not get java doc HTML text from autocomplete context menu in editor");
                  }
                  return javaDocPopupHtmlText.length() > 0
                      && verifyJavaDoc(javaDocPopupHtmlText, expectedText);
                });
  }

  private void waitJavaDocPopupSrcAttributeIsNotEmpty() {
    new FluentWait<>(seleniumWebDriver)
        .withTimeout(LOAD_PAGE_TIMEOUT_SEC * 2, SECONDS)
        .pollingEvery(LOAD_PAGE_TIMEOUT_SEC / 2, SECONDS)
        .ignoring(StaleElementReferenceException.class, NoSuchElementException.class)
        .until(ExpectedConditions.attributeToBeNotEmpty(autocompleteProposalJavaDocPopup, "src"));
  }

  private String getJavaDocPopupText() {
    HttpURLConnection connection = null;

    try {
      URL connectionUrl = new URL(getElementSrcLink(autocompleteProposalJavaDocPopup));
      connection = (HttpURLConnection) connectionUrl.openConnection();
      connection.setRequestMethod("GET");

      return openStreamAndGetAllText(connection);

    } catch (IOException e) {
      LOG.error("Can not open connection for src link ");
    } finally {
      if (connection != null) connection.disconnect();
    }

    return "";
  }

  private boolean verifyJavaDoc(String javaDocHtml, String regex) {
    return Pattern.compile(regex, Pattern.DOTALL).matcher(javaDocHtml).matches();
  }

  private String getElementSrcLink(WebElement element) {
    FluentWait<WebDriver> srcLinkWait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(LOAD_PAGE_TIMEOUT_SEC, SECONDS)
            .pollingEvery(500, MILLISECONDS)
            .ignoring(StaleElementReferenceException.class, NoSuchElementException.class);

    return srcLinkWait.until((ExpectedCondition<String>) driver -> element.getAttribute("src"));
  }

  private String openStreamAndGetAllText(HttpURLConnection httpURLConnection) {
    if (httpURLConnection != null) {
      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"))) {
        return br.lines().collect(Collectors.joining());

      } catch (IOException ex) {
        LOG.error("Can not get stream in openConnectionAndSetRequestMethod");
      }
    }
    return "";
  }

  private List<WebElement> getAllTabsWithProvidedName(String tabName) {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath(
            format("//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']", tabName)));
  }

  public void moveCursorToText(String text) {
    seleniumWebDriverHelper.moveCursorTo(
        By.xpath(format(Locators.TEXT_TO_MOVE_CURSOR_XPATH, text)));
  }

  public void checkProposalDocumentation(String expectedText) {
    seleniumWebDriverHelper.waitTextContains(proposalDoc, expectedText);
  }

  /** Type the comment line in the file by keyboard */
  public void launchCommentCodeFeature() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(CONTROL)
        .sendKeys("/")
        .keyUp(CONTROL)
        .perform();
  }

  /**
   * wait renaming field in the Editor (usually it field is used by language servers), type new
   * value and wait closing of the field
   *
   * @param renameValue
   */
  public void doRenamingByLanguageServerField(String renameValue) {
    seleniumWebDriverHelper.setValue(languageServerRenameField, renameValue);
    seleniumWebDriverHelper.waitAndSendKeysTo(languageServerRenameField, Keys.ENTER.toString());
  }
}
