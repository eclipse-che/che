package org.eclipse.che.plugin.java.languageserver;

import java.lang.reflect.Proxy;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

public class JavaLSWrapper {
  private LanguageServer wrapped;

  public JavaLSWrapper(LanguageServer wrapped) {
    this.wrapped = wrapped;
  }

  public TextDocumentService getTextDocumentService() {
    return (TextDocumentService)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {TextDocumentService.class},
            new DynamicWrapper(
                new JavaTextDocumentServiceWraper(wrapped.getTextDocumentService()),
                wrapped.getTextDocumentService()));
  }
}
