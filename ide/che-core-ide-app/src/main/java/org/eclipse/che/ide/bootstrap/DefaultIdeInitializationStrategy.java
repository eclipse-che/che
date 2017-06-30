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
import org.eclipse.che.ide.actions.StartUpActionsParser;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.theme.ThemeAgentImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the default strategy for initializing Basic IDE.
 * Performs the minimum required initialization steps, such as:
 * <ul>
 * <li>initializing {@link CurrentUser} (loading profile, preferences);</li>
 * <li>initializing UI (setting theme, injecting CSS styles);</li>
 * <li>initializing {@link AppContext}.</li>
 * </ul>
 */
@Singleton
class DefaultIdeInitializationStrategy implements IdeInitializationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultIdeInitializationStrategy.class);

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
    DefaultIdeInitializationStrategy(WorkspaceServiceClient workspaceServiceClient,
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
    public Promise<Void> init() {
        return userInitializer.init()
                              .catchError((Operation<PromiseError>)err -> {
                                  // Fail to initialize the current user.
                                  // Since we can't get theme ID from the user's preferences
                                  // try to inject CSS styles with a default theme at least
                                  // in order to be able to use a minimal UI (dialogs)
                                  // for displaying an error information to the user.
                                  styleInjector.inject();

                                  // Prevent further initialization steps.
                                  throw new OperationException(err.getMessage(), err.getCause());
                              })
                              .then(initUI())
                              .thenPromise(aVoid -> initAppContext())
                              .then(showUI())
                              .then(arg -> {
                                  eventBus.fireEvent(new BasicIDEInitializedEvent());
                              });
    }

    @Override
    public Promise<WorkspaceImpl> getWorkspaceToStart() {
        final String workspaceKey = browserAddress.getWorkspaceKey();
        Promise<WorkspaceImpl> ws = workspaceServiceClient.getWorkspace(workspaceKey);
        LOG.debug("Got workspace: " + workspaceKey);
        return ws;
    }

    private Operation<Void> initUI() {
        return aVoid -> {
            ((ThemeAgentImpl)themeAgent).applyUserTheme();
            styleInjector.inject();
        };
    }

    protected Promise<Void> initAppContext() {

        return getWorkspaceToStart()
                .then((Function<WorkspaceImpl, Void>)workspace -> {

                    Log.info(DefaultIdeInitializationStrategy.class, "Workspace -> " + workspace);

                    ((AppContextImpl)appContext).setWorkspace(workspace);
                    ((AppContextImpl)appContext).setStartAppActions(StartUpActionsParser.getStartUpActions());
                    browserAddress.setAddress(workspace.getNamespace(), workspace.getConfig().getName());
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

    // TODO: remove after fix dashboard
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
