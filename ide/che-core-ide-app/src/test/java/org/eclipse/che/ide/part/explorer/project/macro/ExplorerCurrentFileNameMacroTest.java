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
package org.eclipse.che.ide.part.explorer.project.macro;

import com.google.common.base.Joiner;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the {@link ExplorerCurrentFileNameMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ExplorerCurrentFileNameMacroTest extends AbstractExplorerMacroTest {

    private ExplorerCurrentFileNameMacro provider;

    @Before
    public void init() throws Exception {
        super.init();
        provider = new ExplorerCurrentFileNameMacro(projectExplorer, promiseProvider, localizationConstants);
    }

    @Test
    public void testGetKey() throws Exception {
        assertSame(provider.getName(), ExplorerCurrentFileNameMacro.KEY);
    }

    @Test
    public void getValue() throws Exception {
        initWithOneFile();

        provider.expand();

        verify(promiseProvider).resolve(eq(FILE_1_NAME));
    }

    @Test
    public void getMultipleValues() throws Exception {
        initWithTwoFiles();

        provider.expand();

        verify(promiseProvider).resolve(eq(Joiner.on(", ").join(FILE_1_NAME, FILE_2_NAME)));
    }

    @Test
    public void getEmptyValues() throws Exception {
        initWithNoFiles();

        provider.expand();

        verify(promiseProvider).resolve(eq(""));
    }

}