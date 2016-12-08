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

import elemental.json.JsonException;
import elemental.json.JsonFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JsonRpcEntityValidator {
    private final JsonFactory jsonFactory;

    @Inject
    public JsonRpcEntityValidator(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void validate(String message) throws JsonRpcException {
        try {
            jsonFactory.parse(message);
        } catch (JsonException e) {
            throw new JsonRpcException(-32700, "An error occurred on the server while parsing the JSON text:", e.getMessage());
        }
    }
}
