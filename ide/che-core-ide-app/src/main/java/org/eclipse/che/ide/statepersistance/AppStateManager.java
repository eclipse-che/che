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
package org.eclipse.che.ide.statepersistance;

import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

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

    private final Map<String, Provider<StateComponent>> persistenceComponents;
    private final PreferencesManager                    preferencesManager;
    private final JsonFactory                           jsonFactory;
    private final AppContext                            appContext;
    private       JsonObject                            allWsState;

    @Inject
    public AppStateManager(Map<String, Provider<StateComponent>> persistenceComponents,
                           PreferencesManager preferencesManager,
                           JsonFactory jsonFactory,
                           EventBus eventBus,
                           AppContext appContext) {
        this.persistenceComponents = persistenceComponents;
        this.preferencesManager = preferencesManager;
        this.jsonFactory = jsonFactory;
        this.appContext = appContext;

        eventBus.addHandler(WsStatusChangedEvent.TYPE, event -> {
            if (event.getStatus() == RUNNING) {
                Scheduler.get().scheduleDeferred(this::restoreWorkspaceState);
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
        final String wsId = appContext.getWorkspaceId();

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
                        StateComponent component = persistenceComponents.get(key).get();
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
        for (Map.Entry<String, Provider<StateComponent>> entry : persistenceComponents.entrySet()) {
            try {
                String key = entry.getKey();
                workspace.put(key, entry.getValue().get().getState());
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
