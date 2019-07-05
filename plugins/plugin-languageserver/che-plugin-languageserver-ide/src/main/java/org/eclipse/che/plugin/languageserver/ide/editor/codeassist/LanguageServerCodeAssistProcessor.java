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

import static com.google.common.collect.Lists.newArrayList;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistCallback;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.filters.FuzzyMatches;
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/** Implement code assist with LS */
public class LanguageServerCodeAssistProcessor implements CodeAssistProcessor {

  private final DtoBuildHelper dtoBuildHelper;
  private final LanguageServerResources resources;
  private final CompletionImageProvider imageProvider;
  private final ServerCapabilities serverCapabilities;
  private final TextDocumentServiceClient documentServiceClient;
  private final FuzzyMatches fuzzyMatches;
  private LatestCompletionResult latestCompletionResult;
  private String lastErrorMessage;

  @Inject
  public LanguageServerCodeAssistProcessor(
      TextDocumentServiceClient documentServiceClient,
      DtoBuildHelper dtoBuildHelper,
      LanguageServerResources resources,
      CompletionImageProvider imageProvider,
      @Assisted ServerCapabilities serverCapabilities,
      FuzzyMatches fuzzyMatches) {
    this.documentServiceClient = documentServiceClient;
    this.dtoBuildHelper = dtoBuildHelper;
    this.resources = resources;
    this.imageProvider = imageProvider;
    this.serverCapabilities = serverCapabilities;
    this.fuzzyMatches = fuzzyMatches;
    this.latestCompletionResult = LatestCompletionResult.NO_RESULT;
  }

  @Override
  public void computeCompletionProposals(
      TextEditor editor,
      final int offset,
      final boolean triggered,
      final CodeAssistCallback callback) {
    this.lastErrorMessage = null;

    TextDocumentPositionParams documentPosition =
        dtoBuildHelper.createTDPP(editor.getDocument(), offset);
    final TextDocumentIdentifier documentId = documentPosition.getTextDocument();
    String currentLine =
        editor.getDocument().getLineContent(documentPosition.getPosition().getLine());
    final String currentWord =
        getCurrentWord(currentLine, documentPosition.getPosition().getCharacter());

    if (!triggered && latestCompletionResult.isGoodFor(documentId, offset, currentWord)) {
      // no need to send new completion request
      computeProposals(
          (HasLinkedMode) editor,
          currentWord,
          offset - latestCompletionResult.getOffset(),
          callback);
    } else {
      documentServiceClient
          .completion(documentPosition)
          .then(
              list -> {
                latestCompletionResult =
                    new LatestCompletionResult(documentId, offset, currentWord, list);
                computeProposals((HasLinkedMode) editor, currentWord, 0, callback);
              })
          .catchError(
              error -> {
                lastErrorMessage = error.getMessage();
              });
    }
  }

  @Override
  public String getErrorMessage() {
    return lastErrorMessage;
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
        || c == '_'
        || c == '-';
  }

  private List<Match> filter(String word, CompletionItem item) {
    return filter(word, item.getLabel(), item.getFilterText());
  }

  private List<Match> filter(String word, String label, String filterText) {
    if (filterText == null || filterText.isEmpty()) {
      filterText = label;
    }

    // check if the word matches the filterText
    if (fuzzyMatches.fuzzyMatch(word, filterText) != null) {
      // return the highlights based on the label
      List<Match> highlights = fuzzyMatches.fuzzyMatch(word, label);
      // return empty list of highlights if nothing matches the label
      return (highlights == null) ? new ArrayList<>() : highlights;
    }

    return null;
  }

  private void computeProposals(
      HasLinkedMode editor, String currentWord, int offset, CodeAssistCallback callback) {
    List<CompletionProposal> proposals = newArrayList();
    for (ExtendedCompletionItem item : latestCompletionResult.getCompletionList().getItems()) {
      List<Match> highlights = filter(currentWord, item.getItem());
      if (highlights != null) {
        proposals.add(
            new CompletionItemBasedCompletionProposal(
                editor,
                item,
                currentWord,
                documentServiceClient,
                resources,
                imageProvider.getIcon(item.getItem().getKind()),
                serverCapabilities,
                highlights,
                offset));
      }
    }
    callback.proposalComputed(proposals);
  }
}
