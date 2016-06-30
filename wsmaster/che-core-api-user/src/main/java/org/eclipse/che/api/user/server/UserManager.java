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
package org.eclipse.che.api.user.server;

import com.google.common.collect.Sets;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static org.eclipse.che.api.user.server.Constants.ID_LENGTH;
import static org.eclipse.che.api.user.server.Constants.PASSWORD_LENGTH;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

/**
 * Facade for User related operations.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class UserManager {


    private static final Logger LOG = LoggerFactory.getLogger(UserManager.class);

    private final UserDao        userDao;
    private final UserProfileDao profileDao;
    private final PreferenceDao  preferenceDao;
    private final Set<String>    reservedNames;

    @Inject
    public UserManager(UserDao userDao,
                       UserProfileDao profileDao,
                       PreferenceDao preferenceDao,
                       @Named("user.reserved_names") String[] reservedNames) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.preferenceDao = preferenceDao;
        this.reservedNames = Sets.newHashSet(reservedNames);
    }


    /**
     * Creates new user.
     *
     * @param user
     *         POJO representation of user entity
     * @throws ConflictException
     *         when given user cannot be created
     * @throws ServerException
     *         when any other error occurs
     */
    public void create(User user, boolean isTemporary) throws ConflictException, ServerException {
        if (reservedNames.contains(user.getName().toLowerCase())) {
            throw new ConflictException(String.format("Username \"%s\" is reserved", user.getName()));
        }
        user.withId(generate("user", ID_LENGTH))
            .withPassword(firstNonNull(user.getPassword(), generate("", PASSWORD_LENGTH)));
        userDao.create(user);

        profileDao.create(new Profile(user.getId()));

        final Map<String, String> preferences = new HashMap<>(4);
        preferences.put("temporary", Boolean.toString(isTemporary));
        preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
        try {
            preferenceDao.setPreferences(user.getId(), preferences);
        } catch (NotFoundException e) {
            LOG.warn(format("Cannot set creation time preferences for user %s.", user.getId()), e);
        }
    }


    /**
     * Gets user from persistent layer by his identifier
     *
     * @param id
     *         user identifier
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getById(String id) throws NotFoundException, ServerException {
        return userDao.getById(id);
    }


    /**
     * Updates already present in persistent layer user.
     *
     * @param user
     *         POJO representation of user entity
     * @throws NotFoundException
     *         when user is not found
     * @throws ConflictException
     *         when given user cannot be updated
     * @throws ServerException
     *         when any other error occurs
     *
     */
    public void update(User user) throws NotFoundException, ServerException, ConflictException {
        userDao.update(user);
    }


    /**
     * Gets user from persistent layer by any of his aliases
     *
     * @param alias
     *         user name or alias
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getByAlias(String alias) throws NotFoundException, ServerException {
        return userDao.getByAlias(alias);
    }


    /**
     * Gets user from persistent layer by his username
     *
     * @param userName
     *         user name
     * @return user POJO
     * @throws NotFoundException
     *         when user doesn't exist
     * @throws ServerException
     *         when any other error occurs
     */
    public User getByName(String userName) throws NotFoundException, ServerException {
        return userDao.getByName(userName);
    }


    /**
     * Removes user from persistent layer by his identifier.
     *
     * @param id
     *         user identifier
     * @throws ConflictException
     *         when given user cannot be deleted
     * @throws ServerException
     *         when any other error occurs
     */
    public void remove(String id) throws NotFoundException, ServerException, ConflictException {
        userDao.remove(id);
    }
}
