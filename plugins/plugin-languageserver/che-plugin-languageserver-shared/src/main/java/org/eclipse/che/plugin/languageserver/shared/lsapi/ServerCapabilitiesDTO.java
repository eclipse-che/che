/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.ServerCapabilities;

@DTO
public interface ServerCapabilitiesDTO extends ServerCapabilities {
    /**
     * Defines how text documents are synced.
     * 
     */
    public abstract void setTextDocumentSync(final Integer textDocumentSync);

    /**
     * The server provides hover support.
     * 
     */
    public abstract void setHoverProvider(final Boolean hoverProvider);

    /**
     * The server provides completion support. Overridden to return the DTO
     * type.
     * 
     */
    public abstract CompletionOptionsDTO getCompletionProvider();

    /**
     * The server provides completion support.
     * 
     */
    public abstract void setCompletionProvider(final CompletionOptionsDTO completionProvider);

    /**
     * The server provides signature help support. Overridden to return the DTO
     * type.
     * 
     */
    public abstract SignatureHelpOptionsDTO getSignatureHelpProvider();

    /**
     * The server provides signature help support.
     * 
     */
    public abstract void setSignatureHelpProvider(final SignatureHelpOptionsDTO signatureHelpProvider);

    /**
     * The server provides goto definition support.
     * 
     */
    public abstract void setDefinitionProvider(final Boolean definitionProvider);

    /**
     * The server provides find references support.
     * 
     */
    public abstract void setReferencesProvider(final Boolean referencesProvider);

    /**
     * The server provides document highlight support.
     * 
     */
    public abstract void setDocumentHighlightProvider(final Boolean documentHighlightProvider);

    /**
     * The server provides document symbol support.
     * 
     */
    public abstract void setDocumentSymbolProvider(final Boolean documentSymbolProvider);

    /**
     * The server provides workspace symbol support.
     * 
     */
    public abstract void setWorkspaceSymbolProvider(final Boolean workspaceSymbolProvider);

    /**
     * The server provides code actions.
     * 
     */
    public abstract void setCodeActionProvider(final Boolean codeActionProvider);

    /**
     * The server provides code lens. Overridden to return the DTO type.
     * 
     */
    public abstract CodeLensOptionsDTO getCodeLensProvider();

    /**
     * The server provides code lens.
     * 
     */
    public abstract void setCodeLensProvider(final CodeLensOptionsDTO codeLensProvider);

    /**
     * The server provides document formatting.
     * 
     */
    public abstract void setDocumentFormattingProvider(final Boolean documentFormattingProvider);

    /**
     * The server provides document range formatting.
     * 
     */
    public abstract void setDocumentRangeFormattingProvider(final Boolean documentRangeFormattingProvider);

    /**
     * The server provides document formatting on typing. Overridden to return
     * the DTO type.
     * 
     */
    public abstract DocumentOnTypeFormattingOptionsDTO getDocumentOnTypeFormattingProvider();

    /**
     * The server provides document formatting on typing.
     * 
     */
    public abstract void setDocumentOnTypeFormattingProvider(
            final DocumentOnTypeFormattingOptionsDTO documentOnTypeFormattingProvider);

    /**
     * The server provides rename support.
     * 
     */
    public abstract void setRenameProvider(final Boolean renameProvider);
}
