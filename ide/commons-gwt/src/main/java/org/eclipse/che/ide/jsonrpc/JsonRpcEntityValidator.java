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

import elemental.json.JsonException;
import elemental.json.JsonFactory;

import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple class to validate if we're dealing with a properly constructed
 * json represented by a string. We use {@link JsonFactory} to parse string
 * message and to rise exception if json is incorrect.
 */
@Singleton
public class JsonRpcEntityValidator {
    private final JsonFactory jsonFactory;

    @Inject
    public JsonRpcEntityValidator(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public void validate(String message) throws JsonRpcException {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        Log.debug(getClass(), "Validating message: " + message);

        try {
            jsonFactory.parse(message);

            Log.debug(getClass(), "Validation successful");
        } catch (JsonException e) {
            Log.debug(getClass(), "Validation failed: " + e.getMessage());

            throw new JsonRpcException(-32700, "An error occurred on the server while parsing the JSON text:", e.getMessage());
        }
    }
}
