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
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.machine.server.recipe.PermissionsImpl;
import org.eclipse.che.api.machine.shared.Permissions;

import java.lang.reflect.Type;

/**
 * Custom implementation of deserialize Permission objects
 *
 * @author Anton Korneta
 */
public class PermissionsAdapter implements JsonDeserializer<Permissions>, JsonSerializer<Permissions> {

    @Override
    public Permissions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, PermissionsImpl.class);
    }

    @Override
    public JsonElement serialize(Permissions src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src, PermissionsImpl.class);
    }
}