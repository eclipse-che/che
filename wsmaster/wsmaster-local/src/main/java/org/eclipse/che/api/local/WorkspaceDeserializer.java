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

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import java.lang.reflect.Type;

/**
 * @author Mihail Kuznyetsov
 */
public class WorkspaceDeserializer implements JsonDeserializer<WorkspaceImpl> {
    @Override
    public WorkspaceImpl deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        WorkspaceImpl impl = new Gson().fromJson(jsonElement, WorkspaceImpl.class);
        impl.setAccount(new AccountImpl(null, jsonElement.getAsJsonObject().get("namespace").getAsString(), null));
        return impl;
    }
}
