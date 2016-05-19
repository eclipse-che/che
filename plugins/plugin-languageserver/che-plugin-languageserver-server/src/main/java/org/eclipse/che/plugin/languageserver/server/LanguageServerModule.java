package org.eclipse.che.plugin.languageserver.server;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.languageserver.server.dummyimpl.FooLanguageServer;

import com.google.inject.AbstractModule;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        // HACK LanguageServers should be registered dynamically or at least via
        // some configuration.
        bind(FooLanguageServer.class);

        bind(TextDocumentServiceImpl.class);
    }

}
