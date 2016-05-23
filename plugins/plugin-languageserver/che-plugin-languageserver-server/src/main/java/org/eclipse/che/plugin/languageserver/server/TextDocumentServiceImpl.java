package org.eclipse.che.plugin.languageserver.server;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.che.plugin.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.LanguageServer;

@Singleton
@Path("languageserver/textDocument")
public class TextDocumentServiceImpl {

    LanguageServerRegistry languageServerRegistry;

    @Inject
    public TextDocumentServiceImpl(LanguageServerRegistry languageServerRegistry) {
        this.languageServerRegistry = languageServerRegistry;
    }

    @POST
    @Path("completion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends CompletionItem> completion(TextDocumentPositionParamsDTO textDocumentPositionParams) {
        LanguageServer server = languageServerRegistry
                .findServer(textDocumentPositionParams.getTextDocument().getUri());
        if (server == null) {
            // TODO error handling
            return newArrayList();
        }
        List<? extends CompletionItem> completion = server.getTextDocumentService()
                .completion(textDocumentPositionParams);
        return completion;
    }
    
    @POST
    @Path("didChange")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didChange(DidChangeTextDocumentParamsDTO change) {
        LanguageServer server = languageServerRegistry
                .findServer(change.getTextDocument().getUri());
        if (server == null) {
            // TODO error handling
            return;
        }
        server.getTextDocumentService()
                .didChange(change);
    }

}
