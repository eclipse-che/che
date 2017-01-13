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


import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * In-memory implementation of {@link UserDao}.
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Anton Korneta
 * @author Yevhenii Voevodin
 */
@Singleton
public class LocalUserDaoImpl implements UserDao {

    public static final String FILENAME = "users.json";

    final Map<String, UserImpl> users;

    private final LocalStorage userStorage;

    @Inject
    public LocalUserDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        this.users = new HashMap<>();
        userStorage = storageFactory.create(FILENAME);
    }

    @Inject
    @PostConstruct
    public synchronized void start() {
        users.putAll(userStorage.loadMap(new TypeToken<Map<String, UserImpl>>() {}));
    }

    public synchronized void saveUsers() throws IOException {
        userStorage.store(new HashMap<>(users));
    }

    @Override
    public synchronized UserImpl getByAliasAndPassword(String emailOrName, String password) throws ServerException,
                                                                                                   NotFoundException {
        requireNonNull(emailOrName);
        requireNonNull(password);
        final Optional<UserImpl> userOpt = users.values()
                                                .stream()
                                                .filter(user -> user.getName().equals(emailOrName)
                                                                || user.getEmail().equals(emailOrName))
                                                .findAny();
        if (!userOpt.isPresent() || !userOpt.get().getPassword().equals(password)) {
            throw new NotFoundException(format("User '%s' doesn't exist", emailOrName));
        }
        return erasePassword(userOpt.get());
    }

    @Override
    public synchronized void create(UserImpl newUser) throws ConflictException {
        requireNonNull(newUser);
        if (users.containsKey(newUser.getId())) {
            throw new ConflictException(format("Couldn't create user, user with id '%s' already exists",
                                               newUser.getId()));
        }
        checkConflicts(newUser, "create");
        users.put(newUser.getId(), new UserImpl(newUser));
    }

    @Override
    public synchronized void update(UserImpl update) throws NotFoundException, ConflictException {
        requireNonNull(update);
        final UserImpl user = users.get(update.getId());
        if (user == null) {
            throw new NotFoundException(format("User with id '%s' doesn't exist", update.getId()));
        }
        checkConflicts(update, "update");
        users.put(update.getId(), new UserImpl(update));
    }

    @Override
    public synchronized void remove(String id) {
        requireNonNull(id);
        users.remove(id);
    }

    @Override
    public synchronized UserImpl getByAlias(String alias) throws NotFoundException {
        requireNonNull(alias, "Required non-null alias");
        return erasePassword(find(user -> user.getAliases().contains(alias), "alias", alias));
    }

    @Override
    public synchronized UserImpl getById(String id) throws NotFoundException {
        requireNonNull(id, "Required non-null id");
        final User user = users.get(id);
        if (user == null) {
            throw new NotFoundException(format("User with id '%s' doesn't exist", id));
        }
        return erasePassword(user);
    }

    @Override
    public synchronized UserImpl getByName(String name) throws NotFoundException {
        requireNonNull(name, "Required non-null name");
        return erasePassword(find(user -> user.getName().equals(name), "name", name));
    }

    @Override
    public synchronized UserImpl getByEmail(String email) throws NotFoundException, ServerException {
        requireNonNull(email, "Required non-null email");
        return erasePassword(find(user -> user.getEmail().equals(email), "email", email));
    }

    @Override
    public Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException {
        return new Page<>(users.values()
                               .stream()
                               .skip(skipCount)
                               .limit(maxItems)
                               .map(LocalUserDaoImpl::erasePassword)
                               .collect(Collectors.toCollection(LinkedHashSet::new)),
                          skipCount,
                          maxItems,
                          users.size());
    }

    @Override
    public long getTotalCount() throws ServerException {
        return users.size();
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

    // Returns user instance copy without password
    private static UserImpl erasePassword(User source) {
        return new UserImpl(source.getId(),
                            source.getEmail(),
                            source.getName(),
                            null,
                            source.getAliases());
    }
}
