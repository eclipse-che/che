/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.registry;

import com.google.common.base.Function;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DocumentOnTypeFormattingOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerCapabilitiesOverlay  {
    private ServerCapabilities left;
    private ServerCapabilities right;

    public ServerCapabilitiesOverlay(ServerCapabilities left, ServerCapabilities right) {
        this.left = left;
        this.right = right;
    }

    public CodeLensOptions getCodeLensProvider() {
        CodeLensOptions leftOptions = left.getCodeLensProvider();
        CodeLensOptions rightOptions = right.getCodeLensProvider();
        if (leftOptions == null && rightOptions == null) {
            return null;
        }
        CodeLensOptions result = new CodeLensOptions();
        if (leftOptions != null && leftOptions.isResolveProvider() || rightOptions != null && leftOptions.isResolveProvider()) {
            result.setResolveProvider(true);
        } 
        return result;
    }

    public CompletionOptions getCompletionProvider() {
        CompletionOptions leftOptions = left.getCompletionProvider();
        CompletionOptions rightOptions = right.getCompletionProvider();
        if (leftOptions == null && rightOptions == null) {
            return null;
        }

        CompletionOptions result = new CompletionOptions();
        List<String> triggerChars = new ArrayList<>();

        if (leftOptions != null) {
            triggerChars.addAll(listish(leftOptions.getTriggerCharacters()));
        }
        if (rightOptions != null) {
            triggerChars.addAll(listish(rightOptions.getTriggerCharacters()));
        }
        result.setTriggerCharacters(triggerChars);
        return result;
    }

    public DocumentOnTypeFormattingOptions getDocumentOnTypeFormattingProvider() {
        DocumentOnTypeFormattingOptions leftOptions = left.getDocumentOnTypeFormattingProvider();
        DocumentOnTypeFormattingOptions rightOptions = right.getDocumentOnTypeFormattingProvider();
        if (leftOptions == null && rightOptions == null) {
            return null;
        }

        DocumentOnTypeFormattingOptions result = new DocumentOnTypeFormattingOptions();
        List<String> triggerChars = new ArrayList<>();

        if (leftOptions != null) {
            result.setFirstTriggerCharacter(leftOptions.getFirstTriggerCharacter());
            triggerChars.addAll(listish(leftOptions.getMoreTriggerCharacter()));
        }
        if (rightOptions != null) {
            triggerChars.addAll(listish(rightOptions.getMoreTriggerCharacter()));
        }
        result.setMoreTriggerCharacter(triggerChars);
        return result;
    }

    public SignatureHelpOptions getSignatureHelpProvider() {
        SignatureHelpOptions leftOptions = left.getSignatureHelpProvider();
        SignatureHelpOptions rightOptions = right.getSignatureHelpProvider();
        if (leftOptions == null && rightOptions == null) {
            return null;
        }
        SignatureHelpOptions result = new SignatureHelpOptions();

        List<String> triggerChars = new ArrayList<>();

        if (leftOptions != null) {
            triggerChars.addAll(listish(leftOptions.getTriggerCharacters()));
        }
        if (rightOptions != null) {
            triggerChars.addAll(listish(rightOptions.getTriggerCharacters()));
        }
        result.setTriggerCharacters(triggerChars);
        return result;
    }

    public TextDocumentSyncKind getTextDocumentSync() {
        return mergeTextDocumentSync(left.getTextDocumentSync(), right.getTextDocumentSync());
    }

    private TextDocumentSyncKind mergeTextDocumentSync(TextDocumentSyncKind left, TextDocumentSyncKind right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        if (left.equals(right)) {
            return left;
        }
        if (left == TextDocumentSyncKind.Full) {
            return left;
        }
        if (left == TextDocumentSyncKind.Incremental) {
            if (right == TextDocumentSyncKind.Full) {
                return TextDocumentSyncKind.Full;
            } else {
                return TextDocumentSyncKind.Incremental;
            }
        }
        return right;
    }

    
    private Boolean or(Function<ServerCapabilities, Boolean> f) {
        Boolean leftVal = f.apply(left);
        Boolean rightVal = f.apply(right);
        if (leftVal == null) {
            return rightVal;
        }
        if (rightVal == null) {
            return leftVal;
        }
        return leftVal || rightVal;
    }
    
   private <T> List<T> listish(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public ServerCapabilities compute() {
        
        ServerCapabilities result = new ServerCapabilities();
        result.setCodeActionProvider(or(ServerCapabilities::getCodeActionProvider));
        result.setCodeLensProvider(getCodeLensProvider());
        result.setCompletionProvider(getCompletionProvider());
        result.setDefinitionProvider(or(ServerCapabilities::getDefinitionProvider));
        result.setDocumentFormattingProvider(or(ServerCapabilities::getDocumentFormattingProvider));
        result.setDocumentHighlightProvider(or(ServerCapabilities::getDocumentHighlightProvider));
        result.setDocumentOnTypeFormattingProvider(getDocumentOnTypeFormattingProvider());
        result.setDocumentRangeFormattingProvider(or(ServerCapabilities::getDocumentRangeFormattingProvider));
        result.setDocumentSymbolProvider(or(ServerCapabilities::getDocumentSymbolProvider));
        result.setHoverProvider(or(ServerCapabilities::getHoverProvider));
        result.setReferencesProvider(or(ServerCapabilities::getReferencesProvider));
        result.setRenameProvider(or(ServerCapabilities::getRenameProvider));
        result.setSignatureHelpProvider(getSignatureHelpProvider());
        result.setTextDocumentSync(getTextDocumentSync());
        result.setWorkspaceSymbolProvider(or(ServerCapabilities::getWorkspaceSymbolProvider));
        
        return result;
    }
}
