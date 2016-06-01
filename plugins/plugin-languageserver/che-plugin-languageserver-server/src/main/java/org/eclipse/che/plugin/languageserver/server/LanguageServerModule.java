package org.eclipse.che.plugin.languageserver.server;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;

import com.google.inject.AbstractModule;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FatJarBasedLanguageServerRegistrant.class);

        bind(LanguageRegistryService.class);
        bind(TextDocumentServiceImpl.class);
        bind(PublishDiagnosticsParamsMessenger.class);
    }

}
