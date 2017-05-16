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
package org.eclipse.che.api.testing.server.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.che.api.testing.shared.messages.TestingMessage;
import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.testing.shared.Constants.ATTRIBUTES;
import static org.eclipse.che.api.testing.shared.Constants.NAME;


/**
 * Base class for all test messages.
 * Utilise parsing of the messages.
 * <p>
 * Base format of messages is:
 * <p>
 * <pre>
 *  @@<{"name":"message_name","attributes":{"attribute":"value"}}>
 * </pre>
 */
public class ServerTestingMessage implements TestingMessage {


    public static final ServerTestingMessage TESTING_STARTED = new ServerTestingMessage(TestingMessageNames.TESTING_STARTED);
    public static final ServerTestingMessage FINISH_TESTING = new ServerTestingMessage(TestingMessageNames.FINISH_TESTING);

    public static final String TESTING_MESSAGE_START = "@@<";
    public static final String TESTING_MESSAGE_END = ">";


    private static final Gson GSON = new Gson();

    private String messageName;
    private Map<String, String> attributes = new HashMap<>();

    protected ServerTestingMessage(String messageName) {
        this(messageName, null);
    }

    protected ServerTestingMessage(String messageName, Map<String, String> attributes) {
        this.messageName = messageName;
        setAttributes(attributes);
    }

    public static ServerTestingMessage parse(String text) {
        if (text.startsWith(TESTING_MESSAGE_START) && text.endsWith(TESTING_MESSAGE_END)) {
            return internalParse(text.substring(TESTING_MESSAGE_START.length(), text.length() - TESTING_MESSAGE_END.length()).trim());
        }
        return null;
    }

    private static ServerTestingMessage internalParse(String text) {

        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(text).getAsJsonObject();

            String name = jsonObject.getAsJsonPrimitive(NAME).getAsString();
            Map<String, String> attributes = new HashMap<>();
            if (jsonObject.has(ATTRIBUTES)) {
                Set<Map.Entry<String, JsonElement>> entries = jsonObject.getAsJsonObject(ATTRIBUTES).entrySet();
                for (Map.Entry<String, JsonElement> entry : entries) {
                    attributes.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return new ServerTestingMessage(name, attributes);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String asJsonString() {
        JsonObject object = new JsonObject();
        object.addProperty(NAME, messageName);
        if (!attributes.isEmpty()) {
            JsonObject att = new JsonObject();
            attributes.forEach(att::addProperty);
            object.add(ATTRIBUTES, att);
        }
        return GSON.toJson(object);
    }

    @Override
    public String getName() {
        return messageName;
    }

    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    protected void setAttributes(Map<String, String> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }

}
