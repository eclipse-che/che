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
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static org.eclipse.che.api.workspace.shared.Constants.BOOTSTRAPPER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.INSTALLER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.RUNTIME_LOG_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_STATUS_CHANGED_METHOD;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STATUS_CHANGED_METHOD;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.jsonrpc.RemoteSubscriptionPermissionManager;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link WorkspaceRemoteSubscriptionPermissionFilter}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRemoteSubscriptionPermissionFilterTest {

  @Mock private RemoteSubscriptionPermissionManager permissionManager;

  @Mock private Subject subject;

  private WorkspaceRemoteSubscriptionPermissionFilter permissionFilter;

  @BeforeMethod
  public void setUp() {
    EnvironmentContext.getCurrent().setSubject(subject);
    permissionFilter = new WorkspaceRemoteSubscriptionPermissionFilter();
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldRegisterItself() {
    // when
    permissionFilter.register(permissionManager);

    // then
    verify(permissionManager)
        .registerCheck(
            permissionFilter,
            WORKSPACE_STATUS_CHANGED_METHOD,
            MACHINE_STATUS_CHANGED_METHOD,
            SERVER_STATUS_CHANGED_METHOD,
            RUNTIME_LOG_METHOD,
            MACHINE_LOG_METHOD,
            INSTALLER_LOG_METHOD,
            INSTALLER_STATUS_CHANGED_METHOD,
            BOOTSTRAPPER_STATUS_CHANGED_METHOD);
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "The current user doesn't have permissions to listen to the specified workspace events")
  public void shouldThrowExceptionIfUserDoesNotHaveRunNorUsePermissions() throws Exception {
    // given
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.RUN))
        .thenReturn(false);
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.USE))
        .thenReturn(false);

    // when
    permissionFilter.check("ignored", ImmutableMap.of("workspaceId", "ws123"));
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp = "Workspace id must be specified in scope")
  public void shouldThrowExceptionIfWorkspaceIdIsMissing() throws Exception {
    // given
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.RUN))
        .thenReturn(false);
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.USE))
        .thenReturn(false);

    // when
    permissionFilter.check("ignored", Collections.emptyMap());
  }

  @Test
  public void shouldDoNothingIfUserDoesNotHaveRunPermissions() throws Exception {
    // given
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.RUN))
        .thenReturn(true);
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.USE))
        .thenReturn(false);

    // when
    permissionFilter.check("ignored", ImmutableMap.of("workspaceId", "ws123"));
  }

  @Test
  public void shouldDoNothingIfUserDoesNotHaveUsePermissions() throws Exception {
    // given
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.RUN))
        .thenReturn(false);
    when(subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "ws123", WorkspaceDomain.USE))
        .thenReturn(true);

    // when
    permissionFilter.check("ignored", ImmutableMap.of("workspaceId", "ws123"));
  }
}
