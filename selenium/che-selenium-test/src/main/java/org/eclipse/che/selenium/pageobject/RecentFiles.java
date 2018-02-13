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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class RecentFiles {
  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;

  @Inject
  public RecentFiles(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
  }

  private interface Locators {
    String RECENT_FILES = "//table[@title='Recent Files']";
    String RECENT_FILES_ITEM = "//table[@title='Recent Files']//div[text()='%s']";
  }

  /** Wait the 'Recent Files' window is opened */
  public void waitRecentFiles() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.RECENT_FILES)));
  }

  /** Close the 'Recent Files' window */
  public void closeRecentFiles() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ESCAPE).perform();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.RECENT_FILES)));
  }

  /**
   * Wait item by name is present in the 'Recent Files'
   *
   * @param nameItem name of item
   */
  public void waitItemIsPresent(String nameItem) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.RECENT_FILES_ITEM, nameItem))));
  }

  /**
   * Wait item by name is not present in the 'Recent Files'
   *
   * @param nameItem name of item
   */
  public void waitItemIsNotPresent(String nameItem) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(format(Locators.RECENT_FILES_ITEM, nameItem))));
  }

  /**
   * Open item by name from the 'Recent Files' window in editor
   *
   * @param nameItem name of item
   */
  public void openItemByName(String nameItem) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.RECENT_FILES_ITEM, nameItem))))
        .click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick().perform();
  }
}
