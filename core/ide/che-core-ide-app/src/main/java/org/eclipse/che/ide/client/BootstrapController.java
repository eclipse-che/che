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
import elemental.events.Event;
import elemental.events.EventListener;

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

import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.core.Component;
import org.eclipse.che.ide.logger.AnalyticsEventLoggerExt;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.util.StartUpActionsParser;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

import java.util.HashMap;
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

    private final AnalyticsEventLoggerExt      analyticsEventLoggerExt;
    private final Provider<WorkspacePresenter> workspaceProvider;
    private final ExtensionInitializer         extensionInitializer;
    private final EventBus                     eventBus;
    private final ProductInfoDataProvider      productInfoDataProvider;
    private final Provider<AppStateManager>    appStateManagerProvider;
    private final AppContext                   appContext;

    @Inject
    public BootstrapController(Provider<WorkspacePresenter> workspaceProvider,
                               ExtensionInitializer extensionInitializer,
                               DtoRegistrar dtoRegistrar,
                               AnalyticsEventLoggerExt analyticsEventLoggerExt,
                               EventBus eventBus,
                               ProductInfoDataProvider productInfoDataProvider,
                               Provider<AppStateManager> appStateManagerProvider,
                               AppContext appContext) {
        this.workspaceProvider = workspaceProvider;
        this.extensionInitializer = extensionInitializer;
        this.eventBus = eventBus;
        this.analyticsEventLoggerExt = analyticsEventLoggerExt;
        this.productInfoDataProvider = productInfoDataProvider;
        this.appStateManagerProvider = appStateManagerProvider;
        this.appContext = appContext;

        dtoRegistrar.registerDtoProviders();
    }

    @Inject
    void startComponents(final Map<String, Provider<Component>> components) {
        processStartupActionUrl();
        startComponents(components.values().iterator());
    }

    private void startComponents(final Iterator<Provider<Component>> componentProviderIterator) {
        if (componentProviderIterator.hasNext()) {
            Provider<Component> componentProvider = componentProviderIterator.next();

            componentProvider.get().start(new Callback<Component, Exception>() {
                @Override
                public void onSuccess(Component result) {
                    Log.info(getClass(), result.getClass());
                    startComponents(componentProviderIterator);
                    Log.info(getClass(), "components started");
                }

                @Override
                public void onFailure(Exception reason) {
                    componentStartFail(reason);
                }
            });
        } else {
            startExtensions();
        }
    }

    private void componentStartFail(Exception reason) {
        Log.error(BootstrapController.class, reason);
        initializationFailed(reason.getMessage());
    }

    /** Start extensions */
    private void startExtensions() {
        appStateManagerProvider.get();

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // Instantiate extensions
                extensionInitializer.startExtensions();

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        displayIDE();
                    }
                });
            }
        });
    }

    /** Displays the IDE */
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

        final AnalyticsSessions analyticsSessions = new AnalyticsSessions();

        // Bind browser's window events
        Window.addWindowClosingHandler(new Window.ClosingHandler() {
            @Override
            public void onWindowClosing(Window.ClosingEvent event) {
                onWindowClose(analyticsSessions);
                eventBus.fireEvent(WindowActionEvent.createWindowClosingEvent(event));
            }
        });

        Window.addCloseHandler(new CloseHandler<Window>() {
            @Override
            public void onClose(CloseEvent<Window> event) {
                onWindowClose(analyticsSessions);
                eventBus.fireEvent(WindowActionEvent.createWindowClosedEvent());
            }
        });

        elemental.html.Window window = Browser.getWindow();

        window.addEventListener(Event.FOCUS, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                onSessionUsage(analyticsSessions, false);
            }
        }, true);

        window.addEventListener(Event.BLUR, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                onSessionUsage(analyticsSessions, false);
            }
        }, true);

        onSessionUsage(analyticsSessions, true); // This is necessary to forcibly print the very first event
    }

    private void onSessionUsage(AnalyticsSessions analyticsSessions, boolean force) {
        if (analyticsSessions.getIdleUsageTime() > 600000) { // 10 min
            analyticsSessions.makeNew();
            logSessionUsageEvent(analyticsSessions, true);
        } else {
            logSessionUsageEvent(analyticsSessions, force);
            analyticsSessions.updateUsageTime();
        }
    }

    private void onWindowClose(AnalyticsSessions analyticsSessions) {
        if (analyticsSessions.getIdleUsageTime() <= 60000) { // 1 min
            logSessionUsageEvent(analyticsSessions, true);
            analyticsSessions.updateUsageTime();
        }
    }

    private void logSessionUsageEvent(AnalyticsSessions analyticsSessions, boolean force) {
        if (force || analyticsSessions.getIdleLogTime() > 60000) { // 1 min, don't log frequently than once per minute
            Map<String, String> parameters = new HashMap<>();
            parameters.put("SESSION-ID", analyticsSessions.getId());

            analyticsEventLoggerExt.logEvent("session-usage", parameters);

            UsersWorkspace workspace = appContext.getWorkspace();

            if (workspace != null && workspace.isTemporary()) {
                analyticsEventLoggerExt.logEvent("session-usage", parameters);
            }

            analyticsSessions.updateLogTime();
        }
    }


    private void processStartupActionUrl(){
        appContext.setStartUpActions(StartUpActionsParser.getStartUpActions());
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

}
