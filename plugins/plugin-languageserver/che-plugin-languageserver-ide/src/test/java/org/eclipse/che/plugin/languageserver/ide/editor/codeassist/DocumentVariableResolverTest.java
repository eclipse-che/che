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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.junit.Test;

public class DocumentVariableResolverTest {
  @Test
  public void currentWord() {
    testCurrentWord(" CurrentWo   ", "CurrentWo", 3);
    testCurrentWord("CurrentWo   ", "CurrentWo", 3);
    testCurrentWord(" CurrentWo", "CurrentWo", 3);
    testCurrentWord(" CurrentWo", "CurrentWo", 10);
    testCurrentWord("CurrentWo   ", "CurrentWo", 0);
    testCurrentWord(" CurrentWo", "", 0);
  }

  private void testCurrentWord(String line, String word, int pos) {
    Document d = mock(Document.class);
    when(d.getLineContent(anyInt())).thenReturn(line);

    String resolver =
        new DocumentVariableResolver(d, new TextPosition(0, pos)).resolve("TM_CURRENT_WORD");
    assertEquals(word, resolver);
  }
}
