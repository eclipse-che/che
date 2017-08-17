/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.macro;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

/**
 * Unit tests for the {@link BaseMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class BaseMacroTest {

    public static final String NAME        = "name";
    public static final String VALUE       = "value";
    public static final String DESCRIPTION = "description";

    private BaseMacro macro;

    @Before
    public void init() throws Exception {
        macro = new BaseMacro(NAME, VALUE, DESCRIPTION);
    }

    @Test
    public void getKey() throws Exception {
        assertSame(macro.getName(), NAME);
    }

    @Test
    public void getValue() throws Exception {
        macro.expand().then(value -> {
            assertSame(value, VALUE);
        });
    }

    @Test
    public void getDescription() throws Exception {
        assertSame(macro.getDescription(), DESCRIPTION);
    }

}