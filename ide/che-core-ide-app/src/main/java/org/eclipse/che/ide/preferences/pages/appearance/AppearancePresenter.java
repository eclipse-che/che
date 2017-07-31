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
package org.eclipse.che.ide.preferences.pages.appearance;

import org.eclipse.che.ide.CoreLocalizationConstant;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.theme.ThemeAgent;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Presenter for 'Appearance' preferences page.
 *
 * @author Evgen Vidolob
 * @author Igor Vinokur
 */
@Singleton
public class AppearancePresenter extends AbstractPreferencePagePresenter implements AppearanceView.ActionDelegate {

    public static final String PREF_IDE_THEME              = "ide.theme";
    public static final String PREF_SHOW_MAVEN_ARTIFACT_ID = "ide.project.explorer.show.maven.artifact.id";

    private final AppearanceView     view;
    private final AppContext         appContext;
    private final ThemeAgent         themeAgent;
    private final PreferencesManager preferencesManager;

    private String themeId;
    private boolean dirty = false;
    private boolean showMavenArtifactId;

    @Inject
    public AppearancePresenter(AppearanceView view,
                               AppContext appContext,
                               CoreLocalizationConstant constant,
                               ThemeAgent themeAgent,
                               PreferencesManager preferencesManager) {
        super(constant.appearanceTitle(), constant.appearanceCategory());
        this.view = view;
        this.appContext = appContext;
        this.themeAgent = themeAgent;
        this.preferencesManager = preferencesManager;
        view.setDelegate(this);

        themeId = preferencesManager.getValue(PREF_IDE_THEME);
        showMavenArtifactId = getShowMavenArtifactIdPreferenceValue();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        String currentThemeId = preferencesManager.getValue(PREF_IDE_THEME);
        if (currentThemeId == null || currentThemeId.isEmpty()) {
            currentThemeId = themeAgent.getCurrentThemeId();
        }
        view.setThemes(themeAgent.getThemes(), currentThemeId);
        view.setSelectedShowMavenArtifactIdCheckBox(getShowMavenArtifactIdPreferenceValue());
    }

    @Override
    public void themeSelected(String themeId) {
        this.themeId = themeId;
        dirty = !themeId.equals(themeAgent.getCurrentThemeId());
        delegate.onDirtyChanged();
    }

    @Override
    public void showMavenArtifactIdCheckBoxValueChanged(boolean showMavenModule) {
        this.showMavenArtifactId = showMavenModule;
        dirty = showMavenModule != getShowMavenArtifactIdPreferenceValue();
        delegate.onDirtyChanged();
    }

    @Override
    public void storeChanges() {
        preferencesManager.setValue(PREF_IDE_THEME, themeId);
        preferencesManager.setValue(PREF_SHOW_MAVEN_ARTIFACT_ID, String.valueOf(showMavenArtifactId));
        appContext.getWorkspaceRoot().synchronize();
        dirty = false;
    }

    @Override
    public void revertChanges() {
        String currentThemeId = preferencesManager.getValue(PREF_IDE_THEME);
        if (currentThemeId == null || currentThemeId.isEmpty()) {
            currentThemeId = themeAgent.getCurrentThemeId();
        }
        view.setThemes(themeAgent.getThemes(), currentThemeId);
        view.setSelectedShowMavenArtifactIdCheckBox(getShowMavenArtifactIdPreferenceValue());

        dirty = false;
    }

    private boolean getShowMavenArtifactIdPreferenceValue() {
        return Boolean.valueOf(preferencesManager.getValue(PREF_SHOW_MAVEN_ARTIFACT_ID));
    }
}
