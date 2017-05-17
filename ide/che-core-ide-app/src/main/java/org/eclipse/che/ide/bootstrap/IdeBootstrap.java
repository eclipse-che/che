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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.theme.ThemeAgentImpl;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;

/**
 * Performs initial startup of the CHE IDE application.
 * <ul>
 * <li>initializes IDE;</li>
 * <li>shows UI;</li>
 * <li>starts the extensions;</li>
 * <li>starts a workspace.</li>
 * </ul>
 */
@Singleton
public class IdeBootstrap {

    private final Provider<StandardComponentInitializer> standardComponentsInitializerProvider;
    private final Provider<WorkspacePresenter>           workspacePresenterProvider;
    private final ExtensionInitializer                   extensionInitializer;
    private final AppStateManager                        appStateManager;
    private final ThemeAgent                             themeAgent;
    private final StyleInjector                          styleInjector;
    private final CurrentUserInitializer                 currentUserInitializer;
    private final WorkspaceStarter                       workspaceStarter;
    private final EventBus                               eventBus;
    private final DialogFactory                          dialogFactory;
    private final IdeInitializer                         ideInitializer;
    private final Provider<CreateWorkspacePresenter>     createWsPresenter;

    @Inject
    public IdeBootstrap(Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
                        Provider<WorkspacePresenter> workspacePresenterProvider,
                        ExtensionInitializer extensionInitializer,
                        AppStateManager appStateManager,
                        ThemeAgent themeAgent,
                        StyleInjector styleInjector,
                        CurrentUserInitializer currentUserInitializer,
                        WorkspaceStarter workspaceStarter,
                        EventBus eventBus,
                        DialogFactory dialogFactory,
                        IdeInitializer ideInitializer,
                        Provider<CreateWorkspacePresenter> createWsPresenter) {
        this.standardComponentsInitializerProvider = standardComponentsInitializerProvider;
        this.workspacePresenterProvider = workspacePresenterProvider;
        this.extensionInitializer = extensionInitializer;
        this.appStateManager = appStateManager;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
        this.currentUserInitializer = currentUserInitializer;
        this.workspaceStarter = workspaceStarter;
        this.eventBus = eventBus;
        this.dialogFactory = dialogFactory;
        this.ideInitializer = ideInitializer;
        this.createWsPresenter = createWsPresenter;

        bootstrap();
    }

    private void bootstrap() {
        // init CurrentUser
        currentUserInitializer.init().then(aVoid -> {
            // init UI
            ((ThemeAgentImpl)themeAgent).applyUserTheme();
            styleInjector.inject();

            // init IDE
            ideInitializer.init().then(arg -> {
                // show UI
                standardComponentsInitializerProvider.get().initialize();
                appStateManager.readStateFromPreferences();
                showRootPresenter();

                // start extensions
                extensionInitializer.startExtensions();
                Scheduler.get().scheduleDeferred(this::notifyShowIDE);

                // start WS
                workspaceStarter.startWorkspace();
            }).catchError(err -> {
                // can't get WS or Factory
                dialogFactory.createMessageDialog("IDE initialization failed",
                                                  err.getMessage(),
                                                  null).show();

                // temporary solution while dashboard doesn't work
                createWs();
            });
        }).catchError(err -> {
            // error has been occurred before IDE UI initialization
            // can't use DialogFactory
            onInitializationFailed(err.getMessage());
        });
    }

    private void showRootPresenter() {
        SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();
        RootLayoutPanel.get().add(mainPanel);
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

        workspacePresenterProvider.get().go(mainPanel);

        // Bind browser's window events
        Window.addWindowClosingHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosingEvent(event)));
        Window.addCloseHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosedEvent()));
    }

    /** Informs parent window (e.g. dashboard) that IDE application can be shown. */
    private native void notifyShowIDE() /*-{
        $wnd.parent.postMessage("show-ide", "*");
    }-*/;

    /** Handles IDE initialization error. */
    private native void onInitializationFailed(String reason) /*-{
        try {
            $wnd.IDE.eventHandlers.initializationFailed(reason);
            this.@org.eclipse.che.ide.bootstrap.IdeBootstrap::notifyShowIDE()();
        } catch (e) {
            console.log(e.message);
        }
    }-*/;

    private void createWs() {
        createWsPresenter.get().show(new Callback<Workspace, Exception>() {
            @Override
            public void onSuccess(Workspace result) {
                dialogFactory.createMessageDialog("create WS", "created successfully", null).show();
            }

            @Override
            public void onFailure(Exception reason) {
                dialogFactory.createMessageDialog("create WS", "failed to create", null).show();
            }
        });
    }
}
