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
package org.eclipse.che.plugin.jsonexample.ide.editor;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.rest.Unmarshallable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deserializer for list of strings.
 */
public class StringListUnmarshaller implements Unmarshallable<List<String>> {

    private List<String> payload;

    public List<String> toList(String jsonStr) {

        JSONValue parsed = JSONParser.parseStrict(jsonStr);
        JSONArray jsonArray = parsed.isArray();

        if (jsonArray == null) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONValue jsonValue = jsonArray.get(i);
            JSONString jsonString = jsonValue.isString();
            String stringValue = (jsonString == null) ? jsonValue.toString() : jsonString.stringValue();
            list.add(stringValue);
        }

        return list;
    }

    @Override
    public void unmarshal(Response response) throws UnmarshallerException {
        payload = toList(response.getText());
    }

    @Override
    public List<String> getPayload() {
        return payload;
    }
}
