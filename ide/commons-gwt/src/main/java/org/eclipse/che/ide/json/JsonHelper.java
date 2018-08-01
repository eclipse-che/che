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
package org.eclipse.che.ide.json;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:vparfonov@codenvy.com">Vitaly Parfonov</a> */
public class JsonHelper {

  public static String toJson(Map<String, String> map) {
    String json = "";
    if (map != null && !map.isEmpty()) {
      JSONObject jsonObj = new JSONObject();
      for (Map.Entry<String, String> entry : map.entrySet()) {
        jsonObj.put(entry.getKey(), new JSONString(entry.getValue()));
      }
      json = jsonObj.toString();
    }
    return json;
  }

  public static Map<String, String> toMap(String jsonStr) {
    Map<String, String> map = new HashMap<String, String>();

    JSONValue parsed = JSONParser.parseStrict(jsonStr);
    JSONObject jsonObj = parsed.isObject();
    if (jsonObj != null) {
      for (String key : jsonObj.keySet()) {
        JSONValue jsonValue = jsonObj.get(key);
        JSONString jsonString = jsonValue.isString();
        // if the json value is a string, set the unescaped value, else set the json representation
        // of the value
        String stringValue = (jsonString == null) ? jsonValue.toString() : jsonString.stringValue();
        map.put(key, stringValue);
      }
    }

    return map;
  }

  public static Map<String, List<String>> toMapOfLists(String jsonStr) {
    Map<String, List<String>> map = new HashMap<>();

    JSONValue parsed = JSONParser.parseStrict(jsonStr);
    JSONObject jsonObj = parsed.isObject();
    if (jsonObj != null) {
      for (String key : jsonObj.keySet()) {
        JSONValue jsonValue = jsonObj.get(key);
        JSONArray jsonArray = jsonValue.isArray();

        List<String> values = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
          values.add(jsonArray.get(i).isString().stringValue());
        }
        map.put(key, values);
      }
    }

    return map;
  }

  // TODO: find a way to avoid those util methods here.

  /** Returns message or result of it parse if the message is json. */
  public static String parseJsonMessage(String parsedMessage) {
    try {
      // parsed message
      JSONValue message = JSONParser.parseStrict(parsedMessage).isObject().get("message");
      return message.isString().stringValue();
    } catch (Exception e) {
      // not found json in message
      return parsedMessage;
    }
  }

  /** Returns message or result of it parse if the message is json. */
  public static Map<String, String> parseErrorAttributes(String parsedMessage) {
    try {
      // parsed message
      JSONValue message = JSONParser.parseStrict(parsedMessage).isObject().get("attributes");
      return toMap(message.isObject().toString());
    } catch (Exception e) {
      // not found json in message
      return Collections.emptyMap();
    }
  }

  /** Returns message or result of it parse if the message is json. */
  public static int parseErrorCode(String parsedMessage) {
    try {
      // parsed message
      JSONValue message = JSONParser.parseStrict(parsedMessage).isObject().get("errorCode");
      return new Double(message.isNumber().doubleValue()).intValue();
    } catch (Exception e) {
      // not found json in message
      return -1;
    }
  }
}
