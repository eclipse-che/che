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
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EDIT_BUTTON;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.EMAIL_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.FIRST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LAST_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount.Locators.LOGIN_FIELD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.CheSecondTestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class DashboardAccount {

  private final SeleniumWebDriver seleniumWebDriver;
  private final CheSecondTestUser cheSecondTestUser;
  private final WebDriverWait loadPageWait;
  private final SeleniumWebDriverUtils seleniumWebDriverUtils;

  @Inject
  public DashboardAccount(
      SeleniumWebDriver seleniumWebDriver,
      CheSecondTestUser cheSecondTestUser,
      SeleniumWebDriverUtils seleniumWebDriverUtils) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.cheSecondTestUser = cheSecondTestUser;
    this.seleniumWebDriverUtils = seleniumWebDriverUtils;
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  protected interface Locators {
    String EMAIL_FIELD = "//input[@name='email']";
    String LOGIN_FIELD = "//input[@name='login_name']";
    String FIRST_NAME_FIELD = "//input[@name='first_name']";
    String LAST_NAME_FIELD = "//input[@name='last_name']";
    String EDIT_BUTTON = "//che-button-default[@name='editButton']/button";
    String TITLE_ID = "Account";
  }

  public Account getDefaultFieldsValue() {
    Account account = new Account();

    account.setEmail(cheSecondTestUser.getEmail());
    account.setLogin(cheSecondTestUser.getName());
    account.setFirstName("");
    account.setLastName("");

    return account;
  }

  public Account getCurrentFieldsValue() {
    Account account = new Account();

    account.setEmail(getEmailFieldValue());
    account.setLogin(getLoginFieldValue());
    account.setFirstName(getFirstNameFieldValue());
    account.setLastName(getLastNameFieldValue());

    return account;
  }

  public String getEmailFieldValue() {
    return seleniumWebDriverUtils.getFieldValue(By.xpath(EMAIL_FIELD));
  }

  public String getLoginFieldValue() {
    return seleniumWebDriverUtils.getFieldValue(By.xpath(LOGIN_FIELD));
  }

  public String getFirstNameFieldValue() {
    return seleniumWebDriverUtils.getFieldValue(By.xpath(FIRST_NAME_FIELD));
  }

  public String getLastNameFieldValue() {
    return seleniumWebDriverUtils.getFieldValue(By.xpath(LAST_NAME_FIELD));
  }

  public String getTitle() {
    return seleniumWebDriverUtils.waitAndGetElement(By.id(Locators.TITLE_ID)).getText();
  }

  public void clickOnEditButton() {
    seleniumWebDriverUtils.waitAndClickOnElement(By.xpath(EDIT_BUTTON));
  }

  public void waitPageIsLoaded() {
    asList(EMAIL_FIELD, LOGIN_FIELD, FIRST_NAME_FIELD, LAST_NAME_FIELD, EDIT_BUTTON)
        .forEach(locator -> seleniumWebDriverUtils.waitElementIsVisible(By.xpath(locator)));
  }
}
