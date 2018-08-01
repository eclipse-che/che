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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet.VariableResolver;

/**
 * Resolves snippet variables against an editor document.
 *
 * @author thomas
 */
public class DocumentVariableResolver implements VariableResolver {
  // variables are functions from a document and position to a value.
  private static final Map<String, BiFunction<Document, TextPosition, String>> VARIABLES;

  static {
    // well known variables according to
    // https://github.com/Microsoft/vscode/blob/0ebd01213a65231f0af8187acaf264243629e4dc/src/vs/editor/contrib/snippet/browser/snippet.md
    VARIABLES = new HashMap<>();
    VARIABLES.put("TM_SELECTED_TEXT", DocumentVariableResolver::getSelectedText);
    VARIABLES.put("TM_CURRENT_LINE", DocumentVariableResolver::getCurrentLine);
    VARIABLES.put("TM_CURRENT_WORD", DocumentVariableResolver::getCurrentWord);
    VARIABLES.put("TM_LINE_INDEX", DocumentVariableResolver::getCurrentLineIndex);
    VARIABLES.put("TM_LINE_NUMBER", DocumentVariableResolver::getCurrentLineNumber);
    VARIABLES.put("TM_FILENAME", DocumentVariableResolver::getFileName);
    VARIABLES.put("TM_DIRECTORY", DocumentVariableResolver::getDirectory);
    VARIABLES.put("TM_FILEPATH", DocumentVariableResolver::getPath);
  }

  private Document document;
  private TextPosition position;

  public DocumentVariableResolver(Document document, TextPosition position) {
    this.document = document;
    this.position = position;
  }

  @Override
  public boolean isVar(String name) {
    return VARIABLES.containsKey(name);
  }

  @Override
  public String resolve(String name) {
    return VARIABLES.get(name).apply(document, position);
  }

  private static String getSelectedText(Document doc, TextPosition pos) {
    return doc.getContentRange(doc.getSelectedTextRange());
  }

  private static String getCurrentLine(Document doc, TextPosition pos) {
    return doc.getLineContent(pos.getLine());
  }

  private static String getCurrentLineIndex(Document doc, TextPosition pos) {
    return String.valueOf(pos.getLine());
  }

  private static String getCurrentLineNumber(Document doc, TextPosition pos) {
    return String.valueOf(pos.getLine() + 1);
  }

  private static String getFileName(Document doc, TextPosition pos) {
    return doc.getFile().getName();
  }

  private static String getDirectory(Document doc, TextPosition pos) {
    return doc.getFile().getLocation().parent().toString();
  }

  private static String getPath(Document doc, TextPosition pos) {
    return doc.getFile().getLocation().toString();
  }

  private static String getCurrentWord(Document doc, TextPosition pos) {
    String line = doc.getLineContent(pos.getLine());
    if (line.length() == 0) {
      return "";
    }
    int start = pos.getCharacter();
    int end = start;
    while (start > 0 && !Character.isWhitespace(line.charAt(start - 1))) {
      start--;
    }
    while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
      end++;
    }
    return line.substring(start, end);
  }
}
