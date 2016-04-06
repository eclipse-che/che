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
package org.eclipse.che.ide.ext.debugger.client.debug;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointActivatedEvent;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.debugger.shared.DisconnectEvent;
import org.eclipse.che.ide.ext.debugger.shared.StepEvent;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import java.util.ArrayList;

/**
 * Unmarshaller for deserializing debugger event list, which is received over WebSocket connection.
 *
 * @author Artem Zatsarynnyi
 */
public class DebuggerEventListUnmarshaller implements Unmarshallable<DebuggerEventList> {
    private DtoFactory        dtoFactory;
    private DebuggerEventList events;

    public DebuggerEventListUnmarshaller(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
        this.events = dtoFactory.createDto(DebuggerEventList.class);
    }

    @Override
    public void unmarshal(Message response) throws UnmarshallerException {
        this.events.setEvents(new ArrayList<DebuggerEvent>());

        JSONObject jsonObject = JSONParser.parseStrict(response.getBody()).isObject();
        if (jsonObject == null) {
            return;
        }

        if (jsonObject.containsKey("events")) {
            JSONArray events = jsonObject.get("events").isArray();
            for (int i = 0; i < events.size(); i++) {
                JSONObject event = events.get(i).isObject();
                if (event.containsKey("type")) {
                    final int type = (int)event.get("type").isNumber().doubleValue();
                    if (DebuggerEvent.BREAKPOINT == type) {
                        BreakpointEvent breakpointEvent = dtoFactory.createDtoFromJson(event.toString(), BreakpointEvent.class);
                        this.events.getEvents().add(breakpointEvent);

                    } else if (DebuggerEvent.STEP == type) {
                        StepEvent stepEvent = dtoFactory.createDtoFromJson(event.toString(), StepEvent.class);
                        this.events.getEvents().add(stepEvent);

                    } else if (DebuggerEvent.BREAKPOINT_ACTIVATED == type) {
                        BreakpointActivatedEvent breakpointActivatedEvent =
                                dtoFactory.createDtoFromJson(event.toString(), BreakpointActivatedEvent.class);
                        this.events.getEvents().add(breakpointActivatedEvent);

                    } else if (DebuggerEvent.DISCONNECTED == type) {
                        DisconnectEvent disconnectEvent = dtoFactory.createDtoFromJson(event.toString(), DisconnectEvent.class);
                        this.events.getEvents().add(disconnectEvent);
                    }
                }
            }
        }
    }

    @Override
    public DebuggerEventList getPayload() {
        return events;
    }

}