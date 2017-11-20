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
package org.eclipse.che.selenium.dashboard.organization;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.AddOrganization;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization filter
 *
 * @author Ann Shumilova
 */
@Multiuser
public class FilterOrganizationTest {
  private static final String ORG1_NAME = generate("org1-", 7);
  private static final String ORG2_NAME = generate("org2-", 7);
  private static final String ORG3_NAME = generate("org3-", 7);
  private static final String ORG4_NAME = generate("org4-", 7);
  private static final int ORGANIZATIONS_NUMBER = 4;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private AddOrganization addOrganization;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    try {
      testOrganizationServiceClient.create(ORG1_NAME);
      testOrganizationServiceClient.create(ORG2_NAME);
      testOrganizationServiceClient.create(ORG3_NAME);
      testOrganizationServiceClient.create(ORG4_NAME);

      dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    } catch (Exception e) {
      // remove test organizations in case of error because TestNG skips @AfterClass method here
      tearDown();
      throw e;
    }
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteByName(ORG1_NAME);
    testOrganizationServiceClient.deleteByName(ORG2_NAME);
    testOrganizationServiceClient.deleteByName(ORG3_NAME);
    testOrganizationServiceClient.deleteByName(ORG4_NAME);
  }

  @Test
  public void testOrganizationListFiler() {
    // Test that organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) == ORGANIZATIONS_NUMBER);
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER);
    assertTrue(organizationListPage.getValues(NAME).contains(ORG1_NAME));

    // Tests filter the organization by full organization name
    organizationListPage.typeInSearchInput(ORG1_NAME);
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(ORG1_NAME));
    assertTrue(organizationListPage.getOrganizationListItemCount() == 1);

    // Tests filter the organization by part of organization name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(ORG1_NAME.substring(ORG1_NAME.length() / 2));
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(ORG1_NAME));
    assertTrue(organizationListPage.getOrganizationListItemCount() == 1);

    // Test filter the organization by wrong name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(ORG1_NAME + "_wrong_name");
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getOrganizationListItemCount() == 0);

    organizationListPage.clearSearchInput();
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER);
  }
}
