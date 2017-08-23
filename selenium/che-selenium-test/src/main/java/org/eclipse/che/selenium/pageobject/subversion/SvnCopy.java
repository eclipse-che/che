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
public class SvnCopy {

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;
  private final Loader loader;

  @Inject
  public SvnCopy(
      SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String COPY_FORM = "gwt-debug-svn-copy-window";
    String COPY_BUTTON = "svn-copy-copy";
    String CANCEL_BUTTON = "svn-copy-cancel";
  }

  @FindBy(id = Locators.COPY_FORM)
  WebElement svnCopyForm;

  @FindBy(id = Locators.COPY_BUTTON)
  WebElement copyButton;

  public void waitCopyFormOpened() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(svnCopyForm));
  }

  public void waitCopyFormClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.COPY_FORM)));
  }

  public void waitItem(String path) throws Exception {
    waitItem(path, ELEMENT_TIMEOUT_SEC);
  }

  public void waitItem(String path, int timeout) throws Exception {
    String locator = "//div[@path='/" + path + "']/div";
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
  }

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

  public void clickCopyButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(copyButton));
    copyButton.click();
  }
}
