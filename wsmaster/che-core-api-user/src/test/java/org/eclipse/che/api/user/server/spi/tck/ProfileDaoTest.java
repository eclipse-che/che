/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.spi.tck;

import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ProfileDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(TckListener.class)
@Test(suiteName = ProfileDaoTest.SUITE_NAME)
public class ProfileDaoTest {

  public static final String SUITE_NAME = "ProfileDaoTck";

  private static final int COUNT_OF_PROFILES = 5;

  private ProfileImpl[] profiles;

  @Inject private ProfileDao profileDao;

  @Inject private TckRepository<ProfileImpl> profileTckRepository;
  @Inject private TckRepository<UserImpl> userTckRepository;

  @BeforeMethod
  private void setUp() throws TckRepositoryException {
    UserImpl[] users = new UserImpl[COUNT_OF_PROFILES];
    profiles = new ProfileImpl[COUNT_OF_PROFILES];

    for (int i = 0; i < COUNT_OF_PROFILES; i++) {
      final String userId = NameGenerator.generate("user", Constants.ID_LENGTH);
      users[i] = new UserImpl(userId, userId + "@eclipse.org", userId, "password", emptyList());

      final Map<String, String> attributes = new HashMap<>();
      attributes.put("firstName", "first-name-" + i);
      attributes.put("lastName", "last-name-" + i);
      attributes.put("company", "company-" + i);
      profiles[i] = new ProfileImpl(userId, attributes);
    }
    userTckRepository.createAll(Arrays.asList(users));
    profileTckRepository.createAll(Arrays.asList(profiles));
  }

  @AfterMethod
  private void cleanup() throws TckRepositoryException {
    profileTckRepository.removeAll();
    userTckRepository.removeAll();
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

  @Test(dependsOnMethods = {"shouldGetProfileById", "shouldRemoveProfile"})
  public void shouldCreateProfile() throws Exception {
    final ProfileImpl profile = profiles[0];

    profileDao.remove(profile.getUserId());
    profileDao.create(profile);

    assertEquals(profileDao.getById(profile.getUserId()), profile);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingProfileThatAlreadyExistsForUserWithGivenId()
      throws Exception {
    final ProfileImpl newProfile =
        new ProfileImpl(
            profiles[0].getUserId(),
            ImmutableMap.of(
                "attribute1", "value1",
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

    profileDao.update(
        new ProfileImpl(
            profile.getUserId(),
            ImmutableMap.of(
                "firstName", "new-first-name",
                "lastName", "new-second-name",
                "company", "new-company")));

    final ProfileImpl updated = profileDao.getById(profile.getUserId());
    assertEquals(updated.getUserId(), profile.getUserId());
    assertEquals(
        updated.getAttributes(),
        ImmutableMap.of(
            "firstName", "new-first-name",
            "lastName", "new-second-name",
            "company", "new-company"));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenUpdatingProfileOfNonExistingUser() throws Exception {
    final ProfileImpl profile = profiles[0];

    profileDao.update(new ProfileImpl("non-existing-user-id", profile.getAttributes()));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNull() throws Exception {
    profileDao.update(null);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingProfileById"
  )
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
