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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.workspace.server.stack.image.StackIcon;

import java.lang.reflect.Type;

/**
 * Type adapter for {@link StackIcon} objects.
 *
 * @author Alexander Andrienko
 */
public class StackIconAdapter implements JsonSerializer<StackIcon>, JsonDeserializer<StackIcon> {

    @Override
    public StackIcon deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject stackIconObj = jsonElement.getAsJsonObject();
        return new StackIcon(stackIconObj.get("name") == null ? null : stackIconObj.get("name").getAsString(),
                             stackIconObj.get("mediaType") == null ? null : stackIconObj.get("mediaType").getAsString(),
                             null);
    }

    @Override
    public JsonElement serialize(StackIcon stackIcon, Type type, JsonSerializationContext context) {
        JsonObject stackIconObj = new JsonObject();
        if (stackIcon != null) {
            stackIconObj.addProperty("name", stackIcon.getName());
            stackIconObj.addProperty("mediaType", stackIcon.getMediaType());
        }
        return stackIconObj;
    }
}
