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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev */
@Singleton
public class ConfigureClasspath {

  public static final String CONFIGURE_CLASSPATH_FORM =
      "//div[text()='Configure Classpath']/ancestor::div[3]";
  public static final String CONFIGURE_CLASSPATH_CLOSE_ICON =
      "//div[text()='Configure Classpath']/following::div[1]";
  public static final String JAVA_BUILD_PATH = "gwt-debug-categoryHeader-Java Build Path";
  public static final String LIBRARIES_CATEGORY = "gwt-debug-projectWizard-Libraries";
  public static final String SOURCE_CATEGORY = "gwt-debug-projectWizard-Source";
  public static final String DELETE_JAR_OR_FOLDER = "//div[text()='%s']/following::span[1]";
  public static final String DONE_BUTTON = "window-edit-configurations-close";
  public static final String SELECT_PATH_FORM = "//div[text()='Select Path']/ancestor::div[3]";
  public static final String ITEM_SELECT_PATH_FORM =
      "//div[text()='Select Path']/following::div[text()='%s']";
  public static final String SELECT_PATH_CANCEL_BTN = "select-path-cancel-button";
  public static final String SELECT_PATH_OK_BTN = "select-path-ok-button";
  public static final String ADD_JAR = "//button[text()='Add JAR']";
  public static final String ADD_FOLDER = "//button[text()='Add Folder']";
  public static final String SELECT_PATH_SELECT_BTN =
      "//div[text()='Select Path']/ancestor::div[3]//button[text()='Select']";
  public static final String JARS_AND_FOLDERS_AREA = "gwt-debug-pageViewContainer";
  public static final String JAVA_BUILD_PATH_AREA = "gwt-debug-propertiesWizard";

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public ConfigureClasspath(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @FindBy(id = JAVA_BUILD_PATH_AREA)
  WebElement contentFromJavaBuildPathArea;

  @FindBy(id = JARS_AND_FOLDERS_AREA)
  WebElement contentFromJarsFolders;

  /** wait the 'Configure Classpath' form is open */
  public void waitConfigureClasspathFormIsOpen() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(CONFIGURE_CLASSPATH_FORM)));
  }

  /** wait the 'Configure Classpath' form is closed */
  public void waitConfigureClasspathFormIsClosed() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(CONFIGURE_CLASSPATH_FORM)));
  }

  /** close the 'Configure Classpath' form by click on the close icon */
  public void closeConfigureClasspathFormByIcon() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(CONFIGURE_CLASSPATH_CLOSE_ICON)))
        .click();
    waitConfigureClasspathFormIsClosed();
  }

  /** click on the 'Java Build Path' */
  public void clickOnJavaBuildPathHeader() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(JAVA_BUILD_PATH)))
        .click();
  }

  /**
   * wait expected text in the 'Java Build Path' area
   *
   * @param expText expected value
   */
  public void waitExpectedTextJavaBuildPathArea(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromJavaBuildPathArea().contains(expText));
  }

  /**
   * wait the text is not present in the 'Java Build Path' area
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInJavaBuildPathArea(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> !(getTextFromJavaBuildPathArea().contains(expText)));
  }

  /**
   * get text - representation content from 'Java Build Path' area
   *
   * @return text from 'Java Build Path' area
   */
  public String getTextFromJavaBuildPathArea() {
    return contentFromJavaBuildPathArea.getText();
  }

  /** select the 'Libraries' category */
  public void selectLibrariesCategory() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(LIBRARIES_CATEGORY)))
        .click();
    loader.waitOnClosed();
  }

  /** click on the library container */
  public void clickLibraryContainer(String container) {
    WebElement element =
        seleniumWebDriver.findElement(By.xpath(String.format("//div[text()='%s']", container)));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(element))
        .click();
  }

  /** select the 'Source' category */
  public void selectSourceCategory() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(SOURCE_CATEGORY)))
        .click();
    loader.waitOnClosed();
  }

  /**
   * wait expected text in the 'JARs and folders' area
   *
   * @param expText expected value
   */
  public void waitExpectedTextJarsAndFolderArea(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> getTextFromJarsAndFolderArea().contains(expText));
  }

  /**
   * wait the text is not present in the 'JARs and folders' area
   *
   * @param expText expected value
   */
  public void waitExpectedTextIsNotPresentInJarsAndFolderArea(String expText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until((WebDriver driver) -> !(getTextFromJarsAndFolderArea().contains(expText)));
  }

  /**
   * get text - representation content from 'JARs and folders' area
   *
   * @return text from 'JARs and folders' area
   */
  public String getTextFromJarsAndFolderArea() {
    return contentFromJarsFolders.getText();
  }

  /**
   * delete jar or folder from build path
   *
   * @param nameJarFolder is name of the jar or folder
   */
  public void deleteJarOrFolderFromBuildPath(String nameJarFolder) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[text()='%s']", nameJarFolder))));
    WebElement element =
        seleniumWebDriver.findElement(By.xpath(String.format("//div[text()='%s']", nameJarFolder)));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(element).perform();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(DELETE_JAR_OR_FOLDER, nameJarFolder))))
        .click();
  }

  /**
   * add jar or folder to build path
   *
   * @param folderType is the type of the folder
   */
  public void addJarOrFolderToBuildPath(String folderType) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(folderType)))
        .click();
  }

  /** click on the 'Done' button in the 'Configure Classpath' */
  public void clickOnDoneBtnConfigureClasspath() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(DONE_BUTTON)))
        .click();
    loader.waitOnClosed();
    waitConfigureClasspathFormIsClosed();
  }

  /** wait the 'Select Path' form is open */
  public void waitSelectPathFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SELECT_PATH_FORM)));
  }

  /** wait the 'Select Path' form is closed */
  public void waitSelectPathFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(SELECT_PATH_FORM)));
  }

  /** click on the 'OK' button in the 'Select Path' form */
  public void clickOkBtnSelectPathForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(SELECT_PATH_OK_BTN)))
        .click();
    loader.waitOnClosed();
    waitSelectPathFormIsClosed();
  }

  /** click on the 'Cancel' button in the 'Select Path' form */
  public void clickCancelBtnSelectPathForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(SELECT_PATH_CANCEL_BTN)))
        .click();
    loader.waitOnClosed();
    waitSelectPathFormIsClosed();
  }

  /** click on the 'Select' button in the 'Select Path' form */
  public void clickSelectBtnSelectPathForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SELECT_PATH_SELECT_BTN)))
        .click();
    loader.waitOnClosed();
    waitSelectPathFormIsClosed();
  }

  /**
   * wait item in 'Select Path' form
   *
   * @param item is the name of the item
   */
  public void waitItemInSelectPathForm(String item) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(String.format(ITEM_SELECT_PATH_FORM, item))));
    loader.waitOnClosed();
  }

  /** select item in the 'Select Path' form */
  public void selectItemInSelectPathForm(String itemName) {
    waitItemInSelectPathForm(itemName);
    WebElement item =
        seleniumWebDriver.findElement(By.xpath(String.format(ITEM_SELECT_PATH_FORM, itemName)));
    item.click();
  }

  /** open item in the 'Select Path' form */
  public void openItemInSelectPathForm(String itemName) {
    waitItemInSelectPathForm(itemName);
    WebElement item =
        seleniumWebDriver.findElement(By.xpath(String.format(ITEM_SELECT_PATH_FORM, itemName)));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(item).perform();
  }
}
