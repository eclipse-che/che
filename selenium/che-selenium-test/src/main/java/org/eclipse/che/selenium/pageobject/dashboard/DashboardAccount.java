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
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.Locators.EDIT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.Locators.EMAIL_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.Locators.FIRST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.Locators.LAST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.Locators.LOGIN_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.ACCOUNT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.APPLICATIONS_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.AUTHENTICATOR_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.EMAIL_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.FIRST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.LAST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.PASSWORD_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.SESSIONS_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardAccount.LocatorsKeycloak.USERNAME_FIELD_ID;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class DashboardAccount {

  private final SeleniumWebDriver seleniumWebDriver;
  private final TestUser defaultTestUser;
  private final WebDriverWait loadPageWait;

  @Inject
  public DashboardAccount(SeleniumWebDriver seleniumWebDriver, TestUser defaultTestUser) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.defaultTestUser = defaultTestUser;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  protected interface Locators {
    String EMAIL_FIELD = "//input[@name='email']";
    String LOGIN_FIELD = "//input[@name='login_name']";
    String FIRST_NAME_FIELD = "//input[@name='first_name']";
    String LAST_NAME_FIELD = "//input[@name='last_name']";
    String EDIT_BUTTON = "//div[@name='editButton']/button";
    String TITLE_ID = "Account";
  }

  public AccountFields getDefaultFieldsValue() {
    AccountFields accountFields = new AccountFields();

    accountFields.setEmail(defaultTestUser.getEmail());
    accountFields.setLogin(defaultTestUser.getName());
    accountFields.setFirstName("");
    accountFields.setLastName("");

    return accountFields;
  }

  public AccountFields getCurrentFieldsValue() {
    AccountFields accountFields = new AccountFields();

    accountFields.setEmail(getEmailFieldValue());
    accountFields.setLogin(getLoginFieldValue());
    accountFields.setFirstName(getFirstNameFieldValue());
    accountFields.setLastName(getLastNameFieldValue());

    return accountFields;
  }

  public String getEmailFieldValue() {
    return loadPageWait
        .until(visibilityOfElementLocated(By.xpath(EMAIL_FIELD)))
        .getAttribute("value");
  }

  public String getLoginFieldValue() {
    return loadPageWait
        .until(visibilityOfElementLocated(By.xpath(LOGIN_FIELD)))
        .getAttribute("value");
  }

  public String getFirstNameFieldValue() {
    return loadPageWait
        .until(visibilityOfElementLocated(By.xpath(FIRST_NAME_FIELD)))
        .getAttribute("value");
  }

  public String getLastNameFieldValue() {
    return loadPageWait
        .until(visibilityOfElementLocated(By.xpath(LAST_NAME_FIELD)))
        .getAttribute("value");
  }

  public String getTitle() {
    return loadPageWait.until(visibilityOfElementLocated(By.id(Locators.TITLE_ID))).getText();
  }

  public void clickOnEditButton() {
    loadPageWait.until(visibilityOfElementLocated(By.xpath(EDIT_BUTTON))).click();
  }

  public void waitPageIsLoaded() {
    asList(EMAIL_FIELD, LOGIN_FIELD, FIRST_NAME_FIELD, LAST_NAME_FIELD, EDIT_BUTTON)
        .forEach(locator -> loadPageWait.until(visibilityOfElementLocated(By.xpath(locator))));
  }

  // ===================================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

  protected interface LocatorsKeycloak {
    String ACCOUNT_BUTTON = "//div[@id='tabs-menu']//a[text()='Account']";
    String PASSWORD_BUTTON = "//div[@id='tabs-menu']//a[text()='Password']";
    String AUTHENTICATOR_BUTTON = "//div[@id='tabs-menu']//a[text()='Authenticator']";
    String SESSIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Sessions']";
    String APPLICATIONS_BUTTON = "//div[@id='tabs-menu']//a[text()='Applications']";

    String USERNAME_FIELD_ID = "username";
    String EMAIL_FIELD_ID = "email";
    String FIRST_NAME_FIELD_ID = "firstName";
    String LAST_NAME_FIELD_ID = "lastName";

    String SAVE_BUTTON = "//button[text()='Save']";
    String CANCEL_BUTTON = "//button[text()='Cancel']";
  }

  public void keycloakPageIsLoaded() {
    asList(
        By.xpath(ACCOUNT_BUTTON),
        By.xpath(PASSWORD_BUTTON),
        By.xpath(AUTHENTICATOR_BUTTON),
        By.xpath(SESSIONS_BUTTON),
        By.xpath(APPLICATIONS_BUTTON),
        By.xpath(SAVE_BUTTON),
        By.xpath(CANCEL_BUTTON),
        By.id(USERNAME_FIELD_ID),
        By.id(EMAIL_FIELD_ID),
        By.id(FIRST_NAME_FIELD_ID),
        By.id(LAST_NAME_FIELD_ID));
  }

  public AccountFields getFieldsValue() {
    AccountFields accountFields = new AccountFields();

    accountFields.setLogin(getUserNameValue());
    accountFields.setEmail(getEmailValue());
    accountFields.setFirstName(getFirstNameValue());
    accountFields.setLastName(getLastNameValue());

    return accountFields;
  }

  public String getUserNameValue() {
    return getFieldValue(By.id(USERNAME_FIELD_ID));
  }

  public String getEmailValue() {
    return getFieldValue(By.id(EMAIL_FIELD_ID));
  }

  public String getFirstNameValue() {
    return getFieldValue(By.id(FIRST_NAME_FIELD_ID));
  }

  public String getLastNameValue() {
    return getFieldValue(By.id(LAST_NAME_FIELD_ID));
  }

  public void setUserNameValue(String value) {
    setFieldValue(By.id(USERNAME_FIELD_ID), value);
  }

  public void setEmailValue(String value) {
    setFieldValue(By.id(EMAIL_FIELD_ID), value);
  }

  public void setFirstNameValue(String value) {
    setFieldValue(By.id(FIRST_NAME_FIELD_ID), value);
  }

  public void setLastNameValue(String value) {
    setFieldValue(By.id(LAST_NAME_FIELD_ID), value);
  }

  private WebElement waitAndGetElement(By locator) {
    return loadPageWait.until(visibilityOfElementLocated(locator));
  }

  private String getFieldValue(By fieldLocator) {
    return waitAndGetElement(fieldLocator).getAttribute("value");
  }

  private void waitFieldValue(By fieldLocator, String expectedValue) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>) driver -> getFieldValue(fieldLocator).equals(expectedValue));
  }

  private void setFieldValue(By fieldLocator, String value) {
    waitAndGetElement(fieldLocator).clear();
    waitFieldValue(fieldLocator, "");
    waitAndGetElement(fieldLocator).sendKeys(value);
    waitFieldValue(fieldLocator, value);
  }

  // ==================================================================>>>>>>>>>>>>>>>>>>>>>>>>>>

  protected interface PasswordLocators {
    String PASSWORD_FIELD_ID = "password";
    String NEW_PASSWORD_FIELD_ID = "password-new";
    String NEW_PASSWORD_CONFIRMATION_ID = "password-confirm";
    String SAVE_BUTTON = "//button[text()='Save']";
  }

  public void setPasswordFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.NEW_PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordConfirmationFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.NEW_PASSWORD_CONFIRMATION_ID), value);
  }

  public void clickOnSavePasswordButton() {
    loadPageWait.until(visibilityOfElementLocated(By.xpath(PasswordLocators.SAVE_BUTTON))).click();
  }

  // ==================================================================>>>>>>>>>>>>>>>>>>>>>>>>>>
  private class AccountFields {
    private String login;
    private String email;
    private String firstName;
    private String lastName;

    public void setLogin(String login) {
      this.login = login;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getLogin() {
      return login;
    }

    public String getEmail() {
      return email;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }
  }
}
