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

import org.eclipse.che.api.user.gwt.client.UserProfileServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class PreferencesComponent implements Component {

    public static final String PREF_IDE_THEME = "ide.theme";

    private final UserProfileServiceClient userProfileService;
    private final CurrentUser              currentUser;
    private final AppContext               appContext;
    private final PreferencesManagerImpl   preferencesManager;
    private final ThemeAgent               themeAgent;
    private final StyleInjector            styleInjector;

    @Inject
    public PreferencesComponent(UserProfileServiceClient userProfileService,
                                CurrentUser currentUser,
                                AppContext appContext,
                                PreferencesManagerImpl preferencesManager,
                                ThemeAgent themeAgent,
                                StyleInjector styleInjector) {
        this.userProfileService = userProfileService;
        this.currentUser = currentUser;
        this.appContext = appContext;
        this.preferencesManager = preferencesManager;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        userProfileService.getPreferences(new AsyncRequestCallback<Map<String, String>>(new StringMapUnmarshaller()) {
            @Override
            protected void onSuccess(Map<String, String> preferences) {
                currentUser.setPreferences(preferences);
                appContext.setCurrentUser(currentUser);
                preferencesManager.load(preferences);

                setTheme();

                styleInjector.inject();
                callback.onSuccess(PreferencesComponent.this);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(new Exception("Unable to load preferences", exception));
            }
        });
    }

    /** Applying user defined Theme. */
    private void setTheme() {
        String storedThemeId = preferencesManager.getValue(PREF_IDE_THEME);
        storedThemeId = storedThemeId != null ? storedThemeId : themeAgent.getCurrentThemeId();
        Theme themeToSet = storedThemeId != null ? themeAgent.getTheme(storedThemeId) : themeAgent.getDefault();
        Style.theme = themeToSet;
        themeAgent.setCurrentThemeId(themeToSet.getId());
    }
}
