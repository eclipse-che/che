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
package org.eclipse.che.api.core.jsonrpc;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.jsonrpc.transmission.SendConfiguratorFromOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple class to validate if we're dealing with a properly constructed
 * json represented by a string. We use {@link JsonParser} to parse string
 * message and to rise exception if json is incorrect.
 */
@Singleton
public class JsonRpcEntityValidator {
    private static final Logger LOG = LoggerFactory.getLogger(SendConfiguratorFromOne.class);

    private final JsonParser jsonParser;

    @Inject
    public JsonRpcEntityValidator(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public void validate(String message) throws JsonRpcException {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        LOG.debug("Validating message: {}", message);

        try {
            jsonParser.parse(message);

            LOG.debug("Validation successful");
        } catch (JsonParseException e) {
            LOG.debug("Validation failed: {}", e.getMessage(), e);

            throw new JsonRpcException(-32700, "An error occurred on the server while parsing the JSON text:", e.getMessage());
        }
    }
}
