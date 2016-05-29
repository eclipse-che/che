package org.eclipse.che.plugin.languageserver.server.dummyimpl;

import java.util.concurrent.CompletableFuture;

import org.eclipse.che.plugin.languageserver.server.LanguageServerRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.TextDocumentService;
import io.typefox.lsapi.services.WindowService;
import io.typefox.lsapi.services.WorkspaceService;

/**
 * A dummy language server.
 */
@Singleton
public class FooLanguageServer implements LanguageServer {

    private String rootPath;
    private FooTextDocumentService documentService;

    @Inject
    public FooLanguageServer(LanguageServerRegistry registry) {
        // This is of course a HACK! Language servers should be
        // registered dynamically instead. I.e. by some sort of preferences
        // where e.g.
        // maven coordinates and an implementation class are provided.

        registry.registerForExtension("foo", this);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        rootPath = params.getRootPath();
        this.documentService = new FooTextDocumentService(rootPath);
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
        if (documentService == null)
            throw new IllegalStateException("Langauge server has not been initialized.");
        return documentService;
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
