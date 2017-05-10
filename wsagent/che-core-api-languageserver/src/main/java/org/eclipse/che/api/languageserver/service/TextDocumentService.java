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

import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.removePrefixUri;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.DocumentHighlightDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LocationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SignatureHelpDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.TextEditDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json RPC API for the textDoc
 * <p>
 * Dispatches onto the {@link LanguageServerRegistryImpl}.
 */
@Singleton
public class TextDocumentService {
    private static final Logger LOG = LoggerFactory.getLogger(TextDocumentService.class);

    private final LanguageServerRegistry     languageServerRegistry;
    private final RequestHandlerConfigurator requestHandler;

    @Inject
    public TextDocumentService(LanguageServerRegistry languageServerRegistry, RequestHandlerConfigurator requestHandler) {
        this.languageServerRegistry = languageServerRegistry;
        this.requestHandler = requestHandler;
    }

    @PostConstruct
    public void configureMethods() {
        dtoToDtoList("definition", TextDocumentPositionParams.class, LocationDto.class, this::definition);
        dtoToDtoList("documentSymbol", DocumentSymbolParams.class, SymbolInformationDto.class, this::documentSymbol);
        dtoToDtoList("formatting", DocumentFormattingParams.class, TextEditDto.class, this::formatting);
        dtoToDtoList("rangeFormatting", DocumentRangeFormattingParams.class, TextEditDto.class, this::rangeFormatting);
        dtoToDtoList("references", ReferenceParams.class, LocationDto.class, this::references);
        dtoToDtoList("onTypeFormatting", DocumentOnTypeFormattingParams.class, TextEditDto.class, this::onTypeFormatting);

        dtoToDto("completionItem/resolve", ExtendedCompletionItemDto.class, CompletionItemDto.class, this::completionItemResolve);
        dtoToDtoList("documentHighlight", TextDocumentPositionParams.class, DocumentHighlightDto.class, this::documentHighlight);
        dtoToDto("completion", TextDocumentPositionParams.class, CompletionListDto.class, this::completion);
        dtoToDto("hover", TextDocumentPositionParams.class, HoverDto.class, this::hover);
        dtoToDto("signatureHelp", TextDocumentPositionParams.class, SignatureHelpDto.class, this::signatureHelp);

        dtoToNothing("didChange", DidChangeTextDocumentParams.class, this::didChange);
        dtoToNothing("didClose", DidCloseTextDocumentParams.class, this::didClose);
        dtoToNothing("didOpen", DidOpenTextDocumentParams.class, this::didOpen);
        dtoToNothing("didSave", DidSaveTextDocumentParams.class, this::didSave);
    }

    private CompletionListDto completion(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            TextDocumentIdentifier textDocument = textDocumentPositionParams.getTextDocument();
            textDocument.setUri(prefixURI(textDocument.getUri()));
            textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
            LanguageServer server = getServer(textDocument.getUri());
            return server != null ? new CompletionListDto(server.getTextDocumentService().completion(textDocumentPositionParams).get())
                                  : null;

        } catch (LanguageServerException | InterruptedException | ExecutionException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<SymbolInformationDto> documentSymbol(DocumentSymbolParams documentSymbolParams) {
        try {
            documentSymbolParams.getTextDocument().setUri(prefixURI(documentSymbolParams.getTextDocument().getUri()));
            LanguageServer server = getServer(documentSymbolParams.getTextDocument().getUri());
            return server == null ? Collections.emptyList() : server.getTextDocumentService()
                                                                    .documentSymbol(documentSymbolParams)
                                                                    .get()
                                                                    .stream()
                                                                    .map(SymbolInformationDto::new)
                                                                    .collect(Collectors.toList());

        } catch (ExecutionException | InterruptedException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());

        }
    }

