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
