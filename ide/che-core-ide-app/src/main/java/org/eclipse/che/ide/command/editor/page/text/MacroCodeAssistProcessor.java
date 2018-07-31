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
package org.eclipse.che.ide.command.editor.page.text;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.ide.filters.FuzzyMatches;
import org.eclipse.che.ide.filters.Match;

/** Code assist processor for macro names. */
public class MacroCodeAssistProcessor implements CodeAssistProcessor {

  private MacroRegistry registry;
  private FuzzyMatches fuzzyMatches;
  private Resources resources;
  private LastCompletion lastCompletion;

  @Inject
  public MacroCodeAssistProcessor(
      MacroRegistry registry, FuzzyMatches fuzzyMatches, Resources resources) {
    this.registry = registry;
    this.fuzzyMatches = fuzzyMatches;
    this.resources = resources;
    lastCompletion = new LastCompletion();
  }

  @Override
  public void computeCompletionProposals(
      TextEditor editor, int offset, boolean triggered, CodeAssistCallback callback) {
    Document document = editor.getDocument();
    TextPosition position = document.getPositionFromIndex(offset);

    String currentLine = editor.getDocument().getLineContent(position.getLine());
    final String currentWord = getCurrentWord(currentLine, position.getCharacter());

    List<CompletionProposal> result = new ArrayList<>();
    if (triggered && !lastCompletion.isGoodFor(currentWord, offset)) {
      lastCompletion.offset = offset;
      lastCompletion.wordStartOffset = offset - currentWord.length(); // start completion word
      lastCompletion.word = currentWord;
    }

    List<Macro> macros = registry.getMacros();
    for (Macro macro : macros) {
      List<Match> matches = fuzzyMatches.fuzzyMatch(currentWord, macro.getName());
      if (matches != null) {
        MacroCompletionProposal proposal =
            new MacroCompletionProposal(
                macro, matches, resources, lastCompletion.wordStartOffset, currentWord.length());
        result.add(proposal);
      }
    }

    result.sort(
        (o1, o2) -> {
          MacroCompletionProposal p1 = ((MacroCompletionProposal) o1);
          MacroCompletionProposal p2 = ((MacroCompletionProposal) o2);

          return p1.getMacro().getName().compareTo(p2.getMacro().getName());
        });

    callback.proposalComputed(result);
  }

  private String getCurrentWord(String text, int offset) {
    int i = offset - 1;
    while (i >= 0 && isWordChar(text.charAt(i))) {
      i--;
    }
    return text.substring(i + 1, offset);
  }

  private boolean isWordChar(char c) {
    return c >= 'a' && c <= 'z'
        || c >= 'A' && c <= 'Z'
        || c >= '0' && c <= '9'
        || c >= '\u007f' && c <= '\u00ff'
        || c == '$'
        || c == '{'
        || c == '.'
        || c == '_'
        || c == '-';
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  private class LastCompletion {
    String word = "";
    int wordStartOffset;
    int offset;

    boolean isGoodFor(String currentWord, int offset) {
      return currentWord.startsWith(word)
          && offset - this.offset == currentWord.length() - word.length();
    }
  }
}
