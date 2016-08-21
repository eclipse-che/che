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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for {@link JsonRpcRequestRegistry}
 *
 * @author Dmitry Kuleshov
 */
public class JsonRpcRequestRegistryTest {
    private static final String METHOD_NAME = "method-name";
    private static final int    REQUEST_ID  = 1;

    private JsonRpcRequestRegistry registry;

    @Before
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
