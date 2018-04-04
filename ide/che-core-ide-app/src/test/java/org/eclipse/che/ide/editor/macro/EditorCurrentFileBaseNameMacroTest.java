/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.macro;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link EditorCurrentFileBaseNameMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorCurrentFileBaseNameMacroTest extends AbstractEditorMacroTest {

  private EditorCurrentFileBaseNameMacro provider;

  @Override
  protected AbstractEditorMacro getProvider() {
    return provider;
  }

  @Before
  public void init() throws Exception {
    provider =
        new EditorCurrentFileBaseNameMacro(editorAgent, promiseProvider, localizationConstants);
  }

  @Test
  public void testGetKey() throws Exception {
    assertSame(provider.getName(), EditorCurrentFileBaseNameMacro.KEY);
  }

  @Test
  public void getValue() throws Exception {
    initEditorWithTestFile();

    provider.expand();

    verify(editorAgent).getActiveEditor();
    verify(promiseProvider).resolve(eq(FILE_NAME_WITHOUT_EXT));
  }

  @Test
  public void getEmptyValue() throws Exception {
    provider.expand();

    verify(editorAgent).getActiveEditor();
    verify(promiseProvider).resolve(eq(""));
  }
}
