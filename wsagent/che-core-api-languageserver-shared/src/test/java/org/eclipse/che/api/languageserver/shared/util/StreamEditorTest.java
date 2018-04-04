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
package org.eclipse.che.api.languageserver.shared.util;

import java.util.Arrays;
import java.util.List;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.junit.Assert;
import org.junit.Test;

public class StreamEditorTest {

  @Test
  public void testInsertEmpty() {
    runEdit("", "foobar", new TextEdit(newRange(0, 0, 0, 0), "foobar"));
  }

  @Test
  public void testInsertTwo() {
    runEdit(
        "abcdef",
        "afoobarbcbladef",
        new TextEdit(newRange(0, 1, 0, 1), "foobar"),
        new TextEdit(newRange(0, 3, 0, 3), "bla"));
  }

  @Test
  public void testInsertNewLine() {
    runEdit(
        "abc\r\ndef",
        "afoobarbc\r\ndblaef",
        new TextEdit(newRange(0, 1, 0, 1), "foobar"),
        new TextEdit(newRange(1, 1, 1, 1), "bla"));
    runEdit(
        "abc\ndef",
        "afoobarbc\ndblaef",
        new TextEdit(newRange(0, 1, 0, 1), "foobar"),
        new TextEdit(newRange(1, 1, 1, 1), "bla"));
  }

  @Test
  public void testInsertRemoveNewLine() {
    runEdit(
        "abc\r\ndef",
        "ab\nlaefoobarf",
        new TextEdit(newRange(0, 1, 1, 1), "b\nla"),
        new TextEdit(newRange(1, 2, 1, 2), "foobar"));
  }

  @Test
  public void testDeleteAll() {
    runEdit("foo\nbar", "", new TextEdit(newRange(0, 0, 1, 3), ""));
  }

  @Test
  public void testDeleteEnd() {
    runEdit(
        "abc\r\ndef",
        "abc\r\ndxyz",
        new TextEdit(newRange(1, 1, 1, 3), ""),
        new TextEdit(newRange(1, 1, 1, 1), "xyz"));
  }

  private void runEdit(String originalContent, String editedContent, TextEdit... edits) {
    StringBuilder output = new StringBuilder();
    List<TextEdit> inverse =
        new StringStreamEditor(Arrays.asList(edits), originalContent, output).transform();
    Assert.assertEquals(editedContent, output.toString());

    // now test that applying the inverse edits gives the original content.
    StringBuilder inverseOutput = new StringBuilder();
    new StringStreamEditor(inverse, output.toString(), inverseOutput).transform();
    Assert.assertEquals(originalContent, inverseOutput.toString());
  }

  private Range newRange(int i, int j, int k, int l) {
    return new Range(new Position(i, j), new Position(k, l));
  }
}
