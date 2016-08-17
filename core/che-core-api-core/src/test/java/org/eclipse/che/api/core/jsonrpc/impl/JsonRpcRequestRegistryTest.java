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

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link JsonRpcRequestRegistry}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcRequestRegistryTest {
    private static final String METHOD_NAME = "method-name";
    private static final int    REQUEST_ID  = 1;

    private JsonRpcRequestRegistry registry;

    @BeforeMethod
    public void before() {
        registry = new JsonRpcRequestRegistry();
    }

    @Test
    public void shouldProperlyRegister() {
        registry.add(REQUEST_ID, METHOD_NAME);

        assertEquals(METHOD_NAME, registry.extractFor(1));
    }

    @Test
    public void shouldProperlyExtract() {
        registry.add(REQUEST_ID, METHOD_NAME);

        assertEquals(METHOD_NAME, registry.extractFor(1));
        assertNull(registry.extractFor(1));
    }

}
