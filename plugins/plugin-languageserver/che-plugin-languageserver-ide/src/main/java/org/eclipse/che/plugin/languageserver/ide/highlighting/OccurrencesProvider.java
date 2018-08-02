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
package org.eclipse.che.plugin.languageserver.ide.highlighting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionOccurrencesHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionOccurrenceContextOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionOccurrenceOverlay;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Provides occurrences highlights for the Orion Editor.
 *
 * @author Xavier Coulon, Red Hat
 */
@Singleton
public class OccurrencesProvider implements OrionOccurrencesHandler {
  private final EditorAgent editorAgent;
  private final TextDocumentServiceClient client;
  private final DtoBuildHelper helper;

  /**
   * Constructor.
   *
   * @param editorAgent
   * @param client
   * @param helper
   */
  @Inject
  public OccurrencesProvider(
      EditorAgent editorAgent, TextDocumentServiceClient client, DtoBuildHelper helper) {
    this.editorAgent = editorAgent;
    this.client = client;
    this.helper = helper;
  }

  @Override
  public JsPromise<OrionOccurrenceOverlay[]> computeOccurrences(
      OrionOccurrenceContextOverlay context) {
    final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor == null || !(activeEditor instanceof TextEditor)) {
      return null;
    }
    final TextEditor editor = ((TextEditor) activeEditor);
    if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
      return null;
    }
    final LanguageServerEditorConfiguration configuration =
        (LanguageServerEditorConfiguration) editor.getConfiguration();
    if (configuration.getServerCapabilities().getDocumentHighlightProvider() == null
        || !configuration.getServerCapabilities().getDocumentHighlightProvider()) {
      return null;
    }
    final Document document = editor.getDocument();
    final TextDocumentPositionParams paramsDTO = helper.createTDPP(document, context.getStart());
    Promise<List<DocumentHighlight>> promise = client.documentHighlight(paramsDTO);
    Promise<OrionOccurrenceOverlay[]> then =
        promise.then(
            new Function<List<DocumentHighlight>, OrionOccurrenceOverlay[]>() {
              @Override
              public OrionOccurrenceOverlay[] apply(List<DocumentHighlight> highlights)
                  throws FunctionException {
                final OrionOccurrenceOverlay[] occurrences =
                    new OrionOccurrenceOverlay[highlights.size()];
                for (int i = 0; i < occurrences.length; i++) {
                  DocumentHighlight highlight = highlights.get(i);
                  final OrionOccurrenceOverlay occurrence = OrionOccurrenceOverlay.create();
                  Position start = highlight.getRange().getStart();
                  Position end = highlight.getRange().getEnd();
                  int startIndex =
                      document.getIndexFromPosition(
                          new TextPosition(start.getLine(), start.getCharacter()));
                  int endIndex =
                      document.getIndexFromPosition(
                          new TextPosition(end.getLine(), end.getCharacter()));

                  occurrence.setStart(startIndex);
                  occurrence.setEnd(endIndex + 1);
                  occurrences[i] = occurrence;
                }
                return occurrences;
              }
            });
    return (JsPromise<OrionOccurrenceOverlay[]>) then;
  }
}
