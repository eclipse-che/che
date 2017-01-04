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
package org.eclipse.che.ide.preferences;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.context.AppContextImpl;

import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class PreferencesComponent implements Component {

    public static final String PREF_IDE_THEME = "ide.theme";

    private final CurrentUser              currentUser;
    private final AppContext               appContext;
    private final PreferencesManagerImpl   preferencesManager;
    private final ThemeAgent               themeAgent;
    private final StyleInjector            styleInjector;
    private final CoreLocalizationConstant locale;

    @Inject
    public PreferencesComponent(CurrentUser currentUser,
                                AppContext appContext,
                                PreferencesManagerImpl preferencesManager,
                                ThemeAgent themeAgent,
                                StyleInjector styleInjector,
                                CoreLocalizationConstant locale) {
        this.currentUser = currentUser;
        this.appContext = appContext;
        this.preferencesManager = preferencesManager;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
        this.locale = locale;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        preferencesManager.loadPreferences().then(new Operation<Map<String, String>>() {
            @Override
            public void apply(Map<String, String> preferences) throws OperationException {
                currentUser.setPreferences(preferences);

                if (appContext instanceof AppContextImpl) {
                    ((AppContextImpl)appContext).setCurrentUser(currentUser);
                }

                setTheme();

                styleInjector.inject();
                callback.onSuccess(PreferencesComponent.this);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new Exception(locale.unableToLoadPreference(), arg.getCause()));
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
