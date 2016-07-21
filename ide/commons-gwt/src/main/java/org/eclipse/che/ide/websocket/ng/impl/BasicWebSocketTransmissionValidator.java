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
package org.eclipse.che.ide.websocket.ng.impl;

import com.google.gwt.json.client.JSONParser;

import org.eclipse.che.api.core.websocket.shared.WebSocketTransmission;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of WEB SOCKET transmission validator, validation rules are the following:
 *
 * <ul>
 * <li><code>protocol</code> must be not <code>null</code></li>
 * <li><code>protocol</code> must be not empty</li>
 * <li><code>protocol</code> must be registered (mapped to a corresponding receiver implementation</li>
 * <li><code>message</code> must be not <code>null</code></li>
 * <li><code>message</code> must be not empty</li>
 * <li><code>message</code> must be a valid JSON</li>
 * </ul>
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class BasicWebSocketTransmissionValidator implements WebSocketTransmissionValidator {

    private final Set<String> protocols;

    @Inject
    public BasicWebSocketTransmissionValidator(Map<String, WebSocketMessageReceiver> receivers) {
        this.protocols = receivers.keySet();
    }


    @Override
    public void validate(WebSocketTransmission transmission) {
        validateProtocol(transmission.getProtocol());
        validateMessage(transmission.getMessage());
    }

    private void validateProtocol(String protocol) {
        if (protocols.contains(protocol)) {
            Log.debug(getClass(), "Web socket transmission protocol {} is among registered", protocol);
        } else {
            logError("Web socket transmission of not registered protocol");
        }
    }

    private void validateMessage(String message) {
        validateNull(message);
        validateEmpty(message);
        validateJson(message);
    }

    private void validateNull(String message) {
        if (message == null) {
            logError("Web socket transmission message is null");
        } else {
            Log.debug(getClass(), "Web socket transmission message is not null");
        }
    }

    private void validateEmpty(String message) {
        if (message.isEmpty()) {
            logError("Web socket transmission message is empty");
        } else {
            Log.debug(getClass(), "Web socket transmission message is not empty");
        }
    }

    private void validateJson(String message) {
        boolean error = false;

        try {
            JSONParser.parseStrict(message);
        } catch (Exception e) {
            error = true;
        }

        if (error) {
            logError("Web socket transmission message is not a valid json");
        } else {
            Log.debug(getClass(), "Web socket transmission message is a valid json");
        }
    }

    private void logError(String error) {
        Log.error(getClass(), error);
        throw new IllegalArgumentException(error);
    }
}
