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
import static org.eclipse.che.selenium.pageobject.FileStructure.Locators.FILE_STRUCTURE_ITEM;
import static org.eclipse.che.selenium.pageobject.FileStructure.Locators.SELECTED_FILE_STRUCTURE_ITEM_XPATH_TEMPLATE;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.Keys.F12;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Aleksandr Shmaraev on 11.12.15 */
@Singleton
public class FileStructure {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final Loader loader;

  @Inject
  public FileStructure(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String FILE_STRUCTURE_FORM = "//table[@title='%s.java']";
    String FILE_STRUCTURE_CLOSE_ICON = "gwt-debug-file-structure-windowFrameCloseButton";
    String FILE_STRUCTURE_CONTENT = "gwt-debug-file-structure-mainPanel";
    String FILE_STRUCTURE_ICON_NODE =
        "(//div[text()='%s']/preceding::*[local-name()='svg'][2])[last()]";
    String FILE_STRUCTURE_ITEM =
        "//div[@id='gwt-debug-file-structure-mainPanel']//div[text()='%s']";
    String SELECTED_FILE_STRUCTURE_ITEM_XPATH_TEMPLATE =
        "//div[@id='gwt-debug-file-structure-mainPanel']//div[contains(@class, 'selected')]//div[text()='%s']";
  }

  @FindBy(id = Locators.FILE_STRUCTURE_CONTENT)
  private WebElement fileStructureContent;

  @FindBy(id = Locators.FILE_STRUCTURE_CLOSE_ICON)
  private WebElement fileStructureCloseIcon;

  /** wait the 'file structure' form is open */
  public void waitFileStructureFormIsOpen(String fileName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.FILE_STRUCTURE_FORM, fileName)));
  }

  /** wait the 'file structure' form is closed */
  public void waitFileStructureFormIsClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(Locators.FILE_STRUCTURE_CONTENT));
  }

  /** launch the 'File Structure' form by keyboard */
  public void launchFileStructureFormByKeyboard() {
    loader.waitOnClosed();
    seleniumWebDriverHelper.getAction().keyDown(CONTROL).sendKeys(F12).keyUp(CONTROL).perform();
    loader.waitOnClosed();
  }

  /** close the 'File Structure' form by the key 'Esc' */
  public void closeFileStructureFormByEscape() {
    seleniumWebDriverHelper.getAction().sendKeys(ESCAPE).perform();
  }

  /** close the 'File Structure' form by click on the close icon */
  public void clickFileStructureCloseIcon() {
    seleniumWebDriverHelper.waitAndClick(fileStructureCloseIcon);
  }

  /**
   * wait expected text in the 'file structure' form
   *
   * @param expText expected value
   */
  public void waitExpectedTextInFileStructure(String expText) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.waitTextContains(fileStructureContent, expText);
  }

  /**
   * wait the text is not present in the 'file structure' form
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInFileStructure(String expText) {
    seleniumWebDriverHelper.waitTextIsNotPresented(fileStructureContent, expText);
  }

  /**
   * get text - representation content from 'file structure' panel
   *
   * @return text from 'file structure' container
   */
  public String getTextFromFileStructurePanel() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(fileStructureContent);
  }

  /**
   * perform click on the icon node in the 'file structure' form
   *
   * @param nameNode is name
   */
  public void clickOnIconNodeInFileStructure(String nameNode) {
    // we need to wait a little to avoid node closing after quick clicking
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            seleniumWebDriverHelper.waitAndClick(
                By.xpath(format(Locators.FILE_STRUCTURE_ICON_NODE, nameNode))),
        StaleElementReferenceException.class);
  }

  /**
   * perform 'double click' on the certain item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectItemInFileStructureByDoubleClick(String item) {
    final String itemXpath = getItemXpath(item);

    // we need to wait a little to avoid node closing after quick clicking
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(itemXpath)),
        StaleElementReferenceException.class);
  }

  /**
   * perform 'Enter' on the selected item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectAndOpenItemInFileStructureByEnter(String item) {
    selectItemInFileStructure(item);

    // we need to wait a little to avoid quick nodes opening
    WaitUtils.sleepQuietly(1);
    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * select a certain item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectItemInFileStructure(String item) {
    // we need to wait a little to avoid quick clicking on nodes
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> selectItem(item), StaleElementReferenceException.class);
  }

  private void selectItem(String visibleItemText) {
    final String itemXpath = getItemXpath(visibleItemText);

    seleniumWebDriverHelper.waitAndClick(By.xpath(itemXpath));
    waitItemSelected(visibleItemText);
  }

  private String getItemXpath(String item) {
    return format(FILE_STRUCTURE_ITEM, item);
  }

  private String getSelectedItemXpath(String itemText) {
    return format(SELECTED_FILE_STRUCTURE_ITEM_XPATH_TEMPLATE, itemText);
  }

  public void waitItemSelected(String itemText) {
    final String selectedItemXpath = getSelectedItemXpath(itemText);

    seleniumWebDriverHelper.waitVisibility(By.xpath(selectedItemXpath));
  }

  /**
   * send a keys by keyboard in the 'file structure' form
   *
   * @param command is the command by keyboard
   */
  public void sendCommandByKeyboardInFileStructure(String command) {
    seleniumWebDriverHelper.sendKeys(command);
  }

  public void moveDownToItemInFileStructure(String itemName) {
    List<WebElement> items =
        seleniumWebDriverHelper.waitPresenceOfAllElements(
            By.xpath(
                "//div[@id='gwt-debug-file-structure-mainPanel']//div[@id[contains(.,'gwt-uid-')]]//div[text()]"));

    for (int i = 0; i < items.size(); i++) {
      seleniumWebDriverHelper.pressArrowDown();
      WaitUtils.sleepQuietly(1);
      items =
          seleniumWebDriverHelper.waitPresenceOfAllElements(
              By.xpath(
                  "//div[@id='gwt-debug-file-structure-mainPanel']/div[3]/div/div[1]//div[text()]"));

      if (items.get(i).getText().contains(itemName)) {
        break;
      }
    }
  }

  public void type(String text) {
    WaitUtils.sleepQuietly(1);
    seleniumWebDriverHelper.sendKeys(text);
  }
}
