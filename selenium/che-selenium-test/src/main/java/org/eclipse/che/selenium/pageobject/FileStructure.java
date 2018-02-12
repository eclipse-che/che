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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.Keys.F12;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 11.12.15 */
@Singleton
public class FileStructure {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public FileStructure(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String FILE_STRUCTURE_FORM = "//table[@title='%s.java']";
    String FILE_STRUCTURE_CLOSE_ICON = "gwt-debug-file-structure-windowFrameCloseButton";
    String FILE_STRUCTURE_CONTENT = "gwt-debug-file-structure-mainPanel";
    String FILE_STRUCTURE_ICON_NODE =
        "(//div[text()='%s']/preceding::*[local-name()='svg'][2])[last()]";
    String FILE_STRUCTURE_ITEM =
        "//div[@id='gwt-debug-file-structure-mainPanel']//div[text()='%s']";
  }

  @FindBy(id = Locators.FILE_STRUCTURE_CONTENT)
  WebElement fileStructureContent;

  /** wait the 'file structure' form is open */
  public void waitFileStructureFormIsOpen(String fileName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.FILE_STRUCTURE_FORM, fileName))));
  }

  /** wait the 'file structure' form is closed */
  public void waitFileStructureFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                (By.id(Locators.FILE_STRUCTURE_CONTENT))));
  }

  /** launch the 'File Structure' form by keyboard */
  public void launchFileStructureFormByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(CONTROL).sendKeys(F12).keyUp(CONTROL).perform();
    loader.waitOnClosed();
  }

  /** close the 'File Structure' form by the key 'Esc' */
  public void closeFileStructureFormByEscape() {
    Actions closeWindow = actionsFactory.createAction(seleniumWebDriver);
    closeWindow.sendKeys(ESCAPE).perform();
  }

  /** close the 'File Structure' form by click on the close icon */
  public void clickFileStructureCloseIcon() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.FILE_STRUCTURE_CLOSE_ICON)))
        .click();
  }

  /**
   * wait expected text in the 'file structure' form
   *
   * @param expText expected value
   */
  public void waitExpectedTextInFileStructure(String expText) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> getTextFromFileStructurePanel().contains(expText));
  }

  /**
   * wait the text is not present in the 'file structure' form
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInFileStructure(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                return !(getTextFromFileStructurePanel().contains(expText));
              }
            });
  }

  /**
   * get text - representation content from 'file structure' panel
   *
   * @return text from 'file structure' container
   */
  public String getTextFromFileStructurePanel() {
    return fileStructureContent.getText();
  }

  /**
   * perform click on the icon node in the 'file structure' form
   *
   * @param nameNode is name
   */
  public void clickOnIconNodeInFileStructure(String nameNode) {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.FILE_STRUCTURE_ICON_NODE, nameNode))))
        .click();
  }

  /**
   * perform 'double click' on the certain item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectItemInFileStructureByDoubleClick(String item) {
    selectItemInFileStructure(item);
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(getFileStructureItem(item))
        .doubleClick()
        .perform();
  }

  /**
   * perform 'Enter' on the selected item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectItemInFileStructureByEnter(String item) {
    selectItemInFileStructure(item);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(ENTER).perform();
  }

  /**
   * select a certain item in the 'file structure' form
   *
   * @param item is the name of the item
   */
  public void selectItemInFileStructure(String item) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(getFileStructureItem(item))
        .click()
        .perform();
  }

  private WebElement getFileStructureItem(String item) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.FILE_STRUCTURE_ITEM, item))));
  }

  /**
   * send a keys by keyboard in the 'file structure' form
   *
   * @param command is the command by keyboard
   */
  public void sendCommandByKeyboardInFileStructure(String command) {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(command).perform();
  }

  public void moveDownToItemInFileStructure(String itemName) {
    List<WebElement> items =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                presenceOfAllElementsLocatedBy(
                    By.xpath(
                        "//div[@id='gwt-debug-file-structure-mainPanel']//div[@id[contains(.,'gwt-uid-')]]//div[text()]")));
    for (int i = 0; i < items.size(); i++) {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(ARROW_DOWN).perform();
      WaitUtils.sleepQuietly(1);
      items =
          new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
              .until(
                  presenceOfAllElementsLocatedBy(
                      By.xpath(
                          "//div[@id='gwt-debug-file-structure-mainPanel']/div[3]/div/div[1]//div[text()]")));
      if (items.get(i).getText().contains(itemName)) {
        break;
      }
    }
  }

  public void type(String text) {
    WaitUtils.sleepQuietly(1);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(text).perform();
  }
}
