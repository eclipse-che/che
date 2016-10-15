/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemKind;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface CompletionItemDTO extends CompletionItem {

    /**
     * The TextDocumentIdentifier for which this completion item was generated.
     * Used to select proper language server
     */
    TextDocumentIdentifierDTO getTextDocumentIdentifier();

    void setTextDocumentIdentifier(TextDocumentIdentifierDTO identifier);

    /**
     * The label of this completion item. By default also the text that is
     * inserted when selecting this completion.
     */
    void setLabel(final String label);

    /**
     * The kind of this completion item. Based of the kind an icon is chosen by
     * the editor.
     */
    void setKind(final CompletionItemKind kind);

    /**
     * A human-readable string with additional information about this item, like
     * type or symbol information.
     */
    void setDetail(final String detail);

    /**
     * A human-readable string that represents a doc-comment.
     */
    void setDocumentation(final String documentation);

    /**
     * A string that shoud be used when comparing this item with other items.
     * When `falsy` the label is used.
     */
    void setSortText(final String sortText);

    /**
     * A string that should be used when filtering a set of completion items.
     * When `falsy` the label is used.
     */
    void setFilterText(final String filterText);

    /**
     * A string that should be inserted a document when selecting this
     * completion. When `falsy` the label is used.
     */
    void setInsertText(final String insertText);

    /**
     * An edit which is applied to a document when selecting this completion.
     * When an edit is provided the value of insertText is ignored. Overridden
     * to return the DTO type.
     */
    TextEditDTO getTextEdit();

    /**
     * An edit which is applied to a document when selecting this completion.
     * When an edit is provided the value of insertText is ignored.
     */
    void setTextEdit(final TextEditDTO textEdit);

    /**
     * An data entry field that is preserved on a completion item between a
     * completion and a completion resolve request.
     */
    void setData(final Object data);
}
