/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static org.eclipse.che.api.devfile.server.TestObjectGenerator.TEST_ACCOUNT;
import static org.eclipse.che.api.devfile.server.TestObjectGenerator.TEST_CHE_NAMESPACE;
import static org.eclipse.che.api.devfile.server.TestObjectGenerator.createUserDevfile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Collections;
import java.util.Optional;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.devfile.shared.event.DevfileCreatedEvent;
import org.eclipse.che.api.devfile.shared.event.DevfileDeletedEvent;
import org.eclipse.che.api.devfile.shared.event.DevfileUpdatedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = MockitoTestNGListener.class)
public class UserDevfileManagerTest {
  @Mock UserDevfileDao userDevfileDao;
  @Mock EventService eventService;
  @Mock AccountManager accountManager;
  @InjectMocks UserDevfileManager userDevfileManager;

  @Captor private ArgumentCaptor<UserDevfileImpl> userDevfileArgumentCaptor;
  @Captor private ArgumentCaptor<DevfileCreatedEvent> devfileCreatedEventCaptor;
  @Captor private ArgumentCaptor<DevfileDeletedEvent> devfileDeletedEventCaptor;
  @Captor private ArgumentCaptor<DevfileUpdatedEvent> devfileUpdatedEventCaptor;

  @BeforeMethod
  public void setUp() throws Exception {
    EnvironmentContext.getCurrent().setSubject(TestObjectGenerator.TEST_SUBJECT);
    lenient().doReturn(TEST_ACCOUNT).when(accountManager).getByName(eq(TEST_CHE_NAMESPACE));
  }

  @Test
  public void shouldGenerateUserDevfileIdOnCreation() throws Exception {
    // given
    final UserDevfileImpl userDevfile =
        new UserDevfileImpl(null, TEST_ACCOUNT, createUserDevfile());
    when(userDevfileDao.create(any(UserDevfileImpl.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    // when
    UserDevfile actual = userDevfileManager.createDevfile(userDevfile);
    // then
    verify(userDevfileDao).create(userDevfileArgumentCaptor.capture());
    assertFalse(isNullOrEmpty(userDevfileArgumentCaptor.getValue().getId()));
    assertEquals(new UserDevfileImpl(null, TEST_ACCOUNT, actual), userDevfile);
  }

  @Test
  public void shouldSendDevfileCreatedEventOnCreation() throws Exception {
    // given
    final UserDevfileImpl userDevfile =
        new UserDevfileImpl(null, TEST_ACCOUNT, createUserDevfile());
    when(userDevfileDao.create(any(UserDevfileImpl.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
    // when
    UserDevfile expected = userDevfileManager.createDevfile(userDevfile);
    // then
    verify(eventService).publish(devfileCreatedEventCaptor.capture());
    assertEquals(expected, devfileCreatedEventCaptor.getValue().getUserDevfile());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingUserDevfileByNullId() throws Exception {
    userDevfileManager.getById(null);
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    // given
    final Optional<UserDevfile> toFetch = Optional.of(createUserDevfile());
    when(userDevfileDao.getById(eq("id123"))).thenReturn(toFetch);

    // when
    final UserDevfile fetched = userDevfileManager.getById("id123");
    // then
    assertEquals(fetched, toFetch.get());
    verify(userDevfileDao).getById("id123");
  }

  @Test(
      expectedExceptions = NotFoundException.class,
      expectedExceptionsMessageRegExp = "Devfile with id 'id123' doesn't exist")
  public void shouldThrowNotFoundExceptionOnGetUserDevfileByIdIfNotFound() throws Exception {
    // given
    doReturn(Optional.empty()).when(userDevfileDao).getById(eq("id123"));
    // when
    userDevfileManager.getById("id123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingUserDevfileByNullId() throws Exception {
    userDevfileManager.updateUserDevfile(null);
  }

  @Test
  public void shouldUpdateUserDevfile() throws Exception {
    // given
    final UserDevfileImpl userDevfile = createUserDevfile();
    when(userDevfileDao.update(any(UserDevfileImpl.class)))
        .thenAnswer(invocationOnMock -> Optional.of(invocationOnMock.getArguments()[0]));
    // when
    userDevfileManager.updateUserDevfile(userDevfile);
    // then
    verify(userDevfileDao).update(eq(userDevfile));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundIfUserDevfileIsNotFoundOnUpdate() throws Exception {
    // given
    final UserDevfileImpl userDevfile = createUserDevfile();
    Mockito.doReturn(Optional.empty()).when(userDevfileDao).update(any(UserDevfileImpl.class));
    // when
    userDevfileManager.updateUserDevfile(userDevfile);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhendeleteUserDevfileByNullId() throws Exception {
    userDevfileManager.removeUserDevfile(null);
  }

  @Test
  public void shouldRemoveUserDevfile() throws Exception {
    // given
    final UserDevfileImpl userDevfile = createUserDevfile();
    // when
    userDevfileManager.removeUserDevfile(userDevfile.getId());
    // then
    verify(userDevfileDao).remove(userDevfile.getId());
  }

  @Test
  public void shouldSendDevfileUpdatedEventOnUpdateDevfile() throws Exception {
    // given
    final UserDevfileImpl userDevfile = createUserDevfile();
    when(userDevfileDao.update(any(UserDevfileImpl.class)))
        .thenAnswer(invocationOnMock -> Optional.of(invocationOnMock.getArguments()[0]));
    // when
    userDevfileManager.updateUserDevfile(userDevfile);
    // then
    verify(eventService).publish(devfileUpdatedEventCaptor.capture());
    assertEquals(userDevfile, devfileUpdatedEventCaptor.getValue().getUserDevfile());
  }

  @Test
  public void shouldBeAbleToGetUserDevfilesAvailableToUser() throws ServerException {
    // given
    final UserDevfileImpl userDevfile = createUserDevfile();
    final UserDevfileImpl userDevfile2 = createUserDevfile();
    when(userDevfileDao.getDevfiles(2, 30, Collections.emptyList(), Collections.emptyList()))
        .thenReturn(new Page<>(asList(userDevfile, userDevfile2), 0, 2, 2));
    // when
    Page<UserDevfile> actual =
        userDevfileManager.getUserDevfiles(2, 30, Collections.emptyList(), Collections.emptyList());
    // then
    assertEquals(actual.getItems().size(), 2);
  }
}
