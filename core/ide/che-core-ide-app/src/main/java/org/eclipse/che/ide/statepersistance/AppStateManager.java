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
package org.eclipse.che.ide.statepersistance;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateHandler;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.event.WindowActionHandler;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.event.ProjectExplorerLoadedEvent;
import org.eclipse.che.ide.statepersistance.dto.ActionDescriptor;
import org.eclipse.che.ide.statepersistance.dto.AppState;
import org.eclipse.che.ide.statepersistance.dto.WorkspaceState;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.start.StopWorkspaceEvent;
import org.eclipse.che.ide.workspace.start.StopWorkspaceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for persisting and restoring IDE state across sessions.
 * Uses user preferences as storage for serialized state.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class AppStateManager implements WindowActionHandler,
                                        StopWorkspaceHandler,
                                        ExtServerStateHandler,
                                        ProjectExplorerLoadedEvent.ProjectExplorerLoadedHandler {

    /** The name of the property for the mappings in user preferences. */
    public static final String PREFERENCE_PROPERTY_NAME = "IdeAppState";

    private final Set<PersistenceComponent>    persistenceComponents;
    private final PreferencesManager           preferencesManager;
    private final AppContext                   appContext;
    private final DtoFactory                   dtoFactory;
    private final ActionManager                actionManager;
    private final PresentationFactory          presentationFactory;
    private final Provider<PerspectiveManager> perspectiveManagerProvider;

    private AppState appState;

    @Inject
    public AppStateManager(Set<PersistenceComponent> persistenceComponents,
                           PreferencesManager preferencesManager,
                           AppContext appContext,
                           DtoFactory dtoFactory,
                           ActionManager actionManager,
                           PresentationFactory presentationFactory,
                           Provider<PerspectiveManager> perspectiveManagerProvider,
                           EventBus eventBus) {
        this.persistenceComponents = persistenceComponents;
        this.preferencesManager = preferencesManager;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.actionManager = actionManager;
        this.presentationFactory = presentationFactory;
        this.perspectiveManagerProvider = perspectiveManagerProvider;

        eventBus.addHandler(StopWorkspaceEvent.TYPE, this);
        eventBus.addHandler(WindowActionEvent.TYPE, this);
        eventBus.addHandler(ExtServerStateEvent.TYPE, this);
        eventBus.addHandler(ProjectExplorerLoadedEvent.getType(), this);

        readStateFromPreferences();
    }

    private void readStateFromPreferences() {
        final String json = preferencesManager.getValue(PREFERENCE_PROPERTY_NAME);
        if (json == null) {
            appState = dtoFactory.createDto(AppState.class);
        } else {
            try {
                appState = dtoFactory.createDtoFromJson(json, AppState.class);
            } catch (Exception e) {
                // create 'clear' state if any deserializing error occurred
                appState = dtoFactory.createDto(AppState.class);
            }
        }
    }

    @Override
    public void onWindowClosing(WindowActionEvent event) {
        persistWorkspaceState();
    }

    @Override
    public void onWindowClosed(WindowActionEvent event) {
    }

    @Override
    public void onExtServerStarted(ExtServerStateEvent event) {
    }

    @Override
    public void onExtServerStopped(ExtServerStateEvent event) {
        persistWorkspaceState();
    }

    @Override
    public void onWorkspaceStopped(UsersWorkspaceDto workspace) {
        persistWorkspaceState();
    }

    private void persistWorkspaceState() {
        appState.setRecentWorkspaceId(appContext.getWorkspace().getId());

        final WorkspaceState workspaceState = dtoFactory.createDto(WorkspaceState.class);
        appState.getWorkspaces().put(appContext.getWorkspace().getId(), workspaceState);

        final List<ActionDescriptor> actions = workspaceState.getActions();
        for (PersistenceComponent persistenceComponent : persistenceComponents) {
            actions.addAll(persistenceComponent.getActions());
        }

        writeStateToPreferences();
    }

    private void writeStateToPreferences() {
        final String json = dtoFactory.toJson(appState);
        preferencesManager.setValue(PREFERENCE_PROPERTY_NAME, json);
        preferencesManager.flushPreferences(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> result) {
            }

            @Override
            public void onFailure(Throwable caught) {
                Log.error(AppStateManager.class, "Failed to store app's state to user's preferences");
            }
        });
    }

    @Override
    public void onProjectsLoaded(ProjectExplorerLoadedEvent event) {
        restoreWorkspaceState();
    }

    private void restoreWorkspaceState() {
        final WorkspaceState workspaceState = appState.getWorkspaces().get(appContext.getWorkspace().getId());
        if (workspaceState == null) {
            return;
        }

        List<ActionDescriptor> actions = workspaceState.getActions();
        if (actions.isEmpty()) {
            return;
        }

        List<Pair<Action, ActionEvent>> actionsToPerform = new ArrayList<>(actions.size());
        for (ActionDescriptor actionDescriptor : actions) {
            final Action action = actionManager.getAction(actionDescriptor.getId());
            if (action == null) {
                continue;
            }

            actionsToPerform.add(new Pair<>(action, new ActionEvent(presentationFactory.getPresentation(action),
                                                                    actionManager,
                                                                    perspectiveManagerProvider.get(),
                                                                    actionDescriptor.getParameters())));
        }

        actionManager.performActions(actionsToPerform, false).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.info(AppStateManager.class, "Failed to restore workspace state.");
            }
        });
    }
}
