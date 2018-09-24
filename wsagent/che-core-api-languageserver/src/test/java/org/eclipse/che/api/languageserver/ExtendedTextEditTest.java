/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import static org.eclipse.che.api.languageserver.TextDocumentService.convertToExtendedEdits;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextEdit;
import org.eclipse.che.api.languageserver.shared.util.CharStreamIterator;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.testng.annotations.Test;

public class ExtendedTextEditTest {
  @Test
  public void testConvertSingleEdit() {
    List<ExtendedTextEdit> converted =
        convertToExtendedEdits(
            Collections.singletonList(new TextEdit(range(0, 1, 0, 5), "blabla")),
            new CharStreamIterator(source("abcdefghiklmnop")));
    assertEquals(converted.size(), 1);
    ExtendedTextEdit edit = converted.get(0);
    expect(edit, range(0, 1, 0, 5), "blabla", "abcdefghiklmnop", 1, 5);
  }

  @Test
  public void testConvertLineSpanning() {
    List<ExtendedTextEdit> converted =
        convertToExtendedEdits(
            Collections.singletonList(new TextEdit(range(0, 1, 1, 5), "blabla")),
            new CharStreamIterator(source("abcdefghiklmnop\nfoobar")));
    assertEquals(converted.size(), 1);
    ExtendedTextEdit edit = converted.get(0);
    assertEquals(edit.getRange(), range(0, 1, 1, 5));
    assertEquals(edit.getNewText(), "blabla");
    assertEquals(edit.getLineText(), "abcdefghiklmnop");
    assertEquals(edit.getInLineStart(), 1);
    assertEquals(edit.getInLineEnd(), 14);
  }

  @Test
  public void testConvertTwoInLine() {
    List<ExtendedTextEdit> converted =
        convertToExtendedEdits(
            Arrays.asList(
                new TextEdit(range(0, 1, 0, 5), "blabla"),
                new TextEdit(range(0, 7, 1, 2), "sibi"),
                new TextEdit(range(1, 3, 1, 4), "subu")),
            new CharStreamIterator(source("abcdefghiklmnop\nfoobar")));
    assertEquals(converted.size(), 3);
    expect(converted.get(0), range(0, 1, 0, 5), "blabla", "abcdefghiklmnop", 1, 5);
    expect(converted.get(1), range(0, 7, 1, 2), "sibi", "abcdefghiklmnop", 7, 14);
    expect(converted.get(2), range(1, 3, 1, 4), "subu", "foobar", 3, 4);
  }

  private void expect(
      ExtendedTextEdit first,
      Range range,
      String newText,
      String line,
      int startInLine,
      int endInLine) {
    assertEquals(first.getRange(), range);
    assertEquals(first.getNewText(), newText);
    assertEquals(first.getLineText(), line);
    assertEquals(first.getInLineStart(), startInLine);
    assertEquals(first.getInLineEnd(), endInLine);
  }

  private static Range range(int startLine, int startChar, int endLine, int endChar) {
    return new Range(pos(startLine, startChar), pos(endLine, endChar));
  }

  private static Position pos(int startLine, int startChar) {
    return new Position(startLine, startChar);
  }

  public Supplier<Integer> source(String input) {
    return new Supplier<Integer>() {
      private int pos = 0;

      @Override
      public Integer get() {
        if (pos >= input.length()) {
          return -1;
        } else {
          return (int) input.charAt(pos++);
        }
      }
    };
  }
}
