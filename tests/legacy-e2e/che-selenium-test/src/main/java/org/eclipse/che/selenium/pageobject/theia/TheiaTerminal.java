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
package org.eclipse.che.selenium.pageobject.theia;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.CURSOR_LAYER_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TAB_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TEXT_LAYER_XPATH_TEMPLATE;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.INSERT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@Singleton
public class TheiaTerminal {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final TheiaProjectTree theiaProjectTree;
  private final TheiaEditor theiaEditor;
  private final TheiaIde theiaIde;
  private final TheiaNewFileDialog theiaNewFileDialog;
  private final TheiaDeleteFileDialog theiaDeleteFileDialog;

  @Inject
  private TheiaTerminal(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      TheiaProjectTree theiaProjectTree,
      TheiaEditor theiaEditor,
      TheiaIde theiaIde,
      TheiaNewFileDialog theiaNewFileDialog,
      TheiaDeleteFileDialog theiaDeleteFileDialog) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.theiaProjectTree = theiaProjectTree;
    this.theiaEditor = theiaEditor;
    this.theiaIde = theiaIde;
    this.theiaNewFileDialog = theiaNewFileDialog;
    this.theiaDeleteFileDialog = theiaDeleteFileDialog;
  }

  public interface Locators {
    String BODY_ID_TEMPLATE = "terminal-%s";
    String TEXT_LAYER_XPATH_TEMPLATE = "(//canvas[@class='xterm-text-layer'])[%s]";
    String CURSOR_LAYER_XPATH_TEMPLATE = "(//canvas[@class='xterm-cursor-layer'])[%s]";
    String TAB_XPATH_TEMPLATE = "//div[@class='p-TabBar-tabLabel' and text()='%s']";
  }

  private String getCursorLayerXpath(int terminalIndex) {
    final int adaptedTerminalIndex = terminalIndex + 1;

    return format(CURSOR_LAYER_XPATH_TEMPLATE, adaptedTerminalIndex);
  }

  private String getTextLayerXpath(int terminalIndex) {
    final int adaptedTerminalIndex = terminalIndex + 1;

    return format(TEXT_LAYER_XPATH_TEMPLATE, adaptedTerminalIndex);
  }

  private String getTabXpath(String tabTitle) {
    return format(TAB_XPATH_TEMPLATE, tabTitle);
  }

  private WebElement getTextLayer(int terminalIndex) {
    final String terminalTextLayerXpath = getTextLayerXpath(terminalIndex);

    return seleniumWebDriverHelper.waitVisibility(By.xpath(terminalTextLayerXpath));
  }

  public void waitTabSelected(String tabTitle) {
    final String tabXpath = getTabXpath(tabTitle);
    final String cssTopBorderColorProperty = "border-top-color";
    final String selectedElementTopBorderColor = "rgba(245, 245, 245, 1)";

    seleniumWebDriverHelper.waitCssValueEqualsTo(
        By.xpath(tabXpath), cssTopBorderColorProperty, selectedElementTopBorderColor);
  }

  public void waitTabUnselected(String tabTitle) {
    final String tabXpath = getTabXpath(tabTitle);
    final String cssTopBorderColorProperty = "border-top-color";
    final String unselectedElementTopBorderColor = "rgba(245, 245, 245, 1)";

    seleniumWebDriverHelper.waitCssValueEqualsTo(
        By.xpath(tabXpath), cssTopBorderColorProperty, unselectedElementTopBorderColor);
  }

  public void waitTab(String tabTitle) {
    final String tabXpath = getTabXpath(tabTitle);

    seleniumWebDriverHelper.waitVisibility(By.xpath(tabXpath));
  }

  public void waitTabDisappearance(String tabTitle) {
    final String tabXpath = getTabXpath(tabTitle);

    seleniumWebDriverHelper.waitInvisibility(By.xpath(tabXpath));
  }

  public void clickOnTab(String tabTitle) {
    final String tabXpath = getTabXpath(tabTitle);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> performClick(tabXpath), StaleElementReferenceException.class);
  }

  private void performClick(String elementXpath) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(elementXpath));
  }

  public void waitTerminal() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TEXT_LAYER_XPATH_TEMPLATE));
  }

  public void clickOnTerminal(int terminalIndex) {
    final String terminalCursorLayerXpath = getCursorLayerXpath(terminalIndex);

    seleniumWebDriverHelper.waitAndClick(By.xpath(terminalCursorLayerXpath));
  }

  public void enterText(String text) {
    // performs wait for terminal readiness
    WaitUtils.sleepQuietly(2);

    seleniumWebDriverHelper.sendKeys(text);
  }

  public void performCommand(String command) {
    enterText(command);
    enterText(ENTER.toString());
  }

  private void copyTerminalTextToClipboard(int terminalIndex) {
    Dimension textLayerSize = getTextLayer(terminalIndex).getSize();
    final Actions action = seleniumWebDriverHelper.getAction();

    final int xBeginCoordinateShift = -(textLayerSize.getWidth() / 2);
    final int yBeginCoordinateShift = -(textLayerSize.getHeight() / 2);

    seleniumWebDriverHelper.moveCursorTo(getTextLayer(terminalIndex));

    // shift to top left corner
    seleniumWebDriverHelper
        .getAction()
        .moveByOffset(xBeginCoordinateShift, yBeginCoordinateShift)
        .perform();

    // select all terminal area by mouse
    action.clickAndHold().perform();
    seleniumWebDriverHelper
        .getAction()
        .moveByOffset(textLayerSize.getWidth(), textLayerSize.getHeight())
        .perform();

    action.release().perform();

    // copy terminal output to clipboard
    String keysCombination = Keys.chord(CONTROL, INSERT);
    seleniumWebDriverHelper.sendKeys(keysCombination);

    // cancel terminal area selection
    clickOnTerminal(terminalIndex);
  }

  public void waitTerminalOutput(String expectedText, int terminalIndex) {
    final int timeout = LOADER_TIMEOUT_SEC;

    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> getTerminalOutput(terminalIndex).contains(expectedText), timeout);
  }

  public String getTerminalOutput(int terminalIndex) {
    // TODO this is workaround and should be resolved by issue:
    // https://github.com/eclipse/che/issues/11387

    final String expectedTextFileName = "Untitled.txt";
    String terminalOutput = "";

    copyTerminalTextToClipboard(terminalIndex);

    // create text file
    theiaProjectTree.clickOnProjectsRootItem();
    theiaProjectTree.waitProjectsRootItemSelected();
    theiaIde.runMenuCommand("File", "New File");
    theiaNewFileDialog.waitDialog();
    theiaNewFileDialog.waitNewFileNameValue(expectedTextFileName);
    theiaNewFileDialog.clickOkButton();
    theiaNewFileDialog.waitDialogClosed();

    // check text file availability
    theiaEditor.waitEditorTab(expectedTextFileName);
    theiaEditor.waitTabSelecting(expectedTextFileName);
    theiaEditor.waitActiveEditor();
    theiaEditor.performPasteAction();

    terminalOutput = theiaEditor.getEditorTextAsString();

    // delete text file
    theiaProjectTree.clickOnItem(expectedTextFileName);
    theiaProjectTree.waitItemSelected(expectedTextFileName);
    seleniumWebDriverHelper.sendKeys(Keys.DELETE.toString());
    theiaDeleteFileDialog.waitDialog();
    theiaDeleteFileDialog.clickOkButton();
    theiaDeleteFileDialog.waitDialogDesapearance();
    theiaProjectTree.waitItemDisappearance(expectedTextFileName);
    theiaEditor.waitEditorTabDesapearance(expectedTextFileName);

    return terminalOutput;
  }

  public boolean isTextPresentInTerminalOutput(String expectedText, int terminalIndex) {
    try {
      waitTerminalOutput(expectedText, terminalIndex);
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }
}
