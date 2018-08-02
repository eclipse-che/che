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
package org.eclipse.che.multiuser.permission.account;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PersonalAccountPermissionsCheckerTest {
  private static String userId = "userok";
  @Mock private Subject subject;

  private PersonalAccountPermissionsChecker permissionsChecker;

  @BeforeMethod
  public void setUp() {
    Mockito.when(subject.getUserId()).thenReturn(userId);
    EnvironmentContext.getCurrent().setSubject(subject);

    permissionsChecker = new PersonalAccountPermissionsChecker();
  }

  @AfterMethod
  public void cleanUp() {
    EnvironmentContext.getCurrent().setSubject(null);
  }

  @Test
  public void shouldNotThrowExceptionWhenUserIdFromSubjectEqualsToSpecifiedAccountId()
      throws Exception {
    permissionsChecker.checkPermissions(userId, null);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp = "User is not authorized to use specified account"
  )
  public void shouldThrowForbiddenExceptionWhenUserIdFromSubjectDoesNotEqualToSpecifiedAccountId()
      throws Exception {
    permissionsChecker.checkPermissions("anotherUserId", null);
  }

  @Test
  public void shouldReturnPersonalAccountType() throws Exception {
    // when
    final String accountType = permissionsChecker.getAccountType();

    // then
    Assert.assertEquals(accountType, UserManager.PERSONAL_ACCOUNT);
  }
}
