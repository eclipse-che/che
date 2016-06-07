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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Anton Korneta
 */
@Singleton
public class LocalUserDaoImpl implements UserDao {

    private final List<UserImpl> users;
    private final ReadWriteLock  lock;
    private final LocalStorage   userStorage;

    @Inject
    public LocalUserDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        this.users = new LinkedList<>();
        lock = new ReentrantReadWriteLock();
        userStorage = storageFactory.create("users.json");
    }

    @Inject
    @PostConstruct
    public void start(@Named("codenvy.local.infrastructure.users") Set<UserImpl> defaultUsers) {
        List<UserImpl> storedUsers = userStorage.loadList(new TypeToken<List<UserImpl>>() {});
        users.addAll(storedUsers.isEmpty() ? defaultUsers : storedUsers);
    }

    @PreDestroy
    public void stop() throws IOException {
        userStorage.store(users);
    }

    @Override
    public String authenticate(String aliasOrName, String password) throws UnauthorizedException, ServerException {
        lock.readLock().lock();
        try {
            User myUser = null;
            for (int i = 0, size = users.size(); i < size && myUser == null; i++) {
                if (users.get(i).getAliases().contains(aliasOrName)) {
                    myUser = users.get(i);
                }
            }
            if (myUser == null || !password.equals(myUser.getPassword())) {
                throw new UnauthorizedException(String.format("Authentication failed for user %s", aliasOrName));
            }
            return myUser.getId();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void create(UserImpl user) throws ConflictException {
        lock.writeLock().lock();
        try {
            final String userId = user.getId();
            final Set<String> aliases = new HashSet<>(user.getAliases());
            for (User u : users) {
                if (u.getId().equals(userId)) {
                    throw new ConflictException(
                            String.format("Unable create new user '%s'. User id %s is already in use.", user.getEmail(),
                                          userId));
                }
                for (String alias : u.getAliases()) {
                    if (aliases.contains(alias)) {
                        throw new ConflictException(
                                String.format("Unable create new user '%s'. User alias %s is already in use.",
                                              user.getEmail(), alias));
                    }
                }
            }
            users.add(new UserImpl(user));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(UserImpl user) throws NotFoundException {
        lock.writeLock().lock();
        try {
            User myUser = null;
            for (int i = 0, size = users.size(); i < size && myUser == null; i++) {
                if (users.get(i).getId().equals(user.getId())) {
                    myUser = users.get(i);
                }
            }
            if (myUser == null) {
                throw new NotFoundException(String.format("User not found %s", user.getId()));
            }
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(new UserImpl(user));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(String id) {
        lock.writeLock().lock();
        try {
            users.removeIf(u -> u.getId().equals(id));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public UserImpl getByAlias(String alias) throws NotFoundException {
        lock.readLock().lock();
        try {
            User user = null;
            for (int i = 0, size = users.size(); i < size && user == null; i++) {
                if (users.get(i).getAliases().contains(alias)) {
                    user = users.get(i);
                }
            }
            if (user == null) {
                throw new NotFoundException(String.format("User not found %s", alias));
            }
            return new UserImpl(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getById(String id) throws NotFoundException {
        lock.readLock().lock();
        try {
            User user = null;
            for (int i = 0, size = users.size(); i < size && user == null; i++) {
                if (users.get(i).getId().equals(id)) {
                    user = users.get(i);
                }
            }
            if (user == null) {
                throw new NotFoundException(String.format("User not found %s", id));
            }
            return new UserImpl(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getByName(String name) throws NotFoundException {
        lock.readLock().lock();
        try {
            User user = null;
            for (int i = 0, size = users.size(); i < size && user == null; i++) {
                if (users.get(i).getName().equals(name)) {
                    user = users.get(i);
                }
            }
            if (user == null) {
                throw new NotFoundException(String.format("User not found %s", name));
            }
            return new UserImpl(user);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
        lock.readLock().lock();
        try {
            final Optional<UserImpl> userOpt = users.stream()
                                                    .filter(u -> u.getEmail().equals(email))
                                                    .findAny();
            if (!userOpt.isPresent()) {
                throw new NotFoundException("User with email '%s' doesn't exist");
            }
            return userOpt.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}
