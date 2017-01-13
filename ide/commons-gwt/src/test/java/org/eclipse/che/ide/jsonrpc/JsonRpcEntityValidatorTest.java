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
package org.eclipse.che.ide.jsonrpc;

import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcEntityValidator}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcEntityValidatorTest {
    @Mock
    JsonFactory jsonFactory;
    @InjectMocks
    JsonRpcEntityValidator validator;

    @Mock
    JsonObject jsonObject;

    @Test(expected = JsonRpcException.class)
    public void shouldThrowJsonRpcExceptionWhenParsingFails() throws Exception {
        when(jsonFactory.parse(anyString())).thenThrow(new JsonException(""));

        validator.validate("message");

        verify(jsonFactory).parse("message");
    }

    @Test
    public void shouldNotThrowJsonRpcExceptionWhenParsingFails() throws Exception {
        when(jsonFactory.parse(anyString())).thenReturn(jsonObject);

        validator.validate("message");

        verify(jsonFactory).parse("message");
    }
}
