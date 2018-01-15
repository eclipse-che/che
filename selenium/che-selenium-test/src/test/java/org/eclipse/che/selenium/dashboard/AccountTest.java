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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.AccountFields;
import org.eclipse.che.selenium.pageobject.dashboard.account.DashboardAccount;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakAccount;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AccountTest {

  private static AccountFields changedFields;

  @Inject private Dashboard dashboard;
  @Inject private DashboardAccount dashboardAccount;
  @Inject private TestUser defaultTestUser;
  @Inject private KeycloakAccount keycloakAccount;

  @BeforeClass
  public void setup() {
    changedFields =
        new AccountFields(
            defaultTestUser.getName(), "changed-mail@testmail.ua", "UserFirstName", "UserLastName");

    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.clickOnUsernameButton();
    dashboard.clickOnAccountItem();
    dashboardAccount.waitPageIsLoaded();
  }

  @Test
  public void test1() {
    dashboardAccount.getTitle().equals("Account");
    Assert.assertTrue(
        dashboardAccount
            .getDefaultFieldsValue()
            .isEquals(dashboardAccount.getCurrentFieldsValue()));
    dashboardAccount.clickOnEditButton();

    keycloakAccount.accountPageIsLoaded();
    keycloakAccount.getAllFieldsValue().isEquals(dashboardAccount.getDefaultFieldsValue());
  }
}
