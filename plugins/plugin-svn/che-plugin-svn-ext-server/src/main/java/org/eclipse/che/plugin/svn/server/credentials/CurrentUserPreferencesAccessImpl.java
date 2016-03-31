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
package org.eclipse.che.plugin.svn.server.credentials;

import java.text.MessageFormat;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;


/**
 * Provides access to the current user's preferences.<br>
 * Mostly necessary to allow easier mocking.
 */
public class CurrentUserPreferencesAccessImpl implements CurrentUserPreferencesAccess {

    private final PreferenceDao preferencesDao;

    @Inject
    public CurrentUserPreferencesAccessImpl(final PreferenceDao preferencesDao) {
        this.preferencesDao = preferencesDao;
    }

    @Override
    public void updatePreference(final String key, final String value) throws PreferencesAccessException {
        final User currentUser = getCurrentUser();
        Map<String, String> content;
        try {
            content = this.preferencesDao.getPreferences(currentUser.getId());
        } catch (final ServerException e) {
            throw new PreferencesAccessException(e);
        }
        content.put(key, value);
        try {
            this.preferencesDao.setPreferences(currentUser.getId(), content);
        } catch (final ServerException | NotFoundException e) {
            throw new PreferencesAccessException(e);
        }
    }

    @Override
    public String getPreference(final String key) throws PreferencesAccessException {
        final User currentUser = getCurrentUser();
        final String pattern = MessageFormat.format("^{0}$", key);
        Map<String, String> response;
        try {
            response = this.preferencesDao.getPreferences(currentUser.getId(), pattern);
        } catch (final ServerException e) {
            throw new PreferencesAccessException(e);
        }
        return response.get(key);
    }

    private User getCurrentUser() {
        return EnvironmentContext.getCurrent().getUser();
    }
}
