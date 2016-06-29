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
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link UserManager}
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */

@Listeners(value = {MockitoTestNGListener.class})
public class UserManagerTest {

    @Mock
    UserDao userDao;
    @Mock
    UserProfileDao profileDao;
    @Mock
    PreferenceDao  preferenceDao;

    UserManager manager;

    @BeforeMethod
    public void setUp() {
        manager = new UserManager(userDao, profileDao, preferenceDao, new String[0]);
    }

    @Test
    public void shouldCreateProfileAndPreferencesOnUserCreation() throws Exception {
        final User user = new User().withEmail("test@email.com").withName("testName");

        manager.create(user, false);

        verify(profileDao).create(any(Profile.class));
        verify(preferenceDao).setPreferences(anyString(), anyMapOf(String.class, String.class));
    }

    @Test
    public void shouldGeneratedPasswordWhenCreatingUserAndItIsMissing() throws Exception {
        final User user = new User().withEmail("test@email.com").withName("testName");

        manager.create(user, false);

        verify(userDao).create(eq(user.withPassword("<none>")));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnCreationIfUserNameIsReserved() throws Exception {
        final User user = new User().withEmail("test@email.com").withName("reserved");

        new UserManager(userDao, profileDao, preferenceDao, new String[] {"reserved"}).create(user, false);
    }
}
