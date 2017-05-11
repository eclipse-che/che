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
package org.eclipse.che.ide.bootstrap;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.client.ExtensionInitializer;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.PreferencesManagerImpl;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

/**
 * Initializes CHE IDE application:
 * <ul>
 * <li>initializes UI;</li>
 * <li>starts the extensions.</li>
 * </ul>
 */
@Singleton
public class IdeBootstrapper {

    private final Provider<StandardComponentInitializer> standardComponentsInitializerProvider;
    private final Provider<WorkspacePresenter>           workspaceProvider;
    private final ExtensionInitializer                   extensionInitializer;
    private final Provider<AppStateManager>              appStateManagerProvider;
    private final PreferencesManager                     preferencesManager;
    private final ThemeAgent                             themeAgent;
    private final StyleInjector                          styleInjector;
    private final EventBus                               eventBus;
    private final CurrentUserInitializer                 currentUserInitializer;
    private final WorkspaceStarter                       workspaceStarter;

    @Inject
    public IdeBootstrapper(Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
                           Provider<WorkspacePresenter> workspaceProvider,
                           ExtensionInitializer extensionInitializer,
                           Provider<AppStateManager> appStateManagerProvider,
                           PreferencesManagerImpl preferencesManager,
                           ThemeAgent themeAgent,
                           StyleInjector styleInjector,
                           EventBus eventBus,
                           CurrentUserInitializer currentUserInitializer,
                           WorkspaceStarter workspaceStarter) {
        this.standardComponentsInitializerProvider = standardComponentsInitializerProvider;
        this.workspaceProvider = workspaceProvider;
        this.extensionInitializer = extensionInitializer;
        this.appStateManagerProvider = appStateManagerProvider;
        this.preferencesManager = preferencesManager;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
        this.eventBus = eventBus;
        this.currentUserInitializer = currentUserInitializer;
        this.workspaceStarter = workspaceStarter;

        bootstrap();
    }

    private void bootstrap() {
        currentUserInitializer.init().then(aVoid -> {
            applyUserTheme();
            styleInjector.inject();
            standardComponentsInitializerProvider.get().initialize();
            workspaceStarter.startWorkspace();
            appStateManagerProvider.get();
            showUI();
            extensionInitializer.startExtensions();
        });
    }

    private void showUI() {
        SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();
        RootLayoutPanel.get().add(mainPanel);
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

        workspaceProvider.get().go(mainPanel);
    }

    private void applyUserTheme() {
        final String PREF_IDE_THEME = "ide.theme";
        String storedThemeId = preferencesManager.getValue(PREF_IDE_THEME);
        storedThemeId = storedThemeId != null ? storedThemeId : themeAgent.getCurrentThemeId();
        final Theme themeToSet = storedThemeId != null ? themeAgent.getTheme(storedThemeId) : themeAgent.getDefault();
        Style.theme = themeToSet;
        themeAgent.setCurrentThemeId(themeToSet.getId());

        Document.get().getBody().getStyle().setBackgroundColor(Style.theme.backgroundColor());
    }
}
