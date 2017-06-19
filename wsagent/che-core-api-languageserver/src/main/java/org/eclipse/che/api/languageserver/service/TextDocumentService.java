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
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.InitializedLanguageServer;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CommandDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.DocumentHighlightDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LocationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SignatureHelpDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.TextEditDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.util.LSOperation;
import org.eclipse.che.api.languageserver.util.OperationUtil;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
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
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.removePrefixUri;

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
        dtoToDtoList("codeAction", CodeActionParams.class, CommandDto.class, this::codeAction);
        dtoToDtoList("documentSymbol", DocumentSymbolParams.class, SymbolInformationDto.class, this::documentSymbol);
        dtoToDtoList("formatting", DocumentFormattingParams.class, TextEditDto.class, this::formatting);
        dtoToDtoList("rangeFormatting", DocumentRangeFormattingParams.class, TextEditDto.class, this::rangeFormatting);
        dtoToDtoList("references", ReferenceParams.class, LocationDto.class, this::references);
        dtoToDtoList("onTypeFormatting", DocumentOnTypeFormattingParams.class, TextEditDto.class, this::onTypeFormatting);

        dtoToDto("completionItem/resolve", ExtendedCompletionItemDto.class, CompletionItemDto.class, this::completionItemResolve);
        dtoToDto("documentHighlight", TextDocumentPositionParams.class, DocumentHighlightDto.class, this::documentHighlight);
        dtoToDto("completion", TextDocumentPositionParams.class, ExtendedCompletionListDto.class, this::completion);
        dtoToDto("hover", TextDocumentPositionParams.class, HoverDto.class, this::hover);
        dtoToDto("signatureHelp", TextDocumentPositionParams.class, SignatureHelpDto.class, this::signatureHelp);

        dtoToNothing("didChange", DidChangeTextDocumentParams.class, this::didChange);
        dtoToNothing("didClose", DidCloseTextDocumentParams.class, this::didClose);
        dtoToNothing("didOpen", DidOpenTextDocumentParams.class, this::didOpen);
        dtoToNothing("didSave", DidSaveTextDocumentParams.class, this::didSave);
    }

    private List<CommandDto> codeAction(CodeActionParams params) {
        TextDocumentIdentifier textDocument = params.getTextDocument();
        String uri = prefixURI(textDocument.getUri());
        textDocument.setUri(uri);
        List<CommandDto> result = new ArrayList<>();
        try {
            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            LSOperation<InitializedLanguageServer, List<? extends Command>> op = new LSOperation<InitializedLanguageServer, List<? extends Command>>() {

                @Override
                public boolean canDo(InitializedLanguageServer server) {
                    return truish(server.getInitializeResult().getCapabilities().getCodeActionProvider());
                }

                public CompletableFuture<List<? extends Command>> start(InitializedLanguageServer element) {
                    return element.getServer().getTextDocumentService().codeAction(params);
                };

                @Override
                public boolean handleResult(InitializedLanguageServer element, List<? extends Command> res) {
                    for (Command cmd : res) {
                        result.add(new CommandDto(cmd));
                    }
                    return false;
                };
            };
            OperationUtil.doInParallel(servers, op, 10000);
            return result;
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private ExtendedCompletionListDto completion(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            TextDocumentIdentifier textDocument = textDocumentPositionParams.getTextDocument();
            String uri = prefixURI(textDocument.getUri());
            textDocument.setUri(uri);
            textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
            ExtendedCompletionListDto[] result = new ExtendedCompletionListDto[1];

            LSOperation<Collection<InitializedLanguageServer>, ExtendedCompletionListDto> op = new LSOperation<Collection<InitializedLanguageServer>, ExtendedCompletionListDto>() {

                @Override
                public boolean canDo(Collection<InitializedLanguageServer> servers) {
                    return true;
                }

                @Override
                public CompletableFuture<ExtendedCompletionListDto> start(Collection<InitializedLanguageServer> element) {
                    return CompletableFuture.supplyAsync(() -> {
                        ExtendedCompletionListDto res = new ExtendedCompletionListDto();
                        List<ExtendedCompletionItem> items = new ArrayList<ExtendedCompletionItem>();
                        res.setItems(items);
                        LSOperation<InitializedLanguageServer, CompletionList> op2 = new LSOperation<InitializedLanguageServer, CompletionList>() {

                            @Override
                            public boolean canDo(InitializedLanguageServer element) {
                                return element.getInitializeResult().getCapabilities().getCompletionProvider() != null;
                            }

                            @Override
                            public CompletableFuture<CompletionList> start(InitializedLanguageServer element) {
                                return element.getServer().getTextDocumentService().completion(textDocumentPositionParams);
                            }

                            @Override
                            public boolean handleResult(InitializedLanguageServer element, CompletionList result) {
                                res.setInComplete(res.isInComplete() && result.isIncomplete());
                                for (CompletionItem item : result.getItems()) {
                                    ExtendedCompletionItemDto exItem = new ExtendedCompletionItemDto();
                                    exItem.setItem(new CompletionItemDto(item));
                                    exItem.setTextDocumentIdentifier(textDocument);
                                    items.add(exItem);
                                }
                                return false;
                            }
                        };
                        OperationUtil.doInParallel(element, op2, 10000);

                        return res;
                    });
                }

                @Override
                public boolean handleResult(Collection<InitializedLanguageServer> element, ExtendedCompletionListDto list) {
                    result[0] = list;
                    return !list.getItems().isEmpty();
                }
            };
            OperationUtil.doInSequence(languageServerRegistry.getApplicableLanguageServers(uri), op, 10000);
            return result[0];
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<SymbolInformationDto> documentSymbol(DocumentSymbolParams documentSymbolParams) {
        String uri = prefixURI(documentSymbolParams.getTextDocument().getUri());
        documentSymbolParams.getTextDocument().setUri(uri);
        List<SymbolInformationDto> result = new ArrayList<>();
        try {
            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            OperationUtil.doInParallel(servers,
                                                    new LSOperation<InitializedLanguageServer, List<? extends SymbolInformation>>() {

                                                        @Override
                                                        public boolean canDo(InitializedLanguageServer element) {
                                                            return truish(element.getInitializeResult().getCapabilities()
                                                                            .getDocumentSymbolProvider());
                                                        }

                                                        @Override
                                                        public CompletableFuture<List<? extends SymbolInformation>> start(InitializedLanguageServer element) {
                                                            return element.getServer().getTextDocumentService()
                                                                            .documentSymbol(documentSymbolParams);
                                                        }

                                                        @Override
                                                        public boolean handleResult(InitializedLanguageServer element,
                                                                                    List<? extends SymbolInformation> locations) {
                                                            locations.forEach(o -> {
                                                                o.getLocation().setUri(removePrefixUri(o.getLocation().getUri()));
                                                                result.add(new SymbolInformationDto(o));
                                                            });
                                                            return true;
                                                        }
                                                    }, 10000);
            return result;

        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());

        }
    }

    private List<LocationDto> references(ReferenceParams referenceParams) {
        String uri = prefixURI(referenceParams.getTextDocument().getUri());
        referenceParams.getTextDocument().setUri(uri);
        List<LocationDto> result = new ArrayList<>();
        try {
            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            OperationUtil.doInParallel(servers, new LSOperation<InitializedLanguageServer, List<? extends Location>>() {

                @Override
                public boolean canDo(InitializedLanguageServer element) {
                    return truish(element.getInitializeResult().getCapabilities().getReferencesProvider());
                }

                @Override
                public CompletableFuture<List<? extends Location>> start(InitializedLanguageServer element) {
                    return element.getServer().getTextDocumentService().references(referenceParams);
                }

                @Override
                public boolean handleResult(InitializedLanguageServer element, List<? extends Location> locations) {
                    locations.forEach(o -> {
                        o.setUri(removePrefixUri(o.getUri()));
                        result.add(new LocationDto(o));
                    });
                    return true;
                }
            }, 10000);
            return result;
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<LocationDto> definition(TextDocumentPositionParams textDocumentPositionParams) {
        String uri = prefixURI(textDocumentPositionParams.getTextDocument().getUri());
        textDocumentPositionParams.getTextDocument().setUri(uri);
        try {
            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            List<LocationDto> result = new ArrayList<>();
            OperationUtil.doInParallel(servers, new LSOperation<InitializedLanguageServer, List<? extends Location>>() {

                @Override
                public boolean canDo(InitializedLanguageServer element) {
                    return truish(element.getInitializeResult().getCapabilities().getDefinitionProvider());
                }

                @Override
                public CompletableFuture<List<? extends Location>> start(InitializedLanguageServer element) {
                    return element.getServer().getTextDocumentService().definition(textDocumentPositionParams);
                }

                @Override
                public boolean handleResult(InitializedLanguageServer element, List<? extends Location> locations) {
                    locations.forEach(o -> {
                        o.setUri(removePrefixUri(o.getUri()));
                        result.add(new LocationDto(o));
                    });
                    return true;
                }
            }, 10000);
            return result;
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private CompletionItemDto completionItemResolve(ExtendedCompletionItemDto unresolved) {
        // try {
        // LanguageServer server =
        // getServer(prefixURI(unresolved.getTextDocumentIdentifier().getUri()));
        //
        // return server != null ? new
        // CompletionItemDto(server.getTextDocumentService().resolveCompletionItem(unresolved).get())
        // : new CompletionItemDto(unresolved);
        // } catch (InterruptedException | ExecutionException |
        // LanguageServerException e) {
        // throw new JsonRpcException(-27000, e.getMessage());
        // }
        // TODO: implement
        return null;
    }

    private HoverDto hover(TextDocumentPositionParams positionParams) {
        String uri = prefixURI(positionParams.getTextDocument().getUri());
        positionParams.getTextDocument().setUri(uri);
        positionParams.setUri(prefixURI(positionParams.getUri()));
        HoverDto result = new HoverDto();
        result.setContents(new ArrayList<>());
        try {

            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            OperationUtil.doInParallel(servers, new LSOperation<InitializedLanguageServer, Hover>() {

                @Override
                public boolean canDo(InitializedLanguageServer element) {
                    return truish(element.getInitializeResult().getCapabilities().getHoverProvider());
                }

                @Override
                public CompletableFuture<Hover> start(InitializedLanguageServer element) {
                    return element.getServer().getTextDocumentService().hover(positionParams);
                }

                @Override
                public boolean handleResult(InitializedLanguageServer element, Hover hover) {
                    if (hover != null) {
                        result.getContents().addAll(hover.getContents());
                    }
                    return true;
                }
            }, 10000);
            return result;
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private SignatureHelpDto signatureHelp(TextDocumentPositionParams positionParams) {
        String uri = prefixURI(positionParams.getTextDocument().getUri());
        positionParams.getTextDocument().setUri(uri);
        positionParams.setUri(prefixURI(positionParams.getUri()));
        SignatureHelpDto[] result = new SignatureHelpDto[1];
        try {
            List<InitializedLanguageServer> servers = languageServerRegistry.getApplicableLanguageServers(uri).stream()
                            .flatMap(Collection::stream).collect(Collectors.toList());
            LSOperation<InitializedLanguageServer, SignatureHelp> op = new LSOperation<InitializedLanguageServer, SignatureHelp>() {

                @Override
                public boolean canDo(InitializedLanguageServer element) {
                    return element.getInitializeResult().getCapabilities().getSignatureHelpProvider() != null;
                }

                @Override
                public CompletableFuture<SignatureHelp> start(InitializedLanguageServer element) {
                    return element.getServer().getTextDocumentService().signatureHelp(positionParams);
                }

                @Override
                public boolean handleResult(InitializedLanguageServer element, SignatureHelp res) {
                    if (res != null) {
                        result[0] = new SignatureHelpDto(res);
                        return true;
                    }
                    return false;
                }
            };
            OperationUtil.doInSequence(servers, op, 10000);
            return result[0];
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> formatting(DocumentFormattingParams documentFormattingParams) {
        try {
            String uri = prefixURI(documentFormattingParams.getTextDocument().getUri());
            documentFormattingParams.getTextDocument().setUri(uri);
            InitializedLanguageServer server = languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .filter(s -> truish(s.getInitializeResult().getCapabilities().getDocumentFormattingProvider())).findFirst()
                            .get();
            return server == null ? Collections.emptyList()
                            : server.getServer().getTextDocumentService().formatting(documentFormattingParams)
                                            .get(5000, TimeUnit.MILLISECONDS).stream().map(TextEditDto::new).collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException | LanguageServerException | TimeoutException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> rangeFormatting(DocumentRangeFormattingParams documentRangeFormattingParams) {
        try {
            String uri = prefixURI(documentRangeFormattingParams.getTextDocument().getUri());
            documentRangeFormattingParams.getTextDocument().setUri(uri);
            InitializedLanguageServer server = languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .filter(s -> truish(s.getInitializeResult().getCapabilities().getDocumentRangeFormattingProvider())).findFirst()
                            .get();
            return server == null ? Collections.emptyList()
                            : server.getServer().getTextDocumentService().rangeFormatting(documentRangeFormattingParams).get().stream()
                                            .map(TextEditDto::new).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private List<TextEditDto> onTypeFormatting(DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
        try {
            String uri = prefixURI(documentOnTypeFormattingParams.getTextDocument().getUri());
            documentOnTypeFormattingParams.getTextDocument().setUri(uri);
            InitializedLanguageServer server = languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .filter(s -> s.getInitializeResult().getCapabilities().getDocumentOnTypeFormattingProvider() != null)
                            .findFirst().get();
            return server == null ? Collections.emptyList()
                            : server.getServer().getTextDocumentService().onTypeFormatting(documentOnTypeFormattingParams).get().stream()
                                            .map(TextEditDto::new).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException | LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());
        }
    }

    private void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        try {
            String uri = prefixURI(didChangeTextDocumentParams.getTextDocument().getUri());
            didChangeTextDocumentParams.getTextDocument().setUri(uri);
            didChangeTextDocumentParams.setUri(prefixURI(didChangeTextDocumentParams.getUri()));
            languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .map(InitializedLanguageServer::getServer).forEach(server -> {
                                server.getTextDocumentService().didChange(didChangeTextDocumentParams);
                            });
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didChange", e);
        }
    }

    private void didOpen(DidOpenTextDocumentParams openTextDocumentParams) {
        try {
            String uri = prefixURI(openTextDocumentParams.getTextDocument().getUri());
            openTextDocumentParams.getTextDocument().setUri(uri);
            languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .map(InitializedLanguageServer::getServer).forEach(server -> {
                                server.getTextDocumentService().didOpen(openTextDocumentParams);
                            });
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didOpen", e);
        }
    }

    private void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
        try {
            String uri = prefixURI(didCloseTextDocumentParams.getTextDocument().getUri());
            didCloseTextDocumentParams.getTextDocument().setUri(uri);
            languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .map(InitializedLanguageServer::getServer).forEach(server -> {
                                server.getTextDocumentService().didClose(didCloseTextDocumentParams);
                            });
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didOpen", e);
        }
    }

    private void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        try {
            String uri = prefixURI(didSaveTextDocumentParams.getTextDocument().getUri());
            didSaveTextDocumentParams.getTextDocument().setUri(uri);
            languageServerRegistry.getApplicableLanguageServers(uri).stream().flatMap(Collection::stream)
                            .map(InitializedLanguageServer::getServer).forEach(server -> {
                                server.getTextDocumentService().didSave(didSaveTextDocumentParams);
                            });
        } catch (LanguageServerException e) {
            LOG.error("Error trying to process textDocument/didSave", e);
        }
    }

    private DocumentHighlightDto documentHighlight(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            String uri = prefixURI(textDocumentPositionParams.getTextDocument().getUri());
            textDocumentPositionParams.getTextDocument().setUri(uri);
            @SuppressWarnings("unchecked")
            List<DocumentHighlightDto>[] result = new List[1];
            LSOperation<Collection<InitializedLanguageServer>, List<DocumentHighlightDto>> op = new LSOperation<Collection<InitializedLanguageServer>, List<DocumentHighlightDto>>() {

                @Override
                public boolean canDo(Collection<InitializedLanguageServer> servers) {
                    return true;
                }

                @Override
                public CompletableFuture<List<DocumentHighlightDto>> start(Collection<InitializedLanguageServer> element) {
                    return CompletableFuture.supplyAsync(() -> {
                        List<DocumentHighlightDto> res = new ArrayList<>();
                        LSOperation<InitializedLanguageServer, List<? extends DocumentHighlight>> op2 = new LSOperation<InitializedLanguageServer, List<? extends DocumentHighlight>>() {

                            @Override
                            public boolean canDo(InitializedLanguageServer element) {
                                return truish(element.getInitializeResult().getCapabilities().getDocumentHighlightProvider());
                            }

                            @Override
                            public CompletableFuture<List<? extends DocumentHighlight>> start(InitializedLanguageServer element) {
                                return element.getServer().getTextDocumentService().documentHighlight(textDocumentPositionParams);
                            }

                            @Override
                            public boolean handleResult(InitializedLanguageServer element, List<? extends DocumentHighlight> result) {

                                return false;
                            }
                        };
                        OperationUtil.doInParallel(element, op2, 10000);

                        return res;
                    });
                }

                @Override
                public boolean handleResult(Collection<InitializedLanguageServer> element, List<DocumentHighlightDto> list) {
                    result[0] = list;
                    return list.isEmpty();
                }
            };
            OperationUtil.doInSequence(languageServerRegistry.getApplicableLanguageServers(uri), op, 10000);

            if (!result[0].isEmpty()) {
                return result[0].get(0);
            }
            return null;
        } catch (LanguageServerException e) {
            throw new JsonRpcException(-27000, e.getMessage());

        }
    }

    private <P> void dtoToNothing(String name, Class<P> pClass, Consumer<P> consumer) {
        requestHandler.newConfiguration().methodName("textDocument/" + name).paramsAsDto(pClass).noResult().withConsumer(consumer);
    }

    private <P, R> void dtoToDtoList(String name, Class<P> pClass, Class<R> rClass, Function<P, List<R>> function) {
        requestHandler.newConfiguration().methodName("textDocument/" + name).paramsAsDto(pClass).resultAsListOfDto(rClass)
                        .withFunction(function);
    }

    private <P, R> void dtoToDto(String name, Class<P> pClass, Class<R> rClass, Function<P, R> function) {
        requestHandler.newConfiguration().methodName("textDocument/" + name).paramsAsDto(pClass).resultAsDto(rClass).withFunction(function);
    }

    private boolean truish(Boolean b) {
        return b != null && b;
    }

}
