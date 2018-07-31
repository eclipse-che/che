/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.filters;

import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.api.permission.server.filter.check.DefaultRemovePermissionsChecker;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackDomain;
import org.eclipse.che.multiuser.permission.workspace.server.stack.StackPermissionsImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link PublicPermissionsRemoveChecker}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class PublicPermissionsRemoveCheckerTest {

  private static final String USER = "user123";
  private static final String INSTANCE = "instance123";

  @Mock private Subject subj;
  @Mock private PermissionsManager manager;
  @Mock private DefaultRemovePermissionsChecker defaultChecker;

  private PublicPermissionsRemoveChecker publicPermissionsRemoveChecker;

  @BeforeMethod
  public void setup() throws Exception {
    publicPermissionsRemoveChecker = new PublicPermissionsRemoveChecker(defaultChecker, manager);
    final EnvironmentContext ctx = new EnvironmentContext();
    ctx.setSubject(subj);
    EnvironmentContext.setCurrent(ctx);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    EnvironmentContext.reset();
  }

  @Test
  public void permitsRemoveNonPublicPermissionsWhenDefaultCheckPassed() throws Exception {
    doNothing().when(defaultChecker).check(USER, StackDomain.DOMAIN_ID, INSTANCE);

    publicPermissionsRemoveChecker.check(USER, StackDomain.DOMAIN_ID, INSTANCE);

    verify(defaultChecker, times(1)).check(USER, StackDomain.DOMAIN_ID, INSTANCE);
    verify(manager, never()).get(USER, StackDomain.DOMAIN_ID, INSTANCE);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void throwsForbiddenExceptionWhenRemoveNonPublicPermissionsAndDefaultCheckFailed()
      throws Exception {
    doThrow(ForbiddenException.class)
        .when(defaultChecker)
        .check(USER, StackDomain.DOMAIN_ID, INSTANCE);

    publicPermissionsRemoveChecker.check(USER, StackDomain.DOMAIN_ID, INSTANCE);

    verify(defaultChecker, times(1)).check(USER, StackDomain.DOMAIN_ID, INSTANCE);
    verify(manager, never()).get(USER, StackDomain.DOMAIN_ID, INSTANCE);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void throwsForbiddenExceptionWhenFailedToGetActionRemovingPermissionByAdmin()
      throws Exception {
    doThrow(ServerException.class).when(manager).get("*", StackDomain.DOMAIN_ID, INSTANCE);
    doThrow(ForbiddenException.class)
        .when(defaultChecker)
        .check("*", StackDomain.DOMAIN_ID, INSTANCE);
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(true);

    publicPermissionsRemoveChecker.check("*", StackDomain.DOMAIN_ID, INSTANCE);
  }

  @Test
  public void permitsRemoveStackPermissionsWhenAdminUserPassedDefaultCheck() throws Exception {
    when(manager.get("*", StackDomain.DOMAIN_ID, INSTANCE))
        .thenReturn(new StackPermissionsImpl("*", INSTANCE, singletonList(StackDomain.SEARCH)));
    when(subj.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(true);

    publicPermissionsRemoveChecker.check("*", StackDomain.DOMAIN_ID, INSTANCE);

    verify(manager, times(1)).get("*", StackDomain.DOMAIN_ID, INSTANCE);
    verify(subj, times(1)).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    verify(defaultChecker, never()).check("*", StackDomain.DOMAIN_ID, INSTANCE);
  }
}
