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
package org.eclipse.che.multiuser.api.permission.server;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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

    final AbstractPermissionsDomain mock = mock(AbstractPermissionsDomain.class);
    doNothing().when(permissionsManager).storePermission(any(SystemPermissionsImpl.class));
    when(permissionsManager.getDomain(anyString())).thenReturn(cast(mock));
    when(mock.getAllowedActions()).thenReturn(emptyList());
    when(mock.newInstance(anyString(), anyString(), anyListOf(String.class)))
        .then(
            invocation ->
                new SystemPermissionsImpl(
                    (String) invocation.getArguments()[0],
                    (List<String>) invocation.getArguments()[2]));
    initializer =
        new AdminPermissionInitializer(NAME, userManager, permissionsManager, eventService);
  }

  @SuppressWarnings("unchecked")
  private static <R, T extends R> T cast(R qwe) {
    return (T) qwe;
  }

  @Test
  public void shouldAddSystemPermissionsOnPostUserPersistedEvent() throws Exception {

    when(userManager.getByName(eq(NAME))).thenReturn(user);
    initializer.init();
    initializer.onEvent(
        new PostUserPersistedEvent(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList())));
    verify(permissionsManager)
        .storePermission(
            argThat(
                new ArgumentMatcher<SystemPermissionsImpl>() {
                  @Override
                  public boolean matches(Object argument) {
                    return ((SystemPermissionsImpl) argument).getUserId().equals(NAME);
                  }
                }));
  }

  @Test
  public void shouldNotAddSystemPermissionsOnPostUserPersistedEvent() throws Exception {
    when(userManager.getByName(anyString())).thenThrow(NotFoundException.class);
    initializer.init();
    initializer.onEvent(
        new PostUserPersistedEvent(
            new UserImpl(NAME + "1", EMAIL + "2", NAME + "3", PASSWORD, emptyList())));
    verifyNoMoreInteractions(permissionsManager);
  }

  @Test
  public void shouldAddSystemPermissionsForExistedAdmin() throws Exception {
    when(userManager.getByName(eq(NAME))).thenReturn(adminUser);
    initializer.init();
    verify(permissionsManager)
        .storePermission(
            argThat(
                new ArgumentMatcher<SystemPermissionsImpl>() {
                  @Override
                  public boolean matches(Object argument) {
                    return ((SystemPermissionsImpl) argument).getUserId().equals(adminUser.getId());
                  }
                }));
  }

  @Test
  public void shouldNotAddSystemPermissionsIfAdminNotExists() throws Exception {
    when(userManager.getByName(anyString())).thenThrow(NotFoundException.class);
    initializer.init();

    verifyNoMoreInteractions(permissionsManager);
  }
}
