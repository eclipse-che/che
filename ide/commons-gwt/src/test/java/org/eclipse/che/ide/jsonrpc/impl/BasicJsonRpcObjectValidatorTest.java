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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BasicJsonRpcObjectValidator}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicJsonRpcObjectValidatorTest {
    private static final String REGISTERED_TYPE     = "registered-type";
    private static final String NOT_REGISTERED_TYPE = "not-registered-type";
    private static final String VALID_JSON          = "{ \"name\": \"value\" }";
    private static final String NOT_VALID_JSON      = "not a json value";
    @Mock
    private Map<String, JsonRpcDispatcher> dispatchers;

    private BasicJsonRpcObjectValidator    validator;

    @Mock
    private JsonRpcObject object;

    @Before
    public void before() {
        when(dispatchers.keySet()).thenReturn(singleton(REGISTERED_TYPE));

        validator = new BasicJsonRpcObjectValidator(dispatchers);

        when(object.getType()).thenReturn(REGISTERED_TYPE);
        when(object.getMessage()).thenReturn(VALID_JSON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsNull() {
        when(object.getType()).thenReturn(null);

        validator.validate(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsEmpty() {
        when(object.getType()).thenReturn("");

        validator.validate(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTypeIsNotRegistered() {
        when(object.getType()).thenReturn(NOT_REGISTERED_TYPE);

        validator.validate(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsNull() {
        when(object.getMessage()).thenReturn(null);

        validator.validate(object);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMessageIsEmpty() {
        when(object.getMessage()).thenReturn("");

        validator.validate(object);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void shouldThrowExceptionIfMessageIsNotAJson() {
        when(object.getMessage()).thenReturn(NOT_VALID_JSON);

        validator.validate(object);
    }

    @Test
    @Ignore
    public void shouldNotThrowExceptionIfObjectIsValid() {
        validator.validate(object);
    }
}
