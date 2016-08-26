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
package org.eclipse.che.api.core.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BasicJsonRpcObjectValidator}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class BasicJsonRpcObjectValidatorTest {
    private static final String REGISTERED_TYPE     = "registered-type";
    private static final String NOT_REGISTERED_TYPE = "not-registered-type";
    private static final String VALID_JSON          = "{ \"name\": \"value\" }";
    private static final String NOT_VALID_JSON      = "not a json value";
    @Mock
    private Map<String, JsonRpcDispatcher> dispatchers;

    private BasicJsonRpcObjectValidator validator;

    @Mock
    private JsonRpcObject jsonRpcObject;

    @BeforeMethod
    public void before() {
        when(dispatchers.keySet()).thenReturn(singleton(REGISTERED_TYPE));

        validator = new BasicJsonRpcObjectValidator(dispatchers);

        when(jsonRpcObject.getType()).thenReturn(REGISTERED_TYPE);
        when(jsonRpcObject.getMessage()).thenReturn(VALID_JSON);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsNull() {
        when(jsonRpcObject.getType()).thenReturn(null);

        validator.validate(jsonRpcObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsEmpty() {
        when(jsonRpcObject.getType()).thenReturn("");

        validator.validate(jsonRpcObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsNotRegistered() {
        when(jsonRpcObject.getType()).thenReturn(NOT_REGISTERED_TYPE);

        validator.validate(jsonRpcObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNull() {
        when(jsonRpcObject.getMessage()).thenReturn(null);

        validator.validate(jsonRpcObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsEmpty() {
        when(jsonRpcObject.getMessage()).thenReturn("");

        validator.validate(jsonRpcObject);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNotAJson() {
        when(jsonRpcObject.getMessage()).thenReturn(NOT_VALID_JSON);

        validator.validate(jsonRpcObject);
    }

    @Test
    public void shouldNotThrowExceptionIfObjectIsValid() {
        validator.validate(jsonRpcObject);
    }
}
