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
package org.eclipse.che.plugin.java.languageserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.services.TextDocumentService;

public class JavaTextDocumentServiceWraper {
  private TextDocumentService wrapped;

  public JavaTextDocumentServiceWraper(TextDocumentService wrapped) {
    this.wrapped = wrapped;
  }

  public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
    CompletableFuture<List<? extends Command>> result = wrapped.codeAction(params);
    return result.thenApply(
        (List<? extends Command> commands) -> {
          commands.forEach(
              cmd -> {
                if ("java.apply.workspaceEdit".equals(cmd.getCommand())) {
                  cmd.setCommand("lsp.applyWorkspaceEdit");
                }
              });
          return commands;
        });
  }
}
