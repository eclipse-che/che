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
package org.eclipse.che.ide.preferences;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.user.gwt.client.UserProfileServiceClient;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of {@link PreferencesManager}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class PreferencesManagerImpl implements PreferencesManager {
    private Map<String, String>      persistedPreferences;
    private Map<String, String>      changedPreferences;
    private UserProfileServiceClient userProfileService;

    /**
     * Create preferences.
     *
     * @param userProfileService
     */
    @Inject
    protected PreferencesManagerImpl(UserProfileServiceClient userProfileService) {
        this.persistedPreferences = new HashMap<>();
        this.changedPreferences = new HashMap<>();
        this.userProfileService = userProfileService;
    }

    /** {@inheritDoc} */
    @Override
    public String getValue(String preference) {
        if (changedPreferences.containsKey(preference)) {
            return changedPreferences.get(preference);
        }
        return persistedPreferences.get(preference);
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(String preference, String value) {
        changedPreferences.put(preference, value);
    }

    /** {@inheritDoc} */
    @Override
    public void flushPreferences(final AsyncCallback<Map<String, String>> callback) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.putAll(changedPreferences);

        userProfileService.updatePreferences(attributes, new AsyncRequestCallback<Map<String, String>>() {
            @Override
            protected void onSuccess(Map<String, String> result) {
                persistedPreferences.putAll(changedPreferences);
                changedPreferences.clear();
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
                Log.error(PreferencesManagerImpl.class, exception);
            }
        });
    }

    /**
     * Reads preferences from input map.
     *
     * @param preferences
     */
    public void load(Map<String, String> preferences) {
        if (preferences != null) {
            persistedPreferences.putAll(preferences);
        }
    }
}
