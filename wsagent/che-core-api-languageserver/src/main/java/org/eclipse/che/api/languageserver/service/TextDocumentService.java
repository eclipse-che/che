/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.service;

import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LocationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SignatureHelpDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.TextEditDto;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageServer;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * REST API for the textDoc
 * <p>
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

    @SuppressWarnings("deprecation")
    @POST
    @Path("completion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionListDto completion(TextDocumentPositionParams textDocumentPositionParams)
            throws InterruptedException, ExecutionException, LanguageServerException {
        textDocumentPositionParams.getTextDocument().setUri(prefixURI(textDocumentPositionParams.getTextDocument().getUri()));
        textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
        LanguageServer server = getServer(textDocumentPositionParams.getTextDocument().getUri());
        if (server == null) {
            return null;
        }
        CompletionList result = server.getTextDocumentService().completion(textDocumentPositionParams).get();

        return new CompletionListDto(result);
    }

    @POST
    @Path("documentSymbol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends SymbolInformationDto> documentSymbol(DocumentSymbolParams documentSymbolParams)
            throws ExecutionException, InterruptedException, LanguageServerException {
        documentSymbolParams.getTextDocument().setUri(prefixURI(documentSymbolParams.getTextDocument().getUri()));
        LanguageServer server = getServer(documentSymbolParams.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }

        return server.getTextDocumentService().documentSymbol(documentSymbolParams).get().stream().map(o -> new SymbolInformationDto(o))
                     .collect(Collectors.toList());
    }

    @POST
    @Path("references")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends LocationDto> references(ReferenceParams params)
            throws ExecutionException, InterruptedException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }

        List<? extends Location> locations = server.getTextDocumentService().references(params).get();
        locations.forEach(o -> {
            o.setUri(removePrefixUri(o.getUri()));
        });
        return locations.stream().map(o -> new LocationDto(o)).collect(Collectors.toList());
    }

    @POST
    @Path("definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends LocationDto> definition(TextDocumentPositionParams params)
            throws ExecutionException, InterruptedException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }

        List<? extends Location> locations = server.getTextDocumentService().definition(params).get();
        locations.forEach(o -> {
            o.setUri(removePrefixUri(o.getUri()));
        });
        return locations.stream().map(o -> new LocationDto(o)).collect(Collectors.toList());
    }


    @POST
    @Path("completionItem/resolve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionItemDto resolveCompletionItem(ExtendedCompletionItemDto unresolved)
            throws InterruptedException, ExecutionException, LanguageServerException {
        LanguageServer server = getServer(prefixURI(unresolved.getTextDocumentIdentifier().getUri()));
        if (server != null) {
            return new CompletionItemDto(server.getTextDocumentService().resolveCompletionItem(unresolved).get());
        } else {
            return new CompletionItemDto(unresolved);
        }
    }

    @SuppressWarnings("deprecation")
    @POST
    @Path("hover")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HoverDto hover(TextDocumentPositionParams positionParams)
            throws LanguageServerException, ExecutionException, InterruptedException {
        positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
        positionParams.setUri(prefixURI(positionParams.getUri()));
        LanguageServer server = getServer(positionParams.getTextDocument().getUri());
        if (server != null) {
            return new HoverDto(server.getTextDocumentService().hover(positionParams).get());
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @POST
    @Path("signatureHelp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SignatureHelpDto signatureHelp(TextDocumentPositionParams positionParams)
            throws LanguageServerException, ExecutionException, InterruptedException {
        positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
        positionParams.setUri(prefixURI(positionParams.getUri()));
        LanguageServer server = getServer(positionParams.getTextDocument().getUri());
        if (server != null) {
            return new SignatureHelpDto(server.getTextDocumentService().signatureHelp(positionParams).get());
        } else {
            return null;
        }
    }

    @POST
    @Path("formatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEditDto> formatting(DocumentFormattingParams params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getTextDocumentService().formatting(params).get().stream().map(o -> new TextEditDto(o)).collect(Collectors.toList());

    }

    @POST
    @Path("rangeFormatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEditDto> rangeFormatting(DocumentRangeFormattingParams params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getTextDocumentService().rangeFormatting(params).get().stream().map(o -> new TextEditDto(o))
                     .collect(Collectors.toList());

    }

    @POST
    @Path("onTypeFormatting")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends TextEditDto> onTypeFormatting(DocumentOnTypeFormattingParams params)
            throws InterruptedException, ExecutionException, LanguageServerException {
        params.getTextDocument().setUri(prefixURI(params.getTextDocument().getUri()));
        LanguageServer server = getServer(params.getTextDocument().getUri());
        if (server == null) {
            return Collections.emptyList();
        }
        return server.getTextDocumentService().onTypeFormatting(params).get().stream().map(o -> new TextEditDto(o))
                     .collect(Collectors.toList());

    }

    @SuppressWarnings("deprecation")
    @POST
    @Path("didChange")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didChange(DidChangeTextDocumentParams change) throws LanguageServerException {
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
    public void didOpen(DidOpenTextDocumentParams openEvent) throws LanguageServerException {
        openEvent.getTextDocument().setUri(prefixURI(openEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(openEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didOpen(openEvent);
        }
    }

    @POST
    @Path("didClose")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didClose(DidCloseTextDocumentParams closeEvent) throws LanguageServerException {
        closeEvent.getTextDocument().setUri(prefixURI(closeEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(closeEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didClose(closeEvent);
        }
    }

    @POST
    @Path("didSave")
    @Consumes(MediaType.APPLICATION_JSON)
    public void didSave(DidSaveTextDocumentParams saveEvent) throws LanguageServerException {
        saveEvent.getTextDocument().setUri(prefixURI(saveEvent.getTextDocument().getUri()));
        LanguageServer server = getServer(saveEvent.getTextDocument().getUri());
        if (server != null) {
            server.getTextDocumentService().didSave(saveEvent);
        }
    }

    @POST
    @Path("documentHighlight")
    @Consumes(MediaType.APPLICATION_JSON)
    public DocumentHighlight documentHighlight(TextDocumentPositionParams positionParams)
            throws LanguageServerException, InterruptedException, ExecutionException {
        positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
        LanguageServer server = getServer(positionParams.getTextDocument().getUri());
        if (server != null) {
            return server.getTextDocumentService().documentHighlight(positionParams).get().get(0);
        }
        return null;
    }


    private LanguageServer getServer(String uri) throws LanguageServerException {
        return languageServerRegistry.findServer(uri);
    }
}
