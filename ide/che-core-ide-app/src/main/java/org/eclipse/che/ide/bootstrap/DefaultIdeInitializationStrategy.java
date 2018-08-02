/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import static org.eclipse.che.ide.actions.StartUpActionsParser.getStartUpActions;
import static org.eclipse.che.ide.api.WindowActionEvent.createWindowClosedEvent;
import static org.eclipse.che.ide.api.WindowActionEvent.createWindowClosingEvent;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.core.StandardComponentInitializer;
import org.eclipse.che.ide.preferences.StyleInjector;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Represents the default strategy for initializing Basic IDE. Performs the minimum required
 * initialization steps, such as:
 *
 * <ul>
 *   <li>initializing {@link CurrentUser} (loading profile, preferences);
 *   <li>initializing UI (setting theme, injecting CSS styles);
 *   <li>initializing {@link AppContext}.
 * </ul>
 */
@Singleton
class DefaultIdeInitializationStrategy implements IdeInitializationStrategy {

  protected final WorkspaceServiceClient workspaceServiceClient;
  protected final AppContext appContext;
  protected final BrowserAddress browserAddress;
  protected final CurrentUserInitializer userInitializer;
  protected final ThemeAgent themeAgent;
  protected final StyleInjector styleInjector;
  protected final Provider<StandardComponentInitializer> standardComponentsInitializerProvider;
  protected final Provider<WorkspacePresenter> workspacePresenterProvider;
  protected final EventBus eventBus;
  protected final DialogFactory dialogFactory;
  protected final PreferencesManager preferencesManager;

  @Inject
  DefaultIdeInitializationStrategy(
      WorkspaceServiceClient workspaceServiceClient,
      AppContext appContext,
      BrowserAddress browserAddress,
      CurrentUserInitializer userInitializer,
      ThemeAgent themeAgent,
      StyleInjector styleInjector,
      Provider<StandardComponentInitializer> standardComponentsInitializerProvider,
      Provider<WorkspacePresenter> workspacePresenterProvider,
      EventBus eventBus,
      DialogFactory dialogFactory,
      PreferencesManager preferencesManager) {
    this.workspaceServiceClient = workspaceServiceClient;
    this.appContext = appContext;
    this.browserAddress = browserAddress;
    this.userInitializer = userInitializer;
    this.themeAgent = themeAgent;
    this.styleInjector = styleInjector;
    this.standardComponentsInitializerProvider = standardComponentsInitializerProvider;
    this.workspacePresenterProvider = workspacePresenterProvider;
    this.eventBus = eventBus;
    this.dialogFactory = dialogFactory;
    this.preferencesManager = preferencesManager;
  }

  @Override
  public Promise<Void> init() {
    return userInitializer
        .init()
        .catchError(
            (Operation<PromiseError>)
                err -> {
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
        .then(
            arg -> {
              eventBus.fireEvent(new BasicIDEInitializedEvent());
            });
  }

  @Override
  public Promise<WorkspaceImpl> getWorkspaceToStart() {
    return workspaceServiceClient.getWorkspace(browserAddress.getWorkspaceKey());
  }

  private Operation<Void> initUI() {
    return aVoid -> {
      themeAgent.setTheme(preferencesManager.getValue(ThemeAgent.PREFERENCE_KEY));
      styleInjector.inject();
    };
  }

  protected Promise<Void> initAppContext() {
    return getWorkspaceToStart()
        .then(
            (Function<WorkspaceImpl, Void>)
                workspace -> {
                  ((AppContextImpl) appContext).setWorkspace(workspace);
                  ((AppContextImpl) appContext).setStartAppActions(getStartUpActions());

                  return null;
                })
        .catchError(
            (Operation<PromiseError>)
                err -> {
                  throw new OperationException("Can not get workspace: " + err.getCause());
                });
  }

  private Operation<Void> showUI() {
    return aVoid -> {
      standardComponentsInitializerProvider.get().initialize();
      showRootPresenter();

      // Bind browser's window events
      Window.addWindowClosingHandler(event -> eventBus.fireEvent(createWindowClosingEvent(event)));
      Window.addCloseHandler(event -> eventBus.fireEvent(createWindowClosedEvent()));
    };
  }

  private void showRootPresenter() {
    SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();
    RootLayoutPanel.get().add(mainPanel);
    RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

    workspacePresenterProvider.get().go(mainPanel);
  }
}
