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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev */
@Singleton
public class FindText {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public FindText(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_FORM = "gwt-debug-text-search-mainPanel";
    String FIND_TEXT_INPUT = "gwt-debug-text-search-text";
    String WHOLE_WORD_CHECKLBOX_SPAN = "gwt-debug-wholeWordsOnly-selector";
    String WHOLE_WORD_CHECKLBOX_INP = "gwt-debug-wholeWordsOnly-selector-input";
    String SEARCH_ROOT_CHECKBOX_SPAN = "//div[text()='Scope']/following::span[1]";
    String SEARCH_ROOT_CHECKBOX_INP = "//div[text()='Scope']/following::input[1]";
    String SEARCH_DIR_FIELD = "gwt-debug-text-search-directory";
    String SEARCH_DIR_BUTTON = "gwt-debug-text-search-directory-button";
    String FILE_MASK_CHECKBOX_SPAN = "//div[text()='File name filter']/following::span[1]";
    String FILE_MASK_CHECKBOX_INP = "//div[text()='File name filter']/following::input[1]";
    String FILE_MASK_FIELD = "gwt-debug-text-search-files";
    String CANCEL_BUTTON = "search-cancel-button";
    String SEARCH_BUTTON = "search-button";
    String FIND_INFO_PANEL = "gwt-debug-find-info-panel";
    String FIND_INFO_PANEL_TEXT_CONTAINER = "gwt-debug-partStackContent";
    String FIND_TEXT_BUTTON = "gwt-debug-partButton-Find";
    String OCCURRENCE = "//span[@debugfilepath = '%s']";
    String PREVIOUS_BUTTON = "gwt-debug-previous-button";
    String NEXT_BUTTON = "gwt-debug-next-button";
    String SEARCH_RESULTS = "gwt-debug-search-result-label";
    String FILE_NODE = "//span[@id='%s']";
  }

  @FindBy(id = Locators.WHOLE_WORD_CHECKLBOX_INP)
  WebElement wholeWordCheckBox;

  @FindBy(xpath = Locators.SEARCH_ROOT_CHECKBOX_INP)
  WebElement searchRootCheckBox;

  @FindBy(xpath = Locators.FILE_MASK_CHECKBOX_INP)
  WebElement fileMaskCheckBox;

  @FindBy(id = Locators.FIND_INFO_PANEL)
  WebElement findInfoPanel;

  @FindBy(id = Locators.FIND_INFO_PANEL_TEXT_CONTAINER)
  WebElement findInfoPanelTextContainer;

  @FindBy(id = Locators.FIND_TEXT_BUTTON)
  WebElement findTextBtn;

