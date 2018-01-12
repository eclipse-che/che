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
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.EMAIL_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.FIRST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.LAST_NAME_FIELD_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.SAVE_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount.AccountPageLocators.USERNAME_FIELD_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;

@Singleton
public class KeycloakAccount extends KeycloakAbstract {

  @Inject
  protected KeycloakAccount(SeleniumWebDriver seleniumWebDriver) {
    super(seleniumWebDriver);
  }

  protected interface AccountPageLocators {
    String USERNAME_FIELD_ID = "username";
    String EMAIL_FIELD_ID = "email";
    String FIRST_NAME_FIELD_ID = "firstName";
    String LAST_NAME_FIELD_ID = "lastName";

    String SAVE_BUTTON = "//button[text()='Save']";
    String CANCEL_BUTTON = "//button[text()='Cancel']";
  }

  public void accountPageIsLoaded() {
    asList(
            By.xpath(SAVE_BUTTON),
            By.xpath(CANCEL_BUTTON),
            By.id(USERNAME_FIELD_ID),
            By.id(EMAIL_FIELD_ID),
            By.id(FIRST_NAME_FIELD_ID),
            By.id(LAST_NAME_FIELD_ID))
        .forEach(locator -> waitElementIsVisible(locator));

    waitAllHeaderButtonsIsVisible();
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
    return getFieldValue(By.id(AccountPageLocators.USERNAME_FIELD_ID));
  }

  public String getEmailValue() {
    return getFieldValue(By.id(AccountPageLocators.EMAIL_FIELD_ID));
  }

  public String getFirstNameValue() {
    return getFieldValue(By.id(AccountPageLocators.FIRST_NAME_FIELD_ID));
  }

  public String getLastNameValue() {
    return getFieldValue(By.id(AccountPageLocators.LAST_NAME_FIELD_ID));
  }

  public void setUserNameValue(String value) {
    setFieldValue(By.id(AccountPageLocators.USERNAME_FIELD_ID), value);
  }

  public void setEmailValue(String value) {
    setFieldValue(By.id(AccountPageLocators.EMAIL_FIELD_ID), value);
  }

  public void setFirstNameValue(String value) {
    setFieldValue(By.id(AccountPageLocators.FIRST_NAME_FIELD_ID), value);
  }

  public void setLastNameValue(String value) {
    setFieldValue(By.id(AccountPageLocators.LAST_NAME_FIELD_ID), value);
  }
}
