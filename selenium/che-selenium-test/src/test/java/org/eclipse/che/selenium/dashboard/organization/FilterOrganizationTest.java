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
package org.eclipse.che.selenium.dashboard.organization;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClientFactory;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.AddOrganization;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization filter
 *
 * @author Ann Shumilova
 */
@Test(groups = {TestGroup.MULTIUSER, TestGroup.DOCKER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class FilterOrganizationTest {
  private static final String WRONG_ORG_NAME = generate("wrong-org-", 7);

  private int initialOrgNumber;
  private TestOrganizationServiceClient organizationServiceClient;

  @InjectTestOrganization private TestOrganization organization;

  @Inject private TestOrganizationServiceClientFactory organizationServiceClientFactory;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private AddOrganization addOrganization;
  @Inject private NavigationBar navigationBar;

  @Inject private TestUser testUser;

  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    organizationServiceClient = organizationServiceClientFactory.create(testUser);

    organization.addMember(testUser.getId());
    initialOrgNumber = organizationServiceClient.getAll().size();
    dashboard.open(testUser.getName(), testUser.getPassword());
  }

  public void testOrganizationListFiler() {
    // Test that organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber);
    assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber);
    assertTrue(
        organizationListPage.getValues(NAME).contains(organization.getName()),
        "Organization list consisted of " + organizationListPage.getValues(NAME));

    // Tests filter the organization by full organization name
    organizationListPage.typeInSearchInput(organization.getName());
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(organization.getName()));
    assertEquals(organizationListPage.getOrganizationListItemCount(), 1);

    // Tests filter the organization by part of organization name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(
        organization.getName().substring(organization.getName().length() / 2));
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(organization.getName()));
    assertEquals(organizationListPage.getOrganizationListItemCount(), 1);

    // Test filter the organization by wrong name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(WRONG_ORG_NAME);
    organizationListPage.waitForOrganizationsList();
    assertEquals(organizationListPage.getOrganizationListItemCount(), 0);

    organizationListPage.clearSearchInput();

    assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber);
  }
}
