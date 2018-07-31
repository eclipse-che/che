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
package org.eclipse.che.api.user.server;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ProfileManager}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class ProfileManagerTest {

  @Mock private ProfileDao profileDao;

  @InjectMocks private ProfileManager profileManager;

  @Test
  public void shouldGetProfileById() throws Exception {
    final ProfileImpl profile = new ProfileImpl("user123");
    when(profileDao.getById(profile.getUserId())).thenReturn(profile);

    assertEquals(profile, profileManager.getById(profile.getUserId()));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingProfileByNullId() throws Exception {
    profileManager.getById(null);
  }

  @Test
  public void shouldCreateProfile() throws Exception {
    final ProfileImpl profile = new ProfileImpl("user123");

    profileManager.create(profile);

    final ArgumentCaptor<ProfileImpl> captor = ArgumentCaptor.forClass(ProfileImpl.class);
    verify(profileDao).create(captor.capture());
    assertEquals(captor.getValue(), profile);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenCreatingNullProfile() throws Exception {
    profileManager.create(null);
  }

  @Test
  public void shouldUpdateProfile() throws Exception {
    final ProfileImpl profile = new ProfileImpl("user123");

    profileManager.update(profile);

    final ArgumentCaptor<ProfileImpl> captor = ArgumentCaptor.forClass(ProfileImpl.class);
    verify(profileDao).update(captor.capture());
    assertEquals(captor.getValue(), profile);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNull() throws Exception {
    profileManager.update(null);
  }

  @Test
  public void shouldRemoveProfile() throws Exception {
    profileManager.remove("user123");

    verify(profileDao).remove("user123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingNull() throws Exception {
    profileManager.remove(null);
  }
}
