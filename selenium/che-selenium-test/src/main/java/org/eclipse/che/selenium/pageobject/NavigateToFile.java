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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.Keys.ALT;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Created by aleksandr shmaraev on 12.12.14. */
@Singleton
public class NavigateToFile {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public NavigateToFile(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String NAVIGATE_TO_FILE_FORM = "gwt-debug-file-navigateToFile-mainPanel";
    String FILE_NAME_INPUT = "gwt-debug-navigateToFile-fileName";
    String SUGGESTION_PANEL = "gwt-debug-navigateToFile-suggestionPanel";
    String FILE_NAME_LIST_SELECT_WITH_PATH =
        "//div[@id='gwt-debug-navigateToFile-suggestionPanel']//div[last()]//tr[contains(., '%s')]";
    String FILE_NAME_LIST_SELECT =
        "//div[@id='gwt-debug-navigateToFile-suggestionPanel']//div[last()]//td[text()='%s']";
  }

  @FindBy(id = Locators.NAVIGATE_TO_FILE_FORM)
  WebElement navigateToFileForm;

  @FindBy(id = Locators.FILE_NAME_INPUT)
  WebElement fileNameInput;

  @FindBy(id = Locators.SUGGESTION_PANEL)
  WebElement suggestionPanel;

  /** wait opening of 'Navigate to file' widget */
  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(navigateToFileForm));
  }

  /** wait closing of 'Navigate to file' widget */
  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.id(Locators.NAVIGATE_TO_FILE_FORM)));
  }

  /** launch the 'Navigate To File' widget by keyboard (with Ctrl + 'n' keys) */
  public void launchNavigateToFileByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).keyDown(ALT).sendKeys("n").keyUp(CONTROL).keyUp(ALT).perform();
  }

  /**
   * Wait opening the widget. Clear previous items in the field. Type the user value for searching.
   *
   * @param symbol the first symbol of search with key word
   */
  public void typeSymbolInFileNameField(String symbol) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(fileNameInput));
    fileNameInput.clear();
    WaitUtils.sleepQuietly(1); // timeout for waiting that input field is cleared
    fileNameInput.sendKeys(symbol);
  }

  /**
   * type the user value for searching into field of the widget without clearing the previous values
   *
   * @param symbol the first symbol of search with key word
   */
  public void typeSymbolWithoutClear(String symbol) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(fileNameInput));
    fileNameInput.sendKeys(symbol);
  }

  /** wait appearance of the dropdawn list (may be empty) */
  public void waitFileNamePopUp() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(suggestionPanel));
  }

  /**
   * wait expected text in the dropdawn list of the widget
   *
   * @param text a text that should be into list
   */
  public boolean isFilenameSuggested(String text) {
    return suggestionPanel.getText().contains(text);
  }

  public void waitSuggestedPanel() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC).until(visibilityOf(suggestionPanel));
  }

  public String getText() {
    return suggestionPanel.getText();
  }

  /**
   * select the item from drop dawn list by name of file with path
   *
   * @param pathName full name - means the name of file with path
   */
  public void selectFileByFullName(String pathName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.FILE_NAME_LIST_SELECT_WITH_PATH, pathName))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
    waitFormToClose();
  }

  /**
   * select the defined item with just name of a file (path to file we can not specify)
   *
   * @param nameOfFile name of a file for searching
   */
  public void selectFileByName(String nameOfFile) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.FILE_NAME_LIST_SELECT, nameOfFile))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /** close the Navigate to file widget by 'Escape' key and wait closing of the widget */
  public void closeNavigateToFileForm() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(ESCAPE).perform();
    waitFormToClose();
  }
}
