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
package org.eclipse.che.api.local;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.stack.StackJsonAdapter;

import java.lang.reflect.Type;

/**
 * @author Yevhenii Voevodin
 */
public class StackDeserializer implements JsonDeserializer<StackImpl> {

    private final StackJsonAdapter stackJsonAdapter;

    public StackDeserializer(StackJsonAdapter stackJsonAdapter) {
        this.stackJsonAdapter = stackJsonAdapter;
    }

    @Override
    public StackImpl deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException {
        if (json.isJsonObject()) {
            stackJsonAdapter.adaptModifying(json.getAsJsonObject());
        }
        return new Gson().fromJson(json, StackImpl.class);
    }
}
