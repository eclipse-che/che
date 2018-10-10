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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;

import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class MachineTokenAuthorizedSubjectTest {

  private static final String WS_ID = "ws123";
  private static final String USER_ID = "user123";
  @Mock private PermissionChecker permissionChecker;

  @Mock Subject baseSubject;

  private MachineTokenAuthorizedSubject subject;

  @BeforeMethod
  private void setUp() {
    lenient().when(baseSubject.getUserId()).thenReturn(USER_ID);
    subject = new MachineTokenAuthorizedSubject(baseSubject, permissionChecker, WS_ID);
  }

  @Test
  public void shouldRejectPermissionsFromAnotherWorkspace() {
    assertFalse(
        subject.hasPermission(WorkspaceDomain.DOMAIN_ID, "another_ws", WorkspaceDomain.READ));
  }

  @Test
  public void shouldRequestPermissionsFromBaseSubjectForNonWorkspaceDomains() throws Exception {
    subject.hasPermission(SystemDomain.DOMAIN_ID, "", SystemDomain.MANAGE_SYSTEM_ACTION);
    verify(permissionChecker, atLeastOnce())
        .hasPermission(
            eq(USER_ID), eq(SystemDomain.DOMAIN_ID), eq(""), eq(SystemDomain.MANAGE_SYSTEM_ACTION));
  }
}
