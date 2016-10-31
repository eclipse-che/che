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
package org.eclipse.che.api.local;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.eclipse.che.api.workspace.server.WorkspaceConfigJsonAdapter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

import java.lang.reflect.Type;

/**
 * @author Yevhenii Voevodin
 */
public class WorkspaceConfigDeserializer implements JsonDeserializer<WorkspaceConfigImpl> {

    private final WorkspaceConfigJsonAdapter adapter;

    public WorkspaceConfigDeserializer(WorkspaceConfigJsonAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public WorkspaceConfigImpl deserialize(JsonElement jsonEl, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        if (jsonEl.isJsonObject()) {
            adapter.adaptModifying(jsonEl.getAsJsonObject());
        }
        return new Gson().fromJson(jsonEl, WorkspaceConfigImpl.class);
    }
}
