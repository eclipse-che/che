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
package org.eclipse.che.selenium.pageobject.site;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.LoginPageLocators.LOGIN_BUTTON;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.LoginPageLocators.PASSWORD_FIELD;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.LoginPageLocators.USER_NAME_FIELD;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class CheLoginPage implements LoginPage {
  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait webDriverWait;

  protected interface LoginPageLocators {
    String USER_NAME_FIELD = "username";
    String PASSWORD_FIELD = "password";
    String LOGIN_BUTTON = "login";
  }

  @FindBy(name = USER_NAME_FIELD)
  private WebElement usernameInput;

  @FindBy(name = PASSWORD_FIELD)
  private WebElement passwordInput;

  @FindBy(name = LOGIN_BUTTON)
  private WebElement loginButton;

  @Inject
  public CheLoginPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);

    webDriverWait = new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOADER_TIMEOUT_SEC);
  }

  @Override
  public void login(String username, String password) {
    waitOnOpen();

    rewrite(usernameInput, username);

    rewrite(passwordInput, password);

    clickLoginButton();
    waitOnClose();
  }

  @Override
  public boolean isOpened() {
    try {
      waitOnOpen();
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }

  private void rewrite(WebElement field, String value) {
    webDriverWait.until(visibilityOf(field)).clear();
    waitTextIsPresent(field, "");
    webDriverWait.until(visibilityOf(field)).sendKeys(value);
    waitTextIsPresent(field, value);
  }

  private void clickLoginButton() {
    webDriverWait.until(visibilityOf(loginButton)).click();
  }

  private void waitOnOpen() {
    asList(By.name(USER_NAME_FIELD), By.name(PASSWORD_FIELD), By.name(LOGIN_BUTTON))
        .forEach(locator -> webDriverWait.until(visibilityOfElementLocated(locator)));
  }

  private void waitOnClose() {
    asList(By.name(USER_NAME_FIELD), By.name(PASSWORD_FIELD), By.name(LOGIN_BUTTON))
        .forEach(locator -> webDriverWait.until(invisibilityOfElementLocated(locator)));
  }

  private void waitTextIsPresent(WebElement webElement, String expectedText) {
    webDriverWait.until(
        (ExpectedCondition<Boolean>)
            driver -> webElement.getAttribute("value").equals(expectedText));
  }
}
