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

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.ERROR_ALERT;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.NEW_PASSWORD_CONFIRMATION_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.NEW_PASSWORD_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.PASSWORD_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage.PasswordLocators.SUCCESS_ALERT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class KeycloakPasswordPage {
  private SeleniumWebDriver seleniumWebDriver;
  private WebDriverWait loadPageWait;
  private SeleniumWebDriverUtils seleniumWebDriverUtils;
  private KeycloakHeaderButtons keycloakHeaderButtons;

  @Inject
  public KeycloakPasswordPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverUtils seleniumWebDriverUtils,
      KeycloakHeaderButtons keycloakHeaderButtons) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverUtils = seleniumWebDriverUtils;
    this.keycloakHeaderButtons = keycloakHeaderButtons;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  protected interface PasswordLocators {
    String PASSWORD_FIELD_ID = "password";
    String NEW_PASSWORD_FIELD_ID = "password-new";
    String NEW_PASSWORD_CONFIRMATION_ID = "password-confirm";

    String SAVE_BUTTON = "//button[text()='Save']";

    String ERROR_ALERT = "//div[@class='alert alert-error']";
    String SUCCESS_ALERT = "//div[@class='alert alert-success']";
  }

  public void waitPasswordPageIsLoaded() {
    keycloakHeaderButtons.waitAllHeaderButtonsIsVisible();
    waitAllBodyFieldsAndButtonsIsVisible();
  }

  public void setPasswordFieldValue(String value) {
    seleniumWebDriverUtils.setFieldValue(By.id(PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordFieldValue(String value) {
    seleniumWebDriverUtils.setFieldValue(By.id(NEW_PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordConfirmationFieldValue(String value) {
    seleniumWebDriverUtils.setFieldValue(By.id(NEW_PASSWORD_CONFIRMATION_ID), value);
  }

  public void clickOnSavePasswordButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(SAVE_BUTTON));
  }

  public void waitTextInErrorAlert(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                seleniumWebDriverUtils
                    .waitAndGetElement(By.xpath(ERROR_ALERT))
                    .getText()
                    .equals(expectedText));
  }

  public void waitTextInSuccessAlert(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                seleniumWebDriverUtils
                    .waitAndGetElement(By.xpath(SUCCESS_ALERT))
                    .getText()
                    .equals(expectedText));
  }

  private void waitAllBodyFieldsAndButtonsIsVisible() {
    asList(
            By.id(PASSWORD_FIELD_ID),
            By.id(NEW_PASSWORD_FIELD_ID),
            By.id(NEW_PASSWORD_CONFIRMATION_ID),
            By.xpath(SAVE_BUTTON))
        .forEach(locator -> seleniumWebDriverUtils.waitElementIsVisible(locator));
  }
}
