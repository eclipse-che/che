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
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.user.CheSecondTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.Account;
import org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = TestGroup.MULTIUSER)
public class AccountTest {

  private static Account changedFields;
  private static String parentWindow;

  @Inject private Dashboard dashboard;
  @Inject private DashboardAccount dashboardAccount;
  @Inject private CheSecondTestUser secondTestUser;
  @Inject private KeycloakAccountPage keycloakAccount;
  @Inject private KeycloakPasswordPage testKeycloakPasswordPage;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setup() {
    changedFields =
        new Account(
            secondTestUser.getName(),
            format("%s@testmail.ua", secondTestUser.getName()),
            "UserFirstName",
            "UserLastName");

    dashboard.open(secondTestUser.getName(), secondTestUser.getPassword());
    dashboard.waitDashboardToolbarTitle();
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    parentWindow = seleniumWebDriver.getWindowHandle();
  }

  public void shouldChangeEmailFirstAndLastName() {
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

  public void shouldChangePasswordAndCheckIt() {
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    dashboardAccount.clickOnEditButton();

    keycloakAccount.switchToNoneParentWindow(parentWindow);
    keycloakAccount.waitAccountPageIsLoaded();
    keycloakAccount.clickOnPasswordButton();
    testKeycloakPasswordPage.waitPasswordPageIsLoaded();
    testKeycloakPasswordPage.clickOnSavePasswordButton();

    testKeycloakPasswordPage.waitTextInErrorAlert("Please specify password.");

    testKeycloakPasswordPage.setPasswordFieldValue("wrongPassword");
    testKeycloakPasswordPage.clickOnSavePasswordButton();

    testKeycloakPasswordPage.waitTextInErrorAlert("Invalid existing password.");
    testKeycloakPasswordPage.setPasswordFieldValue(secondTestUser.getPassword());
    testKeycloakPasswordPage.setNewPasswordFieldValue("changedPassword");
    testKeycloakPasswordPage.setNewPasswordConfirmationFieldValue("changedPassword");
    testKeycloakPasswordPage.clickOnSavePasswordButton();

    testKeycloakPasswordPage.waitTextInSuccessAlert("Your password has been updated.");

    testKeycloakPasswordPage.closeWindowAndSwitchToParent(parentWindow);

    dashboard.clickOnUsernameButton();
    dashboard.clickOnLogoutItem();

    dashboard.open(secondTestUser.getName(), "changedPassword");

    dashboard.waitDashboardToolbarTitle();
  }
}
