/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Tests for {@link UserManager}.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserManagerTest {

    @Mock
    private UserDao       userDao;
    @Mock
    private ProfileDao    profileDao;
    @Mock
    private PreferenceDao preferencesDao;

    private UserManager manager;

    @BeforeMethod
    public void setUp() {
        initMocks(this);

        manager = new UserManager(userDao, profileDao, preferencesDao, new String[] {"reserved"});
    }

    @Test
    public void shouldCreateProfileAndPreferencesOnUserCreation() throws Exception {
        final UserImpl user = new UserImpl(null, "test@email.com", "testName", null, null);

        manager.create(user, false);

        verify(userDao).create(any(UserImpl.class));
        verify(profileDao).create(any(ProfileImpl.class));
        verify(preferencesDao).setPreferences(anyString(), anyMapOf(String.class, String.class));
    }

    @Test(dataProvider = "rollback")
    public void shouldTryToRollbackWhenEntityCreationFailed(Callable preAction) throws Exception {
        preAction.call();

        // Creating new user
        try {
            manager.create(new UserImpl(null, "test@email.com", "testName", null, null), false);
            fail("Had to throw Exception");
        } catch (Exception x) {
            // defined by userDao mock
        }

        // Capturing identifier
        final ArgumentCaptor<UserImpl> captor = ArgumentCaptor.forClass(UserImpl.class);
        verify(userDao).create(captor.capture());
        final String userId = captor.getValue().getId();

        // Verifying rollback
        verify(userDao).remove(userId);
        verify(preferencesDao).remove(userId);
        verify(profileDao).remove(userId);
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
        final User user = new UserImpl("identifier", "test@email.com", "testName", "password", Collections.emptyList());

        manager.update(user);

        verify(userDao).update(new UserImpl(user));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrownNpeWhenTryingToGetUserByNullId() throws Exception {
        manager.getById(null);
    }

    @Test
    public void shouldGetUserById() throws Exception {
        final User user = new UserImpl("identifier", "test@email.com", "testName", "password", Collections.singletonList("alias"));
        when(manager.getById(user.getId())).thenReturn(user);

        assertEquals(manager.getById(user.getId()), user);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrownNpeWhenTryingToGetUserByNullAlias() throws Exception {
        manager.getByAlias(null);
    }

    @Test
    public void shouldGetUserByAlias() throws Exception {
        final User user = new UserImpl("identifier", "test@email.com", "testName", "password", Collections.singletonList("alias"));
        when(manager.getByAlias("alias")).thenReturn(user);

        assertEquals(manager.getByAlias("alias"), user);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrownNpeWhenTryingToGetUserByNullName() throws Exception {
        manager.getByName(null);
    }

    @Test
    public void shouldGetUserByName() throws Exception {
        final User user = new UserImpl("identifier", "test@email.com", "testName", "password", Collections.singletonList("alias"));
        when(manager.getByName(user.getName())).thenReturn(user);

        assertEquals(manager.getByName(user.getName()), user);
    }


    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrownNpeWhenTryingToGetUserWithNullEmail() throws Exception {
        manager.getByEmail(null);
    }

    @Test
    public void shouldGetUserByEmail() throws Exception {
        final User user = new UserImpl("identifier", "test@email.com", "testName", "password", Collections.singletonList("alias"));
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
        final Page users = new Page(Arrays.asList(
                new UserImpl("identifier1", "test1@email.com", "testName1", "password", Collections.singletonList("alias1")),
                new UserImpl("identifier2", "test2@email.com", "testName2", "password", Collections.singletonList("alias2")),
                new UserImpl("identifier3", "test3@email.com", "testName3", "password", Collections.singletonList("alias3"))), 0, 30, 3);
        when(userDao.getAll(30, 0)).thenReturn(users);

        assertEquals(manager.getAll(30, 0), users);
        verify(userDao).getAll(30, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionsWhenGetAllUsersWithNegativeMaxItems() throws Exception {
        manager.getAll(-5, 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionsWhenGetAllUsersWithNegativeSkipCount() throws Exception {
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

    @DataProvider(name = "rollback")
    public Object[][] rollbackTestPreActions() throws Exception {
        return new Callable[][] {

                // User creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(userDao)
                            .create(any());
                    return null;
                }},

                // Preferences creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(preferencesDao)
                            .setPreferences(anyString(), any());
                    return null;
                }},

                // Profile creation mocking
                {() -> {
                    doThrow(new ServerException("error"))
                            .when(profileDao)
                            .create(any());
                    return null;
                }}
        };
    }
}
