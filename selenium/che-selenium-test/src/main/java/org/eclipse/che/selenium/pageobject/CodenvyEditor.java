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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

/** @author Musienko Maxim */
@Singleton
public class CodenvyEditor {

  public static final String CLOSE_ALL_TABS = "gwt-debug-contextMenu/closeAllEditors";
  public static final String VCS_RULER = "//div[@class='ruler vcs']/div";
  public static final Logger LOG = getLogger(CodenvyEditor.class);

  public static final class EditorContextMenu {
    public static final String REFACTORING = "contextMenu/Refactoring";
    public static final String REFACTORING_MOVE = "contextMenu/Refactoring/Move";
    public static final String REFACTORING_RENAME = "contextMenu/Refactoring/Rename";

    public static final String UNDO = "contextMenu/Undo";
    public static final String REDO = "contextMenu/Redo";
    public static final String FORMAT = "contextMenu/Format";
    public static final String QUICK_DOC = "contextMenu/Quick Documentation";
    public static final String QUICK_FIX = "contextMenu/Quick Fix";
    public static final String OPEN_DECLARATION = "contextMenu/Open Declaration";
    public static final String NAVIGATE_FILE_STRUCTURE = "contextMenu/Navigate File Structure";
    public static final String FIND = "contextMenu/Find";
    public static final String CLOSE = "contextMenu/Close";

    private EditorContextMenu() {}
  }

  public static final class MarkersType {
    public static final String ERROR_MARKER_OVERVIEW =
        "//div[@class='ruler annotations']/div[@class='annotation error']";
    public static final String WARNING_MARKER_OVERVIEW =
        "//div[@class='ruler annotations']/div[@class='annotation warning']";
    public static final String TASK_MARKER_OVERVIEW =
        "//div[@class='ruler annotations']/div[@class='annotation task']";
    public static final String ERROR_MARKER =
        "//div[@class='ruler overview']/div[@class='annotationOverview error']";
    public static final String WARNING_MARKER =
        "//div[@class='ruler overview']/div[@class='annotationOverview warning']";
    public static final String INFO_MARKER = "//div[@class='annotationHTML info']";

    private MarkersType() {}
  }

  protected final SeleniumWebDriver seleniumWebDriver;
  protected final Loader loader;
  protected final ActionsFactory actionsFactory;
  protected final AskForValueDialog askForValueDialog;

