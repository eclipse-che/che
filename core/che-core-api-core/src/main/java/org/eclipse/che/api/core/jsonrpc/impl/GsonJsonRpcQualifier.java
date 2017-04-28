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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.eclipse.che.api.core.logger.commons.Logger;
import org.eclipse.che.api.core.logger.commons.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class GsonJsonRpcQualifier implements JsonRpcQualifier {
    private final Logger     logger;
    private final JsonParser jsonParser;

    @Inject
    public GsonJsonRpcQualifier(LoggerFactory loggerFactory, JsonParser jsonParser) {
        this.logger = loggerFactory.get(getClass());
        this.jsonParser = jsonParser;
    }

    @Override
    public boolean isValidJson(String message) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");

        logger.debug("Validating message: {}", message);

        try {
            jsonParser.parse(message);

            logger.debug("Validation successful");
            return true;
        } catch (JsonParseException e) {
            logger.debug("Validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isJsonRpcRequest(String message) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");
        logger.debug("Qualifying message: " + message);

        JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
        logger.debug("Json keys: " + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

        if (jsonObject.has("method")) {
            logger.debug("Qualified to request");
            return true;
        } else {
            logger.debug("Qualified to response");
            return false;
        }
    }

    @Override
    public boolean isJsonRpcResponse(String message) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");
        logger.debug("Qualifying message: " + message);

        JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
        logger.debug("Json keys: " + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

        if (jsonObject.has("error") != jsonObject.has("result")) {
            logger.debug("Qualified to response");
            return true;
        }
        return false;
    }
}
