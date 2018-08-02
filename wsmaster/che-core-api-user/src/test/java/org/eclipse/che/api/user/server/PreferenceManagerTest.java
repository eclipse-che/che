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

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link PreferenceManager}.
 *
 * @author Yevhenii Voevodin.
 */
@Listeners(MockitoTestNGListener.class)
public class PreferenceManagerTest {

  @Mock private PreferenceDao preferenceDao;

  @InjectMocks private PreferenceManager preferenceManager;

  @Captor private ArgumentCaptor<Map<String, String>> preferencesCaptor;

  @Test
  public void shouldUseMergeStrategyForPreferencesUpdate() throws Exception {
    // Preparing preferences
    final Map<String, String> existingPreferences = new HashMap<>();
    existingPreferences.put("pKey1", "pValue1");
    existingPreferences.put("pKey2", "pValue2");
    existingPreferences.put("pKey3", "pValue3");
    existingPreferences.put("pKey4", "pValue4");
    when(preferenceDao.getPreferences(any())).thenReturn(existingPreferences);

    // Updating preferences
    final Map<String, String> newPreferences = new HashMap<>();
    newPreferences.put("pKey5", "pValue5");
    newPreferences.put("pKey1", "new-value");
    preferenceManager.update("user123", newPreferences);

    // Checking
    verify(preferenceDao).setPreferences(anyString(), preferencesCaptor.capture());
    assertEquals(
        preferencesCaptor.getValue(),
        ImmutableMap.of(
            "pKey1", "new-value",
            "pKey2", "pValue2",
            "pKey3", "pValue3",
            "pKey4", "pValue4",
            "pKey5", "pValue5"));
  }

  @Test
  public void shouldRemoveSpecifiedPreferences() throws Exception {
    // Preparing preferences
    final Map<String, String> existingPreferences = new HashMap<>();
    existingPreferences.put("pKey1", "pValue1");
    existingPreferences.put("pKey2", "pValue2");
    existingPreferences.put("pKey3", "pValue3");
    existingPreferences.put("pKey4", "pValue4");
    when(preferenceDao.getPreferences(any())).thenReturn(existingPreferences);

    // Removing
    preferenceManager.remove("user123", asList("pKey1", "pKey5", "odd-pref-name"));

    // Checking
    verify(preferenceDao).setPreferences(anyString(), preferencesCaptor.capture());
    assertEquals(
        preferencesCaptor.getValue(),
        ImmutableMap.of(
            "pKey2", "pValue2",
            "pKey3", "pValue3",
            "pKey4", "pValue4"));
  }

  @Test
  public void shouldGetPreferencesByUser() throws Exception {
    final Map<String, String> preferences = ImmutableMap.of("name", "value");
    when(preferenceDao.getPreferences("user123")).thenReturn(preferences);

    assertEquals(preferenceManager.find("user123"), preferences);
  }

  @Test
  public void shouldGetPreferencesByUserAndFilter() throws Exception {
    final Map<String, String> preferences = ImmutableMap.of("name", "value");
    when(preferenceDao.getPreferences("user123", "name.*")).thenReturn(preferences);

    assertEquals(preferenceManager.find("user123", "name.*"), preferences);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void getPreferencesShouldThrowNpeWhenUserIdIsNull() throws Exception {
    preferenceManager.find(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void getPreferencesByUserAndFilterShouldThrowNpeWhenUserIdIsNull() throws Exception {
    preferenceManager.find(null, "name.*");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingPreferencesAndUserIdIsNull() throws Exception {
    preferenceManager.remove(null);
  }

  @Test
  public void shouldRemoveUserPreferences() throws Exception {
    preferenceManager.remove("user123");

    verify(preferenceDao).remove("user123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenSavePreferencesWithNullUser() throws Exception {
    preferenceManager.save(null, Collections.emptyMap());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenSavePreferencesWithNullPreferences() throws Exception {
    preferenceManager.save("user123", null);
  }

  @Test
  public void shouldSavePreferences() throws Exception {
    preferenceManager.save("user123", Collections.emptyMap());

    verify(preferenceDao).setPreferences("user123", Collections.emptyMap());
  }
}
