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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TERMINAL_CURSOR_LAYER;
import static org.eclipse.che.selenium.pageobject.theia.TheiaTerminal.Locators.TERMINAL_TEXT_LAYER;
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
    String TERMINAL_BODY_ID_TEMPLATE = "terminal-%s";
    String TERMINAL_TEXT_LAYER = "//canvas[@class='xterm-text-layer']";
    String TERMINAL_CURSOR_LAYER = "//canvas[@class='xterm-cursor-layer']";
  }

  private WebElement getTerminalTextLayer() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(TERMINAL_TEXT_LAYER));
  }

  public void waitTerminal() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TERMINAL_TEXT_LAYER));
  }

  public void clickOnTerminal() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(TERMINAL_CURSOR_LAYER));
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

  private void copyTerminalTextToClipboard() {
    Dimension textLayerSize = getTerminalTextLayer().getSize();
    final Actions action = seleniumWebDriverHelper.getAction();

    final int xBeginCoordinateShift = -(textLayerSize.getWidth() / 2);
    final int yBeginCoordinateShift = -(textLayerSize.getHeight() / 2);

    seleniumWebDriverHelper.moveCursorTo(getTerminalTextLayer());

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
    clickOnTerminal();
  }

  public void waitTerminalOutput(String expectedText) {
    final int timeout = LOADER_TIMEOUT_SEC;

    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> getTerminalOutput().contains(expectedText), timeout);
  }

  public String getTerminalOutput() {
    // TODO this is workaround and should be resolved by issue:
    // https://github.com/eclipse/che/issues/11387

    final String expectedTextFileName = "Untitled.txt";
    String terminalOutput = "";

    copyTerminalTextToClipboard();

    // create text file
    theiaProjectTree.clickOnProjectsRootItem();
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
}
