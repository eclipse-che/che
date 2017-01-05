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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * Preferences manager layer, simplifies prefernces service by
 * taking all the business logic out from the service and making that logic
 * easily reusable throughout the system.
 *
 * <p>The manager doesn't perform any bean validations and it
 * is expected that all the incoming objects are valid, nevertheless
 * this exactly the right place for performing business validations.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class ProfileManager {

    @Inject
    private ProfileDao profileDao;

    /**
     * Finds the profile related to the user with given {@code userId}.
     *
     * @param userId
     *         the id to search the user's profile
     * @return found profile
     * @throws NullPointerException
     *         when {@code userId} is null
     * @throws NotFoundException
     *         when there is no profile for the user with the id {@code userId}
     * @throws ServerException
     *         when any other error occurs
     */
    public Profile getById(String userId) throws NotFoundException, ServerException {
        requireNonNull(userId, "Required non-null user id");
        return profileDao.getById(userId);
    }

    /**
     * Creates a new user's profile .
     *
     * @param profile
     *         new profile
     * @throws NullPointerException
     *         when profile is null
     * @throws ConflictException
     *         when profile for the user {@code profile.getUserId()} already exists
     * @throws ServerException
     *         when any other error occurs
     */
    public void create(Profile profile) throws ServerException, ConflictException {
        requireNonNull(profile, "Required non-null profile");
        profileDao.create(new ProfileImpl(profile));
    }

    /**
     * Updates current profile using replace strategy.
     *
     * <p>Note that {@link Profile#getEmail()} can't be updated using this method
     * as it is mirrored from the {@link User#getEmail()}.
     *
     * @param profile
     *         profile update
     * @throws NullPointerException
     *         when {@code profile}  is null
     * @throws NotFoundException
     *         when there is no profile for the user with the id {@code profile.getUserId()}
     * @throws ServerException
     *         when any other error occurs
     */
    public void update(Profile profile) throws NotFoundException, ServerException {
        requireNonNull(profile, "Required non-null profile");
        profileDao.update(new ProfileImpl(profile));
    }

    /**
     * Removes the user's profile.
     *
     * <p>Note that this method won't throw any exception when
     * user doesn't have the corresponding profile.
     *
     * @param userId
     *         the id of the user, whose profile should be removed
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ServerException
     *         when any other error occurs
     */
    public void remove(String userId) throws ServerException {
        requireNonNull(userId, "Required non-null user id");
        profileDao.remove(userId);
    }
}
