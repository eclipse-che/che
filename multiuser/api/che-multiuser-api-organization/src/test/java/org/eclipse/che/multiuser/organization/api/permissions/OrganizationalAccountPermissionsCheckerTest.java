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
package org.eclipse.che.multiuser.organization.api.permissions;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.account.AccountOperation;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.permissions.OrganizationalAccountPermissionsChecker}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationalAccountPermissionsCheckerTest {
  private static final String ORG_ID = "org123";

  @Mock private Subject subject;

  private OrganizationalAccountPermissionsChecker permissionsChecker;

  @BeforeMethod
  public void setUp() throws Exception {
    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);

    EnvironmentContext.getCurrent().setSubject(subject);

    permissionsChecker = new OrganizationalAccountPermissionsChecker();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldReturnOrganizationalReturnType() throws Exception {
    // then
    assertEquals(permissionsChecker.getAccountType(), OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
  }

  @Test
  public void shouldCheckCreateWorkspacesPermissionOnOrganizationDomainLevel() throws Exception {
    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.CREATE_WORKSPACE);

    verify(subject)
        .hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.CREATE_WORKSPACES);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp =
        "User is not authorized to create workspaces in specified namespace."
  )
  public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToCreateWorkspaces()
      throws Exception {
    when(subject.hasPermission(
            OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.CREATE_WORKSPACES))
        .thenReturn(false);

    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.CREATE_WORKSPACE);
  }

  @Test
  public void shouldCheckManageWorkspacesPermissionOnOrganizationDomainLevel() throws Exception {
    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.MANAGE_WORKSPACES);

    verify(subject)
        .hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.MANAGE_WORKSPACES);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp = "User is not authorized to use specified namespace."
  )
  public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToManagerWorkspaces()
      throws Exception {
    when(subject.hasPermission(
            OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.MANAGE_WORKSPACES))
        .thenReturn(false);

    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.MANAGE_WORKSPACES);
  }

  @Test(dataProvider = "requiredAction")
  public void
      shouldNotThrowExceptionWhenUserHasAtLeastOnRequiredPermissionOnGettingResourcesInformation(
          String action) throws Exception {
    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);
    when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, action)).thenReturn(true);

    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.SEE_RESOURCE_INFORMATION);

    verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, action);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp =
        "User is not authorized to see resources information of requested organization."
  )
  public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToSeeResourcesInformation()
      throws Exception {
    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

    permissionsChecker.checkPermissions(ORG_ID, AccountOperation.SEE_RESOURCE_INFORMATION);
  }

  @DataProvider
  private Object[][] requiredAction() {
    return new Object[][] {
      {OrganizationDomain.CREATE_WORKSPACES},
      {OrganizationDomain.MANAGE_WORKSPACES},
      {OrganizationDomain.MANAGE_RESOURCES}
    };
  }
}
