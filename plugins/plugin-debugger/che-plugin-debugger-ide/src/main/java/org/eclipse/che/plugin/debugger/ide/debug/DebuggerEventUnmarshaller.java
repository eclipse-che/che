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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import org.eclipse.che.api.debug.shared.dto.event.BreakpointActivatedEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DebuggerEventDto;
import org.eclipse.che.api.debug.shared.dto.event.DisconnectEventDto;
import org.eclipse.che.api.debug.shared.dto.event.SuspendEventDto;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent.TYPE;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

/**
 * Unmarshaller for deserializing debugger event which is received over WebSocket connection.
 *
 * @author Artem Zatsarynnyi
 */
public class DebuggerEventUnmarshaller implements Unmarshallable<DebuggerEventDto> {
    private DtoFactory       dtoFactory;
    private DebuggerEventDto event;

    public DebuggerEventUnmarshaller(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    @Override
    public void unmarshal(Message response) throws UnmarshallerException {
        JSONObject jsonObject = JSONParser.parseStrict(response.getBody()).isObject();
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.containsKey("type")) {
            String type = jsonObject.get("type").isString().stringValue();
            TYPE eventType = TYPE.valueOf(type);
            switch (eventType) {
                case SUSPEND:
                    event = dtoFactory.createDtoFromJson(jsonObject.toString(), SuspendEventDto.class);
                    break;
                case DISCONNECT:
                    event = dtoFactory.createDtoFromJson(jsonObject.toString(), DisconnectEventDto.class);
                    break;
                case BREAKPOINT_ACTIVATED:
                    event = dtoFactory.createDtoFromJson(jsonObject.toString(), BreakpointActivatedEventDto.class);
                    break;
                default:
                    throw new UnmarshallerException("Can't parse response.",
                                                    new IllegalArgumentException("Unknown debug event type: " + eventType));
            }
        }
    }

    @Override
    public DebuggerEventDto getPayload() {
        return event;
    }
}
