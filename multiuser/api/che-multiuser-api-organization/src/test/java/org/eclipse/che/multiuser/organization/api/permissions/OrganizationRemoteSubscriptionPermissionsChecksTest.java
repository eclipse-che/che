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

import static org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster.ORGANIZATION_CHANGED_METHOD_NAME;
import static org.eclipse.che.multiuser.organization.api.listener.OrganizationEventsWebsocketBroadcaster.ORGANIZATION_MEMBERSHIP_METHOD_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationRemoteSubscriptionPermissionsChecks.MembershipsChangedSubscriptionCheck;
import org.eclipse.che.multiuser.organization.api.permissions.OrganizationRemoteSubscriptionPermissionsChecks.OrganizationChangedSubscriptionCheck;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OrganizationRemoteSubscriptionPermissionsChecks}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationRemoteSubscriptionPermissionsChecksTest {
  @Mock private Subject subject;

  @Mock private PermissionsManager permissionsManager;
  @Mock private RemoteSubscriptionPermissionManager permissionManager;

  @InjectMocks private OrganizationRemoteSubscriptionPermissionsChecks permissionsChecks;

  @BeforeMethod
  public void setUp() {
    EnvironmentContext.getCurrent().setSubject(subject);
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldRegisterChecks() {
    // when
    permissionsChecks.register(permissionManager);

    // then
    verify(permissionManager)
        .registerCheck(
            any(OrganizationChangedSubscriptionCheck.class), eq(ORGANIZATION_CHANGED_METHOD_NAME));
    verify(permissionManager)
        .registerCheck(
            any(MembershipsChangedSubscriptionCheck.class),
            eq(ORGANIZATION_MEMBERSHIP_METHOD_NAME));
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp = "User id must be specified in scope")
  public void shouldThrowExceptionIfUserIdIsMissing() throws Exception {
    // given
    MembershipsChangedSubscriptionCheck check = new MembershipsChangedSubscriptionCheck();
    when(subject.getUserId()).thenReturn("user2");

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, Collections.emptyMap());
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp = "It is only allowed to listen to own memberships changes")
  public void shouldThrowExceptionIfUserTryToListenToForeignMemberships() throws Exception {
    // given
    MembershipsChangedSubscriptionCheck check = new MembershipsChangedSubscriptionCheck();
    when(subject.getUserId()).thenReturn("user2");

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, ImmutableMap.of("userId", "user1"));
  }

  @Test
  public void shouldDoNothingIfUserTryToListenToOwnMemberships() throws Exception {
    // given
    MembershipsChangedSubscriptionCheck check = new MembershipsChangedSubscriptionCheck();
    when(subject.getUserId()).thenReturn("user1");

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, ImmutableMap.of("userId", "user1"));
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp = "Organization id must be specified in scope")
  public void shouldThrowExceptionIfOrganizationIdIsMissing() throws Exception {
    // given
    OrganizationChangedSubscriptionCheck check =
        new OrganizationChangedSubscriptionCheck(permissionsManager);

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, Collections.emptyMap());
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "User doesn't have any permissions for the specified organization")
  public void shouldThrowExceptionIfUserDoesNotHaveAnyPermissionsToRequestedOrganization()
      throws Exception {
    // given
    OrganizationChangedSubscriptionCheck check =
        new OrganizationChangedSubscriptionCheck(permissionsManager);
    when(subject.getUserId()).thenReturn("user1");
    when(permissionsManager.get("user1", OrganizationDomain.DOMAIN_ID, "org123"))
        .thenThrow(new NotFoundException(""));

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, ImmutableMap.of("organizationId", "org123"));
  }

  @Test
  public void shouldDoNothingIfUserTryToListenEventsOfOrganizationWhereHeHasPermissions()
      throws Exception {
    // given
    OrganizationChangedSubscriptionCheck check =
        new OrganizationChangedSubscriptionCheck(permissionsManager);
    when(subject.getUserId()).thenReturn("user1");
    when(permissionsManager.get("user1", OrganizationDomain.DOMAIN_ID, "org123"))
        .thenReturn(mock(AbstractPermissions.class));

    // when
    check.check(ORGANIZATION_MEMBERSHIP_METHOD_NAME, ImmutableMap.of("organizationId", "org123"));
  }
}
