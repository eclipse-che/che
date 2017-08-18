/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.statepersistance;

import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * Responsible for persisting and restoring IDE state across sessions.
 * Uses user preferences as storage for serialized state.
 *
 * @author Artem Zatsarynnyi
 * @author Yevhen Vydolob
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AppStateManager {

    /** The name of the property for the mappings in user preferences. */
    public static final String PREFERENCE_PROPERTY_NAME = "IdeAppStates";

    private static final String WORKSPACE = "workspace";

    /**
     * Sorted by execution priority list of persistence state components.
     */
    private final List<StateComponent> persistenceComponents;

    private final PreferencesManager preferencesManager;
    private final JsonFactory        jsonFactory;
    private final PromiseProvider    promises;
    private final AppContext         appContext;
    private       JsonObject         allWsState;

    @Inject
    public AppStateManager(Set<StateComponent> persistenceComponents,
                           PreferencesManager preferencesManager,
                           JsonFactory jsonFactory,
                           PromiseProvider promises,
                           EventBus eventBus,
                           AppContext appContext) {
        this.persistenceComponents = persistenceComponents.stream()
                                                          .sorted(comparingInt(StateComponent::getPriority).reversed())
                                                          .collect(toList());
        this.preferencesManager = preferencesManager;
        this.jsonFactory = jsonFactory;
        this.promises = promises;
        this.appContext = appContext;

        // delay is required because we need to wait some time while different components initialized
        eventBus.addHandler(WorkspaceReadyEvent.getType(), e -> restoreWorkspaceStateWithDelay());

        eventBus.addHandler(WindowActionEvent.TYPE, new WindowActionHandler() {
            @Override
            public void onWindowClosing(WindowActionEvent event) {
                final Workspace workspace = appContext.getWorkspace();
                if (workspace != null) {
                    persistWorkspaceState();
                }
            }

            @Override
            public void onWindowClosed(WindowActionEvent event) {
            }
        });
    }

    public void readStateFromPreferences() {
        final String json = preferencesManager.getValue(PREFERENCE_PROPERTY_NAME);
        if (json == null) {
            allWsState = jsonFactory.createObject();
        } else {
            try {
                allWsState = jsonFactory.parse(json);
            } catch (Exception e) {
                // create 'clear' state if any deserializing error occurred
                allWsState = jsonFactory.createObject();
            }
        }
    }

    @VisibleForTesting
    void restoreWorkspaceState() {
        final String wsId = appContext.getWorkspace().getId();

        if (allWsState.hasKey(wsId)) {
            restoreState(allWsState.getObject(wsId));
        }
    }

    private void restoreWorkspaceStateWithDelay() {
        new Timer() {
            @Override
            public void run() {
                restoreWorkspaceState();
            }
        }.schedule(1000);
    }

    private void restoreState(JsonObject settings) {
        try {
            if (settings.hasKey(WORKSPACE)) {
                JsonObject workspace = settings.getObject(WORKSPACE);
                Promise<Void> sequentialRestore = promises.resolve(null);
                for (String key : workspace.keys()) {
                    Optional<StateComponent> stateComponent = persistenceComponents.stream()
                                                                                   .filter(component -> component.getId().equals(key))
                                                                                   .findAny();
                    if (stateComponent.isPresent()) {
                        StateComponent component = stateComponent.get();
                        Log.debug(getClass(), "Restore state for the component ID: " + component.getId());
                        sequentialRestore = sequentialRestore.thenPromise(ignored -> component.loadState(workspace.getObject(key)));
                    }
                }
            }
        } catch (JsonException e) {
            Log.error(getClass(), e);
        }
    }

    public Promise<Void> persistWorkspaceState() {
        String wsId = appContext.getWorkspace().getId();
        JsonObject settings = Json.createObject();
        JsonObject workspace = Json.createObject();
        settings.put(WORKSPACE, workspace);

        for (StateComponent entry : persistenceComponents) {
            try {
                Log.debug(getClass(), "Persist state for the component ID: " + entry.getId());
                workspace.put(entry.getId(), entry.getState());
            } catch (Exception e) {
                Log.error(getClass(), e);
            }
        }
        allWsState.put(wsId, settings);
        return writeStateToPreferences(allWsState);
    }

    private Promise<Void> writeStateToPreferences(JsonObject state) {
        final String json = state.toJson();
        preferencesManager.setValue(PREFERENCE_PROPERTY_NAME, json);
        return preferencesManager.flushPreferences().catchError(error -> {
            Log.error(AppStateManager.class, "Failed to store app's state to user's preferences: " + error.getMessage());
        });
    }

    @Deprecated
    public boolean hasStateForWorkspace(String wsId) {
        return allWsState.hasKey(wsId);
    }

}
