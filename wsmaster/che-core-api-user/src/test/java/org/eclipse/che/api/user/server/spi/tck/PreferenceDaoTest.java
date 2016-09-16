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

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link PreferenceDao} contract.
 *
 * @author Anton Korneta
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = PreferenceDaoTest.SUITE_NAME)
public class PreferenceDaoTest {

    public static final String SUITE_NAME = "PreferenceDaoTck";

    private static final int ENTRY_COUNT = 5;

    private List<Pair<String, Map<String, String>>> userPreferences;

    @Inject
    private PreferenceDao                                    preferenceDao;
    @Inject
    private TckRepository<UserImpl>                          userTckRepository;
    @Inject
    private TckRepository<Pair<String, Map<String, String>>> preferenceTckRepository;

    @BeforeMethod
    private void setUp() throws Exception {
        userPreferences = new ArrayList<>(ENTRY_COUNT);
        UserImpl[] users = new UserImpl[ENTRY_COUNT];

        for (int index = 0; index < ENTRY_COUNT; index++) {
            String userId = "userId_" + index;
            users[index] = new UserImpl(userId, "email_" + userId, "name_" + userId, "password", emptyList());

            final Map<String, String> prefs = new HashMap<>();
            prefs.put("preference1", "value");
            prefs.put("preference2", "value");
            prefs.put("preference3", "value");
            userPreferences.add(Pair.of(userId, prefs));
        }
        userTckRepository.createAll(Arrays.asList(users));
        preferenceTckRepository.createAll(userPreferences);
    }

    @AfterMethod
    private void cleanUp() throws Exception {
        preferenceTckRepository.removeAll();
        userTckRepository.removeAll();
    }

    @Test(dependsOnMethods = {"shouldGetPreference", "shouldRemovePreference"})
    public void shouldSetPreference() throws Exception {
        final String userId = userPreferences.get(0).first;
        final Map<String, String> prefs = ImmutableMap.of("key", "value");
        preferenceDao.remove(userId);
        preferenceDao.setPreferences(userId, prefs);

        assertEquals(preferenceDao.getPreferences(userId), prefs);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenSetPreferenceUserNull() throws Exception {
        preferenceDao.setPreferences(null, ImmutableMap.of("key", "value"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenSetPreferenceUpdateNull() throws Exception {
        preferenceDao.setPreferences(userPreferences.get(0).first, null);
    }

    @Test(dependsOnMethods = "shouldGetPreference")
    public void shouldOverridePreference() throws Exception {
        final String userId = userPreferences.get(0).first;
        final Map<String, String> update = ImmutableMap.of("key", "value");
        preferenceDao.setPreferences(userId, update);

        assertEquals(preferenceDao.getPreferences(userId), update);
    }

    @Test(dependsOnMethods = "shouldGetPreference")
    public void shouldUpdatePreference() throws Exception {
        final String userId = userPreferences.get(0).first;
        final Map<String, String> update = userPreferences.get(0).second;
        userPreferences.get(0).second.put("preference4", "value");
        preferenceDao.setPreferences(userId, update);

        assertEquals(preferenceDao.getPreferences(userId), update);
    }

    @Test(dependsOnMethods = "shouldGetPreference")
    public void shouldRemovePreferenceWhenUpdateIsEmpty() throws Exception {
        final String userId = userPreferences.get(0).first;
        final Map<String, String> update = emptyMap();
        preferenceDao.setPreferences(userId, update);

        assertEquals(preferenceDao.getPreferences(userId), update);
    }

    @Test
    public void shouldGetPreference() throws Exception {
        final Pair<String, Map<String, String>> prefs = userPreferences.get(0);

        assertEquals(preferenceDao.getPreferences(prefs.first), prefs.second);
    }

    @Test
    public void shouldGetPreferenceWithFilter() throws Exception {
        final Pair<String, Map<String, String>> prefs = userPreferences.get(0);

        assertEquals(preferenceDao.getPreferences(prefs.first, "\\w*"), prefs.second);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGetPreferenceWithNullFilter() throws Exception {
        preferenceDao.getPreferences(userPreferences.get(0).first, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGetPreferenceWithFilterAndNullUser() throws Exception {
        preferenceDao.getPreferences(null, "\\w*");
    }

    @Test
    public void shouldReturnEmptyPreferenceMapWhenNoMatchedResults() throws Exception {
        assertEquals(preferenceDao.getPreferences(userPreferences.get(0).first, "pattern"), emptyMap());
    }

    @Test(dependsOnMethods = "shouldRemovePreference")
    public void shouldReturnEmptyPreferenceMapWhenNoPreferenceUserFound() throws Exception {
        final String userId = userPreferences.get(0).first;
        preferenceDao.remove(userId);

        assertEquals(preferenceDao.getPreferences(userId, "\\w*"), emptyMap());
    }

    @Test
    public void shouldReturnPreferenceWhenFilterEmpty() throws Exception {
        assertEquals(preferenceDao.getPreferences(userPreferences.get(0).first, ""),
                     userPreferences.get(0).second);
    }

    @Test
    public void shouldReturnFilteredPreferences() throws Exception {
        final String userId = userPreferences.get(0).first;
        final Map.Entry<String, String> preference = userPreferences.get(0).second.entrySet()
                                                                                  .iterator()
                                                                                  .next();

        assertEquals(preferenceDao.getPreferences(userId, preference.getKey()),
                     ImmutableMap.of(preference.getKey(), preference.getValue()));
    }

    @Test(dependsOnMethods = {"shouldGetPreference", "shouldRemovePreferenceWhenUpdateIsEmpty"})
    public void shouldGetEmptyPreferenceMapWhenPreferenceForUserNotFound() throws Exception {
        final String userId = userPreferences.get(0).first;
        preferenceDao.setPreferences(userId, emptyMap());

        assertTrue(preferenceDao.getPreferences(userId).isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGetPreferenceUserNull() throws Exception {
        preferenceDao.getPreferences(null);
    }

    @Test(dependsOnMethods = "shouldGetPreference")
    public void shouldRemovePreference() throws Exception {
        final String userId = userPreferences.get(0).first;
        preferenceDao.remove(userId);

        assertTrue(preferenceDao.getPreferences(userId).isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovePreferenceUserNull() throws Exception {
        preferenceDao.remove(null);
    }
}
