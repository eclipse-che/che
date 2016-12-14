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
package org.eclipse.che.ide.jsonrpc;

import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

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
        JsonObject jsonObject = jsonFactory.parse(message);
        List<String> keys = Arrays.asList(jsonObject.keys());
        if (keys.contains("method")) {
            return JsonRpcEntityType.REQUEST;
        } else if (keys.contains("error") != keys.contains("result")) {
            return JsonRpcEntityType.RESPONSE;
        } else {
            return JsonRpcEntityType.UNDEFINED;
        }
    }

    public enum JsonRpcEntityType {
        REQUEST,
        RESPONSE,
        UNDEFINED;
    }
}
