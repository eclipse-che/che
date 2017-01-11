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
package org.eclipse.che.ide.jsonrpc;

import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Qualifier is used to qualify JSON RPC incoming entity. Entities can be of
 * several types: {@link JsonRpcEntityType#REQUEST},
 * {@link JsonRpcEntityType#RESPONSE}, {@link JsonRpcEntityType#UNDEFINED}.
 * This implementations uses {@link JsonFactory} to parse and analyze incoming
 * entities and expects that message that is to be qualified is a valid json.
 */
@Singleton
public class JsonRpcEntityQualifier {

    private final JsonFactory jsonFactory;

    @Inject
    public JsonRpcEntityQualifier(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public JsonRpcEntityType qualify(String message) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        Log.debug(getClass(), "Qualifying message: " + message);

        JsonObject jsonObject = jsonFactory.parse(message);
        List<String> keys = asList(jsonObject.keys());

        Log.debug(getClass(), "Json keys: " + keys);

        if (keys.contains("method")) {
            Log.debug(getClass(), "Qualified to request");

            return JsonRpcEntityType.REQUEST;
        } else if (keys.contains("error") != keys.contains("result")) {
            Log.debug(getClass(), "Qualified to response");

            return JsonRpcEntityType.RESPONSE;
        } else {
            Log.debug(getClass(), "Qualified to undefined");

            return JsonRpcEntityType.UNDEFINED;
        }
    }

    public enum JsonRpcEntityType {
        REQUEST,
        RESPONSE,
        UNDEFINED
    }
}
