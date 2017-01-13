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
package org.eclipse.che.ide.machine;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.macro.CustomMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

/**
 * Unit tests for the {@link CustomMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class CustomMacroTest {

    public static final String KEY   = "key";
    public static final String VALUE = "value";
    public static final String DESCRIPTION = "description";

    private CustomMacro provider;

    @Before
    public void init() throws Exception {
        provider = new CustomMacro(KEY, VALUE, DESCRIPTION);
    }

    @Test
    public void getKey() throws Exception {
        assertSame(provider.getName(), KEY);
    }

    @Test
    public void getValue() throws Exception {
        provider.expand().then(new Operation<String>() {
            @Override
            public void apply(String value) throws OperationException {
                assertSame(value, VALUE);
            }
        });
    }

    @Test
    public void getDescription() throws Exception {
        assertSame(provider.getDescription(), DESCRIPTION);
    }

}