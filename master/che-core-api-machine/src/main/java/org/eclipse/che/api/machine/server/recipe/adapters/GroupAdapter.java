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

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.machine.server.recipe.GroupImpl;
import org.eclipse.che.api.machine.shared.Group;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Custom implementation of deserialize Group objects.
 *
 * @author Anton Korneta
 */
public class GroupAdapter implements JsonDeserializer<Group>, JsonSerializer<Group> {

    @Override
    public Group deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject groupObj = json.getAsJsonObject();
        return new GroupImpl(groupObj.get("name") == null ? null : groupObj.get("name").getAsString(),
                             groupObj.get("unit") == null ? null : groupObj.get("unit").getAsString(),
                             context.deserialize(groupObj.get("acl"), new TypeToken<List<String>>() {}.getType()));
    }

    @Override
    public JsonElement serialize(Group src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src, GroupImpl.class);
    }
}