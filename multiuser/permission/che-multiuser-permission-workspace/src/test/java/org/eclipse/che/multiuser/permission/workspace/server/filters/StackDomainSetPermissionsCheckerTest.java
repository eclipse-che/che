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

import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.DELETE;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.READ;
import static org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain.SEARCH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.filter.check.DefaultSetPermissionsChecker;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link StackDomainSetPermissionsChecker}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class StackDomainSetPermissionsCheckerTest {

  @Mock private Subject subj;

  @Mock private DefaultSetPermissionsChecker defaultChecker;

  private StackDomainSetPermissionsChecker stackSetPermChecker;

  @BeforeMethod
  public void setup() throws Exception {
    stackSetPermChecker = new StackDomainSetPermissionsChecker(defaultChecker);
    final EnvironmentContext ctx = new EnvironmentContext();
    ctx.setSubject(subj);
    EnvironmentContext.setCurrent(ctx);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    EnvironmentContext.reset();
  }

  @Test
  public void permitsToSetNonPublicPermissionsWhenDefaultCheckPassed() throws Exception {
    final Permissions permissions =
        new StackPermissionsImpl("user73", "stack73", singletonList(DELETE));
    doNothing().when(defaultChecker).check(permissions);

    stackSetPermChecker.check(permissions);

    verify(defaultChecker).check(permissions);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void throwsForbiddenExceptionOnSetNonPublicPermissionsWhenDefaultCheckFailed()
      throws Exception {
    final Permissions permissions =
        new StackPermissionsImpl("user73", "stack73", singletonList(DELETE));
    doThrow(ForbiddenException.class).when(defaultChecker).check(permissions);

    stackSetPermChecker.check(permissions);
  }

  @Test
  public void permitsToSetPublicPermissionsWithSearchActionForAdmin() throws Exception {
    final Permissions permissions = new StackPermissionsImpl("*", "stack73", singletonList(SEARCH));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(true);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker, never()).check(permissions);
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "Following actions are not supported for setting as public:.*")
  public void throwsForbiddenExceptionWhenSetPublicPermissionsWithUnsupportedActionByAdmin()
      throws Exception {
    final Permissions permissions =
        new StackPermissionsImpl("*", "stack73", ImmutableList.of(SEARCH, DELETE));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(true);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker, never()).check(permissions);
  }

  @Test
  public void permitsToSetPublicPermissionsWithReadActionForNonAdminUser() throws Exception {
    final Permissions permissions = new StackPermissionsImpl("*", "stack73", singletonList(READ));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(false);
    doNothing().when(defaultChecker).check(permissions);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker).check(permissions);
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "Following actions are not supported for setting as public:.*")
  public void throwsForbiddenExceptionWhenSetPublicPermissionsWithUnsupportedActionByNonAdminUser()
      throws Exception {
    final Permissions permissions = new StackPermissionsImpl("*", "stack73", singletonList(DELETE));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(false);
    doNothing().when(defaultChecker).check(permissions);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker).check(permissions);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void throwsForbiddenExceptionWhenSetPublicPermissionsByNonAdminUserFailedOnDefaultCheck()
      throws Exception {
    final Permissions permissions = new StackPermissionsImpl("*", "stack73", singletonList(READ));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(false);
    doThrow(ForbiddenException.class).when(defaultChecker).check(permissions);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker).check(permissions);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void throwsExceptionWhenChecksAdminPermissionsWithWrongDomainOnSetPermission()
      throws Exception {
    final Permissions permissions = new StackPermissionsImpl("*", "stack73", singletonList(SEARCH));
    when(subj.hasPermission(StackDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(false);

    stackSetPermChecker.check(permissions);

    verify(subj).hasPermission(any(), any(), MANAGE_SYSTEM_ACTION);
    verify(defaultChecker).check(permissions);
  }
}
