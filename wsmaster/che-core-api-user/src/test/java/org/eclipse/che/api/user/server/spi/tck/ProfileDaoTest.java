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
package org.eclipse.che.api.user.server.spi.tck;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link ProfileDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = ProfileDaoTest.SUITE_NAME)
public class ProfileDaoTest {

    public static final String SUITE_NAME = "ProfileDaoTck";

    private static final int COUNT_OF_PROFILES = 5;

    private ProfileImpl[] profiles;

    @Inject
    private ProfileDao profileDao;

    @Inject
    private TckRepository<ProfileImpl> tckRepository;

    @BeforeMethod
    private void setUp() throws TckRepositoryException {
        profiles = new ProfileImpl[COUNT_OF_PROFILES];

        for (int i = 0; i < COUNT_OF_PROFILES; i++) {
            final String userId = NameGenerator.generate("user", Constants.ID_LENGTH);
            final String email = "user-name-" + i + "@eclipse.org";
            final Map<String, String> attributes = new HashMap<>();
            attributes.put("firstName", "first-name-" + i);
            attributes.put("lastName", "last-name-" + i);
            attributes.put("company", "company-" + i);
            profiles[i] = new ProfileImpl(userId, email, attributes);
        }

        tckRepository.createAll(Arrays.asList(profiles));
    }

    @AfterMethod
    private void cleanup() throws TckRepositoryException {
        tckRepository.removeAll();
    }

    @Test
    public void shouldGetProfileById() throws Exception {
        final ProfileImpl profile = profiles[0];

        assertEquals(profileDao.getById(profile.getUserId()), profile);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingProfileById() throws Exception {
        profileDao.getById("non-existing-user-id");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingProfileByNullId() throws Exception {
        profileDao.getById(null);
    }

    @Test(dependsOnMethods = "shouldGetProfileById")
    public void shouldCreateProfile() throws Exception {
        final ProfileImpl newProfile = new ProfileImpl("user123",
                                                       "user123@eclipse.org",
                                                       ImmutableMap.of("attribute1", "value1",
                                                                       "attribute2", "value2",
                                                                       "attribute3", "value3"));

        profileDao.create(newProfile);

        assertEquals(profileDao.getById(newProfile.getUserId()), newProfile);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingProfileThatAlreadyExistsForUserWithGivenId() throws Exception {
        final ProfileImpl newProfile = new ProfileImpl(profiles[0].getUserId(),
                                                       "user123@eclipse.org",
                                                       ImmutableMap.of("attribute1", "value1",
                                                                       "attribute2", "value2",
                                                                       "attribute3", "value3"));

        profileDao.create(newProfile);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenCreatingNull() throws Exception {
        profileDao.create(null);
    }

    @Test(dependsOnMethods = "shouldGetProfileById")
    public void shouldUpdateProfile() throws Exception {
        final ProfileImpl profile = profiles[0];

        profileDao.update(new ProfileImpl(profile.getUserId(),
                                          profile.getEmail(),
                                          ImmutableMap.of("firstName", "new-first-name",
                                                          "lastName", "new-second-name",
                                                          "company", "new-company")));

        final ProfileImpl updated = profileDao.getById(profile.getUserId());
        assertEquals(updated.getUserId(), profile.getUserId());
        assertEquals(updated.getEmail(), profile.getEmail());
        assertEquals(updated.getAttributes(), ImmutableMap.of("firstName", "new-first-name",
                                                              "lastName", "new-second-name",
                                                              "company", "new-company"));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingProfileOfNonExistingUser() throws Exception {
        final ProfileImpl profile = profiles[0];

        profileDao.update(new ProfileImpl("non-existing-user-id", profile.getEmail(), profile.getAttributes()));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingNull() throws Exception {
        profileDao.update(null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingProfileById")
    public void shouldRemoveProfile() throws Exception {
        final ProfileImpl profile = profiles[0];

        profileDao.remove(profile.getUserId());
        profileDao.getById(profile.getUserId());
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenRemovingNonExistingUser() throws Exception {
        profileDao.remove("non-existing-id");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        profileDao.remove(null);
    }
}
