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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
    String NAVI_TO_FILE_FORM = "gwt-debug-file-navigateToFile-mainPanel";
    String FILE_NAME_FIELD = "//div[text()='Enter file name:']/following::input[1]";
    String FILE_NAME_POPUP = "//div[text()='Enter file name:']/following::table[1]";
    String FILE_NAME_LIST_SELECT_WITH_PATH =
        "//div[text()='Enter file name:']//following::tr[contains(., '%s')]";
    String FILE_NAME_LIST_SELECT =
        "//div[text()='Enter file name:']/following::table[1]//td[text()='%s']";
  }

  @FindBy(id = Locators.NAVI_TO_FILE_FORM)
  WebElement naviToFileForm;

  @FindBy(xpath = Locators.FILE_NAME_FIELD)
  WebElement fileNameField;

  @FindBy(xpath = Locators.FILE_NAME_POPUP)
  WebElement fileNamePopUp;

  /** wait opening of 'Navigate to file' widget */
  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(naviToFileForm));
  }

  /** wait closing of 'Navigate to file' widget */
  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.NAVI_TO_FILE_FORM)));
  }

  /** launch the 'Navigate To File' widget by keyboard (with Ctrl + 'n' keys) */
  public void launchNavigateToFileByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action
        .keyDown(Keys.CONTROL)
        .keyDown(Keys.ALT)
        .sendKeys("n")
        .keyUp(Keys.CONTROL)
        .keyUp(Keys.ALT)
        .perform();
  }

  /**
   * Wait opening the widget. Clear previous items in the field. Type the user value for searching.
   *
   * @param symbol the first symbol of search with key word
   */
  public void typeSymbolInFileNameField(String symbol) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(fileNameField));
    fileNameField.clear();
    fileNameField.sendKeys(symbol);
  }

  /**
   * type the user value for searching into field of the widget without clearing the previous values
   *
   * @param symbol the first symbol of search with key word
   */
  public void typeSymbolWithoutClear(String symbol) {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(fileNameField));
    fileNameField.sendKeys(symbol);
  }

  /** wait appearance of the dropdawn list (may be empty) */
  public void waitFileNamePopUp() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(fileNamePopUp));
  }

  /**
   * wait expected text in the dropdawn list of the widget
   *
   * @param text a text that should be into list
   */
  public void waitListOfFilesNames(final String text) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until((ExpectedCondition<Boolean>) webDriver -> fileNamePopUp.getText().contains(text));
  }

  public String getText() {
    return fileNamePopUp.getText();
  }

  /**
   * select the item from drop dawn list by name of file with path
   *
   * @param pathName full name - means the name of file with path
   */
  public void selectFileByFullName(String pathName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.FILE_NAME_LIST_SELECT_WITH_PATH, pathName))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /**
   * select the defined item with just name of a file (path to file we can not specify)
   *
   * @param nameOfFile name of a file for searching
   */
  public void selectFileByName(String nameOfFile) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.FILE_NAME_LIST_SELECT, nameOfFile))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  /** close the Navigate to file widget by 'Escape' key and wait closing of the widget */
  public void closeNavigateToFileForm() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ESCAPE).perform();
    waitFormToClose();
  }
}
