package org.eclipse.che.plugin.java.languageserver;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

public interface JavaLanguageServer extends LanguageServer {
  @JsonRequest("java/classFileContents")
  CompletableFuture<String> classFileContents(TextDocumentIdentifier documentUri);
}
