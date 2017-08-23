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
public class SvnMove {

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private final Loader loader;

  @Inject
  public SvnMove(
      SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MOVE_FORM = "gwt-debug-svn-move-window";
    String CANCEL_BUTTON = "svn-move-cancel";
    String MOVE_BUTTON = "svn-move-move";
  }

  @FindBy(id = Locators.MOVE_FORM)
  WebElement moveForm;

  @FindBy(id = Locators.MOVE_BUTTON)
  WebElement moveButton;

  public void waitMoveFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(moveForm));
  }

  public void waitMoveFormClose() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MOVE_FORM)));
  }

  public void waitItem(String path) throws Exception {
    waitItem(path, ELEMENT_TIMEOUT_SEC);
  }

  public void waitItem(String path, int timeout) throws Exception {
    String locator = "//div[@path='/" + path + "']/div";
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
  }

  /** TODO fix incorrect locator */
  public void openItemByPath(String path) throws Exception {
    String locator = "//div[@path='/" + path + "']/div";
    waitItem(path);
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(item).perform();
    loader.waitOnClosed();
  }

  public void selectItem(String path) throws Exception {
    String locator = "//div[@path='/" + path + "']/div";
    waitItem(path);
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    item.click();
  }

  public void clickMoveButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(moveButton));
    moveButton.click();
  }
}
