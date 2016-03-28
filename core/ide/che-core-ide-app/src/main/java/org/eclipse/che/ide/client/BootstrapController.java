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
package org.eclipse.che.ide.client;

import elemental.client.Browser;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedEvent;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedHandler;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

import java.util.Iterator;
import java.util.Map;

/**
 * Performs initial application startup.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
public class BootstrapController {

    private final Provider<WorkspacePresenter> workspaceProvider;
    private final ExtensionInitializer         extensionInitializer;
    private final EventBus                     eventBus;
    private final ProductInfoDataProvider      productInfoDataProvider;
    private final Provider<AppStateManager>    appStateManagerProvider;
    private final AppContext                   appContext;

    @Inject
    public BootstrapController(Provider<WorkspacePresenter> workspaceProvider,
                               ExtensionInitializer extensionInitializer,
                               EventBus eventBus,
                               ProductInfoDataProvider productInfoDataProvider,
                               Provider<AppStateManager> appStateManagerProvider,
                               AppContext appContext,
                               DtoRegistrar dtoRegistrar) {
        this.workspaceProvider = workspaceProvider;
        this.extensionInitializer = extensionInitializer;
        this.eventBus = eventBus;
        this.productInfoDataProvider = productInfoDataProvider;
        this.appStateManagerProvider = appStateManagerProvider;
        this.appContext = appContext;

        appContext.setStartUpActions(StartUpActionsParser.getStartUpActions());
        dtoRegistrar.registerDtoProviders();

        setCustomInterval();
    }

    @Inject
    private void startComponents(Map<String, Provider<Component>> components) {
        startComponents(components.values().iterator());
    }

    @Inject
    private void startWsAgentComponents(EventBus eventBus, final Map<String, Provider<WsAgentComponent>> components) {
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, new WorkspaceStartedHandler() {
            @Override
            public void onWorkspaceStarted(UsersWorkspaceDto workspace) {
                startWsAgentComponents(components.values().iterator());
            }
        });
    }

    private void startComponents(final Iterator<Provider<Component>> componentProviderIterator) {
        if (componentProviderIterator.hasNext()) {
            Provider<Component> componentProvider = componentProviderIterator.next();

            final Component component = componentProvider.get();
            Log.info(component.getClass(), "starting...");
            component.start(new Callback<Component, Exception>() {
                @Override
                public void onSuccess(Component result) {
                    Log.info(result.getClass(), "started");
                    startComponents(componentProviderIterator);
                }

                @Override
                public void onFailure(Exception reason) {
                    Log.error(component.getClass(), reason);
                    initializationFailed(reason.getMessage());
                }
            });
        } else {
            startExtensionsAndDisplayUI();
        }
    }

    private void startWsAgentComponents(final Iterator<Provider<WsAgentComponent>> componentProviderIterator) {
        if (componentProviderIterator.hasNext()) {
            Provider<WsAgentComponent> componentProvider = componentProviderIterator.next();

            final WsAgentComponent component = componentProvider.get();
            Log.info(component.getClass(), "starting...");
            component.start(new Callback<WsAgentComponent, Exception>() {
                @Override
                public void onSuccess(WsAgentComponent result) {
                    Log.info(result.getClass(), "started");
                    startWsAgentComponents(componentProviderIterator);
                }

                @Override
                public void onFailure(Exception reason) {
                    Log.error(component.getClass(), reason);
                    initializationFailed(reason.getMessage());
                }
            });
        } else {
            eventBus.fireEvent(WsAgentStateEvent.createWsAgentStartedEvent());
        }
    }

    private void startExtensionsAndDisplayUI() {
        appStateManagerProvider.get();

        extensionInitializer.startExtensions();

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                displayIDE();
            }
        });
    }

    private void displayIDE() {
        // Start UI
        SimpleLayoutPanel mainPanel = new SimpleLayoutPanel();

        RootLayoutPanel.get().add(mainPanel);

        // Make sure the root panel creates its own stacking context
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);

        WorkspacePresenter workspacePresenter = workspaceProvider.get();

        // Display IDE
        workspacePresenter.go(mainPanel);

        Document.get().setTitle(productInfoDataProvider.getDocumentTitle());

        // Bind browser's window events
        Window.addWindowClosingHandler(new Window.ClosingHandler() {
            @Override
            public void onWindowClosing(Window.ClosingEvent event) {
                eventBus.fireEvent(WindowActionEvent.createWindowClosingEvent(event));
            }
        });

        Window.addCloseHandler(new CloseHandler<Window>() {
            @Override
            public void onClose(CloseEvent<Window> event) {
                eventBus.fireEvent(WindowActionEvent.createWindowClosedEvent());
            }
        });

        elemental.html.Window window = Browser.getWindow();

    }

    /**
     * Handles any of initialization errors.
     * Tries to call predefined IDE.eventHandlers.ideInitializationFailed function.
     *
     * @param reason
     *         failure encountered
     */
    private native void initializationFailed(String reason) /*-{
        try {
            $wnd.IDE.eventHandlers.initializationFailed(reason);
        } catch (e) {
            console.log(e.message);
        }
    }-*/;

    /**
     * When we change browser tab and IDE executes into inactive tab, browser set code execution interval to improve performance. For
     * example Chrome and Firefox set 1000ms = 1sec interval. The method override global setInterval function and set custom value (100ms)
     * of interval. This solution fix issue when we need execute some code into inactive tab permanently, for example launch factory.
     */
    private native void setCustomInterval() /*-{
        var customInterval = 10;
        var setInterval = function () {
            clearInterval(interval);
            customInterval *= 10;
        };

        var interval = setInterval(setInterval, customInterval);
    }-*/;
}
