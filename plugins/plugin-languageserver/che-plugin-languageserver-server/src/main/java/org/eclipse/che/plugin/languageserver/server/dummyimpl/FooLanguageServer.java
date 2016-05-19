package org.eclipse.che.plugin.languageserver.server.dummyimpl;

import org.eclipse.che.plugin.languageserver.server.LanguageServerRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageServer;
import io.typefox.lsapi.TextDocumentService;
import io.typefox.lsapi.WindowService;
import io.typefox.lsapi.WorkspaceService;

/**
 * A dummy language server. This is of course a HACK! Language servers should be
 * registered dynamically instead. I.e. by some sort of preferences where e.g.
 * maven coordinates and an implementation class are provided.
 */
@Singleton
public class FooLanguageServer implements LanguageServer {

    @Inject
    public FooLanguageServer(LanguageServerRegistry registry) {
        registry.registerForExtension("foo", this);
    }

    @Override
    public InitializeResult initialize(InitializeParams params) {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void exit() {
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new FooTextDocumentService();
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return null;
    }

    @Override
    public WindowService getWindowService() {
        return null;
    }

}
