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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.google.inject.Inject;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization views for simple user being a member of any organization.
 *
 * @author Ann Shumilova
 */
public class UserEmptyOrganizationTest {
  private static final Logger LOG = LoggerFactory.getLogger(UserEmptyOrganizationTest.class);

  @Inject private OrganizationListPage organizationListPage;
  @Inject private NavigationBar navigationBar;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @Test
  public void testOrganizationListComponents() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsEmptyList();

    assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), "0");
    assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertFalse(organizationListPage.isSearchInputVisible());
  }
}
