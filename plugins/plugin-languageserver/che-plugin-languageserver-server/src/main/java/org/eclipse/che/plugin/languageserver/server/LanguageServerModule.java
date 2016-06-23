package org.eclipse.che.plugin.languageserver.server;

import com.google.inject.AbstractModule;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.languageserver.server.json.JsonLanguageServerRegistrant;
import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FatJarBasedLanguageServerRegistrant.class);
        bind(JsonLanguageServerRegistrant.class).asEagerSingleton();
//        bind(CSharpLanguageServerRegistrant.class).asEagerSingleton();
        bind(LanguageRegistryService.class);
        bind(TextDocumentServiceImpl.class);
        bind(WorkspaceServiceImpl.class);
        bind(PublishDiagnosticsParamsMessenger.class);
    }

}
