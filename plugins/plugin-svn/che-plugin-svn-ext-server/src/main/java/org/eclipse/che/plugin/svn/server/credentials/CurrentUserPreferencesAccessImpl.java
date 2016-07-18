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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Map;

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
        final Subject currentSubject = getCurrentSubject();
        Map<String, String> content;
        try {
            content = this.preferencesDao.getPreferences(currentSubject.getUserId());
        } catch (final ServerException e) {
            throw new PreferencesAccessException(e);
        }
        content.put(key, value);
        try {
            this.preferencesDao.setPreferences(currentSubject.getUserId(), content);
        } catch (final ServerException e) {
            throw new PreferencesAccessException(e);
        }
    }

    @Override
    public String getPreference(final String key) throws PreferencesAccessException {
        final Subject currentSubject = getCurrentSubject();
        final String pattern = MessageFormat.format("^{0}$", key);
        Map<String, String> response;
        try {
            response = this.preferencesDao.getPreferences(currentSubject.getUserId(), pattern);
        } catch (final ServerException e) {
            throw new PreferencesAccessException(e);
        }
        return response.get(key);
    }

    private Subject getCurrentSubject() {
        return EnvironmentContext.getCurrent().getSubject();
    }
}