  /** wait the 'Find Text' main form is open */
  public void waitFindTextMainFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.MAIN_FORM)));
  }

  /** launch the 'Find' main form by keyboard */
  public void launchFindFormByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action
        .keyDown(Keys.CONTROL)
        .keyDown(Keys.SHIFT)
        .sendKeys("f")
        .keyUp(Keys.SHIFT)
        .keyUp(Keys.CONTROL)
        .perform();
  }

  /** wait the 'Find Text' main form is closed */
  public void waitFindTextMainFormIsClosed() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM)));
  }

  /** close main form by pressing 'Ctrl' key */
  public void closeFindTextFormByEscape() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ESCAPE.toString()).perform();
    waitFindTextMainFormIsClosed();
  }

  /** close main form by pressing 'Cancel' button */
  public void closeFindTextMainForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.CANCEL_BUTTON)))
        .click();
    waitFindTextMainFormIsClosed();
  }

  /** wait the 'Search' button is disabled on the main form */
  public void waitSearchBtnMainFormIsDisabled() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.not(
                ExpectedConditions.elementToBeClickable(By.id(Locators.SEARCH_BUTTON))));
  }

  /** press on the 'Search' button on the main form */
  public void clickOnSearchButtonMainForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.id(Locators.SEARCH_BUTTON)))
        .click();
    waitFindTextMainFormIsClosed();
  }

  /**
   * type text into 'Text to find' field
   *
   * @param text is text that need to find
   */
  public void typeTextIntoFindField(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.FIND_TEXT_INPUT)))
        .clear();
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.FIND_TEXT_INPUT)))
        .sendKeys(text);
  }

  /**
   * wait text into 'Text to find' field
   *
   * @param expText is expected text
   */
  public void waitTextIntoFindField(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.id(Locators.FIND_TEXT_INPUT))
                        .getAttribute("value")
                        .equals(expText));
  }

  /**
   * Set the 'Whole word only' checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitWholeWordCheckbox(boolean state) {
    if (state) {
      if (!wholeWordCheckBox.isSelected()) {
        clickOnWholeWordCheckbox();
        waitWholeWordIsSelected();
      }
    } else {
      if (wholeWordCheckBox.isSelected()) {
        clickOnWholeWordCheckbox();
        waitWholeWordIsNotSelected();
      }
    }
  }

  /** click on the 'Whole word only' checkbox */
  public void clickOnWholeWordCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(Locators.WHOLE_WORD_CHECKLBOX_SPAN)))
        .click();
  }

  /** wait the 'Whole word only' checkbox is selected */
  public void waitWholeWordIsSelected() {
    String locator = Locators.WHOLE_WORD_CHECKLBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.id(locator), true));
  }

  /** wait the 'Whole word only' checkbox is not selected */
  public void waitWholeWordIsNotSelected() {
    String locator = Locators.WHOLE_WORD_CHECKLBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.id(locator), false));
  }

  /**
   * Set the 'Search root' checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitStateSearchRootCheckbox(boolean state) {
    if (state) {
      if (!searchRootCheckBox.isSelected()) {
        clickOnSearchRootCheckbox();
        waitSearchRootIsSelected();
      }
    } else {
      if (searchRootCheckBox.isSelected()) {
        clickOnSearchRootCheckbox();
        waitSearchRootIsNotSelected();
      }
    }
  }

  /** click on the 'Search root' checkbox */
  public void clickOnSearchRootCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.SEARCH_ROOT_CHECKBOX_SPAN)))
        .click();
  }

  /** wait the 'Search root' checkbox is selected */
  public void waitSearchRootIsSelected() {
    String locator = Locators.SEARCH_ROOT_CHECKBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), true));
  }

  /** wait the 'Search root' checkbox is not selected */
  public void waitSearchRootIsNotSelected() {
    String locator = Locators.SEARCH_ROOT_CHECKBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), false));
  }

  /**
   * wait path into 'Search root' field
   *
   * @param path is the expected path
   */
  public void waitPathIntoRootField(String path) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.id(Locators.SEARCH_DIR_FIELD))
                        .getAttribute("value")
                        .equals(path));
  }

  /** press on the search directory button */
  public void clickSearchDirectoryBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.id(Locators.SEARCH_DIR_BUTTON)))
        .click();
  }

  /**
   * Set the 'File mask' checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitFileMaskCheckbox(boolean state) {
    if (state) {
      if (!fileMaskCheckBox.isSelected()) {
        clickOnFileMaskCheckbox();
        waitFileMaskIsSelected();
      }
    } else {
      if (fileMaskCheckBox.isSelected()) {
        clickOnWholeWordCheckbox();
        waitFileMaskIsNotSelected();
      }
    }
  }

  /** click on the 'File mask' checkbox */
  public void clickOnFileMaskCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(Locators.FILE_MASK_CHECKBOX_SPAN)))
        .click();
  }

  /** wait the 'File mask' checkbox is selected */
  public void waitFileMaskIsSelected() {
    String locator = Locators.FILE_MASK_CHECKBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), true));
  }

  /** wait the 'File mask' checkbox is not selected */
  public void waitFileMaskIsNotSelected() {
    String locator = Locators.FILE_MASK_CHECKBOX_INP;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), false));
  }

  /**
   * type text into 'FileNameFilter' field
   *
   * @param text is symbol or string
   */
  public void typeTextIntoFileNameFilter(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.FILE_MASK_FIELD)))
        .clear();
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.FILE_MASK_FIELD)))
        .sendKeys(text);
  }

  /**
   * wait text into 'FileNameFilter' field
   *
   * @param expText is expected text
   */
  public void waitTextIntoFileNameFilter(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.id(Locators.FILE_MASK_FIELD))
                        .getAttribute("value")
                        .equals(expText));
  }

  /** wait the 'Find' info panel is open */
  public void waitFindInfoPanelIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(findInfoPanel));
  }

  /** wait the 'Find' info panel is closed */
  public void waitFindInfoPanelIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.FIND_INFO_PANEL)));
  }

  /** click on the find text button on the find info panel */
  public void clickFindTextButton() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(findTextBtn))
        .click();
    loader.waitOnClosed();
  }

  /**
   * wait expected text in the 'Find' info panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextInFindInfoPanel(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromFindInfoPanel().contains(expText));
  }

  /**
   * wait expected text in the 'Find' info panel
   *
   * @param expText list of expected values
   */
  public void waitExpectedTextInFindInfoPanel(List<String> expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (Predicate<WebDriver>)
                input ->
                    expText
                        .stream()
                        .allMatch(
                            t -> {
                              String textFromFindInfoPanel = getTextFromFindInfoPanel();
                              return textFromFindInfoPanel.contains(t);
                            }));
  }

  /**
   * wait the text is not present in the 'Find' info panel
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInFindInfoPanel(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> !(getTextFromFindInfoPanel().contains(expText)));
  }

  /**
   * get text - representation content from 'Find' info panel
   *
   * @return text from 'find usages' panel
   */
  public String getTextFromFindInfoPanel() {
    return findInfoPanel.getText();
  }

  public void selectItemInFindInfoPanel(String fileName, String textToFind) {
    List<WebElement> webElementList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.OCCURRENCE, fileName))));
    for (WebElement webElement : webElementList) {
      if (webElement.getText().equals(textToFind)) {
        webElement.click();
        break;
      }
    }
  }

  public void selectItemInFindInfoPanelByDoubleClick(String fileName, String textToFind) {
    List<WebElement> webElementList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.OCCURRENCE, fileName))));
    for (WebElement webElement : webElementList) {
      if (webElement.getText().equals(textToFind)) {
        actionsFactory.createAction(seleniumWebDriver).doubleClick(webElement).perform();
        break;
      }
    }
  }

  /**
   * send a keys by keyboard in the 'Find' info panel
   *
   * @param command is the command by keyboard
   */
  public void sendCommandByKeyboardInFindInfoPanel(String command) {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(command).perform();
    loader.waitOnClosed();
  }

  public String getFindInfoResults() {
    loader.waitOnClosed();
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.SEARCH_RESULTS)))
        .getText();
  }

  public void clickOnPreviousPageButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.PREVIOUS_BUTTON)))
        .click();
  }

  public void clickOnNextPageButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.NEXT_BUTTON)))
        .click();
  }

  public Boolean checkNextPageButtonIsEnabled() {
    return seleniumWebDriver.findElement(By.id(Locators.NEXT_BUTTON)).isEnabled();
  }

  public Boolean checkPreviousPageButtonIsEnabled() {
    return seleniumWebDriver.findElement(By.id(Locators.PREVIOUS_BUTTON)).isEnabled();
  }

  public void openFileNodeByDoubleClick(String pathToFile) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.FILE_NODE, pathToFile))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }

  public static class SearchFileResult {
    private final int occurrencesFoundOnPage;
    private final int filesFoundOnPage;
    private final int totalFilesFound;

    private SearchFileResult(String results) {
      occurrencesFoundOnPage = Integer.parseInt(results.split(" ")[0]);
      filesFoundOnPage = Integer.parseInt(results.split(" ")[4]);
      totalFilesFound = Integer.parseInt(results.substring(results.lastIndexOf(" ") + 1));
    }

    public int getFoundOccurrencesOnPage() {
      return occurrencesFoundOnPage;
    }

    public int getFoundFilesOnPage() {
      return filesFoundOnPage;
    }

    public int getTotalNumberFoundFiles() {
      return totalFilesFound;
    }
  }

  public SearchFileResult getResults() {
    String text =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.SEARCH_RESULTS)))
            .getText();

    return new SearchFileResult(text);
  }
}
