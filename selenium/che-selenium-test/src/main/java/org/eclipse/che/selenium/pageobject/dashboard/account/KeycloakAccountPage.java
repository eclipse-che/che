/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.EMAIL_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.ERROR_ALERT;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.FIRST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.LAST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.SUCCESS_ALERT;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage.AccountPageLocators.USERNAME_FIELD_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Igor Ohrimenko */
@Singleton
public class KeycloakAccountPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final KeycloakHeaderButtons keycloakHeaderButtons;
  private final WebDriverWait loadPageWait;

  @Inject
  protected KeycloakAccountPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      KeycloakHeaderButtons keycloakHeaderButtons) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.keycloakHeaderButtons = keycloakHeaderButtons;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  interface AccountPageLocators {
    String USERNAME_FIELD_ID = "username";
    String EMAIL_FIELD_ID = "email";
    String FIRST_NAME_FIELD_ID = "firstName";
    String LAST_NAME_FIELD_ID = "lastName";

    String SAVE_BUTTON = "//button[text()='Save']";
    String CANCEL_BUTTON = "//button[text()='Cancel']";

    String SUCCESS_ALERT = "//div[@class='alert alert-success']";
    String ERROR_ALERT = "//div[@class='alert alert-error']";
  }

  public void waitAccountPageIsLoaded() {
    keycloakHeaderButtons.waitAllHeaderButtonsAreVisible();
    waitAllBodyFieldsAndButtonsIsVisible();
  }

  public Account getAllFields() {
    return new Account()
        .withLogin(getUserNameValue())
        .withEmail(getEmailValue())
        .withFirstName(getFirstNameValue())
        .withLastName(getLastNameValue());
  }

  public String getUserNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(USERNAME_FIELD_ID));
  }

  public String getEmailValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(EMAIL_FIELD_ID));
  }

  public String getFirstNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(FIRST_NAME_FIELD_ID));
  }

  public String getLastNameValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.id(LAST_NAME_FIELD_ID));
  }

  public void setUserNameValue(String value) {
    seleniumWebDriverHelper.setValue(By.id(USERNAME_FIELD_ID), value);
  }

  public void setEmailValue(String value) {
    seleniumWebDriverHelper.setValue(By.id(EMAIL_FIELD_ID), value);
  }

  public void setFirstNameValue(String value) {
    seleniumWebDriverHelper.setValue(By.id(FIRST_NAME_FIELD_ID), value);
  }

  public void setLastNameValue(String value) {
    seleniumWebDriverHelper.setValue(By.id(LAST_NAME_FIELD_ID), value);
  }

  public boolean usernameFieldIsDisabled() {
    return seleniumWebDriverHelper
        .waitVisibility(By.id(USERNAME_FIELD_ID))
        .getAttribute("disabled")
        .equals("true");
  }

  public void clickOnSaveButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(SAVE_BUTTON));
  }

  public void clickOnCancelButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CANCEL_BUTTON));
  }

  public void waitTextInErrorAlert(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                seleniumWebDriverHelper
                    .waitVisibility(By.xpath(ERROR_ALERT))
                    .getText()
                    .equals(expectedText));
  }

  public void waitTextInSuccessAlert(String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver ->
                seleniumWebDriverHelper
                    .waitVisibility(By.xpath(SUCCESS_ALERT))
                    .getText()
                    .equals(expectedText));
  }

  private void waitAllBodyFieldsAndButtonsIsVisible() {
    asList(
            By.xpath(SAVE_BUTTON),
            By.xpath(CANCEL_BUTTON),
            By.id(USERNAME_FIELD_ID),
            By.id(EMAIL_FIELD_ID),
            By.id(FIRST_NAME_FIELD_ID),
            By.id(LAST_NAME_FIELD_ID))
        .forEach(locator -> seleniumWebDriverHelper.waitVisibility(locator));
  }
}
