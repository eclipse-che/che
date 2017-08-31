package org.eclipse.che.api.languageserver.service;

import java.util.concurrent.CompletableFuture;

public interface FileContentAccess {
  CompletableFuture<String> getFileContent();
}
