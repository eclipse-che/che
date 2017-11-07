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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.EditMode;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization rename.
 *
 * @author Ann Shumilova
 */
public class RenameOrganizationTest {
  private static final Logger LOG = LoggerFactory.getLogger(RenameOrganizationTest.class);

  private OrganizationDto parentOrganization;
  private OrganizationDto childOrganization;
  private String parentNewName;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private EditMode editMode;
  @Inject private Dashboard dashboard;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private TestUser testUser;
  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    parentOrganization =
        testOrganizationServiceClient.create(NameGenerator.generate("organization", 5));
    childOrganization =
        testOrganizationServiceClient.create(
            NameGenerator.generate("organization", 5), parentOrganization.getId());

    parentNewName = NameGenerator.generate("newname", 5);
    testOrganizationServiceClient.addAdmin(parentOrganization.getId(), testUser.getId());
    testOrganizationServiceClient.addAdmin(childOrganization.getId(), testUser.getId());

    dashboard.open(testUser.getName(), testUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteById(parentOrganization.getId());
  }

  @Test(priority = 1)
  public void testParentOrganizationRename() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    organizationListPage.clickOnOrganization(parentOrganization.getName());

    organizationPage.waitOrganizationTitle(parentOrganization.getName());

    organizationPage.setOrganizationName("");
    editMode.waitDisplayed();
    assertFalse(editMode.isSaveEnabled());

    editMode.clickCancel();
    editMode.waitHidden();
    assertEquals(parentOrganization.getName(), organizationPage.getOrganizationName());

    organizationPage.setOrganizationName(parentNewName);
    editMode.waitDisplayed();
    assertTrue(editMode.isSaveEnabled());

    editMode.clickSave();
    editMode.waitHidden();

    organizationPage.waitOrganizationTitle(parentNewName);
    assertEquals(parentNewName, organizationPage.getOrganizationName());
  }

  @Test(priority = 2)
  public void testSubOrganizationRename() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    String organizationPath = parentNewName + "/" + childOrganization.getName();

    organizationListPage.clickOnOrganization(organizationPath);

    organizationPage.waitOrganizationTitle(organizationPath);
    String newName = NameGenerator.generate("newname", 5);

    organizationPage.setOrganizationName(newName);
    editMode.waitDisplayed();
    assertTrue(editMode.isSaveEnabled());

    editMode.clickSave();
    editMode.waitHidden();

    String path = parentNewName + "/" + newName;
    organizationPage.waitOrganizationTitle(path);
    assertEquals(organizationPage.getOrganizationName(), newName);

    organizationPage.clickBackButton();
    organizationPage.waitOrganizationTitle(parentNewName);
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(path));

    organizationPage.clickBackButton();
    organizationListPage.waitForOrganizationsList();
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(path));
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(parentNewName));
  }
}
