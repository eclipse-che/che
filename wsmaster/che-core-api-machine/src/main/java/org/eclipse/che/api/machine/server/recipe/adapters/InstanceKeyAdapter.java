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
package org.eclipse.che.api.machine.server.recipe.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.machine.server.spi.InstanceKey;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Type adapter for {@link InstanceKey}.
 *
 * @author Yevhenii Voevodin
 */
public class InstanceKeyAdapter implements JsonDeserializer<InstanceKey>, JsonSerializer<InstanceKey> {

    @Override
    public InstanceKey deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject recipeObj = jsonElement.getAsJsonObject();
        return new InstanceKey() {

            Map<String, String> fields;

            @Override
            public Map<String, String> getFields() {
                if (fields == null) {
                    fields = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : recipeObj.entrySet()) {
                        fields.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                return fields;
            }
        };
    }

    @Override
    public JsonElement serialize(InstanceKey instanceKey, Type type, JsonSerializationContext context) {
        final JsonObject fields = new JsonObject();
        for (Map.Entry<String, String> entry : instanceKey.getFields().entrySet()) {
            fields.addProperty(entry.getKey(), entry.getValue());
        }
        return fields;
    }
}
