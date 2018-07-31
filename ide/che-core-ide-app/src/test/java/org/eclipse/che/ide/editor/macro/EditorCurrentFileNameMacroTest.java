/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Unit tests for the {@link EditorCurrentFileNameMacro}
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorCurrentFileNameMacroTest extends AbstractEditorMacroTest {

  private EditorCurrentFileNameMacro provider;

  @Override
  protected AbstractEditorMacro getProvider() {
    return provider;
  }

  @Before
  public void init() throws Exception {
    provider = new EditorCurrentFileNameMacro(editorAgent, promiseProvider, localizationConstants);
  }

  @Test
  public void testGetKey() throws Exception {
    assertSame(provider.getName(), EditorCurrentFileNameMacro.KEY);
  }

  @Test
  public void getValue() throws Exception {
    initEditorWithTestFile();

    provider.expand();

    verify(editorAgent).getActiveEditor();
    verify(promiseProvider).resolve(eq(FILE_NAME));
  }

  @Test
  public void getEmptyValue() throws Exception {
    provider.expand();

    verify(editorAgent).getActiveEditor();
    verify(promiseProvider).resolve(eq(""));
  }
}
