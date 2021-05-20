/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.authentication.commons;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import javax.servlet.http.HttpSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SessionStoreTest {

  private final String userId = "userId";

  private SessionStore sessionStore;

  @BeforeMethod
  public void setUp() {
    this.sessionStore = new SessionStore();
  }

  @Test
  public void shouldCreateSessionOnlyIfAbsent() {
    HttpSession sessionMock = mock(HttpSession.class);
    HttpSession result1 = sessionStore.getSession(userId, s -> sessionMock);
    assertEquals(result1, sessionMock);
    HttpSession result2 = sessionStore.getSession(userId, s -> null);
    assertEquals(result2, sessionMock);
  }

  @Test
  public void shouldGetSessionById() {
    HttpSession sessionMock = mock(HttpSession.class);
    sessionStore.getSession(userId, s -> sessionMock);
    HttpSession result = sessionStore.getSession(userId);
    assertEquals(result, sessionMock);
  }

  @Test
  public void shouldRemoveSessionById() {
    HttpSession sessionMock = mock(HttpSession.class);
    sessionStore.getSession(userId, s -> sessionMock);
    sessionStore.remove(userId);
    HttpSession result = sessionStore.getSession(userId);
    assertNull(result);
  }
}
