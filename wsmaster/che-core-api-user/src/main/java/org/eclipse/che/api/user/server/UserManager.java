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
package org.eclipse.che.api.user.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.user.server.Constants.ID_LENGTH;
import static org.eclipse.che.api.user.server.Constants.PASSWORD_LENGTH;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

/**
 * Facade for {@link User} and {@link Profile} related operations.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Yevhenii Voevodin
 */
@Singleton
public class UserManager {

    private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);

    private final UserDao       userDao;
    private final ProfileDao    profileDao;
    private final PreferenceDao preferencesDao;
    private final Set<String>   reservedNames;

    @Inject
    public UserManager(UserDao userDao,
                       ProfileDao profileDao,
                       PreferenceDao preferencesDao,
                       @Named("che.auth.reserved_user_names") String[] reservedNames) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.preferencesDao = preferencesDao;
        this.reservedNames = Sets.newHashSet(reservedNames);
    }

    /**
     * Creates new user and his profile.
     *
     * @param newUser
     *         created user
     * @throws NullPointerException
     *         when {@code newUser} is null
     * @throws ConflictException
     *         when user with such name/email/alias already exists
     * @throws ServerException
     *         when any other error occurs
     */
    public User create(User newUser, boolean isTemporary) throws ConflictException, ServerException {
        requireNonNull(newUser, "Required non-null user");
        if (reservedNames.contains(newUser.getName().toLowerCase())) {
            throw new ConflictException(String.format("Username '%s' is reserved", newUser.getName()));
        }
        final String userId = newUser.getId() != null ? newUser.getId() : generate("user", ID_LENGTH);
        final UserImpl user = new UserImpl(userId,
                                           newUser.getEmail(),
                                           newUser.getName(),
                                           firstNonNull(newUser.getPassword(), generate("", PASSWORD_LENGTH)),
                                           newUser.getAliases());
        try {
            userDao.create(user);
            profileDao.create(new ProfileImpl(user.getId()));
            preferencesDao.setPreferences(user.getId(), ImmutableMap.of("temporary", Boolean.toString(isTemporary),
                                                                        "codenvy:created", Long.toString(currentTimeMillis())));
        } catch (ConflictException | ServerException x) {
            // optimistic rollback(won't remove profile if userDao.remove failed)
            // remove operation is not-found-safe so if any exception
            // during the either user or profile creation occurs remove all entities
            // NOTE: this logic must be replaced with transaction management
            try {
                userDao.remove(user.getId());
                profileDao.remove(user.getId());
                preferencesDao.remove(user.getId());
            } catch (ServerException rollbackEx) {
                LOG.error(format("An attempt to clean up resources due to user creation failure was unsuccessful." +
                                 "Now the system may be in inconsistent state. " +
                                 "User with id '%s' must not exist",
                                 user.getId()),
                          rollbackEx);
            }
            throw x;
        }
        return user;
    }

    /**
     * Updates user by replacing an existing user entity with a new one.
     *
     * @param user
     *         user update
     * @throws NullPointerException
     *         when {@code user} is null
     * @throws NotFoundException
     *         when user with id {@code user.getId()} is not found
     * @throws ConflictException
     *         when user's new alias/email/name is not unique
     * @throws ServerException
     *         when any other error occurs
     */
    public void update(User user) throws NotFoundException, ServerException, ConflictException {
        requireNonNull(user, "Required non-null user");
        userDao.update(new UserImpl(user));
    }

    /**
     * Finds user by given {@code id}.
     *
     * @param id
     *         user identifier
     * @return user instance
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Required non-null id");
        return userDao.getById(id);
    }

    /**
     * Finds user by given {@code alias}.
     *
     * @param alias
     *         user alias
     * @return user instance
     * @throws NullPointerException
     *         when {@code alias} is null
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getByAlias(String alias) throws NotFoundException, ServerException {
        requireNonNull(alias, "Required non-null alias");
        return userDao.getByAlias(alias);
    }

    /**
     * Finds user by given {@code name}.
     *
     * @param name
     *         user name
     * @return user instance
     * @throws NullPointerException
     *         when {@code name} is null
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getByName(String name) throws NotFoundException, ServerException {
        requireNonNull(name, "Required non-null name");
        return userDao.getByName(name);
    }

    /**
     * Finds user by given {@code email}.
     *
     * @param email
     *         user email
     * @return user instance
     * @throws NullPointerException
     *         when {@code email} is null
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getByEmail(String email) throws NotFoundException, ServerException {
        requireNonNull(email, "Required non-null email");
        return userDao.getByEmail(email);
    }

    /**
     * Finds all users {@code email}.
     *
     * @param maxItems
     *         the maximum number of users to return
     * @param skipCount
     *         the number of users to skip
     * @return user instance
     * @throws IllegalArgumentException
     *         when {@code maxItems} or {@code skipCount} is negative
     * @throws ServerException
     *         when any other error occurs
     */
    public Page<UserImpl> getAll(int maxItems, long skipCount) throws ServerException {
        checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
        checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");
        return userDao.getAll(maxItems, skipCount);
    }

    /**
     * Gets total count of all users
     *
     * @return user count
     * @throws ServerException
     *         when any error occurs
     */
    public long getTotalCount() throws ServerException {
        return userDao.getTotalCount();
    }

    /**
     * Removes user by given {@code id}.
     *
     * @param id
     *         user identifier
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ConflictException
     *         when given user cannot be deleted
     * @throws ServerException
     *         when any other error occurs
     */
    public void remove(String id) throws ServerException, ConflictException {
        requireNonNull(id, "Required non-null id");
        userDao.remove(id);
    }
}
