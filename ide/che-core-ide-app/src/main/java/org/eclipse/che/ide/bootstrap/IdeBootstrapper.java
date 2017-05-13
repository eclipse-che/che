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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.client.ExtensionInitializer;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.theme.ThemeAgentImpl;
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
    private final AppStateManager                        appStateManager;
    private final ThemeAgent                             themeAgent;
    private final StyleInjector                          styleInjector;
    private final CurrentUserInitializer                 currentUserInitializer;
    private final WorkspaceStarter                       workspaceStarter;
    private final EventBus                               eventBus;

    @Inject
    public IdeBootstrapper(Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
                           Provider<WorkspacePresenter> workspaceProvider,
                           ExtensionInitializer extensionInitializer,
                           AppStateManager appStateManager,
                           ThemeAgent themeAgent,
                           StyleInjector styleInjector,
                           CurrentUserInitializer currentUserInitializer,
                           WorkspaceStarter workspaceStarter,
                           EventBus eventBus) {
        this.standardComponentsInitializerProvider = standardComponentsInitializerProvider;
        this.workspaceProvider = workspaceProvider;
        this.extensionInitializer = extensionInitializer;
        this.appStateManager = appStateManager;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
        this.currentUserInitializer = currentUserInitializer;
        this.workspaceStarter = workspaceStarter;
        this.eventBus = eventBus;

        bootstrap();
    }

    private void bootstrap() {
        currentUserInitializer.init().then(aVoid -> {
            ((ThemeAgentImpl)themeAgent).applyUserTheme();
            styleInjector.inject();
            standardComponentsInitializerProvider.get().initialize();
            appStateManager.readStateFromPreferences();
            workspaceStarter.startWorkspace();
            showUI();
            extensionInitializer.startExtensions();
        });

        // Bind browser's window events
        Window.addWindowClosingHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosingEvent(event)));
        Window.addCloseHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosedEvent()));
    }

    private void showUI() {
        SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();
        RootLayoutPanel.get().add(mainPanel);
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

        workspaceProvider.get().go(mainPanel);
    }
}
