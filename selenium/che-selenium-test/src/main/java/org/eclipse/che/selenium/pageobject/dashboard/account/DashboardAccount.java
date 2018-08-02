/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EDIT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EMAIL_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.FIRST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LAST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LOGIN_FIELD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

/** @author Igor Ohrimenko */
@Singleton
public class DashboardAccount {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public DashboardAccount(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  interface Locators {
    String EMAIL_FIELD = "email";
    String LOGIN_FIELD = "login_name";
    String FIRST_NAME_FIELD = "first_name";
    String LAST_NAME_FIELD = "last_name";
    String EDIT_BUTTON = "editButton";
    String TITLE_ID = "Account";
  }

  public Account getAllFields() {
    Account account = new Account();

    account.withEmail(getEmailFieldValue());
    account.withLogin(getLoginFieldValue());
    account.withFirstName(getFirstNameFieldValue());
    account.withLastName(getLastNameFieldValue());

    return account;
  }

  public String getEmailFieldValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.name(EMAIL_FIELD));
  }

  public String getLoginFieldValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.name(LOGIN_FIELD));
  }

  public String getFirstNameFieldValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.name(FIRST_NAME_FIELD));
  }

  public String getLastNameFieldValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.name(LAST_NAME_FIELD));
  }

  public String getTitle() {
    return seleniumWebDriverHelper.waitVisibility(By.id(Locators.TITLE_ID)).getText();
  }

  public void clickOnEditButton() {
    seleniumWebDriverHelper.waitAndClick(By.name(EDIT_BUTTON));
  }

  public void waitPageIsLoaded() {
    asList(EMAIL_FIELD, LOGIN_FIELD, FIRST_NAME_FIELD, LAST_NAME_FIELD, EDIT_BUTTON)
        .forEach(locator -> seleniumWebDriverHelper.waitVisibility(By.name(locator)));
  }
}
