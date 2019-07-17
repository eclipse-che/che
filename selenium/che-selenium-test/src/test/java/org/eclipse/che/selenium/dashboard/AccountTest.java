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
package org.eclipse.che.selenium.dashboard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.Account;
import org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccountPage;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakHeaderButtons;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakPasswordPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.MULTIUSER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class AccountTest {

  private Account changedTestUserAccount;
  private String parentWindow;

  @Inject private Dashboard dashboard;
  @Inject private DashboardAccount dashboardAccount;

  @Inject private TestUser testUser;

  @Inject private KeycloakAccountPage keycloakAccount;
  @Inject private KeycloakPasswordPage keycloakPasswordPage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private KeycloakHeaderButtons keycloakHeaderButtons;
  private Account initialTestUserAccount;

  @BeforeClass
  public void setup() {
    initialTestUserAccount =
        new Account()
            .withLogin(testUser.getName())
            .withEmail(testUser.getEmail())
            .withFirstName("")
            .withLastName("");

    changedTestUserAccount =
        new Account()
            .withLogin(testUser.getName())
            .withEmail(testUser.getEmail())
            .withFirstName("UserFirstName")
            .withLastName("UserLastName");

    dashboard.open(testUser.getName(), testUser.getPassword());
    dashboard.waitDashboardToolbarTitle();
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    parentWindow = seleniumWebDriver.getWindowHandle();
  }

  public void shouldChangeEmailFirstAndLastName() {
    dashboardAccount.getTitle().equals("Account");
    assertEquals(dashboardAccount.getAllFields(), initialTestUserAccount);
    dashboardAccount.clickOnEditButton();

    seleniumWebDriverHelper.switchToNextWindow(parentWindow);

    keycloakAccount.waitAccountPageIsLoaded();

    assertEquals(keycloakAccount.getAllFields(), initialTestUserAccount);

    assertTrue(keycloakAccount.usernameFieldIsDisabled());

    keycloakAccount.setEmailValue(changedTestUserAccount.getEmail());
    keycloakAccount.setFirstNameValue(changedTestUserAccount.getFirstName());
    keycloakAccount.setLastNameValue(changedTestUserAccount.getLastName());

    keycloakAccount.clickOnSaveButton();
    keycloakAccount.waitTextInSuccessAlert("Your account has been updated.");

    closeWindowAndSwitchToParent(parentWindow);
    dashboardAccount.waitPageIsLoaded();

    seleniumWebDriver.navigate().refresh();
    dashboardAccount.waitPageIsLoaded();

    assertEquals(dashboardAccount.getAllFields(), changedTestUserAccount);
  }

  public void shouldChangePasswordAndCheckIt() {
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
    dashboardAccount.clickOnEditButton();

    seleniumWebDriverHelper.switchToNextWindow(parentWindow);
    keycloakAccount.waitAccountPageIsLoaded();
    keycloakHeaderButtons.clickOnPasswordButton();
    keycloakPasswordPage.waitPasswordPageIsLoaded();
    keycloakPasswordPage.clickOnSavePasswordButton();

    keycloakPasswordPage.waitTextInErrorAlert("Please specify password.");

    keycloakPasswordPage.setPasswordFieldValue("wrongPassword");
    keycloakPasswordPage.clickOnSavePasswordButton();

    keycloakPasswordPage.waitTextInErrorAlert("Invalid existing password.");
    keycloakPasswordPage.setPasswordFieldValue(testUser.getPassword());
    keycloakPasswordPage.setNewPasswordFieldValue("changedPassword");
    keycloakPasswordPage.setNewPasswordConfirmationFieldValue("changedPassword");
    keycloakPasswordPage.clickOnSavePasswordButton();

    keycloakPasswordPage.waitTextInSuccessAlert("Your password has been updated.");

    closeWindowAndSwitchToParent(parentWindow);

    dashboard.clickOnUsernameButton();
    dashboard.clickOnLogoutItem();

    dashboard.open(testUser.getName(), "changedPassword");

    dashboard.waitDashboardToolbarTitle();
  }

  private void closeWindowAndSwitchToParent(String parentWindow) {
    seleniumWebDriver.close();
    seleniumWebDriver.switchTo().window(parentWindow);
  }
}
