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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
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
  private static final String ORGANIZATION_NAME = generate("organization", 5);

  private List<OrganizationDto> organizations = new ArrayList<>();

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
    organizations.add(testOrganizationServiceClient.create(ORGANIZATION_NAME));
    organizations.add(testOrganizationServiceClient.create(generate("organization", 7)));
    organizations.add(testOrganizationServiceClient.create(generate("organization", 7)));
    organizations.add(testOrganizationServiceClient.create(generate("organization", 7)));

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    for (OrganizationDto organization : organizations) {
      testOrganizationServiceClient.deleteById(organization.getId());
    }
  }

  @Test
  public void testOrganizationListFiler() {
    int organizationsCount = organizations.size();

    // Test that organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) >= organizationsCount);
    assertTrue(organizationListPage.getOrganizationListItemCount() >= organizationsCount);
    assertTrue(organizationListPage.getValues(NAME).contains(ORGANIZATION_NAME));

    // Tests filter the organization by full organization name
    organizationListPage.typeInSearchInput(ORGANIZATION_NAME);
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(ORGANIZATION_NAME));
    assertTrue(organizationListPage.getOrganizationListItemCount() >= 1);

    // Tests filter the organization by part of organization name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(
        ORGANIZATION_NAME.substring(ORGANIZATION_NAME.length() / 2));
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(ORGANIZATION_NAME));
    assertTrue(organizationListPage.getOrganizationListItemCount() >= 1);

    // Test filter the organization by wrong name
    organizationListPage.clearSearchInput();
    organizationListPage.typeInSearchInput(ORGANIZATION_NAME + "wrong_name");
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getOrganizationListItemCount() >= 0);

    organizationListPage.clearSearchInput();
    assertTrue(organizationListPage.getOrganizationListItemCount() >= organizationsCount);
  }
}
