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
package org.eclipse.che.api.workspace.server.devfile;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.Serializable;
import java.util.Map;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PreferencesDeserializerTest {

  @Mock private DeserializationContext ctxt;

  private final JsonFactory factory = new JsonFactory();

  /** Instance to test. */
  private PreferencesDeserializer preferencesDeserializer = new PreferencesDeserializer();

  @Test
  public void shouldParseSimpleTypesCorrectly() throws Exception {
    String json =
        "{"
            + "\"valid.string\": \"/usr/bin/value\","
            + "\"valid.boolean\": false,"
            + "\"valid.integer\": 777555888,"
            + "\"valid.float\": 3.1415926"
            + "}";

    final JsonParser parser = factory.createParser(json);
    Map<String, Serializable> result = preferencesDeserializer.deserialize(parser, ctxt);
    assertEquals(4, result.size());
    assertTrue(result.containsValue(777555888));
    assertTrue(result.containsValue(3.1415926));
    assertTrue(result.containsValue(false));
    assertTrue(result.containsValue("/usr/bin/value"));
  }

  @Test(
      expectedExceptions = JsonParseException.class,
      expectedExceptionsMessageRegExp =
          "Unexpected value of the preference with key 'invalid.object'.\n"
              + " at \\[Source: \\(String\\)\"\\{\"valid\\.string\": \"/usr/bin/value\",\"invalid\\.object\": \\{\"someobject\": true\\}\\}\"; line: 1, column: 54\\]")
  public void shouldThrowExceptionOnUnsupportedPreferenceValue() throws Exception {

    String json =
        "{"
            + "\"valid.string\": \"/usr/bin/value\","
            + "\"invalid.object\": {\"someobject\": true}"
            + "}";

    final JsonParser parser = factory.createParser(json);
    preferencesDeserializer.deserialize(parser, ctxt);
  }
}