  private final WebDriverWait redrawDriverWait;
  private final WebDriverWait elemDriverWait;
  private final WebDriverWait loadPageDriverWait;
  private final WebDriverWait attachElemDriverWait;
  private final WebDriverWait loaderDriverWait;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  public CodenvyEditor(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      AskForValueDialog askForValueDialog,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.askForValueDialog = askForValueDialog;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    redrawDriverWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    elemDriverWait = new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC);
    loadPageDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    attachElemDriverWait = new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC);
    loaderDriverWait = new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Class introduce base Xpath locators for DOM navigation inside editor */
  public static final class Locators {
    private Locators() {}

    public static final String CONTEXT_MENU = "//div[@id='menu-lock-layer-id']/div[2]";
    public static final String EDITOR_TABS_PANEL = "gwt-debug-multiSplitPanel-tabsPanel";
    public static final String ACTIVE_LINE_NUMBER = "gwt-debug-cursorPosition";
    public static final String POSITION_CURSOR_NUMBER =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[text()='%s']";
    public static final String ACTIVE_EDITOR_ENTRY_POINT =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[@active]";
    public static final String ORION_ACTIVE_EDITOR_CONTAINER_XPATH =
        ACTIVE_EDITOR_ENTRY_POINT + "//div[@class='textviewContent' and @contenteditable='true']";
    public static final String ORION_CONTENT_ACTIVE_EDITOR_XPATH =
        ORION_ACTIVE_EDITOR_CONTAINER_XPATH + "/div";
    public static final String ACTIVE_LINES_XPATH =
        "//div[@class='textviewSelection']/preceding::div[@class='annotationLine currentLine'][1]";
    public static final String ACTIVE_LINE_HIGHLIGHT =
        "//div[@class='annotationLine currentLine' and @role='presentation']";
    public static final String ACTIVE_TAB_FILE_NAME = "//div[@active]/descendant::div[text()='%s']";
    public static final String ACTIVE_TAB_UNSAVED_FILE_NAME =
        "//div[@active and @unsaved]//div[text()='%s']";
    public static final String TAB_FILE_NAME_XPATH =
        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']";
    public static final String TAB_FILE_NAME_AND_STYLE =
        "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s' and @style='%s']";
    public static final String TAB_FILE_CLOSE_ICON =
        "//div[@id='gwt-debug-editorMultiPartStack-contentPanel']//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']/following::div[1]";

    public static final String ALL_TABS_XPATH =
        "//div[@id='gwt-debug-editorMultiPartStack-contentPanel']//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[string-length(text())>0]";
    public static final String SELECTED_ITEM_IN_EDITOR =
        "//div[@contenteditable='true']//span[contains(text(), '%s')]";

    public static final String ASSIST_CONTENT_CONTAINER =
        "//div[@class='contentassist']/following-sibling::div";
    public static final String AUTOCOMPLETE_CONTAINER =
        "//div[text()='Proposals:']//following::div/ulist";
    public static final String PROPOSITION_CONTAINER = "//div[@id='gwt_root']/following::div/ulist";
    public static final String SHOW_HINTS_POP_UP = "//div[@class='popupContent']/div[1]";

    public static final String RULER_ANNOTATIONS = "//div[@class='ruler annotations']";
    public static final String RULER_OVERVIEW = "//div[@class='ruler overview']";
    public static final String RULER_LINES = "//div[@class='ruler lines']";
    public static final String RULER_FOLDING = "//div[@class='ruler folding']";

    public static final String IMPLEMENTATION_CONTAINER =
        "//div[contains(text(), 'Choose Implementation of %s')]/parent::div";
    public static final String IMPLEMENTATION_CONTENT =
        "//div[contains(text(), 'Choose Implementation of')]/following::div";
    public static final String IMPLEMENTATIONS_ITEM =
        "//div[contains(text(), 'Choose Implementation of')]/following::span[text()='%s']";
    public static final String PUNCTUATION_SEPARATOR =
        "//span[contains(@class,'punctuation separator space')]";
    public static final String TEXT_VIEW_RULER = "//div[@class='textviewInnerRightRuler']";

    public static final String DOWNLOAD_SOURCES_LINK = "//anchor[text()='Download sources']";

    public static final String TAB_LIST_BUTTON = "gwt-debug-editorMenu";
    public static final String ITEM_TAB_LIST =
        "//div[@class='popupContent']//div[text()='%s']/parent::div";

    public static final String NOTIFICATION_PANEL_ID = "gwt-debug-leftNotificationGutter";
    public static final String DEBUGGER_PREFIX_XPATH =
        "//div[@class[contains(., 'rulerLines')] and text()='%d']";
    public static final String DEBUGGER_BREAK_POINT_INACTIVE =
        "//div[@class='breakpoint inactive' and text()='%d']";
    public static final String DEBUGGER_BREAK_POINT_ACTIVE =
        "//div[@class='breakpoint active' and text()='%d']";
    public static final String DEBUGGER_BREAKPOINT_CONDITION =
        "//div[@class='breakpoint %s condition' and text()='%d']";
    public static final String DEBUGGER_BREAKPOINT_DISABLED =
        "//div[@class='breakpoint disabled' and text()='%d']";
    public static final String JAVA_DOC_POPUP = "//div[@class='gwt-PopupPanel']//iframe";
    public static final String AUTOCOMPLETE_PROPOSAL_JAVA_DOC_POPUP =
        "//div//iframe[contains(@src, 'api/java/code-assist/compute/info?')]";
  }

  public enum TabAction {
    CLOSE("contextMenu/Close"),
    CLOSE_ALL("contextMenu/Close All"),
    CLOSE_OTHER("contextMenu/Close Other"),
    CLOSE_ALL_BUT_PINNED("contextMenu/Close All But Pinned"),
    REOPEN_CLOSED_TAB("contextMenu/Reopen Closed Tab"),
    PIN_UNPIN_TAB("contextMenu/Pin/Unpin Tab"),
    SPLIT_VERTICALLY("contextMenu/Split Pane In Two Columns"),
    SPIT_HORISONTALLY("contextMenu/Split Pane In Two Rows");

    private final String id;

    TabAction(String id) {
      this.id = id;
    }
  }

  @FindBy(id = Locators.EDITOR_TABS_PANEL)
  private WebElement editorTabsPanel;

  @FindAll({@FindBy(id = Locators.ACTIVE_LINE_NUMBER)})
  private List<WebElement> activeLineNumbers;

  @FindBy(xpath = Locators.AUTOCOMPLETE_CONTAINER)
  private WebElement autocompleteContainer;

  @FindBy(id = Locators.NOTIFICATION_PANEL_ID)
  private WebElement notificationPanel;

  @FindBy(xpath = Locators.PROPOSITION_CONTAINER)
  private WebElement propositionContainer;

  @FindBy(xpath = Locators.JAVA_DOC_POPUP)
  private WebElement javaDocPopUp;

  @FindBy(xpath = Locators.ASSIST_CONTENT_CONTAINER)
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

  @FindBy(xpath = Locators.ALL_TABS_XPATH)
  private WebElement someOpenedTab;

  /**
   * wait active editor
   *
   * @param userTimeOut timeout defined of the user
   */
  public void waitActive(int userTimeOut) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, userTimeOut).until(visibilityOf(activeEditorContainer));
  }

  /** wait active editor */
  public void waitActive() {
    loader.waitOnClosed();
    loadPageDriverWait.until(visibilityOf(activeEditorContainer));
  }

  /**
   * get text from active tab of orion editor
   *
   * @return text from active tab of orion editor
   */
  public String getVisibleTextFromEditor() {
    waitActive();
    List<WebElement> lines =
        elemDriverWait.until(
            presenceOfAllElementsLocatedBy(By.xpath(Locators.ORION_CONTENT_ACTIVE_EDITOR_XPATH)));
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
        elemDriverWait.until(
            presenceOfAllElementsLocatedBy(By.xpath(Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH)));
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
    new WebDriverWait(seleniumWebDriver, customTimeout)
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
    new WebDriverWait(seleniumWebDriver, customTimeout)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromSplitEditor(numOfEditor).contains(expectedText));
  }

  public void waitTextIsNotPresentInDefinedSplitEditor(
      int numOfEditor, final int customTimeout, String text) {
    new WebDriverWait(seleniumWebDriver, customTimeout)
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
      loadPageDriverWait.until(
          (ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().contains(text));
    } catch (Exception ex) {
      LOG.warn(ex.getLocalizedMessage());
      WaitUtils.sleepQuietly(REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
      attachElemDriverWait.until(
          (ExpectedCondition<Boolean>) driver -> getVisibleTextFromEditor().contains(text));
    }
    loader.waitOnClosed();
  }

  /**
   * wait expected text is not present in orion editor
   *
   * @param text expected text
   */
  public void waitTextNotPresentIntoEditor(final String text) {
    loadPageDriverWait.until(
        (ExpectedCondition<Boolean>) webDriver -> !(getVisibleTextFromEditor().contains(text)));
  }

  /** wait closing of tab with specified name */
  public void waitWhileFileIsClosed(String nameOfFile) {
    elemDriverWait.until(
        invisibilityOfElementLocated(
            By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfFile))));
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
    loadPageDriverWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_FILE_CLOSE_ICON, fileName))))
        .click();
  }

  /**
   * checks if some tab is opened in the Editor
   *
   * @return true if any tab is open
   */
  public boolean isAnyTabsOpened() {
    try {
      return someOpenedTab.isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  /** get all open editor tabs and close this */
  public void closeAllTabs() {
    loader.waitOnClosed();
    List<WebElement> tabs;
    tabs =
        redrawDriverWait.until(visibilityOfAllElementsLocatedBy(By.xpath(Locators.ALL_TABS_XPATH)));
    for (WebElement tab : tabs) {
      closeFileByNameWithSaving(tab.getText());
    }
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.ALL_TABS_XPATH)));
  }

  /** close all tabs by using context menu */
  public void closeAllTabsByContextMenu() {
    List<WebElement> tabs;
    tabs =
        redrawDriverWait.until(visibilityOfAllElementsLocatedBy(By.xpath(Locators.ALL_TABS_XPATH)));
    WebElement tab =
        loadPageDriverWait.until(
            visibilityOfElementLocated(
                By.xpath(
                    String.format(
                        Locators.TAB_FILE_CLOSE_ICON, tabs.get(tabs.size() - 1).getText()))));
    actionsFactory.createAction(seleniumWebDriver).contextClick(tab).perform();
    redrawDriverWait.until(visibilityOfElementLocated(By.id(CLOSE_ALL_TABS))).click();
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.ALL_TABS_XPATH)));
  }

  /**
   * Open context menu for tab by name
   *
   * @param tabName name of tab
   */
  public void openContextMenuForTabByName(String tabName) {
    WebElement tab =
        redrawDriverWait.until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_FILE_CLOSE_ICON, tabName))));
    actionsFactory.createAction(seleniumWebDriver).contextClick(tab).perform();
  }

  /** Run action for tab from the context menu */
  public void runActionForTabFromContextMenu(TabAction tabAction) {
    redrawDriverWait.until(visibilityOfElementLocated(By.id(tabAction.id))).click();
  }

  /** type text by into orion editor with pause 1 sec. */
  public void typeTextIntoEditor(String text) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
    loader.waitOnClosed();
  }

  /**
   * type text into orion editor pause for saving on server side is not set
   *
   * @param text text to type
   */
  public void typeTextIntoEditorWithoutDelayForSaving(String text) {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
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
    actionsFactory
        .createAction(seleniumWebDriver)
        .sendKeys(Keys.chord(Keys.CONTROL, "l"))
        .perform();
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
    action.keyDown(Keys.CONTROL).perform();
    typeTextIntoEditor(Keys.SPACE.toString());
    action.keyUp(Keys.CONTROL).perform();
    waitAutocompleteContainer();
  }

  /** launch code assistant with ctrl+space keys */
  public void launchAutocomplete() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    typeTextIntoEditor(Keys.SPACE.toString());
    action.keyUp(Keys.CONTROL).perform();
  }

  /** type ESC key into editor and wait closing of the autocomplete */
  public void closeAutocomplete() {
    typeTextIntoEditor(Keys.ESCAPE.toString());
    waitAutocompleteContainerIsClosed();
  }

  /** wait while autocomplete form opened */
  public void waitAutocompleteContainer() {
    elemDriverWait.until(visibilityOf(autocompleteContainer));
  }

  /** wait while autocomplete form will closed */
  public void waitAutocompleteContainerIsClosed() {
    loadPageDriverWait.until(
        invisibilityOfElementLocated(By.xpath(Locators.AUTOCOMPLETE_CONTAINER)));
  }

  /**
   * wait specified text in autocomplete
   *
   * @param value
   */
  public void waitTextIntoAutocompleteContainer(final String value) {
    elemDriverWait.until(
        (ExpectedCondition<Boolean>)
            webDriver -> getAllVisibleTextFromAutocomplete().contains(value));
  }

  /**
   * wait the marker
   *
   * @param markerType is the type of the marker
   * @param position is the number position
   */
  public void waitMarkerInPosition(String markerType, int position) {
    elemDriverWait.until(visibilityOfElementLocated(By.xpath(String.format(markerType, position))));
    setCursorToLine(position);
    expectedNumberOfActiveLine(position);
  }

  /** Wait for no Git change markers in the opened editor. */
  public void waitNoGitChangeMarkers() {

    List<WebElement> rulerVcsElements = seleniumWebDriver.findElements(By.xpath(VCS_RULER));

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    rulerVcsElements
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

    List<WebElement> rulerVcsElements =
        seleniumWebDriver.findElements(By.xpath("//div[@class='ruler vcs']/div"));

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  for (int i = startLine; i <= endLine; i++) {
                    if (!"git-change-marker insertion"
                        .equals(rulerVcsElements.get(i).getAttribute("class"))) {
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

    List<WebElement> rulerVcsElements =
        seleniumWebDriver.findElements(By.xpath("//div[@class='ruler vcs']/div"));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  for (int i = startLine; i <= endLine; i++) {
                    if (!"git-change-marker modification"
                        .equals(rulerVcsElements.get(i).getAttribute("class"))) {
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

    List<WebElement> rulerVcsElements =
        seleniumWebDriver.findElements(By.xpath("//div[@class='ruler vcs']/div"));

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    "git-change-marker deletion"
                        .equals(rulerVcsElements.get(line).getAttribute("class")));
  }

  /**
   * wait the marker and click him
   *
   * @param markerType is the type of the marker
   * @param position is the number position
   */
  public void waitMarkerInPositionAndClick(String markerType, int position) {
    loadPageDriverWait
        .until(visibilityOfElementLocated(By.xpath(String.format(markerType, position))))
        .click();
  }

  /**
   * wait the 'marker' disappears
   *
   * @param markerType is the type of the marker
   * @param position is the number position
   */
  public void waitMarkerDisappears(String markerType, int position) {
    elemDriverWait.until(
        invisibilityOfElementLocated(By.xpath(String.format(markerType, position))));
    expectedNumberOfActiveLine(position);
  }

  /**
   * wait while all markers disappear
   *
   * @param markerType is type of the marker
   */
  public void waitAllMarkersDisappear(String markerType) {
    loaderDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> driver.findElements(By.xpath(markerType)).size() == 0);
  }

  /**
   * wait while all markers appear
   *
   * @param markerType is type of the marker
   */
  public void waitCodeAssistMarkers(String markerType) {
    elemDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> driver.findElements(By.xpath(markerType)).size() > 0);
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
    redrawDriverWait.until(ExpectedConditions.visibilityOf(highlightEItem));
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER.toString()).perform();
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
    redrawDriverWait.until(visibilityOf(highlightEItem));
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * Select item by clicking on it in autocomplete container.
   *
   * @param item item from autocomplete list.
   */
  public void selectAutocompleteProposal(String item) {
    String locator = String.format(Locators.AUTOCOMPLETE_CONTAINER + "/li/span[text()='%s']", item);
    redrawDriverWait
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
  }

  /** move the mouse to the marker and wait the 'assist content container' */
  public void moveToMarkerAndWaitAssistContent(String markerType) {
    WebElement element = seleniumWebDriver.findElement(By.xpath(markerType));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(element).perform();
    waitAnnotationCodeAssistIsOpen();
  }

  /** wait annotations code assist is open */
  public void waitAnnotationCodeAssistIsOpen() {
    elemDriverWait.until(ExpectedConditions.visibilityOf(assistContentContainer));
  }

  /** wait annotations code assist is closed */
  public void waitAnnotationCodeAssistIsClosed() {
    loaderDriverWait.until(
        invisibilityOfElementLocated(By.xpath(Locators.ASSIST_CONTENT_CONTAINER)));
  }

  /** wait specified text in annotation code assist */
  public void waitTextIntoAnnotationAssist(final String expectedText) {
    elemDriverWait.until(
        (ExpectedCondition<Boolean>)
            webDriver -> {
              List<WebElement> items =
                  seleniumWebDriver.findElements(
                      By.xpath(Locators.ASSIST_CONTENT_CONTAINER + "//span"));
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
    elemDriverWait.until(
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
    elemDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.PROPOSITION_CONTAINER)));
  }

  /** wait assist proposition container is closed */
  public void waitErrorPropositionPanelClosed() {
    loaderDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.PROPOSITION_CONTAINER)));
  }

  /** launch the 'code assist proposition' container */
  public void launchPropositionAssistPanel() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.ALT).perform();
    action.sendKeys(Keys.ENTER.toString()).perform();
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
    String tmpLocator = Locators.PROPOSITION_CONTAINER + "/li";
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(tmpLocator))).click();
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(seleniumWebDriver.findElement(By.xpath(tmpLocator)))
        .perform();
    waitErrorPropositionPanelClosed();
  }

  /**
   * selected the first item into the 'assist proposition' container and send enter key to the first
   * item
   */
  public void selectFirstItemIntoFixErrorPropByEnter() {
    String tmpLocator = Locators.PROPOSITION_CONTAINER + "/li";
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(tmpLocator))).click();
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(seleniumWebDriver.findElement(By.xpath(tmpLocator)))
        .perform();
    waitErrorPropositionPanelClosed();
  }

  /**
   * select the expectedItem into assist proposition container and send double click to expectedItem
   *
   * @param expectedItem
   */
  // TODO this method is not checked in 4.x version. Locators can be invalid
  public void enterTextIntoFixErrorPropByDoubleClick(String expectedItem) {
    WebElement itemForClick =
        seleniumWebDriver.findElement(
            By.xpath(
                String.format(
                    Locators.PROPOSITION_CONTAINER + "/li/span[text()=\"%s\"]", expectedItem)));
    redrawDriverWait.until(visibilityOf(itemForClick));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(itemForClick).perform();
  }

  /**
   * click on the item into proposition container and send enter key to item
   *
   * @param item
   */
  public void enterTextIntoFixErrorPropByEnter(String item) {
    attachElemDriverWait
        .until(
            visibilityOf(
                propositionContainer.findElement(
                    By.xpath(String.format("//li//span[text()=\"%s\"]", item)))))
        .click();
    WebElement highlightEItem =
        propositionContainer.findElement(
            By.xpath(String.format("//li//span[text()=\"%s\"]", item)));
    redrawDriverWait.until(visibilityOf(highlightEItem));
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER.toString()).perform();
  }

  /**
   * in Js file editor click on the item into proposition container and send enter key to item
   *
   * @param item
   */
  public void enterTextIntoFixErrorPropByEnterForJsFiles(String item, String description) {
    attachElemDriverWait
        .until(
            presenceOfElementLocated(
                By.xpath(
                    String.format(
                        "//span[text()='%s']/span[text()='%s']/ancestor::div[1]",
                        description, item))))
        .click();
  }

  /** invoke the 'Show hints' to all parameters on the overloaded constructor or method */
  public void callShowHintsPopUp() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    loader.waitOnClosed();
    action.keyDown(Keys.CONTROL).sendKeys("p").perform();

    testWebElementRenderChecker.waitElementIsRendered(By.xpath("//div[@class='gwt-PopupPanel']"));

    action.keyUp(Keys.CONTROL).perform();
    loader.waitOnClosed();
  }

  /** wait the 'Show hints' pop up panel is opened */
  public void waitShowHintsPopUpOpened() {
    redrawDriverWait.until(visibilityOf(showHintsPopUp));
  }

  /** wait the 'Show hints' pop up panel is closed */
  public void waitShowHintsPopUpClosed() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.SHOW_HINTS_POP_UP)));
  }

  /**
   * wait expected text into the 'Show hints' pop up panel
   *
   * @param expText expected value
   */
  public void waitExpTextIntoShowHintsPopUp(String expText) {
    loadPageDriverWait.until(
        (ExpectedCondition<Boolean>) webDriver -> getTextFromShowHintsPopUp().contains(expText));
  }

  /** get text from the 'Show hints' pop up panel */
  public String getTextFromShowHintsPopUp() {
    waitShowHintsPopUpOpened();
    return showHintsPopUp.getText();
  }

  /**
   * wait while open file tab with specified name becomes without '*' unsaved status
   *
   * @param nameOfFile name of tab for checking
   */
  public void waitTabFileWithSavedStatus(String nameOfFile) {
    elemDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.ACTIVE_TAB_FILE_NAME, nameOfFile))));
  }

  /**
   * wait the active tab file name
   *
   * @param nameOfFile is specified file name
   */
  public void waitActiveTabFileName(String nameOfFile) {
    loadPageDriverWait.until(
        presenceOfElementLocated(
            By.xpath(String.format(Locators.ACTIVE_TAB_FILE_NAME, nameOfFile))));
  }

  /** check that files have been closed. (Check disappears all text areas and tabs) */
  public void waitWhileAllFilesWillClosed() {
    elemDriverWait.until(
        invisibilityOfElementLocated(
            By.xpath("//div[@id='gwt-debug-editorPartStack-tabsPanel']//div[text()]")));
    elemDriverWait.until(
        invisibilityOfElementLocated(By.xpath(Locators.ORION_ACTIVE_EDITOR_CONTAINER_XPATH)));
  }

  /**
   * select open file tab with click
   *
   * @param nameOfFile name of tab for select
   */
  public void selectTabByName(String nameOfFile) {
    redrawDriverWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfFile))))
        .click();
  }

  public void waitYellowTab(String fileName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    String.format(
                        Locators.TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(224, 185, 29);"))));
  }

  public void waitGreenTab(String fileName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    String.format(
                        Locators.TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(114, 173, 66);"))));
  }

  public void waitBlueTab(String fileName) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    String.format(
                        Locators.TAB_FILE_NAME_AND_STYLE, fileName, "color: rgb(49, 147, 212);"))));
  }

  public void waitDefaultColorTab(final String fileName) {
    waitActive();
    boolean isEditorFocused =
        !(seleniumWebDriver
                .findElement(
                    By.xpath(
                        String.format(Locators.TAB_FILE_NAME_XPATH + "/parent::div", fileName)))
                .getAttribute("focused")
            == null);
    final String currentStateEditorColor =
        isEditorFocused ? "rgba(255, 255, 255, 1)" : "rgba(170, 170, 170, 1)";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(
                            By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, fileName)))
                        .getCssValue("color")
                        .equals(currentStateEditorColor));
  }

  /**
   * wait tab with expected name is not present
   *
   * @param nameOfFile name of closing tab
   */
  public void waitTabIsNotPresent(String nameOfFile) {
    elemDriverWait.until(
        invisibilityOfElementLocated(
            By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfFile))));
  }

  /**
   * wait tab with expected name is present
   *
   * @param nameOfFile name of appearing tab
   */
  public void waitTabIsPresent(String nameOfFile) {
    loadPageDriverWait.until(
        visibilityOfAllElementsLocatedBy(
            By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfFile))));
    loader.waitOnClosed();
  }

  public String getAssociatedPathFromTheTab(String nameOfOpenedFile) {
    return redrawDriverWait
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfOpenedFile))))
        .getAttribute("path");
  }

  /**
   * wait tab with expected name is present
   *
   * @param nameOfFile name of appearing tab
   * @param customTimeout time waiting of tab in editor. Set in seconds.
   */
  public void waitTabIsPresent(String nameOfFile, int customTimeout) {
    new WebDriverWait(seleniumWebDriver, customTimeout)
        .until(
            visibilityOfAllElementsLocatedBy(
                By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, nameOfFile))));
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
    action.keyDown(Keys.CONTROL).perform();
    action.sendKeys("d").perform();
    action.keyUp(Keys.CONTROL).perform();
    loader.waitOnClosed();
  }

  /** Deletes current line with Ctrl+D and inserts new line instead */
  public void deleteCurrentLineAndInsertNew() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    action.sendKeys("d").perform();
    action.keyUp(Keys.CONTROL).perform();
    action.sendKeys(Keys.ARROW_UP.toString()).perform();
    action.sendKeys(Keys.END.toString()).perform();
    action.sendKeys(Keys.ENTER.toString()).perform();
  }

  /** delete all content with ctrl+A and del keys */
  public void deleteAllContent() {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    action.sendKeys("a").perform();
    action.keyUp(Keys.CONTROL).perform();
    action.sendKeys(Keys.DELETE.toString()).perform();
    waitEditorIsEmpty();
  }

  /** wait while the IDE line panel with number of line will be visible */
  public void waitDebugerLineIsVisible(int line) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(By.xpath(String.format(Locators.DEBUGGER_PREFIX_XPATH, line))));
  }

  /**
   * wait breakpoint with inactive state in defined position
   *
   * @param position the position in the codenvy - editor
   */
  public void waitBreakPointWithInactiveState(int position) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.DEBUGGER_BREAK_POINT_INACTIVE, position))));
  }

  /**
   * set breakpoint on specified position on the IDE breakpoint panel
   *
   * @param position position of the breakpoint
   */
  public void setInactiveBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.DEBUGGER_PREFIX_XPATH, position)))
        .click();
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
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.DEBUGGER_PREFIX_XPATH, position)))
        .click();
    waitActiveBreakpoint(position);
  }

  public void setBreakpoint(int position) {
    waitActive();
    waitDebugerLineIsVisible(position);
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.DEBUGGER_PREFIX_XPATH, position)))
        .click();
  }

  /**
   * wait breakpoint with active state in defined position
   *
   * @param position the position in the codenvy - editor
   */
  public void waitActiveBreakpoint(int position) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.DEBUGGER_BREAK_POINT_ACTIVE, position))));
  }

  public void waitInactiveBreakpoint(int position) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.DEBUGGER_BREAK_POINT_INACTIVE, position))));
  }

  public void waitConditionalBreakpoint(int lineNumber, boolean active) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    Locators.DEBUGGER_BREAKPOINT_CONDITION,
                    active ? "active" : "inactive",
                    lineNumber))));
  }

  public void waitDisabledBreakpoint(int lineNumber) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.DEBUGGER_BREAKPOINT_DISABLED, lineNumber))));
  }

  /** wait while editor will be empty */
  public void waitEditorIsEmpty() {
    elemDriverWait.until((WebDriver driver) -> getVisibleTextFromEditor().isEmpty());
  }

  /** wait javadoc popup opened */
  public void waitJavaDocPopUpOpened() {
    elemDriverWait.until(ExpectedConditions.visibilityOf(javaDocPopUp));
  }

  /** wait javadoc popup closed */
  public void waitJavaDocPopUpClosed() {
    loadPageDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.JAVA_DOC_POPUP)));
  }

  /** check text present in javadoc popup */
  public void checkTextToBePresentInJavaDocPopUp(String text) {
    loadPageDriverWait.until(frameToBeAvailableAndSwitchToIt(By.xpath(Locators.JAVA_DOC_POPUP)));
    waitTextInJavaDoc(text);
    seleniumWebDriver.switchTo().parentFrame();
  }

  /**
   * sometimes javadoc invoces with delays, in this case empty frame displaying first, and text
   * waits in this frame even if javadoc was loaded successfuly
   */
  private void waitTextInJavaDoc(String expectedText) {
    try {
      loadPageDriverWait.until(textToBePresentInElementLocated(By.tagName("body"), expectedText));
    } catch (TimeoutException ex) {
      seleniumWebDriver.switchTo().parentFrame();
      loadPageDriverWait.until(frameToBeAvailableAndSwitchToIt(By.xpath(Locators.JAVA_DOC_POPUP)));
      loadPageDriverWait.until(textToBePresentInElementLocated(By.tagName("body"), expectedText));
    }
  }

  /**
   * check text after go to link in javadoc at popup
   *
   * @param text
   * @param textLink text of link
   */
  public void checkTextAfterGoToLinkInJavaDocPopUp(String text, String textLink) {
    loadPageDriverWait.until(frameToBeAvailableAndSwitchToIt(By.xpath(Locators.JAVA_DOC_POPUP)));
    WebElement link =
        loadPageDriverWait.until(
            elementToBeClickable(By.xpath(String.format("//a[text()='%s']", textLink))));
    seleniumWebDriver.get(link.getAttribute("href"));
    loadPageDriverWait.until(textToBePresentInElementLocated(By.tagName("body"), text));
    seleniumWebDriver.navigate().back();
    seleniumWebDriver.switchTo().parentFrame();
  }

  /** open JavaDoc popup */
  public void openJavaDocPopUp() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    action.sendKeys(Keys.chord("q")).perform();
    action.keyUp(Keys.CONTROL).perform();
  }

  /**
   * wait the text in active line of current tab
   *
   * @param text is the text of elements active line
   */
  public void waitTextElementsActiveLine(final String text) {
    waitActive();
    redrawDriverWait.until(visibilityOf(activeLineXpath));
    redrawDriverWait.until(
        (ExpectedCondition<Boolean>) driver -> activeLineXpath.getText().contains(text));
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
        redrawDriverWait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(xpathToCurrentActiveCursorPosition))));
  }

  /**
   * wait specified values for Line and Char cursor positions in the Codenvy editor
   *
   * @param linePosition expected line position
   * @param charPosition expected char position
   */
  public void waitCursorPosition(final int linePosition, final int charPosition) {
    redrawDriverWait.until(
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
    redrawDriverWait.until(
        (ExpectedCondition<Boolean>) driver -> expectedLine == getPositionVisible());
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
    redrawDriverWait.until(
        (ExpectedCondition<Boolean>)
            webDriver ->
                (getPositionVisible() == linePosition) && (getPositionOfChar() == charPosition));
  }

  /**
   * wait specified values for Line and Char cursor positions in the Codenvy editor
   *
   * @param lineAndChar expected line and char position. For example: 1:25
   */
  public void waitSpecifiedValueForLineAndChar(String lineAndChar) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.POSITION_CURSOR_NUMBER, lineAndChar))));
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
    WebElement item =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.SELECTED_ITEM_IN_EDITOR, nameElement)));
    item.click();
    waitActive();
  }

  /**
   * wait the 'Implementation(s)' form is open
   *
   * @param fileName is name of the selected file
   */
  public void waitImplementationFormIsOpen(String fileName) {
    redrawDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(Locators.IMPLEMENTATION_CONTAINER, fileName))));
  }

  /** wait the 'Implementation(s)' form is closed */
  public void waitImplementationFormIsClosed(String fileName) {
    redrawDriverWait.until(
        invisibilityOfElementLocated(
            By.xpath(String.format(Locators.IMPLEMENTATION_CONTAINER, fileName))));
  }

  /** launch the 'Implementation(s)' form by keyboard */
  public void launchImplementationFormByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action
        .keyDown(Keys.CONTROL)
        .keyDown(Keys.ALT)
        .sendKeys("b")
        .keyUp(Keys.ALT)
        .keyUp(Keys.CONTROL)
        .perform();
  }

  /** close the forms in the editor by 'Escape' */
  public void cancelFormInEditorByEscape() {
    typeTextIntoEditor(Keys.ESCAPE.toString());
  }

  /**
   * wait expected text in the 'Implementation' form
   *
   * @param expText expected value
   */
  public void waitTextInImplementationForm(String expText) {
    redrawDriverWait.until(
        (ExpectedCondition<Boolean>) driver -> getTextFromImplementationForm().contains(expText));
  }

  /** get text from 'Implementation(s)' form */
  public String getTextFromImplementationForm() {
    return implementationContent.getText();
  }

  /**
   * perform 'double click on the item in the 'Implementation' form
   *
   * @param fileName is name of item
   */
  public void chooseImplementationByDoubleClick(String fileName) {
    WebElement fileImplement =
        seleniumWebDriver.findElement(
            By.xpath(String.format(Locators.IMPLEMENTATIONS_ITEM, fileName)));
    redrawDriverWait.until(visibilityOf(fileImplement));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(fileImplement).perform();
  }

  /**
   * select defined implementation in the 'Implementation' form
   *
   * @param fileName is name of item
   */
  public void selectImplementationByClick(String fileName) {
    redrawDriverWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.IMPLEMENTATIONS_ITEM, fileName))))
        .click();
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
    redrawDriverWait.until(elementToBeClickable(By.xpath(xPath))).click();
  }

  /**
   * returns the quantity of annotations submitted to the left ruler in editor
   *
   * @param markerType type of marker
   * @return quantity of annotations
   */
  public int getQuantityMarkers(String markerType) {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.RULER_OVERVIEW)));
    List<WebElement> annotationList =
        redrawDriverWait.until(presenceOfAllElementsLocatedBy(By.xpath(markerType)));
    return annotationList.size();
  }

  /**
   * wait that annotations are not present
   *
   * @param markerType type of marker
   */
  public void waitAnnotationsAreNotPresent(String markerType) {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(markerType)));
  }

  /**
   * remove line and everything after it
   *
   * @param numberOfLine number of line
   */
  public void removeLineAndAllAfterIt(int numberOfLine) {
    setCursorToLine(numberOfLine);
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.CONTROL).perform();
    action.keyDown(Keys.SHIFT).perform();
    action.sendKeys(Keys.END.toString()).perform();
    action.keyUp(Keys.CONTROL).perform();
    action.keyUp(Keys.SHIFT).perform();
    action.sendKeys(Keys.DELETE).perform();
  }

  /** wait the ruler overview is present */
  public void waitRulerOverviewIsPresent() {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.RULER_OVERVIEW)));
  }

  /** wait the ruler overview is not present */
  public void waitRulerOverviewIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.RULER_OVERVIEW)));
  }

  /** wait the ruler annotation is present */
  public void waitRulerAnnotationsIsPresent() {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.RULER_ANNOTATIONS)));
  }

  /** wait the ruler annotation is not present */
  public void waitRulerAnnotationsIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.RULER_ANNOTATIONS)));
  }

  /** wait the ruler line is present */
  public void waitRulerLineIsPresent() {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.RULER_LINES)));
  }

  /** wait the ruler line is not present */
  public void waitRulerLineIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.RULER_LINES)));
  }

  /** wait the ruler folding is present */
  public void waitRulerFoldingIsPresent() {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.RULER_FOLDING)));
  }

  /** wait the ruler folding is not present */
  public void waitRulerFoldingIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.RULER_FOLDING)));
  }

  /** wait the all punctuation separators are present */
  public void waitAllPunctuationSeparatorsArePresent() {
    redrawDriverWait.until(
        visibilityOfAllElementsLocatedBy(By.xpath(Locators.PUNCTUATION_SEPARATOR)));
  }

  /** wait the all punctuation separators are not present */
  public void waitAllPunctuationSeparatorsAreNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.PUNCTUATION_SEPARATOR)));
  }

  /** wait the text view ruler is present */
  public void waitTextViewRulerIsPresent() {
    redrawDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.TEXT_VIEW_RULER)));
  }

  /** wait the text view ruler is not present */
  public void waitTextViewRulerIsNotPresent() {
    redrawDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.TEXT_VIEW_RULER)));
  }

  /** click on 'Download sources' link in the editor */
  public void clickOnDownloadSourcesLink() {
    redrawDriverWait
        .until(visibilityOfElementLocated(By.xpath(Locators.DOWNLOAD_SOURCES_LINK)))
        .click();
  }

  /**
   * Change width of window for the Editor.
   *
   * @param xOffset horizontal move offset.
   */
  public void changeWidthWindowForEditor(int xOffset) {
    WebElement moveElement =
        redrawDriverWait.until(
            visibilityOfElementLocated(
                By.xpath("//div[@id='gwt-debug-navPanel']/parent::div/following::div[1]")));
    actionsFactory.createAction(seleniumWebDriver).dragAndDropBy(moveElement, xOffset, 0).perform();
  }

  /** Open list of the tabs Note: This possible if opened tabs don't fit in the tab bar. */
  public void openTabList() {
    redrawDriverWait.until(visibilityOfElementLocated(By.id(Locators.TAB_LIST_BUTTON))).click();
  }

  /**
   * Wait the tab is present in the tab list
   *
   * @param tabName name of tab
   */
  public void waitTabIsPresentInTabList(String tabName) {
    redrawDriverWait.until(
        visibilityOfElementLocated(By.xpath(String.format(Locators.ITEM_TAB_LIST, tabName))));
  }

  /**
   * Wait the tab is not present in the tab list
   *
   * @param tabName name of tab
   */
  public void waitTabIsNotPresentInTabList(String tabName) {
    redrawDriverWait.until(
        invisibilityOfElementLocated(By.xpath(String.format(Locators.ITEM_TAB_LIST, tabName))));
  }

  public void waitCountTabsWithProvidedName(int countTabs, String tabName) {
    loaderDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> countTabs == getAllTabsWithProvidedName(tabName).size());
  }

  /**
   * Click on tab in the tab list
   *
   * @param tabName name of tab
   */
  public void clickOnTabInTabList(String tabName) {
    redrawDriverWait
        .until(visibilityOfElementLocated(By.xpath(String.format(Locators.ITEM_TAB_LIST, tabName))))
        .click();
  }

  /**
   * Select tab by index of editor window after split
   *
   * @param index index of editor window
   * @param tabName name of tab
   */
  public void selectTabByIndexEditorWindow(int index, String tabName) {
    List<WebElement> windowList =
        redrawDriverWait.until(
            presenceOfAllElementsLocatedBy(
                By.xpath(String.format(Locators.TAB_FILE_NAME_XPATH, tabName))));
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
        redrawDriverWait.until(
            presenceOfAllElementsLocatedBy(
                By.xpath(String.format(Locators.TAB_FILE_CLOSE_ICON, tabName))));
    actionsFactory.createAction(seleniumWebDriver).contextClick(windowList.get(index)).perform();
  }

  /**
   * Check the tab of file is present once in editor
   *
   * @param expectTab expect name of tab
   */
  public boolean tabIsPresentOnce(String expectTab) {
    List<WebElement> windowList =
        redrawDriverWait.until(presenceOfAllElementsLocatedBy(By.id(Locators.EDITOR_TABS_PANEL)));
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
            elemDriverWait.until(
                ExpectedConditions.presenceOfNestedElementLocatedBy(
                    line, By.xpath(String.format("span[%s]", a + 1))));
          }

          nestedElements.remove(nestedElements.size() - 1);
          nestedElements.forEach(elem -> stringBuilder.append(elem.getText()));
          stringBuilder.append("\n");
        });

    return stringBuilder.deleteCharAt(stringBuilder.length() - 1);
  }

  /** open context menu into editor */
  public void openContextMenuInEditor() {
    WebElement element = loadPageDriverWait.until(visibilityOf(activeEditorContainer));
    Actions act = actionsFactory.createAction(seleniumWebDriver);
    act.contextClick(element).perform();
    waitContextMenu();
  }

  /**
   * open context menu on the selected element into editor
   *
   * @param selectedElement is the selected element into editor
   */
  public void openContextMenuOnElementInEditor(String selectedElement) {
    WebElement element =
        loadPageDriverWait.until(
            visibilityOfElementLocated(
                By.xpath(String.format(Locators.SELECTED_ITEM_IN_EDITOR, selectedElement))));
    Actions act = actionsFactory.createAction(seleniumWebDriver);
    act.contextClick(element).perform();
    waitContextMenu();
  }

  /** wait context menu form is open */
  public void waitContextMenu() {
    loadPageDriverWait.until(visibilityOfElementLocated(By.xpath(Locators.CONTEXT_MENU)));
  }

  /** wait context menu form is not present */
  public void waitContextMenuIsNotPresent() {
    loadPageDriverWait.until(invisibilityOfElementLocated(By.xpath(Locators.CONTEXT_MENU)));
  }

  /**
   * click on element from context menu by name
   *
   * @param item is a name into context menu
   */
  public void clickOnItemInContextMenu(String item) {
    redrawDriverWait.until(visibilityOfElementLocated(By.id(item))).click();
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

    loadPageDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> {
              String javaDocPopupHtmlText = "";
              try {
                javaDocPopupHtmlText = getJavaDocPopupText();
              } catch (StaleElementReferenceException e) {
                LOG.warn("Can not get java doc HTML text from autocomplete context menu in editor");
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
    return loaderDriverWait.until(
        visibilityOfAllElementsLocatedBy(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-multiSplitPanel-tabsPanel']//div[text()='%s']",
                    tabName))));
  }
}
