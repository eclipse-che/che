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
package com.codenvy.api.account.personal;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.account.personal.PersonalAccountPermissionsChecker;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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
    when(subject.getUserId()).thenReturn(userId);
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
    //when
    final String accountType = permissionsChecker.getAccountType();

    //then
    assertEquals(accountType, UserManager.PERSONAL_ACCOUNT);
  }
}
