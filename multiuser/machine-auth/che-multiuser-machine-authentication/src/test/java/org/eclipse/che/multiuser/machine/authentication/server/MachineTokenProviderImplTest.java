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
package org.eclipse.che.multiuser.machine.authentication.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.workspace.server.token.MachineAccessForbidden;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link MachineTokenProviderImpl}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class MachineTokenProviderImplTest {
  @Mock private PermissionChecker permissionChecker;

  @Mock private MachineTokenRegistry tokenRegistry;

  @InjectMocks private MachineTokenProviderImpl tokenProvider;

  @Mock private Subject currentSubject;

  @BeforeMethod
  public void setUp() {
    EnvironmentContext environmentContext = new EnvironmentContext();
    environmentContext.setSubject(currentSubject);
    EnvironmentContext.setCurrent(environmentContext);
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldReturnMachineTokenForCurrentSubject() throws Exception {
    // given
    doReturn("user123").when(currentSubject).getUserId();
    doReturn("secret").when(tokenRegistry).getOrCreateToken(any(), any());
    doReturn(true)
        .when(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);

    // when
    String token = tokenProvider.getToken("workspace123");

    // then
    assertEquals(token, "secret");
    verify(tokenRegistry).getOrCreateToken("user123", "workspace123");
    verify(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);
  }

  @Test(
    expectedExceptions = IllegalStateException.class,
    expectedExceptionsMessageRegExp =
        "Unable to get machine token of the workspace "
            + "'workspace123' because it does not exist for an anonymous user\\."
  )
  public void shouldThrowExceptionIfCurrentSubjectIsAnonymous() throws Exception {
    // given
    doReturn(true).when(currentSubject).isAnonymous();

    // when
    tokenProvider.getToken("workspace123");
  }

  @Test(
    expectedExceptions = MachineAccessForbidden.class,
    expectedExceptionsMessageRegExp =
        "The user `user123` doesn't have the required `use` permission for workspace `workspace123`"
  )
  public void shouldThrowExceptionIfCurrentSubjectDoesNotHavePermissionToRetrieveToken()
      throws Exception {
    // given
    doReturn(false)
        .when(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);
    doReturn("user123").when(currentSubject).getUserId();
    doReturn("secret").when(tokenRegistry).getOrCreateToken(any(), any());

    // when
    tokenProvider.getToken("workspace123");
  }

  @Test
  public void shouldReturnMachineTokenForTheSpecifiedUser() throws Exception {
    // given
    doReturn("secret").when(tokenRegistry).getOrCreateToken(any(), any());
    doReturn(true)
        .when(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);

    // when
    String token = tokenProvider.getToken("user123", "workspace123");

    // then
    assertEquals(token, "secret");
    verify(tokenRegistry).getOrCreateToken("user123", "workspace123");
    verify(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);
  }

  @Test(
    expectedExceptions = MachineAccessForbidden.class,
    expectedExceptionsMessageRegExp =
        "The user `user123` doesn't have the required `use` permission for workspace `workspace123`"
  )
  public void shouldThrowExceptionIfTheSpecifiedUserDoesNotHavePermissionToRetrieveToken()
      throws Exception {
    // given
    doReturn(false)
        .when(permissionChecker)
        .hasPermission("user123", WorkspaceDomain.DOMAIN_ID, "workspace123", WorkspaceDomain.USE);
    doReturn("secret").when(tokenRegistry).getOrCreateToken(any(), any());

    // when
    tokenProvider.getToken("user123", "workspace123");
  }
}
