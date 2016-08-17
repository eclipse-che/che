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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of JSON RPC object validator. Validation rules are simple:
 *
 * <ul>
 * <li><code>type</code> must not be <code>null</code></li>
 * <li><code>type</code> must not be empty</li>
 * <li><code>type</code> must be registered (mapped to a corresponding receiver implementation)</li>
 * <li><code>message</code> must not be <code>null</code></li>
 * <li><code>message</code> must not be empty</li>
 * <li><code>message</code> must be a valid JSON</li>
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicJsonRpcObjectValidator implements JsonRpcObjectValidator {
    private static final Logger LOG = LoggerFactory.getLogger(BasicJsonRpcObjectValidator.class);

    private final Set<String> registeredTypes;

    @Inject
    public BasicJsonRpcObjectValidator(Map<String, JsonRpcDispatcher> dispatchers) {
        this.registeredTypes = dispatchers.keySet();
    }

    @Override
    public void validate(JsonRpcObject object) {
        validateType(object.getType());
        validateMessage(object.getMessage());
    }

    private void validateType(String type) {
        if (registeredTypes.contains(type)) {
            LOG.trace("Json rpc object type {} is among registered", type);
        } else {
            logError("Json rpc object is of not registered type");
        }
    }

    private void validateMessage(String message) {
        validateNull(message);
        validateEmpty(message);
        validateJson(message);
    }

    private void validateNull(String message) {
        if (message == null) {
            logError("Json rpc object message is null");
        } else {
            LOG.trace("Json rpc object message is not null");
        }
    }

    private void validateEmpty(String message) {
        if (message.isEmpty()) {
            logError("Json rpc object message is empty");
        } else {
            LOG.trace("Json rpc object message is not empty");
        }
    }

    private void validateJson(String message) {
        boolean error = false;

        try {
            new JsonParser().parse(message);
        } catch (JsonSyntaxException e) {
            error = true;
        }

        if (error) {
            logError("Json rpc object message is not a valid json");
        } else {
            LOG.trace("Json rpc object message is a valid json");
        }
    }

    private void logError(String error) {
        LOG.error(error);
        throw new IllegalArgumentException(error);
    }
}
