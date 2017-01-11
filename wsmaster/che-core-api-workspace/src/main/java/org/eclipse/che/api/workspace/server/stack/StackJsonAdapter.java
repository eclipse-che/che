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
package org.eclipse.che.api.workspace.server.stack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Singleton;

import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;

import javax.inject.Inject;

import static org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter.findDevMachine;

/**
 * Moves stack source to the workspace configuration, and adapts
 * workspace configuration from an old format to a new one.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class StackJsonAdapter {

    private final WorkspaceConfigJsonAdapter workspaceConfigAdapter;

    @Inject
    public StackJsonAdapter(WorkspaceConfigJsonAdapter workspaceConfigAdapter) {
        this.workspaceConfigAdapter = workspaceConfigAdapter;
    }

    public void adaptModifying(JsonObject stack) {
        // inject a new source into workspace configuration dev machine's source
        // and adapt an old workspace configuration to a new one
        final JsonElement wsConfEl = stack.get("workspaceConfig");
        if (wsConfEl != null && wsConfEl.isJsonObject()) {
            final JsonObject wsConfig = wsConfEl.getAsJsonObject();
            final JsonObject defaultEnv = findDefaultEnv(wsConfig);
            if (defaultEnv != null) {
                final JsonObject devMachine = findDevMachine(defaultEnv);
                if (devMachine != null) {
                    // consider stack source as the source of the dev-machine
                    if (stack.has("source") && stack.get("source").isJsonObject()) {
                        devMachine.add("source", asWorkspaceConfigSource(stack.getAsJsonObject("source")));
                    }
                }

                // convert workspace config
                workspaceConfigAdapter.adaptModifying(wsConfig);
            }
        }
    }

    private static JsonObject asWorkspaceConfigSource(JsonObject stackSource) {
        final JsonObject newWsSource = new JsonObject();
        if (stackSource.has("type") && !stackSource.get("type").isJsonNull()) {
            final String type = stackSource.get("type").getAsString();
            switch (type) {
                case "image":
                    newWsSource.addProperty("type", "image");
                    if (stackSource.has("origin")) {
                        newWsSource.addProperty("location", stackSource.get("origin").getAsString());
                    }
                    break;
                case "location":
                    newWsSource.addProperty("type", "dockerfile");
                    if (stackSource.has("origin")) {
                        newWsSource.addProperty("location", stackSource.get("origin").getAsString());
                    }
                    break;
                case "dockerfile":
                case "recipe":
                    newWsSource.addProperty("type", "dockerfile");
                    if (stackSource.has("origin")) {
                        newWsSource.addProperty("content", stackSource.get("origin").getAsString());
                    }
                    break;
            }
        }
        return newWsSource;
    }

    private static JsonObject findDefaultEnv(JsonObject wsConfig) {
        final JsonElement defaultEnvNameEl = wsConfig.get("defaultEnv");
        if (defaultEnvNameEl != null && wsConfig.has("environments") && wsConfig.get("environments").isJsonArray()) {
            final String defaultEnvName = defaultEnvNameEl.getAsString();
            for (JsonElement envEl : wsConfig.getAsJsonArray("environments")) {
                if (envEl.isJsonObject()) {
                    final JsonObject envObj = envEl.getAsJsonObject();
                    if (envObj.has("name") && envObj.get("name").getAsString().equals(defaultEnvName)) {
                        return envEl.getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }
}