    private List<LocationDto> references(ReferenceParams referenceParams) {
        try {
            referenceParams.getTextDocument().setUri(prefixURI(referenceParams.getTextDocument().getUri()));
            LanguageServer server = getServer(referenceParams.getTextDocument().getUri());
            if (server == null) {
                return Collections.emptyList();
            }

            List<? extends Location> locations = server.getTextDocumentService().references(referenceParams).get();
            locations.forEach(o -> o.setUri(removePrefixUri(o.getUri())));
            return locations.stream().map(LocationDto::new).collect(Collectors.toList());
        } catch (ExecutionException | InterruptedException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());

        }
    }

    private List<LocationDto> definition(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            textDocumentPositionParams.getTextDocument().setUri(prefixURI(textDocumentPositionParams.getTextDocument().getUri()));
            LanguageServer server = getServer(textDocumentPositionParams.getTextDocument().getUri());
            if (server == null) {
                return Collections.emptyList();
            }

            List<? extends Location> locations = server.getTextDocumentService().definition(textDocumentPositionParams).get();
            locations.forEach(o -> o.setUri(removePrefixUri(o.getUri())));
            return locations.stream().map(LocationDto::new).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private CompletionItemDto completionItemResolve(ExtendedCompletionItem unresolved) {
        try {
            LanguageServer server = getServer(prefixURI(unresolved.getTextDocumentIdentifier().getUri()));

            return server != null ? new CompletionItemDto(server.getTextDocumentService().resolveCompletionItem(unresolved).get())
                                  : new CompletionItemDto(unresolved);
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private HoverDto hover(TextDocumentPositionParams positionParams) {
        try {
            positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
            positionParams.setUri(prefixURI(positionParams.getUri()));
            LanguageServer server = getServer(positionParams.getTextDocument().getUri());
            if(server != null) {
                Hover hover = server.getTextDocumentService().hover(positionParams).get();
                if (hover != null) {
                    return new HoverDto(hover);
                }
            }
            return null;
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private SignatureHelpDto signatureHelp(TextDocumentPositionParams positionParams) {
        try {
            positionParams.getTextDocument().setUri(prefixURI(positionParams.getTextDocument().getUri()));
            positionParams.setUri(prefixURI(positionParams.getUri()));
            LanguageServer server = getServer(positionParams.getTextDocument().getUri());
            return server != null ? new SignatureHelpDto(server.getTextDocumentService().signatureHelp(positionParams).get()) : null;
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> formatting(DocumentFormattingParams documentFormattingParams) {
        try {
            documentFormattingParams.getTextDocument().setUri(prefixURI(documentFormattingParams.getTextDocument().getUri()));
            LanguageServer server = getServer(documentFormattingParams.getTextDocument().getUri());
            return server == null ? Collections.emptyList()
                                  : server.getTextDocumentService()
                                          .formatting(documentFormattingParams)
                                          .get().stream()
                                          .map(TextEditDto::new)
                                          .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> rangeFormatting(DocumentRangeFormattingParams documentRangeFormattingParams) {
        try {
            documentRangeFormattingParams.getTextDocument().setUri(prefixURI(documentRangeFormattingParams.getTextDocument().getUri()));
            LanguageServer server = getServer(documentRangeFormattingParams.getTextDocument().getUri());
            return server == null ? Collections.emptyList()
                                  : server.getTextDocumentService()
                                          .rangeFormatting(documentRangeFormattingParams)
                                          .get().stream()
                                          .map(TextEditDto::new)
                                          .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> onTypeFormatting(DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
        try {
            documentOnTypeFormattingParams.getTextDocument().setUri(prefixURI(documentOnTypeFormattingParams.getTextDocument().getUri()));
            LanguageServer server = getServer(documentOnTypeFormattingParams.getTextDocument().getUri());
            return server == null ? Collections.emptyList() : server.getTextDocumentService()
                                                                    .onTypeFormatting(documentOnTypeFormattingParams)
                                                                    .get().stream()
                                                                    .map(TextEditDto::new)
                                                                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        try {
            didChangeTextDocumentParams.getTextDocument().setUri(prefixURI(didChangeTextDocumentParams.getTextDocument().getUri()));
            didChangeTextDocumentParams.setUri(prefixURI(didChangeTextDocumentParams.getUri()));
            LanguageServer server = getServer(didChangeTextDocumentParams.getTextDocument().getUri());
            if (server != null) {
                server.getTextDocumentService().didChange(didChangeTextDocumentParams);
            }
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didChange", e);
        }
    }

    private void didOpen(DidOpenTextDocumentParams openTextDocumentParams) {
        try {
            openTextDocumentParams.getTextDocument().setUri(prefixURI(openTextDocumentParams.getTextDocument().getUri()));
            LanguageServer server = getServer(openTextDocumentParams.getTextDocument().getUri());
            if (server != null) {
                server.getTextDocumentService().didOpen(openTextDocumentParams);
            }
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didOpen", e);
        }
    }

    private void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
        try {
            didCloseTextDocumentParams.getTextDocument().setUri(prefixURI(didCloseTextDocumentParams.getTextDocument().getUri()));
            LanguageServer server = getServer(didCloseTextDocumentParams.getTextDocument().getUri());
            if (server != null) {
                server.getTextDocumentService().didClose(didCloseTextDocumentParams);
            }
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didOpen", e);
        }
    }

    private void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        try {
            didSaveTextDocumentParams.getTextDocument().setUri(prefixURI(didSaveTextDocumentParams.getTextDocument().getUri()));
            LanguageServer server = getServer(didSaveTextDocumentParams.getTextDocument().getUri());
            if (server != null) {
                server.getTextDocumentService().didSave(didSaveTextDocumentParams);
            }
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didSave", e);
        }
    }

    private List<DocumentHighlightDto> documentHighlight(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            textDocumentPositionParams.getTextDocument().setUri(prefixURI(textDocumentPositionParams.getTextDocument().getUri()));
            LanguageServer server = getServer(textDocumentPositionParams.getTextDocument().getUri());
            if (server != null) {
                List<? extends DocumentHighlight> result = server.getTextDocumentService().documentHighlight(textDocumentPositionParams).get();
                return result.stream().map(DocumentHighlightDto::new).collect(Collectors.toList());
            }
            return null;
        } catch (LanguageServerException | InterruptedException | ExecutionException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private LanguageServer getServer(String uri) throws LanguageServerException {
        return languageServerRegistry.findServer(uri);
    }


    private <P> void dtoToNothing(String name, Class<P> pClass, Consumer<P> consumer) {
        requestHandler.newConfiguration()
                      .methodName("textDocument/" + name)
                      .paramsAsDto(pClass)
                      .noResult()
                      .withConsumer(consumer);
    }

    private <P, R> void dtoToDtoList(String name, Class<P> pClass, Class<R> rClass, Function<P, List<R>> function) {
        requestHandler.newConfiguration()
                      .methodName("textDocument/" + name)
                      .paramsAsDto(pClass)
                      .resultAsListOfDto(rClass)
                      .withFunction(function);
    }

    private <P, R> void dtoToDto(String name, Class<P> pClass, Class<R> rClass, Function<P, R> function) {
        requestHandler.newConfiguration()
                      .methodName("textDocument/" + name)
                      .paramsAsDto(pClass)
                      .resultAsDto(rClass)
                      .withFunction(function);
    }
}
