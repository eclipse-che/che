/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.languageserver;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.services.TextDocumentService;

public class JavaLSWrapper {
  private JavaLanguageServer wrapped;

  public JavaLSWrapper(JavaLanguageServer wrapped) {
    this.wrapped = wrapped;
  }

  public CompletableFuture<String> getFileContent(String uri) {
    return wrapped.classFileContents(new org.eclipse.lsp4j.TextDocumentIdentifier(uri));
  }

  public TextDocumentService getTextDocumentService() {
    return (TextDocumentService)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {TextDocumentService.class, FileContentAccess.class},
            new DynamicWrapper(
                new JavaTextDocumentServiceWraper(wrapped.getTextDocumentService()),
                wrapped.getTextDocumentService()));
  }
}
