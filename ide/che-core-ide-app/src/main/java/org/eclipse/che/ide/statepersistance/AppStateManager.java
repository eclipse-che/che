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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
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
    private       JsonObject         allWsState;

    @Inject
    public AppStateManager(Set<StateComponent> persistenceComponents,
                           PreferencesManager preferencesManager,
                           JsonFactory jsonFactory,
                           PromiseProvider promises) {
        this.persistenceComponents = persistenceComponents.stream()
                                                          .sorted(comparingInt(StateComponent::getPriority).reversed())
                                                          .collect(toList());
        this.preferencesManager = preferencesManager;
        this.jsonFactory = jsonFactory;
        this.promises = promises;
        readStateFromPreferences();
    }

    private void readStateFromPreferences() {
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

    public void restoreWorkspaceState(String wsId) {
        if (allWsState.hasKey(wsId)) {
            restoreState(allWsState.getObject(wsId));
        }
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

    public Promise<Void> persistWorkspaceState(String wsId) {
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
