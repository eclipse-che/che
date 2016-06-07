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
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anton Korneta
 */
@Singleton
public class LocalProfileDaoImpl implements ProfileDao {

    private final Map<String, ProfileImpl> profiles;
    private final ReadWriteLock            lock;
    private final LocalStorage             profileStorage;

    @Inject
    public LocalProfileDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        profiles = new LinkedHashMap<>();
        lock = new ReentrantReadWriteLock();
        profileStorage = storageFactory.create("profiles.json");
    }

    @PostConstruct
    private void start() {
        profiles.putAll(profileStorage.loadMap(new TypeToken<Map<String, ProfileImpl>>() {}));
        // Add default entry if file doesn't exist or invalid or empty.
        if (profiles.isEmpty()) {
            final Map<String, String> attributes = new HashMap<>(2);
            attributes.put("firstName", "Che");
            attributes.put("lastName", "Codenvy");

            ProfileImpl profile = new ProfileImpl("che", "che@eclipse.org", attributes);
            profiles.put(profile.getUserId(), profile);
        }
    }

    @PreDestroy
    private void stop() throws IOException {
        profileStorage.store(profiles);
    }

    @Override
    public void create(ProfileImpl profile) {
        lock.writeLock().lock();
        try {
            // just replace existed profile
            profiles.put(profile.getUserId(), new ProfileImpl(profile));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(ProfileImpl profile) throws NotFoundException {
        lock.writeLock().lock();
        try {
            final Profile myProfile = profiles.get(profile.getUserId());
            if (myProfile == null) {
                throw new NotFoundException(String.format("Profile with id '%s' not found", profile.getUserId()));
            }
            myProfile.getAttributes().clear();
            myProfile.getAttributes().putAll(profile.getAttributes());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(String id) {
        lock.writeLock().lock();
        try {
           profiles.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ProfileImpl getById(String id) throws NotFoundException {
        lock.readLock().lock();
        try {
            final Profile profile = profiles.get(id);
            if (profile == null) {
                throw new NotFoundException(String.format("Profile with id '%s' not found", id));
            }
            return new ProfileImpl(profile);
        } finally {
            lock.readLock().unlock();
        }
    }
}
