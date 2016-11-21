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

import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;

/**
 * Responsible for persisting and restoring IDE state across sessions.
 * Uses user preferences as storage for serialized state.
 *
 * @author Artem Zatsarynnyi
 * @author Yevhen Vydolob
 */
@Singleton
public class AppStateManager {

    /** The name of the property for the mappings in user preferences. */
    public static final String PREFERENCE_PROPERTY_NAME = "IdeAppStates";

    private static final String WORKSPACE = "workspace";

    private final Map<String, StateComponent> persistenceComponents;
    private final PreferencesManager          preferencesManager;
    private final JsonFactory                 jsonFactory;
    private       JsonObject                  allWsState;

    @Inject
    public AppStateManager(Map<String, StateComponent> persistenceComponents,
                           PreferencesManager preferencesManager,
                           JsonFactory jsonFactory) {
        this.persistenceComponents = persistenceComponents;
        this.preferencesManager = preferencesManager;
        this.jsonFactory = jsonFactory;
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
                for (String key : workspace.keys()) {
                    if (persistenceComponents.containsKey(key)) {
                        StateComponent component = persistenceComponents.get(key);
                        component.loadState(workspace.getObject(key));
                    }
                }
            }
        } catch (JsonException e) {
            Log.error(getClass(), e);
        }
    }

    public Promise<Void> persistWorkspaceState(String wsId) {
        final JsonObject settings = Json.createObject();
        JsonObject workspace = Json.createObject();
        settings.put(WORKSPACE, workspace);
        for (Map.Entry<String, StateComponent> entry : persistenceComponents.entrySet()) {
            String key = entry.getKey();
            workspace.put(key, entry.getValue().getState());
        }
        allWsState.put(wsId, settings);
        return writeStateToPreferences(allWsState);
    }

    private Promise<Void> writeStateToPreferences(JsonObject state) {
        final String json = state.toJson();
        Log.info(getClass(), "write: " + json);
        preferencesManager.setValue(PREFERENCE_PROPERTY_NAME, json);
        return preferencesManager.flushPreferences().catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(AppStateManager.class, "Failed to store app's state to user's preferences");
            }
        });
    }

    public boolean hasStateForWorkspace(String wsId) {
        return allWsState.hasKey(wsId);
    }

}
