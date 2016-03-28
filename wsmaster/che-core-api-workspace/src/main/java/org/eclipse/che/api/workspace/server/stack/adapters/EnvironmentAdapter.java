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

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;

import java.lang.reflect.Type;

/**
 * Type adapter for {@link Environment} objects
 *
 * @author Alexander Andrienko
 */
public class EnvironmentAdapter implements JsonSerializer<Environment>, JsonDeserializer<Environment> {
    @Override
    public Environment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(jsonElement, EnvironmentImpl.class);
    }

    @Override
    public JsonElement serialize(Environment src, Type type, JsonSerializationContext context) {
        return context.serialize(src, EnvironmentImpl.class);
    }
}
