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

import com.google.gwt.json.client.JSONParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
