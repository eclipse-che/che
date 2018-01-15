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
package org.eclipse.che.selenium.dashboard;

import static java.lang.String.format;
import static org.testng.AssertJUnit.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.AccountFields;
import org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPassword;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AccountTest {

  private static AccountFields changedFields;
  private static String parentWindow;

  @Inject private Dashboard dashboard;
  @Inject private DashboardAccount dashboardAccount;
  @Inject private TestUser defaultTestUser;
  @Inject private KeycloakAccount keycloakAccount;
  @Inject private KeycloakPassword keycloakPassword;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private Entrance entrance;
  @Inject private TestUserFactory testUserFactory;

  @BeforeClass
  public void setup() {
    changedFields =
        new AccountFields(
            defaultTestUser.getName(),
            format("%s@testmail.ua", defaultTestUser.getName()),
            "UserFirstName",
            "UserLastName");

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    parentWindow = seleniumWebDriver.getWindowHandle();
  }

  @AfterClass
  public void restoreDefaultUser() {
    testUserFactory.create(defaultTestUser.getEmail(), defaultTestUser.getPassword());
  }

  @Test
  public void test1() {
    dashboardAccount.getTitle().equals("Account");
    assertTrue(
        dashboardAccount
            .getDefaultFieldsValue()
            .isEquals(dashboardAccount.getCurrentFieldsValue()));
    dashboardAccount.clickOnEditButton();

    keycloakAccount.switchToNoneParentWindow(parentWindow);

    keycloakAccount.waitAccountPageIsLoaded();

    assertTrue(
        keycloakAccount.getAllFieldsValue().isEquals(dashboardAccount.getDefaultFieldsValue()));

    assertTrue(keycloakAccount.usernameFieldIsDisabled());

    keycloakAccount.setEmailValue(changedFields.getEmail());
    keycloakAccount.setFirstNameValue(changedFields.getFirstName());
    keycloakAccount.setLastNameValue(changedFields.getLastName());

    keycloakAccount.clickOnSaveButton();
    keycloakAccount.waitTextInSuccessAlert("Your account has been updated.");

    keycloakAccount.closeWindowAndSwitchToParent(parentWindow);
    dashboardAccount.waitPageIsLoaded();

    seleniumWebDriver.navigate().refresh();
    dashboardAccount.waitPageIsLoaded();

    assertTrue(dashboardAccount.getCurrentFieldsValue().isEquals(changedFields));
  }

  @Test(priority = 1)
  public void test2() {
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    dashboardAccount.clickOnEditButton();

    keycloakAccount.switchToNoneParentWindow(parentWindow);
    keycloakAccount.waitAccountPageIsLoaded();
    keycloakAccount.clickOnPasswordButton();
    keycloakPassword.waitPasswordPageIsLoaded();
    keycloakPassword.clickOnSavePasswordButton();

    keycloakPassword.waitTextInErrorAlert("Please specify password.");

    keycloakPassword.setPasswordFieldValue("wrongPassword");
    keycloakPassword.clickOnSavePasswordButton();

    keycloakPassword.waitTextInErrorAlert("Invalid existing password.");
    keycloakPassword.setPasswordFieldValue(defaultTestUser.getPassword());
    keycloakPassword.setNewPasswordFieldValue("changedPassword");
    keycloakPassword.setNewPasswordConfirmationFieldValue("changedPassword");
    keycloakPassword.clickOnSavePasswordButton();

    keycloakPassword.waitTextInSuccessAlert("Your password has been updated.");

    keycloakPassword.closeWindowAndSwitchToParent(parentWindow);

    dashboard.clickOnUsernameButton();
    dashboard.clickOnLogoutItem();

    dashboard.open(defaultTestUser.getName(), "changedPassword");

    dashboard.waitDashboardToolbarTitle();
  }
}
