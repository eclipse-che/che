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
package org.eclipse.che.ide.machine;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link CustomCommandPropertyValueProvider}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class CustomCommandPropertyValueProviderTest {

    public static final String KEY   = "key";
    public static final String VALUE = "value";

    private CustomCommandPropertyValueProvider provider;

    @Before
    public void init() throws Exception {
        provider = new CustomCommandPropertyValueProvider(KEY, VALUE);
    }

    @Test
    public void getKey() throws Exception {
        assertSame(provider.getKey(), KEY);
    }

    @Test
    public void getValue() throws Exception {
        provider.getValue().then(new Operation<String>() {
            @Override
            public void apply(String value) throws OperationException {
                assertSame(value, VALUE);
            }
        });
    }

}