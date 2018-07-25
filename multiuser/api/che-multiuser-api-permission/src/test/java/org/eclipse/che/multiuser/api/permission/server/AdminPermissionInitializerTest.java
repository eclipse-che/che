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
package org.eclipse.che.multiuser.api.permission.server;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AdminPermissionInitializer}.
 *
 * @author Anton Korneta
 * @author Sergii Kabashniuk
 */
@Listeners(MockitoTestNGListener.class)
public class AdminPermissionInitializerTest {

  private static final String NAME = "admin";
  private static final String PASSWORD = "root";
  private static final String EMAIL = "admin@rb.com";

  @Mock private PermissionsManager permissionsManager;
  @Mock private UserManager userManager;
  @Mock private EventService eventService;

  private UserImpl user;
  private UserImpl adminUser;
  private AdminPermissionInitializer initializer;

  @BeforeMethod
  public void setUp() throws Exception {
    user = new UserImpl("qwe", "qwe", "qwe", "qwe", emptyList());
    adminUser = new UserImpl("id-admin", EMAIL, NAME, PASSWORD, emptyList());

    doNothing().when(permissionsManager).storePermission(any(SystemPermissionsImpl.class));
    doReturn(new SystemDomain(Collections.emptySet()))
        .when(permissionsManager)
        .getDomain(anyString());
    initializer =
        new AdminPermissionInitializer(NAME, userManager, permissionsManager, eventService);
  }

  @Test
  public void shouldAddSystemPermissionsOnPostUserPersistedEvent() throws Exception {
    // given
    when(userManager.getByName(eq(NAME))).thenReturn(user);
    initializer.init();
    // when
    initializer.onEvent(
        new PostUserPersistedEvent(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList())));
    // then
    verify(permissionsManager)
        .storePermission(
            argThat(
                (ArgumentMatcher<SystemPermissionsImpl>)
                    argument -> argument.getUserId().equals(NAME)));
  }

  @Test
  public void shouldNotAddSystemPermissionsOnPostUserPersistedEvent() throws Exception {
    // given
    when(userManager.getByName(anyString())).thenThrow(NotFoundException.class);
    initializer.init();
    // when
    initializer.onEvent(
        new PostUserPersistedEvent(
            new UserImpl(NAME + "1", EMAIL + "2", NAME + "3", PASSWORD, emptyList())));
    // then
    verifyNoMoreInteractions(permissionsManager);
  }

  @Test
  public void shouldAddSystemPermissionsForExistedAdmin() throws Exception {
    // given
    when(userManager.getByName(eq(NAME))).thenReturn(adminUser);
    // when
    initializer.init();
    // then
    verify(permissionsManager)
        .storePermission(
            argThat(
                (ArgumentMatcher<SystemPermissionsImpl>)
                    argument ->
                        ((SystemPermissionsImpl) argument).getUserId().equals(adminUser.getId())));
  }

  @Test
  public void shouldNotAddSystemPermissionsIfAdminNotExists() throws Exception {
    // given
    when(userManager.getByName(anyString())).thenThrow(NotFoundException.class);
    // when
    initializer.init();
    // then
    verifyNoMoreInteractions(permissionsManager);
  }
}
