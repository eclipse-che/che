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
package org.eclipse.che.plugin.languageserver.ide.hover;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionHoverHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverContextOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionHoverOverlay;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Provides hover LS functionality for Orion editor.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class HoverProvider implements OrionHoverHandler {

  private final EditorAgent editorAgent;
  private final TextDocumentServiceClient client;
  private final DtoBuildHelper helper;

  @Inject
  public HoverProvider(
      EditorAgent editorAgent, TextDocumentServiceClient client, DtoBuildHelper helper) {
    this.editorAgent = editorAgent;
    this.client = client;
    this.helper = helper;
  }

  @Override
  public JsPromise<OrionHoverOverlay> computeHover(OrionHoverContextOverlay context) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor == null || !(activeEditor instanceof TextEditor)) {
      return null;
    }

    TextEditor editor = ((TextEditor) activeEditor);
    if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
      return null;
    }

    LanguageServerEditorConfiguration configuration =
        (LanguageServerEditorConfiguration) editor.getConfiguration();
    if (configuration.getServerCapabilities().getHoverProvider() == null
        || !configuration.getServerCapabilities().getHoverProvider()) {
      return null;
    }

    Document document = editor.getDocument();
    TextDocumentPositionParams paramsDTO = helper.createTDPP(document, context.getOffset());

    Promise<Hover> promise = client.hover(paramsDTO);
    Promise<OrionHoverOverlay> then =
        promise.then(
            (Hover arg) -> {
              OrionHoverOverlay hover = OrionHoverOverlay.create();
              hover.setType("markdown");
              String content = renderContent(arg);
              // do not show hover with only white spaces
              if (StringUtils.isNullOrWhitespace(content)) {
                return null;
              }
              hover.setContent(content);

              return hover;
            });
    return (JsPromise<OrionHoverOverlay>) then;
  }

  private String renderContent(Hover hover) {
    List<String> contents = new ArrayList<>();
    for (Either<String, MarkedString> dto : hover.getContents()) {
      if (dto.isLeft()) {
        // plain markdown text
        contents.add(dto.getLeft());
      } else {
        contents.add(dto.getRight().getLanguage());
        contents.add(dto.getRight().getValue());
      }
    }
    return Joiner.on("\n\n").join(contents);
  }
}
