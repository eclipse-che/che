/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.server.messages;

import static org.eclipse.che.api.testing.shared.Constants.ATTRIBUTES;
import static org.eclipse.che.api.testing.shared.Constants.NAME;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.testing.shared.messages.TestingMessage;
import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;

/**
 * Base class for all test messages. Utilise parsing of the messages.
 *
 * <p>Base format of messages is:
 *
 * <p>
 *
 * <pre>
 *  @@<{"name":"message_name","attributes":{"attribute":"value"}}>
 * </pre>
 */
public class ServerTestingMessage implements TestingMessage {

  public static final ServerTestingMessage TESTING_STARTED =
      new ServerTestingMessage(TestingMessageNames.TESTING_STARTED);
  public static final ServerTestingMessage FINISH_TESTING =
      new ServerTestingMessage(TestingMessageNames.FINISH_TESTING);
  public static final String TESTING_MESSAGE_START = "@@<";
  public static final String TESTING_MESSAGE_END = ">";
  private static final char ESCAPE_SEPARATOR = '!';
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
      return internalParse(
          text.substring(
                  TESTING_MESSAGE_START.length(), text.length() - TESTING_MESSAGE_END.length())
              .trim());
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
        Set<Map.Entry<String, JsonElement>> entries =
            jsonObject.getAsJsonObject(ATTRIBUTES).entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
          attributes.put(entry.getKey(), unescape(entry.getValue().getAsString()));
        }
      }

      return new ServerTestingMessage(name, attributes);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static String unescape(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    int finalLength = calculateUnescapedLength(text);
    int length = text.length();
    char[] result = new char[finalLength];
    int resultPos = 0;
    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      if (c == ESCAPE_SEPARATOR && i < length - 1) {
        char nextChar = text.charAt(i + 1);
        char unescape = unescape(nextChar);
        if (unescape != 0) {
          c = unescape;
          i++;
        }
      }
      result[resultPos++] = c;
    }

    return new String(result);
  }

  private static int calculateUnescapedLength(String text) {
    int result = 0;
    int length = text.length();

    for (int i = 0; i < length; i++) {
      char c = text.charAt(i);
      if (c == ESCAPE_SEPARATOR && i < length - 1) {
        char nextChar = text.charAt(i + 1);
        if (unescape(nextChar) != 0) {
          i++;
        }
      }
      result++;
    }
    return result;
  }

  private static char unescape(char c) {
    switch (c) {
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 'x':
        return '\u0085';
      case 'l':
        return '\u2028';
      case 'p':
        return '\u2029';
      default:
        return 0;
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
