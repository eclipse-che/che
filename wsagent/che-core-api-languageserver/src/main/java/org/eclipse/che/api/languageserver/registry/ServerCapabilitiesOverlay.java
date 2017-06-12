package org.eclipse.che.api.languageserver.registry;

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

    public Boolean getCodeActionProvider() {
        return truish(left.getCodeActionProvider()) || truish(right.getCodeActionProvider());
    }

    public Boolean getDefinitionProvider() {
        return truish(left.getDefinitionProvider()) || truish(right.getDefinitionProvider());
    }

    public Boolean getDocumentFormattingProvider() {
        return truish(left.getDocumentFormattingProvider()) || truish(right.getDocumentFormattingProvider());
    }

    public Boolean getDocumentHighlightProvider() {
        return truish(left.getDocumentHighlightProvider()) || truish(right.getDocumentHighlightProvider());
    }

    public Boolean getDocumentRangeFormattingProvider() {
        return truish(left.getDocumentRangeFormattingProvider()) || truish(right.getDocumentRangeFormattingProvider());
    }

    public Boolean getDocumentSymbolProvider() {
        return truish(left.getDocumentSymbolProvider()) || truish(right.getDocumentSymbolProvider());
    }

    public Boolean getHoverProvider() {
        return truish(left.getHoverProvider()) || truish(right.getHoverProvider());
    }

    public Boolean getReferencesProvider() {
        return truish(left.getReferencesProvider()) || truish(right.getReferencesProvider());
    }

    public Boolean getRenameProvider() {
        return truish(left.getRenameProvider()) || truish(right.getRenameProvider());
    }

    public Boolean getWorkspaceSymbolProvider() {
        return truish(left.getWorkspaceSymbolProvider()) || truish(right.getWorkspaceSymbolProvider());
    }

    private boolean truish(Boolean b) {
        return b != null && b;
    }

    private <T> List<T> listish(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public ServerCapabilities compute() {
        ServerCapabilities result = new ServerCapabilities();
        result.setCodeActionProvider(getCodeActionProvider());
        result.setCodeLensProvider(getCodeLensProvider());
        result.setCompletionProvider(getCompletionProvider());
        result.setDefinitionProvider(getDefinitionProvider());
        result.setDocumentFormattingProvider(getDocumentFormattingProvider());
        result.setDocumentHighlightProvider(getDocumentHighlightProvider());
        result.setDocumentOnTypeFormattingProvider(getDocumentOnTypeFormattingProvider());
        result.setDocumentRangeFormattingProvider(getDocumentRangeFormattingProvider());
        result.setDocumentSymbolProvider(getDocumentSymbolProvider());
        result.setHoverProvider(getHoverProvider());
        result.setReferencesProvider(getReferencesProvider());
        result.setRenameProvider(getRenameProvider());
        result.setSignatureHelpProvider(getSignatureHelpProvider());
        result.setTextDocumentSync(getTextDocumentSync());
        result.setWorkspaceSymbolProvider(getWorkspaceSymbolProvider());
        
        return result;
    }
}
