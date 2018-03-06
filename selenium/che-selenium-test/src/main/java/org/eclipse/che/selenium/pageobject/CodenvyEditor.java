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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ACTIVE_TAB_FILE_NAME;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ALL_TABS_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ASSIST_CONTENT_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.AUTOCOMPLETE_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.CONTEXT_MENU;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAKPOINT_DISABLED;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_BREAK_POINT_ACTIVE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.DEBUGGER_PREFIX_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.EDITOR_TABS_PANEL;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.IMPLEMENTATIONS_ITEM;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.IMPLEMENTATION_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ITEM_TAB_LIST;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.JAVA_DOC_POPUP;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.ORION_CONTENT_ACTIVE_EDITOR_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.POSITION_CURSOR_NUMBER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.PROPOSITION_CONTAINER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.PUNCTUATION_SEPARATOR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_ANNOTATIONS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_FOLDING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_LINES;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.RULER_OVERVIEW;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.SELECTED_ITEM_IN_EDITOR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_CLOSE_ICON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_NAME_AND_STYLE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_FILE_NAME_XPATH;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_LIST_BUTTON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TAB_WITH_UNSAVED_STATUS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.Locators.TEXT_VIEW_RULER;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfNestedElementLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElements;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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
    String POSITION_CURSOR_NUMBER =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[text()='%s']";
    String ACTIVE_EDITOR_ENTRY_POINT =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[@active]";
    String ORION_ACTIVE_EDITOR_CONTAINER_XPATH =
        ACTIVE_EDITOR_ENTRY_POINT + "//div[@class='textviewContent' and @contenteditable='true']";
    String ORION_CONTENT_ACTIVE_EDITOR_XPATH = ORION_ACTIVE_EDITOR_CONTAINER_XPATH + "/div";
    String ACTIVE_LINES_XPATH =
        "//div[@class='textviewSelection']/preceding::div[@class='annotationLine currentLine'][1]";
    String ACTIVE_LINE_HIGHLIGHT =
        "//div[@class='annotationLine currentLine' and @role='presentation']";
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

  @FindBy(xpath = Locators.ACTIVE_LINES_XPATH)
  private WebElement activeLineXpath;

  @FindBy(xpath = Locators.SHOW_HINTS_POP_UP)
  private WebElement showHintsPopUp;

  @FindBy(xpath = Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH)
  private WebElement activeEditorContainer;

  @FindBy(xpath = Locators.AUTOCOMPLETE_PROPOSAL_JAVA_DOC_POPUP)
  private WebElement autocompleteProposalJavaDocPopup;

  @FindBy(xpath = ALL_TABS_XPATH)
  private WebElement someOpenedTab;

  /**
   * wait active editor
   *
   * @param userTimeOut timeout defined of the user
   */
  public void waitActive(int userTimeOut) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.waitVisibility(activeEditorContainer, userTimeOut);
  }

  /** wait active editor */
  public void waitActive() {
    loader.waitOnClosed();
    seleniumWebDriverHelper.waitVisibility(activeEditorContainer, ELEMENT_TIMEOUT_SEC);
  }

  /**
   * get text from active tab of orion editor
   *
   * @return text from active tab of orion editor
   */
  public String getVisibleTextFromEditor() {
    waitActive();
    List<WebElement> lines =
        webDriverWaitFactory
            .get(ELEMENT_TIMEOUT_SEC)
            .until(presenceOfAllElementsLocatedBy(By.xpath(ORION_CONTENT_ACTIVE_EDITOR_XPATH)));
    return getTextFromOrionLines(lines);
  }

  /**
   * get visible text from split editor
   *
   * @param indexOfEditor index of editor that was split
   */
  public String getTextFromSplitEditor(int indexOfEditor) {
    waitActive();
    List<WebElement> lines =
        webDriverWaitFactory
            .get(ELEMENT_TIMEOUT_SEC)
            .until(
                presenceOfAllElementsLocatedBy(
                    By.xpath(Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH)));
    List<WebElement> inner = lines.get(indexOfEditor - 1).findElements(By.tagName("div"));
    return getTextFromOrionLines(inner);
  }

  /**
   * wait expected text in orion editor
   *
   * @param text expected text
   * @param customTimeout time for waiting , that was defined by user
   */
  public void waitTextIntoEditor(final String text, final int customTimeout) {
    webDriverWaitFactory
        .get(customTimeout)
        .until((ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().contains(text));
  }

  /**
   * wait text into split editor with defined index
   *
   * @param numOfEditor number of the split editor
   * @param customTimeout timeout defined by user
   */
  public void waitTextInDefinedSplitEditor(
      int numOfEditor, final int customTimeout, String expectedText) {
    webDriverWaitFactory
        .get(customTimeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromSplitEditor(numOfEditor).contains(expectedText));
  }

  public void waitTextIsNotPresentInDefinedSplitEditor(
      int numOfEditor, final int customTimeout, String text) {
    webDriverWaitFactory
        .get(customTimeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> !getTextFromSplitEditor(numOfEditor).contains(text));
  }

  /**
   * wait expected text in orion editor
   *
   * @param text expected text
   */
  public void waitTextIntoEditor(final String text) {
    try {
      webDriverWaitFactory
          .get()
          .until((ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().contains(text));
    } catch (Exception ex) {
      LOG.warn(ex.getLocalizedMessage());
      WaitUtils.sleepQuietly(REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
      webDriverWaitFactory
          .get(ATTACHING_ELEM_TO_DOM_SEC)
          .until((ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().contains(text));
    }
    loader.waitOnClosed();
  }

  /**
   * wait expected text is not present in orion editor
   *
   * @param text expected text
   */
  public void waitTextNotPresentIntoEditor(final String text) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>) webDriver -> !(getVisibleTextFromEditor().contains(text)));
  }

  /** wait closing of tab with specified name */
  public void waitWhileFileIsClosed(String nameOfFile) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile))));
  }

  /**
   * select tab by name, click on close icon and wait while content will be saved and orion
   * disappear file should be unchanged
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
   * click on close icon in the file
   *
   * @param fileName name of File which must be close
   */
  public void clickOnCloseFileIcon(String fileName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(TAB_FILE_CLOSE_ICON, fileName)));
  }

  /**
   * checks if some tab is opened in the Editor
   *
   * @return true if any tab is open
   */
  public boolean isAnyTabsOpened() {
    return seleniumWebDriverHelper.isVisible(someOpenedTab);
  }

  /** get all open editor tabs and close this */
  public void closeAllTabs() {
    loader.waitOnClosed();
    List<WebElement> tabs;
    tabs =
        webDriverWaitFactory
            .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOfAllElementsLocatedBy(By.xpath(ALL_TABS_XPATH)));
    for (WebElement tab : tabs) {
      closeFileByNameWithSaving(tab.getText());
    }
    webDriverWaitFactory
        .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(ALL_TABS_XPATH)));
  }

  /** close all tabs by using context menu */
  public void closeAllTabsByContextMenu() {
    List<WebElement> tabs;
    tabs =
        webDriverWaitFactory
            .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(visibilityOfAllElementsLocatedBy(By.xpath(ALL_TABS_XPATH)));
    WebElement tab =
        seleniumWebDriverHelper.waitVisibility(
            By.xpath(format(TAB_FILE_CLOSE_ICON, tabs.get(tabs.size() - 1).getText())));
    actionsFactory.createAction(seleniumWebDriver).contextClick(tab).perform();
    seleniumWebDriverHelper.waitAndClick(By.id(CLOSE_ALL_TABS));
    seleniumWebDriverHelper.waitInvisibility(By.xpath(ALL_TABS_XPATH));
  }

  /**
   * Open context menu for tab by name
   *
   * @param tabName name of tab
   */
  public void openContextMenuForTabByName(String tabName) {
    WebElement tab =
        seleniumWebDriverHelper.waitVisibility(By.xpath(format(TAB_FILE_CLOSE_ICON, tabName)));
    actionsFactory.createAction(seleniumWebDriver).contextClick(tab).perform();
  }

  /** Run action for tab from the context menu */
  public void runActionForTabFromContextMenu(TabActionLocator tabAction) {
    seleniumWebDriverHelper.waitAndClick(tabAction.get());
  }

  /** type text by into orion editor with pause 1 sec. */
  public void typeTextIntoEditor(String text) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.sendKeys(text);
    loader.waitOnClosed();
  }

  /**
   * type text into orion editor pause for saving on server side is not set
   *
   * @param text text to type
   */
  public void typeTextIntoEditorWithoutDelayForSaving(String text) {
    seleniumWebDriverHelper.sendKeys(text);
  }

  /**
   * type text into specific line editor. Cursor will be activated id the end of the line. Line must
   * not be empty. Not recommended to use
   *
   * @param text text to type
   */
  public void typeTextIntoEditor(String text, int line) {
    setCursorToLine(line);
    typeTextIntoEditor(text);
  }

  /** returns focus in the end of current line (in active tab) */
  // TODO in some cases (for example if we do step into in debug mode and opens editor after that,
  // focus will be lost). But this problem should be fixed. After the we can remove this method
  public void returnFocusInCurrentLine() {
    List<WebElement> lines =
        seleniumWebDriver.findElements(By.xpath(Locators.ACTIVE_LINE_HIGHLIGHT));
    Collections.sort(
        lines, (o1, o2) -> Integer.compare(o1.getLocation().getX(), o2.getLocation().getX()));
    lines = lines.subList(0, lines.size() / 2); // filter lines from preview
    for (WebElement line : lines) {
      if (line.isDisplayed()) {
        try {
          int lineWidth = line.getSize().getWidth();
          Actions action = actionsFactory.createAction(seleniumWebDriver);
          action.moveToElement(line, lineWidth - 1, 0).click().perform();
        } catch (Exception ignore) {
        }
      }
    }
  }

  /**
   * set cursor in specified line
   *
   * @param positionLine is the specified number line
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
   * set cursor in specified position for current visible editor
   *
   * @param positionLine is the specified number line
   * @param positionChar is the specified number char
   */
  public void goToCursorPositionVisible(int positionLine, int positionChar) {
    openGoToLineFormAndSetCursorToPosition(positionLine, positionChar);
    waitActive();
    waitSpecifiedValueForLineAndChar(positionLine, positionChar);
  }

  /**
   * set cursor in specified position for current active and focused editor
   *
   * @param positionLine is the specified number line
   * @param positionChar is the specified number char
   */
  public void goToPosition(int positionLine, int positionChar) {
    openGoToLineFormAndSetCursorToPosition(positionLine, positionChar);
    waitActive();
    waitCursorPosition(positionLine, positionChar);
  }

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

  /** launch code assistant with ctrl+space keys and wait container is open */
  public void launchAutocompleteAndWaitContainer() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    typeTextIntoEditor(Keys.SPACE.toString());
    action.keyUp(CONTROL).perform();
    waitAutocompleteContainer();
  }

  /** launch code assistant with ctrl+space keys */
  public void launchAutocomplete() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    typeTextIntoEditor(Keys.SPACE.toString());
    action.keyUp(CONTROL).perform();
  }

  /** type ESC key into editor and wait closing of the autocomplete */
  public void closeAutocomplete() {
    typeTextIntoEditor(ESCAPE.toString());
    waitAutocompleteContainerIsClosed();
  }

  /** wait while autocomplete form opened */
  public void waitAutocompleteContainer() {
    seleniumWebDriverHelper.waitVisibility(autocompleteContainer, ELEMENT_TIMEOUT_SEC);
  }

  /** wait while autocomplete form will closed */
  public void waitAutocompleteContainerIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(AUTOCOMPLETE_CONTAINER));
  }

  /**
   * wait specified text in autocomplete
   *
   * @param value
   */
  public void waitTextIntoAutocompleteContainer(final String value) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getAllVisibleTextFromAutocomplete().contains(value));
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

  /** Wait for no Git change markers in the opened editor. */
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
   * Wait for Git insertion marker in the opened editor.
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
   * Wait for Git modification marker in the opened editor.
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
                    if (!"git-change-marker modification"
                        .equals(getListGitMarkers().get(i).getAttribute("class"))) {
                      return false;
                    }
                  }
                  return true;
                });
  }

  /**
   * Wait for Git deletion marker in the opened editor.
   *
   * @param line line number of the marker
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
    List<WebElement> rulerVcsElements = seleniumWebDriver.findElements(By.xpath(VCS_RULER));
    List<WebElement> subList = rulerVcsElements.subList(1, rulerVcsElements.size() - 1);
    webDriverWaitFactory.get().until(visibilityOfAllElements(subList));
    return rulerVcsElements;
  }

  /**
   * Waits marker with specified {@code markerLocator} on the defined {@code position} and click on
   * it
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @param position line's number, where marker is expected
   */
  public void waitMarkerInPositionAndClick(MarkerLocator markerLocator, int position) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(markerLocator.get(), position)));
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
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> driver.findElements(By.xpath(markerLocator.get())).size() == 0);
  }

  /**
   * Waits until at list one marker with specified {@code markerLocator} be visible
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void waitCodeAssistMarkers(MarkerLocator markerLocator) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> driver.findElements(By.xpath(markerLocator.get())).size() > 0);
  }

  /** @return text from autocomplete */
  public String getAllVisibleTextFromAutocomplete() {
    waitAutocompleteContainer();
    return autocompleteContainer.getText();
  }

  /**
   * Go to the item in autocomplete proposals list and press enter key.
   *
   * @param item item in the autocomplete proposals list.
   */
  public void enterAutocompleteProposal(String item) {
    selectAutocompleteProposal(item);
    WebElement highlightEItem =
        autocompleteContainer.findElement(
            By.xpath("//li[@selected='true']//span[text()=\"" + item + "\"]"));

    seleniumWebDriverHelper.waitVisibility(highlightEItem);
    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * select the item into autocomplete container and send double click to item
   *
   * @param item item in the autocomplete proposals list.
   */
  public void selectItemIntoAutocompleteAndPasteByDoubleClick(String item) {
    selectAutocompleteProposal(item);
    WebElement highlightEItem =
        autocompleteContainer.findElement(
            By.xpath("//li[@selected='true']//span[text()=\"" + item + "\"]"));
    seleniumWebDriverHelper.waitVisibility(highlightEItem);
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Select item by clicking on it in autocomplete container.
   *
   * @param item item from autocomplete list.
   */
  public void selectAutocompleteProposal(String item) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(AUTOCOMPLETE_CONTAINER + "/li/span[text()='%s']", item)));
  }

  /**
   * Moves mouse to the marker with specified {@code markerLocator} and waits until 'assist content
   * container' be visible.
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   */
  public void moveToMarkerAndWaitAssistContent(MarkerLocator markerLocator) {
    WebElement element = seleniumWebDriver.findElement(By.xpath(markerLocator.get()));
    seleniumWebDriverHelper.moveCursorTo(element);
    waitAnnotationCodeAssistIsOpen();
  }

  /** wait annotations code assist is open */
  public void waitAnnotationCodeAssistIsOpen() {
    seleniumWebDriverHelper.waitVisibility(assistContentContainer, ELEMENT_TIMEOUT_SEC);
  }

  /** wait annotations code assist is closed */
  public void waitAnnotationCodeAssistIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(ASSIST_CONTENT_CONTAINER));
  }

  /** wait specified text in annotation code assist */
  public void waitTextIntoAnnotationAssist(final String expectedText) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  List<WebElement> items =
                      seleniumWebDriver.findElements(By.xpath(ASSIST_CONTENT_CONTAINER + "//span"));
                  for (WebElement item : items) {
                    if (item.getText().contains(expectedText)) {
                      return true;
                    }
                  }
                  return false;
                });
  }

  /**
   * wait specified text in proposition assist panel
   *
   * @param value expected text in error proposition
   */
  public void waitTextIntoFixErrorProposition(final String value) {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getAllVisibleTextFromProposition().contains(value));
  }

  /** @return text from proposition assist panel */
  public String getAllVisibleTextFromProposition() {
    waitPropositionAssistContainer();
    return propositionContainer.getText();
  }

  /** wait assist proposition container is open */
  public void waitPropositionAssistContainer() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(PROPOSITION_CONTAINER), ELEMENT_TIMEOUT_SEC);
  }

  /** wait assist proposition container is closed */
  public void waitErrorPropositionPanelClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PROPOSITION_CONTAINER));
  }

  /** launch the 'code assist proposition' container */
  public void launchPropositionAssistPanel() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.ALT).perform();
    action.sendKeys(ENTER.toString()).perform();
    action.keyUp(Keys.ALT).perform();
    waitPropositionAssistContainer();
  }

  /** launch the 'code assist proposition' container in JS files */
  public void launchPropositionAssistPanelForJSFiles() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.LEFT_CONTROL).perform();
    action.sendKeys(Keys.SPACE.toString()).perform();
    action.keyUp(Keys.LEFT_CONTROL).perform();
  }

  /**
   * selected the first item into the 'assist proposition' container and send double click to the
   * first item
   */
  public void selectFirstItemIntoFixErrorPropByDoubleClick() {
    String tmpLocator = PROPOSITION_CONTAINER + "/li";
    seleniumWebDriverHelper.waitAndClick(By.xpath(tmpLocator));
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(tmpLocator));

    waitErrorPropositionPanelClosed();
  }

  /**
   * selected the first item into the 'assist proposition' container and send enter key to the first
   * item
   */
  public void selectFirstItemIntoFixErrorPropByEnter() {
    String tmpLocator = PROPOSITION_CONTAINER + "/li";
    seleniumWebDriverHelper.waitAndClick(By.xpath(tmpLocator));
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(tmpLocator));

    waitErrorPropositionPanelClosed();
  }

  /**
   * select the expectedItem into assist proposition container and send double click to expectedItem
   *
   * @param expectedItem
   */
  public void enterTextIntoFixErrorPropByDoubleClick(String expectedItem) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(PROPOSITION_CONTAINER + "/li/span[text()=\"%s\"]", expectedItem)));
  }

  /**
   * click on the item into proposition container and send enter key to item
   *
   * @param item
   */
  public void enterTextIntoFixErrorPropByEnter(String item) {
    seleniumWebDriverHelper.waitAndClick(
        propositionContainer.findElement(By.xpath(format("//li//span[text()=\"%s\"]", item))));

    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * in Js file editor click on the item into proposition container and send enter key to item
   *
   * @param item
   */
  public void enterTextIntoFixErrorPropByEnterForJsFiles(String item, String description) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(
            format("//span[text()='%s']/span[text()='%s']/ancestor::div[1]", description, item)));
  }

  /** invoke the 'Show hints' to all parameters on the overloaded constructor or method */
  public void callShowHintsPopUp() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    loader.waitOnClosed();
    action.keyDown(CONTROL).sendKeys("p").perform();

    testWebElementRenderChecker.waitElementIsRendered(By.xpath("//div[@class='gwt-PopupPanel']"));

    action.keyUp(CONTROL).perform();
    loader.waitOnClosed();
  }

  /** wait the 'Show hints' pop up panel is opened */
  public void waitShowHintsPopUpOpened() {
    seleniumWebDriverHelper.waitVisibility(showHintsPopUp);
  }

  /** wait the 'Show hints' pop up panel is closed */
  public void waitShowHintsPopUpClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(Locators.SHOW_HINTS_POP_UP));
  }

  /**
   * wait expected text into the 'Show hints' pop up panel
   *
   * @param expText expected value
   */
  public void waitExpTextIntoShowHintsPopUp(String expText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> getTextFromShowHintsPopUp().contains(expText));
  }

  /** get text from the 'Show hints' pop up panel */
  public String getTextFromShowHintsPopUp() {
    return seleniumWebDriverHelper.waitVisibility(showHintsPopUp).getText();
  }

  /**
   * wait while open file tab with specified name becomes without '*' unsaved status
   *
   * @param nameOfFile name of tab for checking
   */
  public void waitTabFileWithSavedStatus(String nameOfFile) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(TAB_WITH_UNSAVED_STATUS, nameOfFile)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * wait the active tab file name
   *
   * @param nameOfFile is specified file name
   */
  public void waitActiveTabFileName(String nameOfFile) {
    webDriverWaitFactory
        .get()
        .until(presenceOfElementLocated(By.xpath(format(ACTIVE_TAB_FILE_NAME, nameOfFile))));
  }

  /** check that files have been closed. (Check disappears all text areas and tabs) */
  public void waitWhileAllFilesWillClosed() {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath("//div[@id='gwt-debug-editorPartStack-tabsPanel']//div[text()]"),
        ELEMENT_TIMEOUT_SEC);

    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH));
  }

  /**
   * select open file tab with click
   *
   * @param nameOfFile name of tab for select
   */
  public void selectTabByName(String nameOfFile) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)));
  }

  public void waitYellowTab(String fileName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(224, 185, 29);")));
  }

  public void waitGreenTab(String fileName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(114, 173, 66);")));
  }

  public void waitBlueTab(String fileName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(49, 147, 212);")));
  }

  public void waitDefaultColorTab(final String fileName) {
    waitActive();
    boolean isEditorFocused =
        !(seleniumWebDriver
                .findElement(By.xpath(format(TAB_FILE_NAME_XPATH + "/parent::div", fileName)))
                .getAttribute("focused")
            == null);
    final String currentStateEditorColor =
        isEditorFocused ? "rgba(255, 255, 255, 1)" : "rgba(170, 170, 170, 1)";
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.xpath(format(TAB_FILE_NAME_XPATH, fileName)))
                        .getCssValue("color")
                        .equals(currentStateEditorColor));
  }

  /**
   * wait tab with expected name is not present
   *
   * @param nameOfFile name of closing tab
   */
  public void waitTabIsNotPresent(String nameOfFile) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * wait tab with expected name is present
   *
   * @param nameOfFile name of appearing tab
   */
  public void waitTabIsPresent(String nameOfFile) {
    webDriverWaitFactory
        .get()
        .until(visibilityOfAllElementsLocatedBy(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile))));

    loader.waitOnClosed();
  }

  public String getAssociatedPathFromTheTab(String nameOfOpenedFile) {
    return seleniumWebDriverHelper
        .waitVisibility(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfOpenedFile)))
        .getAttribute("path");
  }

  /**
   * wait tab with expected name is present
   *
   * @param nameOfFile name of appearing tab
   * @param customTimeout time waiting of tab in editor. Set in seconds.
   */
  public void waitTabIsPresent(String nameOfFile, int customTimeout) {
    webDriverWaitFactory
        .get(customTimeout)
        .until(visibilityOfAllElementsLocatedBy(By.xpath(format(TAB_FILE_NAME_XPATH, nameOfFile))));

    loader.waitOnClosed();
  }

  // TODO this will be able to after adding feature 'Go to line' and 'Delete line'
  public void selectLineAndDelete(int numberOfLine) {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    setCursorToLine(numberOfLine);
    typeTextIntoEditor(Keys.HOME.toString());
    action.keyDown(Keys.SHIFT).perform();
    typeTextIntoEditor(Keys.END.toString());
    action.keyUp(Keys.SHIFT).perform();
    typeTextIntoEditor(Keys.DELETE.toString());
    loader.waitOnClosed();
  }

  // TODO this will be able to after adding feature 'Go to line' and 'Delete line'
  public void selectLineAndDelete() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    typeTextIntoEditor(Keys.HOME.toString());
    action.keyDown(Keys.SHIFT).perform();
    typeTextIntoEditor(Keys.END.toString());
    action.keyUp(Keys.SHIFT).perform();
    typeTextIntoEditor(Keys.DELETE.toString());
  }

  /** Deletes current line with Ctrl+D */
  public void deleteCurrentLine() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    action.sendKeys("d").perform();
    action.keyUp(CONTROL).perform();
    loader.waitOnClosed();
  }

  /** Deletes current line with Ctrl+D and inserts new line instead */
  public void deleteCurrentLineAndInsertNew() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    action.sendKeys("d").perform();
    action.keyUp(CONTROL).perform();
    action.sendKeys(Keys.ARROW_UP.toString()).perform();
    action.sendKeys(Keys.END.toString()).perform();
    action.sendKeys(ENTER.toString()).perform();
  }

  /** delete all content with ctrl+A and del keys */
  public void deleteAllContent() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    action.sendKeys("a").perform();
    action.keyUp(CONTROL).perform();
    action.sendKeys(Keys.DELETE.toString()).perform();
    waitEditorIsEmpty();
  }

  /** wait while the IDE line panel with number of line will be visible */
  public void waitDebugerLineIsVisible(int line) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(DEBUGGER_PREFIX_XPATH, line)));
  }

  /**
   * wait breakpoint with inactive state in defined position
   *
   * @param position the position in the codenvy - editor
   */
  public void waitBreakPointWithInactiveState(int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.DEBUGGER_BREAK_POINT_INACTIVE, position)));
  }

  /**
   * set breakpoint on specified position on the IDE breakpoint panel
   *
   * @param position position of the breakpoint
   */
  public void setInactiveBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));

    waitBreakPointWithInactiveState(position);
  }

  /**
   * Set breakpoint on specified position on the IDE breakpoint panel and wait on active state of
   * it.
   *
   * @param position position of the breakpoint
   */
  public void setBreakPointAndWaitActiveState(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));

    waitActiveBreakpoint(position);
  }

  public void setBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(DEBUGGER_PREFIX_XPATH, position)));
  }

  /**
   * wait breakpoint with active state in defined position
   *
   * @param position the position in the codenvy - editor
   */
  public void waitActiveBreakpoint(int position) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(DEBUGGER_BREAK_POINT_ACTIVE, position)));
  }

  public void waitInactiveBreakpoint(int position) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.DEBUGGER_BREAK_POINT_INACTIVE, position)));
  }

  public void waitConditionalBreakpoint(int lineNumber, boolean active) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(
            format(
                Locators.DEBUGGER_BREAKPOINT_CONDITION,
                active ? "active" : "inactive",
                lineNumber)));
  }

  public void waitDisabledBreakpoint(int lineNumber) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(DEBUGGER_BREAKPOINT_DISABLED, lineNumber)));
  }

  /** wait while editor will be empty */
  public void waitEditorIsEmpty() {
    webDriverWaitFactory
        .get(ELEMENT_TIMEOUT_SEC)
        .until((WebDriver driver) -> getVisibleTextFromEditor().isEmpty());
  }

  /** wait javadoc popup opened */
  public void waitJavaDocPopUpOpened() {
    seleniumWebDriverHelper.waitVisibility(javaDocPopUp, ELEMENT_TIMEOUT_SEC);
  }

  /** wait javadoc popup closed */
  public void waitJavaDocPopUpClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(JAVA_DOC_POPUP));
  }

  /** check text present in javadoc popup */
  public void checkTextToBePresentInJavaDocPopUp(String text) {
    webDriverWaitFactory.get().until(frameToBeAvailableAndSwitchToIt(By.xpath(JAVA_DOC_POPUP)));
    waitTextInJavaDoc(text);
    seleniumWebDriver.switchTo().parentFrame();
  }

  /**
   * sometimes javadoc invoces with delays, in this case empty frame displaying first, and text
   * waits in this frame even if javadoc was loaded successfuly
   */
  private void waitTextInJavaDoc(String expectedText) {
    try {
      webDriverWaitFactory
          .get()
          .until(textToBePresentInElementLocated(By.tagName("body"), expectedText));
    } catch (TimeoutException ex) {
      seleniumWebDriver.switchTo().parentFrame();
      webDriverWaitFactory.get().until(frameToBeAvailableAndSwitchToIt(By.xpath(JAVA_DOC_POPUP)));
      webDriverWaitFactory
          .get()
          .until(textToBePresentInElementLocated(By.tagName("body"), expectedText));
    }
  }

  /**
   * check text after go to link in javadoc at popup
   *
   * @param text
   * @param textLink text of link
   */
  public void checkTextAfterGoToLinkInJavaDocPopUp(String text, String textLink) {
    webDriverWaitFactory.get().until(frameToBeAvailableAndSwitchToIt(By.xpath(JAVA_DOC_POPUP)));

    WebElement link =
        webDriverWaitFactory
            .get()
            .until(elementToBeClickable(By.xpath(format("//a[text()='%s']", textLink))));

    seleniumWebDriver.get(link.getAttribute("href"));

    webDriverWaitFactory.get().until(textToBePresentInElementLocated(By.tagName("body"), text));

    seleniumWebDriver.navigate().back();
    seleniumWebDriver.switchTo().parentFrame();
  }

  /** open JavaDoc popup */
  public void openJavaDocPopUp() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    action.sendKeys(Keys.chord("q")).perform();
    action.keyUp(CONTROL).perform();
  }

  /**
   * wait the text in active line of current tab
   *
   * @param text is the text of elements active line
   */
  public void waitTextElementsActiveLine(final String text) {
    waitActive();
    seleniumWebDriverHelper.waitVisibility(activeLineXpath);

    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> activeLineXpath.getText().contains(text));
  }

  /** get positions of the current line and char */
  public Pair<Integer, Integer> getCurrentCursorPositionsFromVisible() {
    waitActive();
    WebElement currentActiveElement =
        activeLineNumbers.stream().filter(webElement -> webElement.isDisplayed()).findFirst().get();
    return getCursorPositionFromWebElement(currentActiveElement);
  }

  /**
   * Check that editor active and focused on first step. After that get cursor position. Note! For
   * correct work a editor must be active.
   *
   * @return char and line position from current visible, active and focused editor (usual uses with
   *     split editor)
   */
  public Pair<Integer, Integer> getCursorPositionsFromActive() {
    String xpathToCurrentActiveCursorPosition =
        "//div[@active and @focused]/parent::div[@id='gwt-debug-multiSplitPanel-tabsPanel']/div[@active and @focused]/parent::div[@id='gwt-debug-multiSplitPanel-tabsPanel']/parent::div/parent::div/following-sibling::div//div[@active]//div[@id='gwt-debug-cursorPosition']";
    waitActive();
    return getCursorPositionFromWebElement(
        webDriverWaitFactory
            .get()
            .until(visibilityOfElementLocated(By.xpath(xpathToCurrentActiveCursorPosition))));
  }

  /**
   * wait specified values for Line and Char cursor positions in the Codenvy editor
   *
   * @param linePosition expected line position
   * @param charPosition expected char position
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

  /** get number of current active line */
  public int getPositionVisible() {
    waitActive();
    return getCurrentCursorPositionsFromVisible().first;
  }

  /**
   * check that active line has expected number
   *
   * @param expectedLine expected number of active line
   */
  public void expectedNumberOfActiveLine(final int expectedLine) {
    waitActive();
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedLine == getPositionVisible());
  }

  /**
   * get Char value for cursor from the codenvy - editor
   *
   * @return char value
   */
  public int getPositionOfChar() {
    return getCurrentCursorPositionsFromVisible().second;
  }

  /**
   * wait specified values for Line and Char cursor positions in the Codenvy editor
   *
   * @param linePosition expected line position
   * @param charPosition expected char position
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
   * wait specified values for Line and Char cursor positions in the Codenvy editor
   *
   * @param lineAndChar expected line and char position. For example: 1:25
   */
  public void waitSpecifiedValueForLineAndChar(String lineAndChar) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(POSITION_CURSOR_NUMBER, lineAndChar)));
  }

  /** launch refactor for local variables by keyboard */
  public void launchLocalRefactor() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.SHIFT).sendKeys(Keys.F6).keyUp(Keys.SHIFT).perform();
    loader.waitOnClosed();
  }

  /**
   * the first invocation of launchLocalRefactor() runs local refactoring, the second invocation
   * opens "Refactor" form
   */
  public void launchRefactorForm() {
    launchLocalRefactor();
    launchLocalRefactor();
  }

  /**
   * click on the selected element in the editor
   *
   * @param nameElement name of element
   */
  public void clickOnSelectedElementInEditor(String nameElement) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(SELECTED_ITEM_IN_EDITOR, nameElement)));

    waitActive();
  }

  /**
   * wait the 'Implementation(s)' form is open
   *
   * @param fileName is name of the selected file
   */
  public void waitImplementationFormIsOpen(String fileName) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(IMPLEMENTATION_CONTAINER, fileName)));
  }

  /** wait the 'Implementation(s)' form is closed */
  public void waitImplementationFormIsClosed(String fileName) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(IMPLEMENTATION_CONTAINER, fileName)));
  }

  /** launch the 'Implementation(s)' form by keyboard */
  public void launchImplementationFormByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action
        .keyDown(CONTROL)
        .keyDown(Keys.ALT)
        .sendKeys("b")
        .keyUp(Keys.ALT)
        .keyUp(CONTROL)
        .perform();
  }

  /** close the forms in the editor by 'Escape' */
  public void cancelFormInEditorByEscape() {
    typeTextIntoEditor(ESCAPE.toString());
  }

  /**
   * wait expected text in the 'Implementation' form
   *
   * @param expText expected value
   */
  public void waitTextInImplementationForm(String expText) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromImplementationForm().contains(expText));
  }

  /** get text from 'Implementation(s)' form */
  public String getTextFromImplementationForm() {
    return seleniumWebDriverHelper.waitVisibility(implementationContent).getText();
  }

  /**
   * perform 'double click on the item in the 'Implementation' form
   *
   * @param fileName is name of item
   */
  public void chooseImplementationByDoubleClick(String fileName) {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(
        By.xpath(format(IMPLEMENTATIONS_ITEM, fileName)));
  }

  /**
   * select defined implementation in the 'Implementation' form
   *
   * @param fileName is name of item
   */
  public void selectImplementationByClick(String fileName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(IMPLEMENTATIONS_ITEM, fileName)));
  }

  /**
   * check what text line is present once
   *
   * @param line text line
   * @return true if text line present once or false in other cases
   */
  public boolean checkWhatTextLinePresentOnce(String line) {
    String visibleTextFromEditor = getVisibleTextFromEditor();
    int index = visibleTextFromEditor.indexOf(line);
    if (index > 0) {
      return !visibleTextFromEditor
          .substring(index + 1, visibleTextFromEditor.length())
          .contains(line);
    }
    return false;
  }

  /**
   * click on web element by Xpath
   *
   * @param xPath is Xpath of web element
   */
  public void clickOnElementByXpath(String xPath) {
    webDriverWaitFactory.get().until(elementToBeClickable(By.xpath(xPath))).click();
  }

  /**
   * Gets quantity of visible markers with specified {@code markerLocator}
   *
   * @param markerLocator marker's type, defined in {@link MarkerLocator}
   * @return markers quantity
   */
  public int getMarkersQuantity(MarkerLocator markerLocator) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_OVERVIEW));
    List<WebElement> annotationList =
        webDriverWaitFactory
            .get()
            .until(presenceOfAllElementsLocatedBy(By.xpath(markerLocator.get())));

    return annotationList.size();
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
   * remove line and everything after it
   *
   * @param numberOfLine number of line
   */
  public void removeLineAndAllAfterIt(int numberOfLine) {
    setCursorToLine(numberOfLine);
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).perform();
    action.keyDown(Keys.SHIFT).perform();
    action.sendKeys(Keys.END.toString()).perform();
    action.keyUp(CONTROL).perform();
    action.keyUp(Keys.SHIFT).perform();
    action.sendKeys(Keys.DELETE).perform();
  }

  /** wait the ruler overview is present */
  public void waitRulerOverviewIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_OVERVIEW));
  }

  /** wait the ruler overview is not present */
  public void waitRulerOverviewIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_OVERVIEW));
  }

  /** wait the ruler annotation is present */
  public void waitRulerAnnotationsIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_ANNOTATIONS));
  }

  /** wait the ruler annotation is not present */
  public void waitRulerAnnotationsIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_ANNOTATIONS));
  }

  /** wait the ruler line is present */
  public void waitRulerLineIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_LINES));
  }

  /** wait the ruler line is not present */
  public void waitRulerLineIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_LINES));
  }

  /** wait the ruler folding is present */
  public void waitRulerFoldingIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RULER_FOLDING));
  }

  /** wait the ruler folding is not present */
  public void waitRulerFoldingIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RULER_FOLDING));
  }

  /** wait the all punctuation separators are present */
  public void waitAllPunctuationSeparatorsArePresent() {
    webDriverWaitFactory
        .get()
        .until(visibilityOfAllElementsLocatedBy(By.xpath(PUNCTUATION_SEPARATOR)));
  }

  /** wait the all punctuation separators are not present */
  public void waitAllPunctuationSeparatorsAreNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PUNCTUATION_SEPARATOR));
  }

  /** wait the text view ruler is present */
  public void waitTextViewRulerIsPresent() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TEXT_VIEW_RULER));
  }

  /** wait the text view ruler is not present */
  public void waitTextViewRulerIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(TEXT_VIEW_RULER));
  }

  /** click on 'Download sources' link in the editor */
  public void clickOnDownloadSourcesLink() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.DOWNLOAD_SOURCES_LINK));
  }

  /**
   * Change width of window for the Editor.
   *
   * @param xOffset horizontal move offset.
   */
  public void changeWidthWindowForEditor(int xOffset) {
    WebElement moveElement =
        seleniumWebDriverHelper.waitVisibility(
            By.xpath("//div[@id='gwt-debug-navPanel']/parent::div/following::div[1]"));

    actionsFactory.createAction(seleniumWebDriver).dragAndDropBy(moveElement, xOffset, 0).perform();
  }

  /** Open list of the tabs Note: This possible if opened tabs don't fit in the tab bar. */
  public void openTabList() {
    seleniumWebDriverHelper.waitAndClick(By.id(TAB_LIST_BUTTON));
  }

  /**
   * Wait the tab is present in the tab list
   *
   * @param tabName name of tab
   */
  public void waitTabIsPresentInTabList(String tabName) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(ITEM_TAB_LIST, tabName)));
  }

  /**
   * Wait the tab is not present in the tab list
   *
   * @param tabName name of tab
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
   * Click on tab in the tab list
   *
   * @param tabName name of tab
   */
  public void clickOnTabInTabList(String tabName) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(ITEM_TAB_LIST, tabName)));
  }

  /**
   * Select tab by index of editor window after split
   *
   * @param index index of editor window
   * @param tabName name of tab
   */
  public void selectTabByIndexEditorWindow(int index, String tabName) {
    List<WebElement> windowList =
        webDriverWaitFactory
            .get(REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(presenceOfAllElementsLocatedBy(By.xpath(format(TAB_FILE_NAME_XPATH, tabName))));

    windowList.get(index).click();
  }

  /**
   * Select tab by index of editor window after split and open context menu
   *
   * @param index index of editor window
   * @param tabName name of tab
   */
  public void selectTabByIndexEditorWindowAndOpenMenu(int index, String tabName) {
    List<WebElement> windowList =
        webDriverWaitFactory
            .get()
            .until(presenceOfAllElementsLocatedBy(By.xpath(format(TAB_FILE_CLOSE_ICON, tabName))));
    actionsFactory.createAction(seleniumWebDriver).contextClick(windowList.get(index)).perform();
  }

  /**
   * Check the tab of file is present once in editor
   *
   * @param expectTab expect name of tab
   */
  public boolean tabIsPresentOnce(String expectTab) {
    List<WebElement> windowList =
        webDriverWaitFactory.get().until(presenceOfAllElementsLocatedBy(By.id(EDITOR_TABS_PANEL)));
    int counter = 0;
    for (WebElement webElement : windowList) {
      if (webElement.getText().contains(expectTab)) {
        counter++;
      }
    }

    return counter == 1;
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

  /** open context menu into editor */
  public void openContextMenuInEditor() {
    actionsFactory
        .createAction(seleniumWebDriver)
        .contextClick(seleniumWebDriverHelper.waitVisibility(activeEditorContainer))
        .perform();

    waitContextMenu();
  }

  /**
   * open context menu on the selected element into editor
   *
   * @param selectedElement is the selected element into editor
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

  /** wait context menu form is open */
  public void waitContextMenu() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(CONTEXT_MENU));
  }

  /** wait context menu form is not present */
  public void waitContextMenuIsNotPresent() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(CONTEXT_MENU));
  }

  /**
   * Clicks on {@code item} in context menu
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
    return webDriverWaitFactory
        .get()
        .until(
            visibilityOfAllElementsLocatedBy(
                By.xpath(
                    format(
                        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']",
                        tabName))));
  }
}
