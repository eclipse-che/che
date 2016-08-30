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
package org.eclipse.che.ide.editor.macro;

import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the {@link EditorCurrentFilePathProvider}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorCurrentFilePathProviderTest extends AbstractEditorMacroProviderTest {

    private EditorCurrentFilePathProvider provider;

    @Override
    protected AbstractEditorMacroProvider getProvider() {
        return provider;
    }

    @Before
    public void init() throws Exception {
        provider = new EditorCurrentFilePathProvider(editorAgent, promiseProvider, appContext);
    }

    @Test
    public void testGetKey() throws Exception {
        assertSame(provider.getKey(), EditorCurrentFilePathProvider.KEY);
    }

    @Test
    public void getValue() throws Exception {
        initEditorWithTestFile();

        provider.getValue();

        verify(editorAgent).getActiveEditor();
        verify(promiseProvider).resolve(eq(Path.valueOf(PROJECTS_ROOT).append(FILE_PATH).toString()));
    }

    @Test
    public void getEmptyValue() throws Exception {
        provider.getValue();

        verify(editorAgent).getActiveEditor();
        verify(promiseProvider).resolve(eq(""));
    }

}