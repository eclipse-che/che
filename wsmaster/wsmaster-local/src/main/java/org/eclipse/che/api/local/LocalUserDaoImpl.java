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


import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Anton Korneta
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalUserDaoImpl implements UserDao {

    @VisibleForTesting
    final Map<String, UserImpl> users;

    private final ReadWriteLock rwLock;
    private final LocalStorage  userStorage;

    @Inject
    public LocalUserDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        this.users = new HashMap<>();
        rwLock = new ReentrantReadWriteLock();
        userStorage = storageFactory.create("users.json");
    }

    @Inject
    @PostConstruct
    public void start(@Named("codenvy.local.infrastructure.users") Set<UserImpl> defaultUsers) {
        final Map<String, UserImpl> storedUsers = userStorage.loadMap(new TypeToken<Map<String, UserImpl>>() {});
        rwLock.writeLock().lock();
        try {
            final Collection<UserImpl> preloadedUsers = storedUsers.isEmpty() ? defaultUsers : storedUsers.values();
            for (UserImpl defaultUser : preloadedUsers) {
                users.put(defaultUser.getId(), new UserImpl(defaultUser));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        rwLock.readLock().lock();
        try {
            userStorage.store(new HashMap<>(users));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getByAliasAndPassword(String aliasOrNameOrEmail, String password) throws ServerException, NotFoundException {
        requireNonNull(aliasOrNameOrEmail);
        requireNonNull(password);
        rwLock.readLock().lock();
        try {
            final Optional<UserImpl> userOpt = users.values()
                                                    .stream()
                                                    .filter(user -> user.getName().equals(aliasOrNameOrEmail)
                                                                    || user.getEmail().equals(aliasOrNameOrEmail)
                                                                    || user.getAliases().contains(aliasOrNameOrEmail))
                                                    .findAny();
            if (!userOpt.isPresent() || !userOpt.get().getPassword().equals(password)) {
                throw new NotFoundException(format("User '%s' doesn't exist", aliasOrNameOrEmail));
            }
            return userOpt.get();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void create(UserImpl newUser) throws ConflictException {
        requireNonNull(newUser);
        rwLock.writeLock().lock();
        try {
            if (users.containsKey(newUser.getId())) {
                throw new ConflictException(format("Couldn't create user, user with id '%s' already exists",
                                                   newUser.getId()));
            }
            checkConflicts(newUser, "create");
            users.put(newUser.getId(), new UserImpl(newUser));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void update(UserImpl update) throws NotFoundException, ConflictException {
        requireNonNull(update);
        rwLock.writeLock().lock();
        try {
            final UserImpl user = users.get(update.getId());
            if (user == null) {
                throw new NotFoundException(format("User with id '%s' doesn't exist", update.getId()));
            }
            checkConflicts(update, "update");
            users.put(update.getId(), new UserImpl(update));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void remove(String id) {
        requireNonNull(id);
        rwLock.writeLock().lock();
        try {
            users.remove(id);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public UserImpl getByAlias(String alias) throws NotFoundException {
        requireNonNull(alias, "Required non-null alias");
        rwLock.readLock().lock();
        try {
            return new UserImpl(find(user -> user.getAliases().contains(alias), "alias", alias));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getById(String id) throws NotFoundException {
        requireNonNull(id, "Required non-null id");
        rwLock.readLock().lock();
        try {
            final User user = users.get(id);
            if (user == null) {
                throw new NotFoundException(format("User with id '%s' doesn't exist", id));
            }
            return new UserImpl(user);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getByName(String name) throws NotFoundException {
        requireNonNull(name, "Required non-null name");
        rwLock.readLock().lock();
        try {
            return new UserImpl(find(user -> user.getName().equals(name), "name", name));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public UserImpl getByEmail(String email) throws NotFoundException, ServerException {
        requireNonNull(email, "Required non-null email");
        rwLock.readLock().lock();
        try {
            return new UserImpl(find(user -> user.getEmail().equals(email), "email", email));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void checkConflicts(UserImpl user, String operation) throws ConflictException {
        for (UserImpl existingUser : users.values()) {
            if (!existingUser.getId().equals(user.getId())) {
                if (existingUser.getName().equals(user.getName()))
                    throw new ConflictException(
                            format("Unable to %s a new user with name '%s' the name is already in use.",
                                   operation,
                                   user.getName()));
                if (existingUser.getEmail().equals(user.getEmail())) {
                    throw new ConflictException(
                            format("Unable to %s a new user with email '%s' the email is already in use.",
                                   operation,
                                   user.getEmail()));
                }
                if (!Collections.disjoint(existingUser.getAliases(), user.getAliases())) {
                    final HashSet<String> aliases = new HashSet<>(existingUser.getAliases());
                    aliases.retainAll(user.getAliases());
                    throw new ConflictException(
                            format("Unable to %s a new user with aliases '%s', the aliases are already in use",
                                   operation,
                                   aliases));
                }
            }
        }
    }

    private UserImpl find(Predicate<UserImpl> predicate, String subjectName, String subject) throws NotFoundException {
        final Optional<UserImpl> userOpt = users.values()
                                                .stream()
                                                .filter(predicate)
                                                .findAny();
        if (!userOpt.isPresent()) {
            throw new NotFoundException(format("User with %s '%s' doesn't exist", subjectName, subject));
        }
        return userOpt.get();
    }
}
