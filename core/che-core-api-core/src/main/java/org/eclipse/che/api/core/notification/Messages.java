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
package org.eclipse.che.api.core.notification;

import org.eclipse.che.commons.lang.NameGenerator;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.everrest.core.impl.provider.json.StringValue;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.everrest.websockets.message.InputMessage;
import org.everrest.websockets.message.RestInputMessage;
import org.everrest.websockets.message.RestOutputMessage;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/**
 * @author andrew00x
 */
class Messages {
    static RestInputMessage clientMessage(Object event) throws Exception {
        RestInputMessage message = new RestInputMessage();
        message.setBody(toJson(event));
        message.setMethod(HttpMethod.POST);
        message.setHeaders(new org.everrest.websockets.message.Pair[]{
                new org.everrest.websockets.message.Pair("Content-type", MediaType.APPLICATION_JSON)});
        message.setUuid(NameGenerator.generate(null, 8));
        message.setPath("/event-bus");
        return message;
    }

    static InputMessage subscribeChannelMessage(String channel) throws Exception {
        return RestInputMessage.newSubscribeChannelMessage(NameGenerator.generate(null, 8), channel);
    }

    static ChannelBroadcastMessage broadcastMessage(String channel, Object event) throws Exception {
        final ChannelBroadcastMessage message = new ChannelBroadcastMessage();
        message.setBody(toJson(event));
        message.setChannel(channel);
        return message;
    }

    static Object restoreEventFromBroadcastMessage(RestOutputMessage message) throws Exception {
        return fromJson(message.getBody());
    }

    static Object restoreEventFromClientMessage(String message) throws Exception {
        if (message != null) {
            return fromJson(message);
        }
        return null;
    }

    private static String toJson(Object event) throws Exception {
        final String type = event.getClass().getName();
        final JsonValue json = JsonGenerator.createJsonObject(event);
        json.addElement("$type", new StringValue(type));
        final Writer w = new StringWriter();
        json.writeTo(new JsonWriter(w));
        return w.toString();
    }

    private static Object fromJson(String json) throws Exception {
        if (json == null || json.isEmpty()) {
            return null;
        }
        final JsonParser parser = new JsonParser();
        parser.parse(new StringReader(json));
        final JsonValue node = parser.getJsonObject();
        final JsonValue typeNode = node.getElement("$type");
        final String type;
        if (typeNode == null || (type = typeNode.getStringValue()) == null) {
            return null;
        }
        return ObjectBuilder.createObject(Class.forName(type), node);
    }

    private Messages() {
    }
}
