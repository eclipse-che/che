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
package org.eclipse.che.api.machine.server.model.impl.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;

import java.lang.reflect.Type;

/**
 * Type adapter for {@link MachineSource}.
 *
 * @author Florent Benoit
 */
public class MachineSourceAdapter implements JsonDeserializer<MachineSource>, JsonSerializer<MachineSource> {

    @Override
    public MachineSource deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(jsonElement, MachineSourceImpl.class);
    }

    @Override
    public JsonElement serialize(MachineSource machineSource, Type type, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        // we can't rely on MachineSourceImpl as custom InstanceProvider can build their own implementation
        jsonObject.addProperty("content", machineSource.getContent());
        jsonObject.addProperty("location", machineSource.getLocation());
        jsonObject.addProperty("type", machineSource.getType());

        return jsonObject;
    }
}
