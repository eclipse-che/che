/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.service;

import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.Hover;
import io.typefox.lsapi.Location;
import io.typefox.lsapi.SignatureHelp;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.TextEdit;
import io.typefox.lsapi.impl.LocationImpl;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentOnTypeFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentRangeFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentSymbolParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.ReferenceParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

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
 * Dispatches onto the {@link LanguageServerRegistryImpl}.
 */
@Singleton
@Path("languageserver/textDocument")
public class TextDocumentService {

    private static final String FILE_PROJECTS = "file:///projects";

    private final LanguageServerRegistry languageServerRegistry;

    @Inject
    public TextDocumentService(LanguageServerRegistry languageServerRegistry) {
        this.languageServerRegistry = languageServerRegistry;
    }

    static String prefixURI(String relativePath) {
        return FILE_PROJECTS + relativePath;
    }

    static String removePrefixUri(String uri) {
        if (uri.startsWith(FILE_PROJECTS)) {
            return uri.substring(FILE_PROJECTS.length());
        }
        return uri;
    }

    @POST
    @Path("completion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends CompletionItem> completion(TextDocumentPositionParamsDTO textDocumentPositionParams) throws InterruptedException,
                                                                                                                      ExecutionException,
                                                                                                                      LanguageServerException {
        textDocumentPositionParams.getTextDocument().setUri(prefixURI(textDocumentPositionParams.getTextDocument().getUri()));
        textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
        LanguageServer server = getServer(textDocumentPositionParams.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }
        return server.getTextDocumentService()
                     .completion(textDocumentPositionParams).get().getItems();
    }

    @POST
    @Path("documentSymbol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends SymbolInformation> documentSymbol(DocumentSymbolParamsDTO documentSymbolParams) throws ExecutionException,
                                                                                                                 InterruptedException,
                                                                                                                 LanguageServerException {
        documentSymbolParams.getTextDocument().setUri(prefixURI(documentSymbolParams.getTextDocument().getUri()));
        LanguageServer server = getServer(documentSymbolParams.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }

        return server.getTextDocumentService().documentSymbol(documentSymbolParams).get();
    }

    @POST
    @Path("references")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends Location> references(ReferenceParamsDTO params) throws ExecutionException,
                                                                                 InterruptedException,
                                                                                 LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }

        List<? extends Location> locations = server.getTextDocumentService().references(params).get();
        locations.forEach(o -> {
            if (o instanceof LocationImpl) {
                ((LocationImpl)o).setUri(removePrefixUri(o.getUri()));
            }
        });
        return locations;
    }


    @POST
    @Path("definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends Location> definition(TextDocumentPositionParamsDTO params) throws ExecutionException,
                                                                                            InterruptedException,
                                                                                            LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }

        List<? extends Location> locations = server.getTextDocumentService().definition(params).get();
        locations.forEach(o -> {
            if (o instanceof LocationImpl) {
                ((LocationImpl)o).setUri(removePrefixUri(o.getUri()));
            }
        });
        return locations;
    }


    @POST
    @Path("completionItem/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionItem resolveCompletionItem(CompletionItemDTO unresolved) throws InterruptedException,
                                                                                     ExecutionException,
                                                                                     LanguageServerException {
        LanguageServer server = getServer(unresolved.getTextDocumentIdentifier().getUri());
        if (server != null) {
            return server.getTextDocumentService().resolveCompletionItem(unresolved).get();
        } else {
            return unresolved;
        }
    }

    @POST
    @Path("hover")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Hover hover(TextDocumentPositionParamsDTO positionParams)
            throws LanguageServerException, ExecutionException, InterruptedException {
        positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
        positionParams.setUri(prefixURI(positionParams.getUri()));
        LanguageServer server = getServer(positionParams.getTextDocument().getUri());
        if (server != null) {
            return server.getTextDocumentService().hover(positionParams).get();
        } else {
            return null;
        }
    }

    @POST
    @Path("signatureHelp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SignatureHelp signatureHelp(TextDocumentPositionParamsDTO positionParams)
            throws LanguageServerException, ExecutionException, InterruptedException {
        positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
        positionParams.setUri(prefixURI(positionParams.getUri()));
        LanguageServer server = getServer(positionParams.getTextDocument().getUri());
        if (server != null) {
            return server.getTextDocumentService().signatureHelp(positionParams).get();
        } else {
            return null;
        }
    }

    @POST
    @Path("formatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEdit> formatting(DocumentFormattingParamsDTO params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }
        return server.getTextDocumentService().formatting(params).get();

    }

    @POST
    @Path("rangeFormatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEdit> rangeFormatting(DocumentRangeFormattingParamsDTO params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }
        return server.getTextDocumentService().rangeFormatting(params).get();

    }

    @POST
    @Path("onTypeFormatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEdit> onTypeFormatting(DocumentOnTypeFormattingParamsDTO params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return emptyList();
        }
        return server.getTextDocumentService().onTypeFormatting(params).get();

    }

    @POST
    @Path("didChange")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didChange(DidChangeTextDocumentParamsDTO change) throws LanguageServerException {
        change.getTextDocument().setUri(prefixURI(change.getTextDocument().getUri()));
        change.setUri(prefixURI(change.getUri()));
        LanguageServer server = getServer(change.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didChange(change);
        }
    }

    @POST
    @Path("didOpen")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didOpen(DidOpenTextDocumentParamsDTO openEvent) throws LanguageServerException {
        openEvent.getTextDocument().setUri(prefixURI(openEvent.getTextDocument().getUri()));
        openEvent.setUri(prefixURI(openEvent.getUri()));
        LanguageServer server = getServer(openEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didOpen(openEvent);
        }
    }

    @POST
    @Path("didClose")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didClose(DidCloseTextDocumentParamsDTO closeEvent) throws LanguageServerException {
        closeEvent.getTextDocument().setUri(prefixURI(closeEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(closeEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didClose(closeEvent);
        }
    }

    @POST
    @Path("didSave")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didSave(DidSaveTextDocumentParamsDTO saveEvent) throws LanguageServerException {
        saveEvent.getTextDocument().setUri(prefixURI(saveEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(saveEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didSave(saveEvent);
        }
    }

    private LanguageServer getServer(String uri) throws LanguageServerException {
        return languageServerRegistry.findServer(uri);
    }
}
