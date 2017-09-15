package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet.VariableResolver;

public class DocumentVariableResolver implements VariableResolver {
  private static final Map<String, BiFunction<Document, TextPosition, String>> VARIABLES;

  static {
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
    int start = pos.getCharacter();
    while (start > 0 && !Character.isWhitespace(line.charAt(start))) {
      start--;
    }
    int end = pos.getCharacter();
    while (end < line.length() - 1 && !Character.isWhitespace(line.charAt(start))) {
      end++;
    }
    return line.substring(start, end);
  }
}
