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
package org.eclipse.che;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;

import java.lang.reflect.Type;

/**
 * The adapter which allows serialize deserialize modules in workspace.json. Each module in our model is presented as project and
 * {@link ProjectConfig} describes module in our model.
 *
 * @author Dmitry Shnurenko
 */
public class ProjectConfigAdapter implements JsonDeserializer<ProjectConfig>, JsonSerializer<ProjectConfig> {

    @Override
    public ProjectConfig deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(element, ProjectConfigImpl.class);
    }

    @Override
    public JsonElement serialize(ProjectConfig moduleConfig, Type type, JsonSerializationContext context) {
        return serializeModule(moduleConfig, context);
    }

    private JsonElement serializeModule(ProjectConfig moduleConfig, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("name", moduleConfig.getName());
        object.addProperty("path", moduleConfig.getPath());
        object.addProperty("type", moduleConfig.getType());
        object.add("attributes", context.serialize(moduleConfig.getAttributes()));
        object.add("source", context.serialize(moduleConfig.getSource()));

        return object;
    }
}
