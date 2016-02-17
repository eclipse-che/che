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
package org.eclipse.che.api.workspace.server.stack.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;

import java.lang.reflect.Type;

/**
 * Type adapter for {@link ProjectConfig} objects
 *
 * @author Alexander Andrienko
 */
public class ProjectConfigAdapter implements JsonSerializer<ProjectConfig>, JsonDeserializer<ProjectConfig> {
    @Override
    public ProjectConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(jsonElement, ProjectConfigImpl.class);
    }

    @Override
    public JsonElement serialize(ProjectConfig src, Type type, JsonSerializationContext context) {
        return context.serialize(src, ProjectConfigImpl.class);
    }
}
