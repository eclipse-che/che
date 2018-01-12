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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EDIT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EMAIL_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.FIRST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LAST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LOGIN_FIELD;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.openqa.selenium.By;
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
}
