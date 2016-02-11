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
package org.eclipse.che.api.local;


import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * @author Eugene Voevodin
 * @author Dmitry Shnurenko
 * @author Anton Korneta
 * @author Valeriy Svydenko
 */
@Singleton
public class LocalPreferenceDaoImpl implements PreferenceDao {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPreferenceDaoImpl.class);

    private final Map<String, Map<String, String>> preferences;
    private final ReadWriteLock                    lock;
    private final LocalStorage                     preferenceStorage;

    @Inject
    public LocalPreferenceDaoImpl(LocalStorageFactory localStorageFactory) throws IOException {
        preferences = new HashMap<>();
        lock = new ReentrantReadWriteLock();
        preferenceStorage = localStorageFactory.create("preferences.json");
    }

    @PostConstruct
    private void start() {
        preferences.putAll(preferenceStorage.loadMap(new TypeToken<Map<String, Map<String, String>>>() {}));
        // Add default entry if file doesn't exist or invalid or empty.
        if (preferences.isEmpty()) {
            final Map<String, String> newPreferences = new HashMap<>(4);
            newPreferences.put("preference1", "value");
            newPreferences.put("preference2", "value");
            preferences.put("codenvy", newPreferences);
        }
    }

    @PreDestroy
    private void stop() throws IOException {
        preferenceStorage.store(preferences);
    }

    @Override
    public void setPreferences(String userId, Map<String, String> prefs) throws ServerException, NotFoundException {
        lock.writeLock().lock();
        try {
            preferences.put(userId, new HashMap<>(prefs));
            preferenceStorage.store(preferences);
        } catch (IOException e) {
            LOG.warn("Impossible to store preferences");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId) throws ServerException {
        lock.readLock().lock();
        try {
            //Need read all new preferences without restarting dev-machine. It is needed for  IDEX-2180
            preferences.putAll(preferenceStorage.loadMap(new TypeToken<Map<String, Map<String, String>>>() {}));
            final Map<String, String> prefs = new HashMap<>();
            if (preferences.containsKey(userId)) {
                prefs.putAll(preferences.get(userId));
            }
            return prefs;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId, String filter) throws ServerException {
        lock.readLock().lock();
        try {
            return filter(getPreferences(userId), filter);
        } finally {
            lock.readLock().unlock();
        }
    }

    private Map<String, String> filter(Map<String, String> prefs, String filter) {
        final Map<String, String> filtered = new HashMap<>();
        final Pattern pattern = Pattern.compile(filter);
        for (Map.Entry<String, String> entry : prefs.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches()) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    @Override
    public void remove(String userId) throws ServerException {
        lock.writeLock().lock();
        try {
            preferences.remove(userId);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
