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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.theme.ThemeAgentImpl;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;

/**
 * Performs essential initialization routines of the IDE application, such as:
 * <ul>
 * <li>initializing {@link CurrentUser} (loading profile, preferences);</li>
 * <li>initializing UI (setting theme, injecting CSS styles);</li>
 * <li>initializing {@link AppContext}.</li>
 * </ul>
 */
@Singleton
class GeneralIdeInitializer implements IdeInitializer {

    protected final WorkspaceServiceClient                 workspaceServiceClient;
    protected final AppContext                             appContext;
    protected final BrowserAddress                         browserAddress;
    protected final CurrentUserInitializer                 userInitializer;
    protected final ThemeAgent                             themeAgent;
    protected final StyleInjector                          styleInjector;
    protected final Provider<StandardComponentInitializer> standardComponentsInitializerProvider;
    protected final AppStateManager                        appStateManager;
    protected final Provider<WorkspacePresenter>           workspacePresenterProvider;
    protected final EventBus                               eventBus;
    protected final Provider<CreateWorkspacePresenter>     createWsPresenter;
    protected final DialogFactory                          dialogFactory;

    @Inject
    GeneralIdeInitializer(WorkspaceServiceClient workspaceServiceClient,
                          AppContext appContext,
                          BrowserAddress browserAddress,
                          CurrentUserInitializer userInitializer,
                          ThemeAgent themeAgent,
                          StyleInjector styleInjector,
                          Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
                          AppStateManager appStateManager,
                          Provider<WorkspacePresenter> workspacePresenterProvider,
                          EventBus eventBus,
                          Provider<CreateWorkspacePresenter> createWsPresenter,
                          DialogFactory dialogFactory) {
        this.workspaceServiceClient = workspaceServiceClient;
        this.appContext = appContext;
        this.browserAddress = browserAddress;
        this.userInitializer = userInitializer;
        this.themeAgent = themeAgent;
        this.styleInjector = styleInjector;
        this.standardComponentsInitializerProvider = standardComponentsInitializerProvider;
        this.appStateManager = appStateManager;
        this.workspacePresenterProvider = workspacePresenterProvider;
        this.eventBus = eventBus;
        this.createWsPresenter = createWsPresenter;
        this.dialogFactory = dialogFactory;
    }

    @Override
    public Promise<WorkspaceDto> getWorkspaceToStart() {
        final String workspaceKey = browserAddress.getWorkspaceKey();

        return workspaceServiceClient.getWorkspace(workspaceKey);
    }

    @Override
    public Promise<Void> init() {
        return userInitializer.init()
                              .catchError((Operation<PromiseError>)err -> {
                                  // Error occurred before UI initialization.
                                  // As a fallback, let's try to inject CSS styles at least
                                  // in order to be able to use DialogFactory
                                  // for showing an error information to the user.
                                  styleInjector.inject();
                                  throw new OperationException(err.getMessage(), err.getCause());
                              })
                              .then(initUI())
                              .thenPromise(initAppContext())
                              .then(showUI());
    }

    private Operation<Void> initUI() {
        return aVoid -> {
            ((ThemeAgentImpl)themeAgent).applyUserTheme();
            styleInjector.inject();
        };
    }

    private Function<Void, Promise<Void>> initAppContext() {
        return aVoid -> getWorkspaceToStart()
                .then((Function<WorkspaceDto, Void>)workspace -> {
                    appContext.setWorkspace(workspace);
//                  browserAddress.setAddress(workspace.getNamespace(), workspace.getConfig().getName());
                    return null;
                })
                .catchError((Operation<PromiseError>)err -> {
                    createWs(); // temporary solution while dashboard doesn't work
                    throw new OperationException("Can not get workspace: " + err.getCause());
                });
    }

    private Operation<Void> showUI() {
        return aVoid -> {
            standardComponentsInitializerProvider.get().initialize();
            appStateManager.readStateFromPreferences();
            showRootPresenter();
            // Bind browser's window events
            Window.addWindowClosingHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosingEvent(event)));
            Window.addCloseHandler(event -> eventBus.fireEvent(WindowActionEvent.createWindowClosedEvent()));
        };
    }

    private void showRootPresenter() {
        SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();
        RootLayoutPanel.get().add(mainPanel);
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

        workspacePresenterProvider.get().go(mainPanel);
    }

    private void createWs() {
        createWsPresenter.get().show(new Callback<Workspace, Exception>() {
            @Override
            public void onSuccess(Workspace result) {
                dialogFactory.createMessageDialog("create WS", "created successfully",
                                                  Location::reload).show();
            }

            @Override
            public void onFailure(Exception reason) {
                dialogFactory.createMessageDialog("create WS", "failed to create", null).show();
            }
        });
    }
}
