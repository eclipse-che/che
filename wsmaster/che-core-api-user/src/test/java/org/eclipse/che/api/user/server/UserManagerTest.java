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
package org.eclipse.che.api.user.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.event.UserCreatedEvent;
import org.eclipse.che.api.user.server.event.UserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link UserManager}.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserManagerTest {

  @Mock private UserDao userDao;
  @Mock private ProfileDao profileDao;
  @Mock private PreferenceDao preferencesDao;
  @Mock private EventService eventService;
  @Mock private PostUserPersistedEvent postUserPersistedEvent;
  @Mock private BeforeUserRemovedEvent beforeUserRemovedEvent;

  private UserManager manager;

  @BeforeMethod
  public void setUp() {
    initMocks(this);
    manager =
        new UserManager(
            userDao, profileDao, preferencesDao, eventService, new String[] {"reserved"});

    when(eventService.publish(any()))
        .thenAnswer(
            invocationOnMock -> {
              Object arg = invocationOnMock.getArguments()[0];
              if (arg instanceof BeforeUserRemovedEvent) {
                return beforeUserRemovedEvent;
              } else {
                return postUserPersistedEvent;
              }
            });
  }

  @Test
  public void shouldCreateAccountAndProfileAndPreferencesOnUserCreation() throws Exception {
    final UserImpl user = new UserImpl(null, "test@email.com", "testName", null, null);

    manager.create(user, false);

    verify(userDao).create(any(UserImpl.class));
    verify(profileDao).create(any(ProfileImpl.class));
    verify(preferencesDao).setPreferences(anyString(), anyMapOf(String.class, String.class));
  }

  @Test
  public void shouldGeneratePasswordWhenCreatingUserAndItIsMissing() throws Exception {
    final User user = new UserImpl(null, "test@email.com", "testName", null, null);

    manager.create(user, false);

    final ArgumentCaptor<UserImpl> userCaptor = ArgumentCaptor.forClass(UserImpl.class);
    verify(userDao).create(userCaptor.capture());
    assertNotNull(userCaptor.getValue().getPassword());
  }

  @Test
  public void shouldGenerateIdentifierWhenCreatingUserWithNullId() throws Exception {
    final User user = new UserImpl(null, "test@email.com", "testName", null, null);

    manager.create(user, false);

    final ArgumentCaptor<UserImpl> userCaptor = ArgumentCaptor.forClass(UserImpl.class);
    verify(userDao).create(userCaptor.capture());
    final String id = userCaptor.getValue().getId();
    assertNotNull(id);
  }

  @Test
  public void shouldNotGenerateIdentifierWhenCreatingUserWithNotNullId() throws Exception {
    final User user = new UserImpl("identifier", "test@email.com", "testName", null, null);

    manager.create(user, false);

    final ArgumentCaptor<UserImpl> userCaptor = ArgumentCaptor.forClass(UserImpl.class);
    verify(userDao).create(userCaptor.capture());
    final String id = userCaptor.getValue().getId();
    assertNotNull(id);
    assertEquals(id, "identifier");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingUserWithNullEntity() throws Exception {
    manager.update(null);
  }

  @Test
  public void shouldUpdateUser() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier", "test@email.com", "testName", "password", Collections.emptyList());
    when(manager.getById(user.getId())).thenReturn(user);
    UserImpl user2 = new UserImpl(user);
    user2.setName("testName2");
    manager.update(user2);

    verify(userDao).update(new UserImpl(user2));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrownNpeWhenTryingToGetUserByNullId() throws Exception {
    manager.getById(null);
  }

  @Test
  public void shouldGetUserById() throws Exception {
    final User user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(manager.getById(user.getId())).thenReturn(user);

    assertEquals(manager.getById(user.getId()), user);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrownNpeWhenTryingToGetUserByNullAlias() throws Exception {
    manager.getByAlias(null);
  }

  @Test
  public void shouldGetUserByAlias() throws Exception {
    final User user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(manager.getByAlias("alias")).thenReturn(user);

    assertEquals(manager.getByAlias("alias"), user);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrownNpeWhenTryingToGetUserByNullName() throws Exception {
    manager.getByName(null);
  }

  @Test
  public void shouldGetUserByName() throws Exception {
    final User user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(manager.getByName(user.getName())).thenReturn(user);

    assertEquals(manager.getByName(user.getName()), user);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrownNpeWhenTryingToGetUserWithNullEmail() throws Exception {
    manager.getByEmail(null);
  }

  @Test
  public void shouldGetUserByEmail() throws Exception {
    final User user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(manager.getByEmail(user.getEmail())).thenReturn(user);

    assertEquals(manager.getByEmail(user.getEmail()), user);
  }

  @Test
  public void shouldGetTotalUserCount() throws Exception {
    when(userDao.getTotalCount()).thenReturn(5L);

    assertEquals(manager.getTotalCount(), 5);
    verify(userDao).getTotalCount();
  }

  @Test
  public void shouldGetAllUsers() throws Exception {
    final Page users =
        new Page(
            Arrays.asList(
                new UserImpl(
                    "identifier1",
                    "test1@email.com",
                    "testName1",
                    "password",
                    Collections.singletonList("alias1")),
                new UserImpl(
                    "identifier2",
                    "test2@email.com",
                    "testName2",
                    "password",
                    Collections.singletonList("alias2")),
                new UserImpl(
                    "identifier3",
                    "test3@email.com",
                    "testName3",
                    "password",
                    Collections.singletonList("alias3"))),
            0,
            30,
            3);
    when(userDao.getAll(30, 0)).thenReturn(users);

    assertEquals(manager.getAll(30, 0), users);
    verify(userDao).getAll(30, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionsWhenGetAllUsersWithNegativeMaxItems()
      throws Exception {
    manager.getAll(-5, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionsWhenGetAllUsersWithNegativeSkipCount()
      throws Exception {
    manager.getAll(30, -11);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingUserByNullId() throws Exception {
    manager.remove(null);
  }

  @Test
  public void shouldRemoveUser() throws Exception {
    manager.remove("user123");

    verify(userDao).remove("user123");
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnCreationIfUserNameIsReserved() throws Exception {
    final User user = new UserImpl("id", "test@email.com", "reserved");

    manager.create(user, false);
  }

  @Test
  public void shouldFireBeforeUserRemovedEventOnRemoveExistedUser() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(userDao.getById(user.getId())).thenReturn(user);

    manager.remove(user.getId());

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(2)).publish(firedEvents.capture());

    // the first event - BeforeUserRemovedEvent
    // the second event - UserRemovedEvent
    Object event = firedEvents.getAllValues().get(0);
    assertTrue(event instanceof BeforeUserRemovedEvent, "Not a BeforeUserRemovedEvent");
    assertEquals(((BeforeUserRemovedEvent) event).getUser(), user);
  }

  @Test
  public void shouldFireUserRemovedEventOnRemoveExistedUser() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(userDao.getById(user.getId())).thenReturn(user);

    manager.remove(user.getId());

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(2)).publish(firedEvents.capture());

    // the first event - BeforeUserRemovedEvent
    // the second event - UserRemovedEvent
    Object event = firedEvents.getAllValues().get(1);
    assertTrue(event instanceof UserRemovedEvent, "Not a UserRemovedEvent");
    assertEquals(((UserRemovedEvent) event).getUserId(), user.getId());
  }

  @Test
  public void shouldNotRemoveUserWhenSubscriberThrowsExceptionOnRemoveExistedUser()
      throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    when(userDao.getById(user.getId())).thenReturn(user);
    doThrow(new ServerException("error")).when(beforeUserRemovedEvent).propagateException();

    try {
      manager.remove(user.getId());
      fail("ServerException expected.");
    } catch (ServerException ignored) {
    }

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(1)).publish(firedEvents.capture());

    assertTrue(
        firedEvents.getValue() instanceof BeforeUserRemovedEvent, "Not a BeforeUserRemovedEvent");
  }

  @Test
  public void shouldFirePostUserPersistedEventNewUserCreatedAndBeforeCommit() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));

    manager.create(user, false);

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(2)).publish(firedEvents.capture());

    // the first event - PostUserPersistedEvent
    // the second event - UserCreatedEvent
    Object event = firedEvents.getAllValues().get(0);
    assertTrue(event instanceof PostUserPersistedEvent, "Not a PostUserPersistedEvent");
    assertEquals(((PostUserPersistedEvent) event).getUser(), user);
  }

  @Test
  public void shouldFireUserCreatedEventOnNewUserCreated() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));

    manager.create(user, false);

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(2)).publish(firedEvents.capture());

    // the first event - PostUserPersistedEvent
    // the second event - UserCreatedEvent
    Object event = firedEvents.getAllValues().get(1);
    assertTrue(event instanceof UserCreatedEvent, "Not a UserCreatedEvent");
    assertEquals(((UserCreatedEvent) event).getUser(), user);
  }

  @Test
  public void shouldNotCreteUserWhenSubscriberThrowsExceptionOnCreatingNewUser() throws Exception {
    final UserImpl user =
        new UserImpl(
            "identifier",
            "test@email.com",
            "testName",
            "password",
            Collections.singletonList("alias"));
    doThrow(new ServerException("error")).when(postUserPersistedEvent).propagateException();

    try {
      manager.create(user, false);
      fail("ServerException expected.");
    } catch (ServerException ignored) {
    }

    ArgumentCaptor<Object> firedEvents = ArgumentCaptor.forClass(Object.class);
    verify(eventService, times(1)).publish(firedEvents.capture());

    assertTrue(
        firedEvents.getValue() instanceof PostUserPersistedEvent, "Not a PostUserPersistedEvent");
  }
}
