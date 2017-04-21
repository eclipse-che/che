/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.hover;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
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
import org.eclipse.lsp4j.TextDocumentPositionParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides hover LS functionality for Orion editor.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class HoverProvider implements OrionHoverHandler {

    private final EditorAgent               editorAgent;
    private final TextDocumentServiceClient client;
    private final DtoBuildHelper            helper;

    @Inject
    public HoverProvider(EditorAgent editorAgent, TextDocumentServiceClient client, DtoBuildHelper helper) {
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

        TextEditor editor = ((TextEditor)activeEditor);
        if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
            return null;
        }

        LanguageServerEditorConfiguration configuration = (LanguageServerEditorConfiguration)editor.getConfiguration();
        if (configuration.getServerCapabilities().getHoverProvider() == null
            || !configuration.getServerCapabilities().getHoverProvider()) {
            return null;
        }

        Document document = editor.getDocument();
        TextDocumentPositionParams paramsDTO = helper.createTDPP(document, context.getOffset());

        Promise<Hover> promise = client.hover(paramsDTO);
        Promise<OrionHoverOverlay> then = promise.then(new Function<Hover, OrionHoverOverlay>() {
            @Override
            public OrionHoverOverlay apply(Hover arg) throws FunctionException {
                OrionHoverOverlay hover = OrionHoverOverlay.create();
                hover.setType("markdown");
                String content = renderContent(arg);
                // do not show hover with only white spaces
                if (StringUtils.isNullOrWhitespace(content)) {
                    return null;
                }
                hover.setContent(content);

                return hover;
            }

            private String renderContent(Hover hover) {
                List<String> contents = new ArrayList<String>();
                for (String dto : hover.getContents()) {
                    // plain markdown text
                    contents.add(dto);
                }
                return Joiner.on("\n\n").join(contents);
            }
        });
        return (JsPromise<OrionHoverOverlay>)then;

    }
}
