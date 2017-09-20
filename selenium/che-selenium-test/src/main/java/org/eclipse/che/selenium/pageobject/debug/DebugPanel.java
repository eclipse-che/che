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
package org.eclipse.che.selenium.pageobject.debug;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Musienko Maxim */
@Singleton
public class DebugPanel {

  private static final Logger LOG = LoggerFactory.getLogger(DebugPanel.class);

  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsExplorer commandsExplorer;

  @Inject
  public DebugPanel(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      CodenvyEditor editor,
      CommandsExplorer commandsExplorer) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.editor = editor;
    this.commandsExplorer = commandsExplorer;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String DEBUGGER_BREAKPOINTS_PANEL_ID = "gwt-debug-debugger-breakpointsPanel";
    String DEBUGGER_PANEL_TAB = "gwt-debug-partButton-Debug";
    String FRAMES_LIST_ID = "gwt-debug-debugger-frames-list";
    String THREADS_LIST_ID = "gwt-debug-debugger-threads-list";
    String VARIABLES_PANEL_ID = "gwt-debug-debugger-variablesPanel";
    String VARIABLE_PANEL_SELECT_VAL =
        "//div[@id='gwt-debug-debugger-variablesPanel']//span[text()='%s']";
  }

  interface LocatorsChangeVariable {
    String CHANGE_VARIABLE_BTN = "//button[text()='Change']";
    String CANCEL_BTN = "//button[text()='Cancel']";
    String TEXTAREA =
        "//div[text()='Enter a new value for ']/parent::div/following-sibling::div/textarea";
  }

  public interface DebuggerButtonsPanel {
    String RESUME_BTN_ID = "gwt-debug-ActionButton/resumeExecution-true";
    String STEP_INTO = "gwt-debug-ActionButton/stepInto-true";
    String STEP_OVER = "gwt-debug-ActionButton/stepOver-true";
    String STEP_OUT = "gwt-debug-ActionButton/stepOut-true";
    String BTN_DISCONNECT = "gwt-debug-ActionButton/disconnectDebug-true";
    String REMOVE_ALL_BREAKPOINTS = "gwt-debug-ActionButton/null-true";
    String CHANGE_VARIABLE = "gwt-debug-ActionButton/changeVariableValue-true";
    String EVALUATE_EXPRESSIONS = "gwt-debug-ActionButton/evaluateExpression-true";
  }

  @FindBy(id = Locators.DEBUGGER_BREAKPOINTS_PANEL_ID)
  WebElement debuggerBreakPointPanel;

  @FindBy(id = Locators.DEBUGGER_PANEL_TAB)
  WebElement debuggerTab;

  @FindBy(id = Locators.VARIABLES_PANEL_ID)
  WebElement variablesPanel;

  @FindBy(xpath = LocatorsChangeVariable.TEXTAREA)
  WebElement changeVariableTextAreaForm;

  @FindBy(xpath = LocatorsChangeVariable.CHANGE_VARIABLE_BTN)
  WebElement changeVariableBtn;

  @FindBy(xpath = LocatorsChangeVariable.CANCEL_BTN)
  WebElement cancelVariableBtn;

  @FindBy(id = Locators.FRAMES_LIST_ID)
  WebElement frames;

  @FindBy(id = Locators.THREADS_LIST_ID)
  WebElement threads;

  /** Wait while debugger panel will be clear for all breakpoints */
  public void waitWhileAllBreakPointsOnEditorPanelDisapper() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.breakpoint")));
  }

  /**
   * Wait specified content in the IDE breakpoint panel
   *
   * @param content
   */
  public void waitContentInBreakPointPanel(final String content) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver webDriver) -> {
              return debuggerBreakPointPanel.getText().contains(content);
            });
  }

  /** Wait disappearance any breakpoints from debugger breakpoints panel */
  public void waitBreakPointsPanelIsEmpty() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (WebDriver webDriver) -> {
              return debuggerBreakPointPanel.getText().isEmpty();
            });
  }

  /** Wait while the Variables panel appears */
  public void waitVariablesPanel() {
    new WebDriverWait(seleniumWebDriver, 20).until(ExpectedConditions.visibilityOf(variablesPanel));
  }

  /**
   * Wait expected text in variable panel
   *
   * @param text expected text
   */
  public void waitTextInVariablesPanel(final String text) {
    waitVariablesPanel();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until((ExpectedCondition<Boolean>) (webDriver -> variablesPanel.getText().contains(text)));
  }

  /**
   * Select specified value in Variable panel
   *
   * @param variable
   */
  public void selectVarInVariablePanel(String variable) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.VARIABLE_PANEL_SELECT_VAL, variable))));
    seleniumWebDriver
        .findElement(By.xpath(String.format(Locators.VARIABLE_PANEL_SELECT_VAL, variable)))
        .click();
  }

  /**
   * Click on specific button on Debugger panel
   *
   * @param buttonIdLocator use interface DebuggerButtonsPanel for select buttonIdLocator
   */
  public void clickOnButton(String buttonIdLocator) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(buttonIdLocator)));
    seleniumWebDriver.findElement(By.id(buttonIdLocator)).click();
    loader.waitOnClosed();
  }

  /** Wait appearance change variable form */
  public void waitAppearChangeVariableForm() {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(ExpectedConditions.visibilityOf(changeVariableTextAreaForm));
  }

  /** Wait disappear variable form */
  public void waitDisappearChangeVariableForm() {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(LocatorsChangeVariable.TEXTAREA)));
  }

  /**
   * Clear the debugger variable field and type new value
   *
   * @param value new variable value
   */
  public void typeNewValueInVariableForm(String value) {
    waitAppearChangeVariableForm();
    changeVariableTextAreaForm.clear();
    changeVariableTextAreaForm.sendKeys(value);
  }

  /**
   * Clear aria with variables, type new value and click change button
   *
   * @param newVariable new value for variable
   */
  public void typeAndChangeVariable(String newVariable) {
    typeNewValueInVariableForm(newVariable);
    changeVariableBtn.click();
    waitDisappearChangeVariableForm();
    loader.waitOnClosed();
  }

  /**
   * Type user expression to the IDE Enter an expression field
   *
   * @param expression user expression
   */
  public void typeEvaluateExpression(String expression) {
    String exprFieldLocator =
        "//div[text()='Enter an expression:']/parent::div/following-sibling::div/input";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(exprFieldLocator)))
        .clear();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(exprFieldLocator)))
        .sendKeys(expression);
  }

  /**
   * Wait expected value in the Result field after evaluating
   *
   * @param expVal exoected value
   */
  public void waitExpectedResultInEvaluateExpression(final String expVal) {
    final String locator = "//div[text()='Result:']/parent::div/following-sibling::div/textarea";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> {
                  return seleniumWebDriver
                      .findElement(By.xpath(locator))
                      .getAttribute("value")
                      .equals(expVal);
                });
  }

  /** Click on evaluate exp. button */
  public void clickEvaluateBtn() {
    String locator = "//button[text()='Evaluate']";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
  }

  /** Click on close evaluate exp. button */
  public void clickCloseEvaluateBtn() {
    String locator = "//button[text()='Evaluate']/preceding-sibling::button";
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
  }

  /**
   * Check debugger button panel
   *
   * @return true if present on page and false on other cases
   */
  public boolean isDebuggerBtnPanelPresent() {

    try {
      return seleniumWebDriver
          .findElement(By.id(DebuggerButtonsPanel.REMOVE_ALL_BREAKPOINTS))
          .isDisplayed();
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Wait text in debug highlighted area (red line into Che editor under debugger)
   *
   * @param text the text under debug - highlighter
   */
  public void waitDebugHighlightedText(String text) {
    StringBuilder highLightedText = new StringBuilder();
    String locatorWithHiglightedText =
        "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[@active]//div[@class='textviewContent' and @contenteditable='true']//span[@debugid='debug-line']";
    List<WebElement> hilightedElements =
        new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(locatorWithHiglightedText)));
    for (WebElement hilightedElement : hilightedElements) {
      highLightedText.append(hilightedElement.getText());
    }
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(
            (WebDriver driver) -> {
              return highLightedText.toString().contains(text);
            });
  }

  /**
   * Wait highlighter in the specified position
   *
   * @param numOfPosition
   */
  public void waitBreakPointHighlighterInDefinedPosition(int numOfPosition) {
    editor.returnFocusInCurrentLine();
    editor.waitActiveEditor();
    List<WebElement> editorLines =
        seleniumWebDriver.findElements(
            By.xpath(
                "//div[@id='gwt-debug-editorPartStack-contentPanel']//div[@active]//div[@class='textviewContent' and @contenteditable='true']/div"));
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver driver) -> {
              try {
                return editorLines
                    .get(numOfPosition - 1)
                    .findElement(By.tagName("span"))
                    .getAttribute("debugid")
                    .equals("debug-line");
              } catch (Exception e) {
                return false;
              }
            });
  }

  /** Open debug panel by clicking on "Debug" tab. */
  public void openDebugPanel() {
    if (!isDebuggerBtnPanelPresent()) {
      debuggerTab.click();
    }
  }

  /**
   * Click on "Remove All Breakpoints" button in Debug panel and wait until breakpoints go away from
   * Debug panel.
   */
  public void removeAllBreakpoints() {
    clickOnButton(DebugPanel.DebuggerButtonsPanel.REMOVE_ALL_BREAKPOINTS);
    waitWhileAllBreakPointsOnEditorPanelDisapper();
    waitBreakPointsPanelIsEmpty();
  }

  /**
   * This method use for stopping Debugger using UI. Check if debugger panel presents. If present
   * click on remove all breakpoints button. than click on stop debugger session button, if some web
   * element covers the panel - browser will be refreshed for closing. If the Debugger have not
   * appeared on UI. After stopping the debugger we call clean up command for tomcat (the command
   * should be prepared in a test. The command stop the tomcat and clea webapp folder)
   *
   * @param cleanUpTomcat
   */
  public void stopDebuggerWithUiAndCleanUpTomcat(String cleanUpTomcat) {
    //if something went wrong on ui we refresh browser and try to click on remove all breakpoints button again
    if (isDebuggerBtnPanelPresent()) {
      try {
        removeAllBreakpoints();
      } catch (WebDriverException ex) {
        LOG.error(ex.getLocalizedMessage(), ex);
        seleniumWebDriver.navigate().refresh();
        clickOnButton(DebugPanel.DebuggerButtonsPanel.REMOVE_ALL_BREAKPOINTS);
      }
      clickOnButton(DebugPanel.DebuggerButtonsPanel.BTN_DISCONNECT);
    }

    try {
      commandsExplorer.waitCommandExplorerIsOpened();
      commandsExplorer.openCommandsExplorer();
      commandsExplorer.waitCommandExplorerIsOpened();
      commandsExplorer.runCommandByName(cleanUpTomcat);
    } catch (TimeoutException ex) {
      commandsExplorer.openCommandsExplorer();
      commandsExplorer.waitCommandExplorerIsOpened();
      commandsExplorer.runCommandByName(cleanUpTomcat);
    }
  }

  public String getVariables() {
    waitVariablesPanel();
    return variablesPanel.getText();
  }

  public void waitFramesListPanelReady() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((Function<WebDriver, Object>) webDriver -> !frames.getText().isEmpty());
  }

  public String[] getFrames() {
    waitFramesListPanelReady();
    return frames.getText().split("\n");
  }

  /** Waits */
  public void waitThreadListPanelReady() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((Function<WebDriver, Object>) webDriver -> !threads.getText().isEmpty());
  }

  public void selectFrame(int frameIndex) {
    waitFramesListPanelReady();

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//td[text()='" + getFrames()[frameIndex] + "']")))
        .click();
  }

  public String[] getThreads() {
    waitThreadListPanelReady();
    return threads.getText().split("\n");
  }

  public String getSelectedThread() {
    waitThreadListPanelReady();

    String selectedThreadId = threads.getAttribute("value");
    for (String thread : getThreads()) {
      if (thread.contains("@" + selectedThreadId + " in")) {
        return thread;
      }
    }

    return null;
  }

  public void selectThread(String threadName) {
    waitThreadListPanelReady();

    threads.click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'\"" + threadName + "\"@')]")))
        .click();
    threads.click();
  }
}
