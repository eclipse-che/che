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
package org.eclipse.che.selenium.pageobject.subversion;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnMerge {

  interface Locators {
    String MERGE_MAIN_FORM = "gwt-debug-plugin-svn merge-dialog";
    String PATH_TEXT_BOX = "//input[@class='gwt-TextBox']";
    String URL_CHECKBOX = "//label[text()='URL']";
    String URL_TEXT_BOX = "//div[text()='URL:']/following::input";
    String MERGE_BUTTON = "plugin-svn-merge-dialog-merge-button";
    String CANCEL_BUTTON = "plugin-svn-merge-dialog-cancel-button";
  }

  @FindBy(id = Locators.MERGE_MAIN_FORM)
  WebElement mainFormMerge;

  @FindBy(xpath = Locators.PATH_TEXT_BOX)
  WebElement textBoxPath;

  @FindBy(xpath = Locators.URL_CHECKBOX)
  WebElement urlCheckbox;

  @FindBy(xpath = Locators.URL_TEXT_BOX)
  WebElement urlTextBox;

  @FindBy(id = Locators.MERGE_BUTTON)
  WebElement buttonMerge;

  @FindBy(id = Locators.CANCEL_BUTTON)
  WebElement cancelButton;

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;

  @Inject
  public SvnMerge(
      SeleniumWebDriver seleniumWebDriver, Loader loader, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitMainFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainFormMerge));
  }

  public void waitMainFormClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MERGE_MAIN_FORM)));
  }

  public void clickUrlCheckbox() {
    urlCheckbox.click();
  }

  public void typeTextUrlBox(String text) {
    urlTextBox.clear();
    urlTextBox.sendKeys(text);
  }

  public void clickMergeButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonMerge));
    buttonMerge.click();
  }

  public void waitItem(String path) throws Exception {
    waitItem(path, ELEMENT_TIMEOUT_SEC);
  }

  public void waitItem(String path, int timeout) throws Exception {
    String locator = "//div[@id='gwt-debug-plugin-svn merge-dialog']//span[text()='" + path + "']";
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
  }

  public void openItemByPath(String path) throws Exception {
    String locator =
        "//span[@id[contains(.,'gwt-debug-projectTree-')] and contains(., '" + path + "')]";
    waitItem(path);
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(item).perform();
    loader.waitOnClosed();
  }

  public void selectItem(String path) throws Exception {
    String locator = "//div[@id='gwt-debug-plugin-svn merge-dialog']//span[text()='" + path + "']";
    waitItem(path);
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    item.click();
  }
}
