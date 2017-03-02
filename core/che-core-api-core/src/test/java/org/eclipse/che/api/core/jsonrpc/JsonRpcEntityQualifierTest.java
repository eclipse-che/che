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

import org.eclipse.che.api.core.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests for {@link JsonRpcEntityQualifier}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcEntityQualifierTest {

    JsonRpcEntityQualifier qualifier;

    @BeforeMethod
    public void setUp() throws Exception {
        qualifier = new JsonRpcEntityQualifier(new JsonParser());
    }

    @Test
    public void shouldQualifyRequestWhenMessageContainsMethod() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("method", "method-name");

        JsonRpcEntityType type = qualifier.qualify(jsonObject.toString());

        assertEquals(JsonRpcEntityType.REQUEST, type);
    }

    @Test
    public void shouldQualifyResponseWhenMessageContainsError() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", "error-value");

        JsonRpcEntityType type = qualifier.qualify(jsonObject.toString());

        assertEquals(JsonRpcEntityType.RESPONSE, type);
    }

    @Test
    public void shouldQualifyResponseWhenMessageContainsResult() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("result", "result-value");

        JsonRpcEntityType type = qualifier.qualify(jsonObject.toString());

        assertEquals(JsonRpcEntityType.RESPONSE, type);
    }

    @Test
    public void shouldQualifyUndefinedWhenMessageContainsResultAndError() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", "error-value");
        jsonObject.addProperty("result", "result-value");

        JsonRpcEntityType type = qualifier.qualify(jsonObject.toString());

        assertEquals(JsonRpcEntityType.UNDEFINED, type);
    }

    @Test
    public void shouldQualifyUndefinedWhenMessageContainsNoValuableKeys() {
        JsonObject jsonObject = new JsonObject();

        JsonRpcEntityType type = qualifier.qualify(jsonObject.toString());

        assertEquals(JsonRpcEntityType.UNDEFINED, type);
    }
}
