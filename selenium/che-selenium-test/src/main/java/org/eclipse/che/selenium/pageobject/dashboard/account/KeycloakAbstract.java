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
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAbstract.ButtonsLocators.ACCOUNT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAbstract.ButtonsLocators.APPLICATIONS_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAbstract.ButtonsLocators.AUTHENTICATOR_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAbstract.ButtonsLocators.PASSWORD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAbstract.ButtonsLocators.SESSIONS_BUTTON;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Singleton;
import java.util.Arrays;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public abstract class KeycloakAbstract {
  protected final SeleniumWebDriver seleniumWebDriver;
  protected final WebDriverWait loadPageWait;

  protected KeycloakAbstract(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  protected interface ButtonsLocators {
    String ACCOUNT_BUTTON = "//div[@id='tabs-menu']//a[text()='Account']";
    String PASSWORD_BUTTON = "//div[@id='tabs-menu']//a[text()='Password']";
    String AUTHENTICATOR_BUTTON = "//div[@id='tabs-menu']//a[text()='Authenticator']";
    String SESSIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Sessions']";
    String APPLICATIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Applications']";
  }

  /** click on the "Account" button in the header oh the page */
  public void clickOnAccountButton() {
    waitAndClickOnElement(By.xpath(ACCOUNT_BUTTON));
  }

  /** click on the "Password" button in the header oh the page */
  public void clickOnPasswordButton() {
    waitAndClickOnElement(By.xpath(PASSWORD_BUTTON));
  }

  /** click on the "Authenticator" button in the header oh the page */
  public void clickOnAuthenticatorButton() {
    waitAndClickOnElement(By.xpath(AUTHENTICATOR_BUTTON));
  }

  /** click on the "Session" button in the header oh the page */
  public void clickOnSessionsButton() {
    waitAndClickOnElement(By.xpath(SESSIONS_BUTTON));
  }

  /** click on the "Application" button in the header oh the page */
  public void clickOnApplicationsButton() {
    waitAndClickOnElement(By.xpath(APPLICATIONS_BUTTON));
  }

  /** wait until all buttons which placed in the header of the page will be visible */
  public void waitAllHeaderButtonsIsVisible() {
    Arrays.asList(
            By.xpath(ACCOUNT_BUTTON),
            By.xpath(PASSWORD_BUTTON),
            By.xpath(AUTHENTICATOR_BUTTON),
            By.xpath(SESSIONS_BUTTON),
            By.xpath(APPLICATIONS_BUTTON))
        .forEach(locator -> waitElementIsVisible(locator));
  }

  public void switchToNoneParentWindow(String parentWindow) {
    seleniumWebDriver.switchToNoneCurrentWindow(parentWindow);
  }

  public void closeWindowAndSwitchToParent(String parentWindow) {
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(parentWindow);
  }

  protected void setFieldValue(By fieldLocator, String value) {
    waitAndGetElement(fieldLocator).clear();
    waitFieldValue(fieldLocator, "");
    waitAndGetElement(fieldLocator).sendKeys(value);
    waitFieldValue(fieldLocator, value);
  }

  protected void waitElementIsVisible(By elementLocator) {
    loadPageWait.until(visibilityOfElementLocated(elementLocator));
  }

  protected String getFieldValue(By fieldLocator) {
    return waitAndGetElement(fieldLocator).getAttribute("value");
  }

  public WebElement waitAndGetElement(By locator) {
    return loadPageWait.until(visibilityOfElementLocated(locator));
  }

  private void waitFieldValue(By fieldLocator, String expectedValue) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>) driver -> getFieldValue(fieldLocator).equals(expectedValue));
  }

  private void waitAndClickOnElement(By elementLocator) {
    loadPageWait.until(visibilityOfElementLocated(elementLocator)).click();
  }
}
