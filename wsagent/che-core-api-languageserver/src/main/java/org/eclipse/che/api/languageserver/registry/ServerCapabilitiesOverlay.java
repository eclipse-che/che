/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.registry;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DocumentOnTypeFormattingOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class ServerCapabilitiesOverlay {
  private ServerCapabilities left;
  private ServerCapabilities right;

  public ServerCapabilitiesOverlay(ServerCapabilities left, ServerCapabilities right) {
    this.left = left;
    this.right = right;
  }

  public CodeLensOptions getCodeLensProvider() {
    CodeLensOptions leftOptions = left.getCodeLensProvider();
    CodeLensOptions rightOptions = right.getCodeLensProvider();
    if (leftOptions == null) {
      return rightOptions;
    }
    if (rightOptions == null) {
      return leftOptions;
    }
    CodeLensOptions result = new CodeLensOptions();
    if (leftOptions != null && leftOptions.isResolveProvider()
        || rightOptions != null && leftOptions.isResolveProvider()) {
      result.setResolveProvider(true);
    }
    return result;
  }

  public CompletionOptions getCompletionProvider() {
    CompletionOptions leftOptions = left.getCompletionProvider();
    CompletionOptions rightOptions = right.getCompletionProvider();
    if (leftOptions == null) {
      return rightOptions;
    }
    if (rightOptions == null) {
      return leftOptions;
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
    if (leftOptions == null) {
      return rightOptions;
    }
    if (rightOptions == null) {
      return leftOptions;
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
    if (leftOptions == null) {
      return rightOptions;
    }
    if (rightOptions == null) {
      return leftOptions;
    }
    SignatureHelpOptions result = new SignatureHelpOptions();

    List<String> triggerChars = new ArrayList<>();

    triggerChars.addAll(listish(leftOptions.getTriggerCharacters()));
    triggerChars.addAll(listish(rightOptions.getTriggerCharacters()));
    result.setTriggerCharacters(triggerChars);
    return result;
  }

  public Either<TextDocumentSyncKind, TextDocumentSyncOptions> getTextDocumentSync() {
    return mergeTextDocumentSync(left.getTextDocumentSync(), right.getTextDocumentSync());
  }

  private Either<TextDocumentSyncKind, TextDocumentSyncOptions> mergeTextDocumentSync(
      Either<TextDocumentSyncKind, TextDocumentSyncOptions> left,
      Either<TextDocumentSyncKind, TextDocumentSyncOptions> right) {
    if (left == null) {
      return right;
    }
    if (right == null) {
      return left;
    }
    if (left.equals(right)) {
      return left;
    }

    if (left.isLeft() && left.getLeft() == TextDocumentSyncKind.Full) {
      return left;
    }

    if (left.isLeft() && left.getLeft() == TextDocumentSyncKind.Incremental) {
      if (right.isLeft() && right.getLeft() == TextDocumentSyncKind.Full) {
        return right;
      } else {
        return left;
      }
    }

    if (left.isRight() && right.isRight()) {
      TextDocumentSyncOptions leftRight = left.getRight();
      TextDocumentSyncOptions rightRight = right.getRight();
      if (leftRight.getChange() == TextDocumentSyncKind.Full) {
        return left;
      }

      if (leftRight.getChange() == TextDocumentSyncKind.Incremental) {
        if (rightRight.getChange() == TextDocumentSyncKind.Full) {
          return right;
        } else {
          return left;
        }
      }
    }

    if (left.isLeft() && right.isRight()) {
      return right;
    }

    if (left.isRight() && right.isLeft()) {
      return left;
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
    result.setDocumentRangeFormattingProvider(
        or(ServerCapabilities::getDocumentRangeFormattingProvider));
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
