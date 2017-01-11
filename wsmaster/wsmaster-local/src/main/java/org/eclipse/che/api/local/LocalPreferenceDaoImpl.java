/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * In-memory implementation of {@link PreferenceDao}.
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Yevhenii Voevodin
 * @author Dmitry Shnurenko
 * @author Anton Korneta
 * @author Valeriy Svydenko
 */
@Singleton
public class LocalPreferenceDaoImpl implements PreferenceDao {

    public static final String FILENAME = "preferences.json";

    private static final Logger LOG = LoggerFactory.getLogger(LocalPreferenceDaoImpl.class);

    private final LocalStorage preferenceStorage;

    @VisibleForTesting
    final Map<String, Map<String, String>> preferences;

    @Inject
    public LocalPreferenceDaoImpl(LocalStorageFactory localStorageFactory) throws IOException {
        preferences = new HashMap<>();
        preferenceStorage = localStorageFactory.create("preferences.json");
    }

    @PostConstruct
    private synchronized void start() {
        preferences.putAll(preferenceStorage.loadMap(new TypeToken<Map<String, Map<String, String>>>() {}));
        // Add default entry if file doesn't exist or invalid or empty.
        if (preferences.isEmpty()) {
            final Map<String, String> newPreferences = new HashMap<>(4);
            newPreferences.put("preference1", "value");
            newPreferences.put("preference2", "value");
            preferences.put("codenvy", newPreferences);
        }
    }

    public synchronized void savePreferences() throws IOException {
        preferenceStorage.store(preferences);
    }

    @Override
    public synchronized void setPreferences(String userId, Map<String, String> prefs) throws ServerException {
        requireNonNull(userId);
        requireNonNull(prefs);
        try {
            preferences.put(userId, new HashMap<>(prefs));
            preferenceStorage.store(preferences);
        } catch (IOException e) {
            LOG.warn("Impossible to store preferences");
        }
    }

    @Override
    public synchronized Map<String, String> getPreferences(String userId) throws ServerException {
        requireNonNull(userId);
        //Need read all new preferences without restarting dev-machine. It is needed for  IDEX-2180
        preferences.putAll(preferenceStorage.loadMap(new TypeToken<Map<String, Map<String, String>>>() {}));
        final Map<String, String> prefs = new HashMap<>();
        if (preferences.containsKey(userId)) {
            prefs.putAll(preferences.get(userId));
        }
        return prefs;
    }

    @Override
    public synchronized Map<String, String> getPreferences(String userId, String filter) throws ServerException {
        requireNonNull(userId);
        requireNonNull(filter);
        return filter(getPreferences(userId), filter);
    }

    @Override
    public synchronized void remove(String userId) throws ServerException {
        requireNonNull(userId);
        preferences.remove(userId);
    }

    private Map<String, String> filter(Map<String, String> prefs, String filter) {
        final Map<String, String> filtered = new HashMap<>();
        final Pattern pattern = Pattern.compile(filter);
        if (filter.isEmpty()) {
            return prefs;
        }
        for (Map.Entry<String, String> entry : prefs.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches()) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }
}
