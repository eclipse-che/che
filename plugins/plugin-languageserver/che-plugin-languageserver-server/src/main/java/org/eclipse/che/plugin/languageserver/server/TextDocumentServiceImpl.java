package org.eclipse.che.plugin.languageserver.server;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DocumentSymbolParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;

/**
 * REST API for the textDocument/* services defined in https://github.com/Microsoft/vscode-languageserver-protocol
 * Dispatches onto the {@link LanguageServerRegistry}.
 */
@Singleton
@Path("languageserver/textDocument")
public class TextDocumentServiceImpl {

    LanguageServerRegistry languageServerRegistry;

    @Inject
    public TextDocumentServiceImpl(LanguageServerRegistry languageServerRegistry) {
        this.languageServerRegistry = languageServerRegistry;
    }
    
    static String prefixURI(String relativePath) {
        return "file:///projects"+relativePath;
    }

    @POST
    @Path("completion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends CompletionItem> completion(TextDocumentPositionParamsDTO textDocumentPositionParams) throws InterruptedException, ExecutionException {
        textDocumentPositionParams.getTextDocument().setUri(prefixURI(textDocumentPositionParams.getTextDocument().getUri()));
        textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
        LanguageServer server = getServer(textDocumentPositionParams.getTextDocument().getUri());
        if (server == null) {
        	return emptyList();
        }
        List<? extends CompletionItem> completion = server.getTextDocumentService()
                                                          .completion(textDocumentPositionParams).get().getItems();
        return completion;
    }

    @POST
    @Path("documentSymbol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends SymbolInformation> documentSymbol(DocumentSymbolParamsDTO documentSymbolParams)
            throws ExecutionException, InterruptedException {
        documentSymbolParams.getTextDocument().setUri(prefixURI(documentSymbolParams.getTextDocument().getUri()));
        LanguageServer server = getServer(documentSymbolParams.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }

        return server.getTextDocumentService().documentSymbol(documentSymbolParams).get();
    }


    @POST
    @Path("completionItem/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionItem resolveCompletionItem(CompletionItemDTO unresolved) throws InterruptedException, ExecutionException {
        LanguageServer server = getServer(unresolved.getTextDocumentIdentifier().getUri());
        if (server != null)
        	return server.getTextDocumentService().resolveCompletionItem(unresolved).get();
        else
        	return unresolved;
    }

    @POST
    @Path("didChange")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didChange(DidChangeTextDocumentParamsDTO change) {
        change.getTextDocument().setUri(prefixURI(change.getTextDocument().getUri()));
        change.setUri(prefixURI(change.getUri()));
        LanguageServer server = getServer(change.getTextDocument().getUri());
        if (server != null)
        	server.getTextDocumentService().didChange(change);
    }

    @POST
    @Path("didOpen")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didOpen(DidOpenTextDocumentParamsDTO openEvent) {
        openEvent.getTextDocument().setUri(prefixURI(openEvent.getTextDocument().getUri()));
        openEvent.setUri(prefixURI(openEvent.getUri()));
        LanguageServer server = getServer(openEvent.getTextDocument().getUri());
        if (server != null)
        	server.getTextDocumentService().didOpen(openEvent);
    }

    @POST
    @Path("didClose")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didClose(DidCloseTextDocumentParamsDTO closeEvent) {
        closeEvent.getTextDocument().setUri(prefixURI(closeEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(closeEvent.getTextDocument().getUri());
        if (server != null)
        	server.getTextDocumentService().didClose(closeEvent);
    }

    @POST
    @Path("didSave")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didSave(DidSaveTextDocumentParamsDTO saveEvent) {
        saveEvent.getTextDocument().setUri(prefixURI(saveEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(saveEvent.getTextDocument().getUri());
        if (server != null)
        	server.getTextDocumentService().didSave(saveEvent);
    }

    private LanguageServer getServer(String uri) {
        return languageServerRegistry.findServer(uri);
    }
}
