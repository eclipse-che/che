/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.TextDocumentSyncKind;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface ServerCapabilitiesDTO extends ServerCapabilities {
    /**
     * Defines how text documents are synced.
     */
    void setTextDocumentSync(final TextDocumentSyncKind textDocumentSync);

    /**
     * The server provides hover support.
     */
    void setHoverProvider(final Boolean hoverProvider);

    /**
     * The server provides completion support. Overridden to return the DTO
     * type.
     */
    CompletionOptionsDTO getCompletionProvider();

    /**
     * The server provides completion support.
     */
    void setCompletionProvider(final CompletionOptionsDTO completionProvider);

    /**
     * The server provides signature help support. Overridden to return the DTO
     * type.
     */
    SignatureHelpOptionsDTO getSignatureHelpProvider();

    /**
     * The server provides signature help support.
     */
    void setSignatureHelpProvider(final SignatureHelpOptionsDTO signatureHelpProvider);

    /**
     * The server provides goto definition support.
     */
    void setDefinitionProvider(final Boolean definitionProvider);

    /**
     * The server provides find references support.
     */
    void setReferencesProvider(final Boolean referencesProvider);

    /**
     * The server provides document highlight support.
     */
    void setDocumentHighlightProvider(final Boolean documentHighlightProvider);

    /**
     * The server provides document symbol support.
     */
    void setDocumentSymbolProvider(final Boolean documentSymbolProvider);

    /**
     * The server provides workspace symbol support.
     */
    void setWorkspaceSymbolProvider(final Boolean workspaceSymbolProvider);

    /**
     * The server provides code actions.
     */
    void setCodeActionProvider(final Boolean codeActionProvider);

    /**
     * The server provides code lens. Overridden to return the DTO type.
     */
    CodeLensOptionsDTO getCodeLensProvider();

    /**
     * The server provides code lens.
     */
    void setCodeLensProvider(final CodeLensOptionsDTO codeLensProvider);

    /**
     * The server provides document formatting.
     */
    void setDocumentFormattingProvider(final Boolean documentFormattingProvider);

    /**
     * The server provides document range formatting.
     */
    void setDocumentRangeFormattingProvider(final Boolean documentRangeFormattingProvider);

    /**
     * The server provides document formatting on typing. Overridden to return
     * the DTO type.
     */
    DocumentOnTypeFormattingOptionsDTO getDocumentOnTypeFormattingProvider();

    /**
     * The server provides document formatting on typing.
     */
    void setDocumentOnTypeFormattingProvider(
            final DocumentOnTypeFormattingOptionsDTO documentOnTypeFormattingProvider);

    /**
     * The server provides rename support.
     */
    void setRenameProvider(final Boolean renameProvider);
}
