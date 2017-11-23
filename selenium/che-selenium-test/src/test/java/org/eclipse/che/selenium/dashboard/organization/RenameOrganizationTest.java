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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.EditMode;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization rename.
 *
 * @author Ann Shumilova
 */
@Multiuser
public class RenameOrganizationTest {
  private static final String PARENT_ORG_NAME = generate("parent-org-", 5);
  private static final String CHILD_ORG_NAME = generate("child-org-", 5);
  private static final String NEW_PARENT_ORG_NAME = generate("new-parent-org-", 5);
  private static final String NEW_CHILD_ORG_NAME = generate("new-child-org-", 5);

  // more than 20 symbols
  private static final String TOO_LONG_ORG_NAME = generate("too-long-org-name-", 10);

  private static final String EMPTY_ORGANIZATION_NAME = " ";
  private static final String ORG_NAME_WITH_INVALID_CHARS = "_organization$";

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private EditMode editMode;
  @Inject private Dashboard dashboard;
  @Inject private TestUser testUser;
  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    try {
      OrganizationDto parentOrg = testOrganizationServiceClient.create(PARENT_ORG_NAME);
      testOrganizationServiceClient.addAdmin(parentOrg.getId(), testUser.getId());

      OrganizationDto childOrg =
          testOrganizationServiceClient.create(CHILD_ORG_NAME, parentOrg.getId());
      testOrganizationServiceClient.addAdmin(childOrg.getId(), testUser.getId());

      dashboard.open(testUser.getName(), testUser.getPassword());
    } catch (Exception e) {
      // remove test organizations in case of error because TestNG skips @AfterClass method here
      tearDown();
      throw e;
    }
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteByName(NEW_CHILD_ORG_NAME);
    testOrganizationServiceClient.deleteByName(NEW_PARENT_ORG_NAME);
    testOrganizationServiceClient.deleteByName(CHILD_ORG_NAME);
    testOrganizationServiceClient.deleteByName(PARENT_ORG_NAME);
  }

  @Test
  public void testParentOrganizationRename() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(PARENT_ORG_NAME);

    // Check organization renaming with name just ' '
    renameOrganizationWithInvalidName(EMPTY_ORGANIZATION_NAME);

    // Check organization renaming with name more than 20 symbols
    renameOrganizationWithInvalidName(TOO_LONG_ORG_NAME);

    // Check organization renaming with name that contains invalid characters
    renameOrganizationWithInvalidName(ORG_NAME_WITH_INVALID_CHARS);

    // Test renaming of the parent organization
    organizationPage.waitOrganizationTitle(PARENT_ORG_NAME);
    organizationPage.setOrganizationName(NEW_PARENT_ORG_NAME);
    editMode.waitDisplayed();
    assertTrue(editMode.isSaveEnabled());
    editMode.clickSave();
    editMode.waitHidden();
    organizationPage.waitOrganizationTitle(NEW_PARENT_ORG_NAME);
    assertEquals(NEW_PARENT_ORG_NAME, organizationPage.getOrganizationName());
  }

  @Test(priority = 1)
  public void testSubOrganizationRename() {
    String childOrgQualifiedName = NEW_PARENT_ORG_NAME + "/" + CHILD_ORG_NAME;
    String newChildOrgQualifiedName = NEW_PARENT_ORG_NAME + "/" + NEW_CHILD_ORG_NAME;

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Test renaming of the child organization
    organizationListPage.clickOnOrganization(childOrgQualifiedName);
    organizationPage.waitOrganizationTitle(childOrgQualifiedName);
    organizationPage.setOrganizationName(NEW_CHILD_ORG_NAME);
    editMode.waitDisplayed();
    assertTrue(editMode.isSaveEnabled());
    editMode.clickSave();
    editMode.waitHidden();
    organizationPage.waitOrganizationTitle(newChildOrgQualifiedName);
    assertEquals(organizationPage.getOrganizationName(), NEW_CHILD_ORG_NAME);

    // Back to the parent organization and test that the child organization renamed
    organizationPage.clickBackButton();
    organizationPage.waitOrganizationTitle(NEW_PARENT_ORG_NAME);
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(newChildOrgQualifiedName));

    // Back to the Organizations list and test that the organizations renamed
    organizationPage.clickBackButton();
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getValues(NAME).contains(newChildOrgQualifiedName));
    assertTrue(organizationListPage.getValues(NAME).contains(NEW_PARENT_ORG_NAME));
  }

  private void renameOrganizationWithInvalidName(String organizationName) {
    organizationPage.waitOrganizationTitle(PARENT_ORG_NAME);
    organizationPage.setOrganizationName(organizationName);
    editMode.waitDisplayed();
    assertFalse(editMode.isSaveEnabled());
    editMode.clickCancel();
    editMode.waitHidden();
    assertEquals(PARENT_ORG_NAME, organizationPage.getOrganizationName());
  }
}
