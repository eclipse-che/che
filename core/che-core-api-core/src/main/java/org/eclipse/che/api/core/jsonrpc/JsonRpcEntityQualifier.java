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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.api.core.jsonrpc.transmission.SendConfiguratorFromOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Qualifier is used to qualify JSON RPC incoming entity. Entities can be of
 * several types: {@link JsonRpcEntityType#REQUEST},
 * {@link JsonRpcEntityType#RESPONSE}, {@link JsonRpcEntityType#UNDEFINED}.
 * This implementations uses {@link JsonParser} to parse and analyze incoming
 * entities and expects that message that is to be qualified is a valid json.
 */
@Singleton
public class JsonRpcEntityQualifier {
    private static final Logger LOG = LoggerFactory.getLogger(SendConfiguratorFromOne.class);

    private final JsonParser jsonParser;

    @Inject
    public JsonRpcEntityQualifier(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public JsonRpcEntityType qualify(String message) {
        checkNotNull(message, "Message must not be null");
        checkArgument(!message.isEmpty(), "Message must not be empty");
        LOG.debug("Qualifying message: " + message);

        JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
        LOG.debug("Json keys: " + jsonObject.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet()));

        if (jsonObject.has("method")) {
            LOG.debug("Qualified to request");

            return JsonRpcEntityType.REQUEST;
        } else if (jsonObject.has("error") != jsonObject.has("result")) {
            LOG.debug("Qualified to response");

            return JsonRpcEntityType.RESPONSE;
        } else {
            LOG.debug("Qualified to undefined");

            return JsonRpcEntityType.UNDEFINED;
        }
    }

    public enum JsonRpcEntityType {
        REQUEST,
        RESPONSE,
        UNDEFINED
    }
}
