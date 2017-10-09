/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.service;

import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.isStartWithProject;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixProject;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.removeUriScheme;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedLocationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.RenameResultDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SignatureHelpDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.TextEditDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedLocation;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextDocumentEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceEdit;
import org.eclipse.che.api.languageserver.shared.model.FileContentParameters;
import org.eclipse.che.api.languageserver.shared.model.RenameResult;
import org.eclipse.che.api.languageserver.shared.util.Constants;
import org.eclipse.che.api.languageserver.util.LSOperation;
import org.eclipse.che.api.languageserver.util.OperationUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
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
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

/**
 * Json RPC API for the textDoc
 *
 * <p>Dispatches onto the {@link LanguageServerRegistryImpl}.
 */
@Singleton
public class TextDocumentService {
  private static final Logger LOG = LoggerFactory.getLogger(TextDocumentService.class);

  private final LanguageServerRegistry languageServerRegistry;
  private final RequestHandlerConfigurator requestHandler;

  @Inject
  public TextDocumentService(
      LanguageServerRegistry languageServerRegistry, RequestHandlerConfigurator requestHandler) {
    this.languageServerRegistry = languageServerRegistry;
    this.requestHandler = requestHandler;
  }

  @PostConstruct
  public void configureMethods() {
    dtoToDtoList(
        "definition",
        TextDocumentPositionParams.class,
        ExtendedLocationDto.class,
        this::definition);
    dtoToDtoList("codeAction", CodeActionParams.class, CommandDto.class, this::codeAction);
    dtoToDtoList(
        "documentSymbol",
        DocumentSymbolParams.class,
        SymbolInformationDto.class,
        this::documentSymbol);
    dtoToDtoList("formatting", DocumentFormattingParams.class, TextEditDto.class, this::formatting);
    dtoToDtoList(
        "rangeFormatting",
        DocumentRangeFormattingParams.class,
        TextEditDto.class,
        this::rangeFormatting);
    dtoToDtoList("references", ReferenceParams.class, ExtendedLocationDto.class, this::references);
    dtoToDtoList(
        "onTypeFormatting",
        DocumentOnTypeFormattingParams.class,
        TextEditDto.class,
        this::onTypeFormatting);

    dtoToDto(
        "completionItem/resolve",
        ExtendedCompletionItem.class,
        ExtendedCompletionItemDto.class,
        this::completionItemResolve);
    dtoToDto(
        "documentHighlight",
        TextDocumentPositionParams.class,
        DocumentHighlight.class,
        this::documentHighlight);
    dtoToDto(
        "completion",
        TextDocumentPositionParams.class,
        ExtendedCompletionListDto.class,
        this::completion);
    dtoToDto("hover", TextDocumentPositionParams.class, HoverDto.class, this::hover);
    dtoToDto(
        "signatureHelp",
        TextDocumentPositionParams.class,
        SignatureHelpDto.class,
        this::signatureHelp);

    dtoToDto("rename", RenameParams.class, RenameResultDto.class, this::rename);

    dtoToNothing("didChange", DidChangeTextDocumentParams.class, this::didChange);
    dtoToNothing("didClose", DidCloseTextDocumentParams.class, this::didClose);
    dtoToNothing("didOpen", DidOpenTextDocumentParams.class, this::didOpen);
    dtoToNothing("didSave", DidSaveTextDocumentParams.class, this::didSave);

    requestHandler
        .newConfiguration()
        .methodName("textDocument/fileContent")
        .paramsAsDto(FileContentParameters.class)
        .resultAsString()
        .withFunction(this::getFileContent);
  }

  private List<CommandDto> codeAction(CodeActionParams params) {
    TextDocumentIdentifier textDocument = params.getTextDocument();
    String uri = prefixURI(textDocument.getUri());
    textDocument.setUri(uri);
    List<CommandDto> result = new ArrayList<>();
    try {
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      LSOperation<InitializedLanguageServer, List<? extends Command>> op =
          new LSOperation<InitializedLanguageServer, List<? extends Command>>() {

            @Override
            public boolean canDo(InitializedLanguageServer server) {
              return truish(server.getInitializeResult().getCapabilities().getCodeActionProvider());
            }

            public CompletableFuture<List<? extends Command>> start(
                InitializedLanguageServer element) {
              return element.getServer().getTextDocumentService().codeAction(params);
            };

            @Override
            public boolean handleResult(
                InitializedLanguageServer element, List<? extends Command> res) {
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

  private ExtendedCompletionListDto completion(
      TextDocumentPositionParams textDocumentPositionParams) {
    try {
      TextDocumentIdentifier textDocument = textDocumentPositionParams.getTextDocument();
      String uri = prefixURI(textDocument.getUri());
      textDocument.setUri(uri);
      textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));
      ExtendedCompletionListDto[] result = new ExtendedCompletionListDto[1];

      LSOperation<Collection<InitializedLanguageServer>, ExtendedCompletionListDto> op =
          new LSOperation<Collection<InitializedLanguageServer>, ExtendedCompletionListDto>() {

            @Override
            public boolean canDo(Collection<InitializedLanguageServer> servers) {
              return true;
            }

            @Override
            public CompletableFuture<ExtendedCompletionListDto> start(
                Collection<InitializedLanguageServer> element) {
              return CompletableFuture.supplyAsync(
                  () -> {
                    ExtendedCompletionListDto res = new ExtendedCompletionListDto();
                    List<ExtendedCompletionItem> items = new ArrayList<>();
                    res.setItems(items);
                    LSOperation<
                            InitializedLanguageServer, Either<List<CompletionItem>, CompletionList>>
                        op2 =
                            new LSOperation<
                                InitializedLanguageServer,
                                Either<List<CompletionItem>, CompletionList>>() {

                              @Override
                              public boolean canDo(InitializedLanguageServer element) {
                                return element
                                        .getInitializeResult()
                                        .getCapabilities()
                                        .getCompletionProvider()
                                    != null;
                              }

                              @Override
                              public CompletableFuture<Either<List<CompletionItem>, CompletionList>>
                                  start(InitializedLanguageServer element) {
                                return element
                                    .getServer()
                                    .getTextDocumentService()
                                    .completion(textDocumentPositionParams);
                              }

                              @Override
                              public boolean handleResult(
                                  InitializedLanguageServer element,
                                  Either<List<CompletionItem>, CompletionList> result) {
                                List<CompletionItem> itemList;
                                if (result.isRight()) {
                                  res.setInComplete(
                                      res.isInComplete() && result.getRight().isIncomplete());
                                  itemList = result.getRight().getItems();
                                } else {
                                  itemList = result.getLeft();
                                }

                                for (CompletionItem item : itemList) {
                                  ExtendedCompletionItemDto exItem =
                                      new ExtendedCompletionItemDto();
                                  exItem.setItem(new CompletionItemDto(item));
                                  exItem.setLanguageServerId(element.getId());
                                  items.add(exItem);
                                }
                                return false;
                              }
                            };
                    OperationUtil.doInParallel(element, op2, 30000);

                    return res;
                  });
            }

            @Override
            public boolean handleResult(
                Collection<InitializedLanguageServer> element, ExtendedCompletionListDto list) {
              result[0] = list;
              return !list.getItems().isEmpty();
            }
          };
      OperationUtil.doInSequence(
          languageServerRegistry.getApplicableLanguageServers(uri), op, 10000);
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
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      OperationUtil.doInParallel(
          servers,
          new LSOperation<InitializedLanguageServer, List<? extends SymbolInformation>>() {

            @Override
            public boolean canDo(InitializedLanguageServer element) {
              return truish(
                  element.getInitializeResult().getCapabilities().getDocumentSymbolProvider());
            }

            @Override
            public CompletableFuture<List<? extends SymbolInformation>> start(
                InitializedLanguageServer element) {
              return element
                  .getServer()
                  .getTextDocumentService()
                  .documentSymbol(documentSymbolParams);
            }

            @Override
            public boolean handleResult(
                InitializedLanguageServer element, List<? extends SymbolInformation> locations) {
              locations.forEach(
                  o -> {
                    o.getLocation().setUri(removePrefixUri(o.getLocation().getUri()));
                    result.add(new SymbolInformationDto(o));
                  });
              return true;
            }
          },
          10000);
      return result;

    } catch (LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<ExtendedLocationDto> references(ReferenceParams referenceParams) {
    String uri = prefixURI(referenceParams.getTextDocument().getUri());
    referenceParams.getTextDocument().setUri(uri);
    List<ExtendedLocationDto> result = new ArrayList<>();
    try {
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      OperationUtil.doInParallel(
          servers,
          new LSOperation<InitializedLanguageServer, List<? extends Location>>() {

            @Override
            public boolean canDo(InitializedLanguageServer element) {
              return truish(
                  element.getInitializeResult().getCapabilities().getReferencesProvider());
            }

            @Override
            public CompletableFuture<List<? extends Location>> start(
                InitializedLanguageServer element) {
              return element.getServer().getTextDocumentService().references(referenceParams);
            }

            @Override
            public boolean handleResult(
                InitializedLanguageServer element, List<? extends Location> locations) {
              locations.forEach(
                  o -> {
                    ExtendedLocation extendedLocation = extendLocation(element, o);
                    result.add(new ExtendedLocationDto(extendedLocation));
                  });
              return true;
            }
          },
          30000);
      return result;
    } catch (LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<ExtendedLocationDto> definition(
      TextDocumentPositionParams textDocumentPositionParams) {
    String uri = prefixURI(textDocumentPositionParams.getTextDocument().getUri());
    textDocumentPositionParams.getTextDocument().setUri(uri);
    try {
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      List<ExtendedLocationDto> result = new ArrayList<>();
      OperationUtil.doInParallel(
          servers,
          new LSOperation<InitializedLanguageServer, List<? extends Location>>() {

            @Override
            public boolean canDo(InitializedLanguageServer element) {
              return truish(
                  element.getInitializeResult().getCapabilities().getDefinitionProvider());
            }

            @Override
            public CompletableFuture<List<? extends Location>> start(
                InitializedLanguageServer element) {
              return element
                  .getServer()
                  .getTextDocumentService()
                  .definition(textDocumentPositionParams);
            }

            @Override
            public boolean handleResult(
                InitializedLanguageServer element, List<? extends Location> locations) {
              locations.forEach(
                  o -> {
                    result.add(new ExtendedLocationDto(extendLocation(element, o)));
                  });
              return true;
            }
          },
          30000);
      return result;
    } catch (LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private ExtendedCompletionItemDto completionItemResolve(ExtendedCompletionItem unresolved) {
    try {
      InitializedLanguageServer server =
          languageServerRegistry.getServer(unresolved.getLanguageServerId());

      if (server != null) {
        ExtendedCompletionItem res = new ExtendedCompletionItem();
        res.setItem(
            server
                .getServer()
                .getTextDocumentService()
                .resolveCompletionItem(unresolved.getItem())
                .get());
        res.setLanguageServerId(unresolved.getLanguageServerId());
        return new ExtendedCompletionItemDto(res);
      }
      return new ExtendedCompletionItemDto(unresolved);
    } catch (InterruptedException | ExecutionException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private HoverDto hover(TextDocumentPositionParams positionParams) {
    String uri = prefixURI(positionParams.getTextDocument().getUri());
    positionParams.getTextDocument().setUri(uri);
    positionParams.setUri(prefixURI(positionParams.getUri()));
    HoverDto result = new HoverDto();
    result.setContents(new ArrayList<>());
    try {

      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      OperationUtil.doInParallel(
          servers,
          new LSOperation<InitializedLanguageServer, Hover>() {

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
              if (hover != null && hover.getContents() != null) {
                HoverDto hoverDto = new HoverDto(hover);
                result.getContents().addAll(hoverDto.getContents());
              }
              return true;
            }
          },
          10000);
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
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      LSOperation<InitializedLanguageServer, SignatureHelp> op =
          new LSOperation<InitializedLanguageServer, SignatureHelp>() {

            @Override
            public boolean canDo(InitializedLanguageServer element) {
              return element.getInitializeResult().getCapabilities().getSignatureHelpProvider()
                  != null;
            }

            @Override
            public CompletableFuture<SignatureHelp> start(InitializedLanguageServer element) {
              return element.getServer().getTextDocumentService().signatureHelp(positionParams);
            }

            @Override
            public boolean handleResult(InitializedLanguageServer element, SignatureHelp res) {
              if (res != null && !res.getSignatures().isEmpty()) {
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
      InitializedLanguageServer server =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .filter(
                  s ->
                      truish(
                          s.getInitializeResult()
                              .getCapabilities()
                              .getDocumentFormattingProvider()))
              .findFirst()
              .get();
      return server == null
          ? Collections.emptyList()
          : server
              .getServer()
              .getTextDocumentService()
              .formatting(documentFormattingParams)
              .get(5000, TimeUnit.MILLISECONDS)
              .stream()
              .map(TextEditDto::new)
              .collect(Collectors.toList());

    } catch (InterruptedException
        | ExecutionException
        | LanguageServerException
        | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<TextEditDto> rangeFormatting(
      DocumentRangeFormattingParams documentRangeFormattingParams) {
    try {
      String uri = prefixURI(documentRangeFormattingParams.getTextDocument().getUri());
      documentRangeFormattingParams.getTextDocument().setUri(uri);
      InitializedLanguageServer server =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .filter(
                  s ->
                      truish(
                          s.getInitializeResult()
                              .getCapabilities()
                              .getDocumentRangeFormattingProvider()))
              .findFirst()
              .get();
      return server == null
          ? Collections.emptyList()
          : server
              .getServer()
              .getTextDocumentService()
              .rangeFormatting(documentRangeFormattingParams)
              .get()
              .stream()
              .map(TextEditDto::new)
              .collect(Collectors.toList());
    } catch (InterruptedException | ExecutionException | LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<TextEditDto> onTypeFormatting(
      DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
    try {
      String uri = prefixURI(documentOnTypeFormattingParams.getTextDocument().getUri());
      documentOnTypeFormattingParams.getTextDocument().setUri(uri);
      InitializedLanguageServer server =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .filter(
                  s ->
                      s.getInitializeResult()
                              .getCapabilities()
                              .getDocumentOnTypeFormattingProvider()
                          != null)
              .findFirst()
              .get();
      return server == null
          ? Collections.emptyList()
          : server
              .getServer()
              .getTextDocumentService()
              .onTypeFormatting(documentOnTypeFormattingParams)
              .get()
              .stream()
              .map(TextEditDto::new)
              .collect(Collectors.toList());
    } catch (InterruptedException | ExecutionException | LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    try {
      String uri = prefixURI(didChangeTextDocumentParams.getTextDocument().getUri());
      didChangeTextDocumentParams.getTextDocument().setUri(uri);
      didChangeTextDocumentParams.setUri(prefixURI(didChangeTextDocumentParams.getUri()));
      languageServerRegistry
          .getApplicableLanguageServers(uri)
          .stream()
          .flatMap(Collection::stream)
          .map(InitializedLanguageServer::getServer)
          .forEach(
              server -> {
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
      languageServerRegistry
          .getApplicableLanguageServers(uri)
          .stream()
          .flatMap(Collection::stream)
          .map(InitializedLanguageServer::getServer)
          .forEach(
              server -> {
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
      languageServerRegistry
          .getApplicableLanguageServers(uri)
          .stream()
          .flatMap(Collection::stream)
          .map(InitializedLanguageServer::getServer)
          .forEach(
              server -> {
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
      languageServerRegistry
          .getApplicableLanguageServers(uri)
          .stream()
          .flatMap(Collection::stream)
          .map(InitializedLanguageServer::getServer)
          .forEach(
              server -> {
                server.getTextDocumentService().didSave(didSaveTextDocumentParams);
              });
    } catch (LanguageServerException e) {
      LOG.error("Error trying to process textDocument/didSave", e);
    }
  }

  private DocumentHighlightDto documentHighlight(
      TextDocumentPositionParams textDocumentPositionParams) {
    try {
      String uri = prefixURI(textDocumentPositionParams.getTextDocument().getUri());
      textDocumentPositionParams.getTextDocument().setUri(uri);
      @SuppressWarnings("unchecked")
      List<DocumentHighlightDto>[] result = new List[1];
      LSOperation<Collection<InitializedLanguageServer>, List<DocumentHighlightDto>> op =
          new LSOperation<Collection<InitializedLanguageServer>, List<DocumentHighlightDto>>() {

            @Override
            public boolean canDo(Collection<InitializedLanguageServer> servers) {
              return true;
            }

            @Override
            public CompletableFuture<List<DocumentHighlightDto>> start(
                Collection<InitializedLanguageServer> element) {
              return CompletableFuture.supplyAsync(
                  () -> {
                    List<DocumentHighlightDto> res = new ArrayList<>();
                    LSOperation<InitializedLanguageServer, List<? extends DocumentHighlight>> op2 =
                        new LSOperation<
                            InitializedLanguageServer, List<? extends DocumentHighlight>>() {

                          @Override
                          public boolean canDo(InitializedLanguageServer element) {
                            return truish(
                                element
                                    .getInitializeResult()
                                    .getCapabilities()
                                    .getDocumentHighlightProvider());
                          }

                          @Override
                          public CompletableFuture<List<? extends DocumentHighlight>> start(
                              InitializedLanguageServer element) {
                            return element
                                .getServer()
                                .getTextDocumentService()
                                .documentHighlight(textDocumentPositionParams);
                          }

                          @Override
                          public boolean handleResult(
                              InitializedLanguageServer element,
                              List<? extends DocumentHighlight> result) {

                            return false;
                          }
                        };
                    OperationUtil.doInParallel(element, op2, 10000);

                    return res;
                  });
            }

            @Override
            public boolean handleResult(
                Collection<InitializedLanguageServer> element, List<DocumentHighlightDto> list) {
              result[0] = list;
              return !list.isEmpty();
            }
          };
      OperationUtil.doInSequence(
          languageServerRegistry.getApplicableLanguageServers(uri), op, 10000);

      if (!result[0].isEmpty()) {
        return result[0].get(0);
      }
      return null;
    } catch (LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private RenameResultDto rename(RenameParams renameParams) {
    String uri = prefixURI(renameParams.getTextDocument().getUri());
    renameParams.getTextDocument().setUri(uri);
    Map<String, ExtendedWorkspaceEdit> edits = new ConcurrentHashMap<>();
    try {
      List<InitializedLanguageServer> servers =
          languageServerRegistry
              .getApplicableLanguageServers(uri)
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      LSOperation<InitializedLanguageServer, WorkspaceEdit> op =
          new LSOperation<InitializedLanguageServer, WorkspaceEdit>() {
            @Override
            public boolean canDo(InitializedLanguageServer server) {
              Boolean renameProvider =
                  server.getInitializeResult().getCapabilities().getRenameProvider();
              return renameProvider != null && renameProvider;
            }

            @Override
            public CompletableFuture<WorkspaceEdit> start(InitializedLanguageServer element) {
              return element.getServer().getTextDocumentService().rename(renameParams);
            }

            @Override
            public boolean handleResult(InitializedLanguageServer element, WorkspaceEdit result) {

              addRenameResult(edits, element.getLauncher().getDescription().getId(), result);
              return true;
            }
          };
      OperationUtil.doInParallel(servers, op, TimeUnit.SECONDS.toMillis(30));
    } catch (LanguageServerException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
    return new RenameResultDto(new RenameResult(edits));
  }

  private void addRenameResult(
      Map<String, ExtendedWorkspaceEdit> map, String id, WorkspaceEdit workspaceEdit) {

    ExtendedWorkspaceEdit result = new ExtendedWorkspaceEdit();
    List<ExtendedTextDocumentEdit> edits = new ArrayList<>();
    if (workspaceEdit.getDocumentChanges() != null) {
      for (TextDocumentEdit documentEdit : workspaceEdit.getDocumentChanges()) {
        ExtendedTextDocumentEdit edit = new ExtendedTextDocumentEdit();
        edit.setTextDocument(documentEdit.getTextDocument());
        edit.getTextDocument().setUri(removePrefixUri(edit.getTextDocument().getUri()));
        edit.setEdits(
            convertToExtendedEdit(
                documentEdit.getEdits(), removeUriScheme(documentEdit.getTextDocument().getUri())));
        edits.add(edit);
      }
    } else if (workspaceEdit.getChanges() != null) {
      for (Entry<String, List<TextEdit>> entry : workspaceEdit.getChanges().entrySet()) {
        ExtendedTextDocumentEdit edit = new ExtendedTextDocumentEdit();
        VersionedTextDocumentIdentifier documentIdentifier = new VersionedTextDocumentIdentifier();
        documentIdentifier.setVersion(-1);
        documentIdentifier.setUri(removePrefixUri(entry.getKey()));
        edit.setTextDocument(documentIdentifier);
        edit.setEdits(convertToExtendedEdit(entry.getValue(), removeUriScheme(entry.getKey())));
        edits.add(edit);
      }
    }

    if (!edits.isEmpty()) {
      result.setDocumentChanges(edits);
      map.put(id, result);
    }
  }

  private List<ExtendedTextEdit> convertToExtendedEdit(List<TextEdit> edits, String filePath) {
    try {
      // for some reason C# LS sends ws related path,
      if (!isStartWithProject(filePath)) {
        filePath = prefixProject(filePath);
      }
      String fileContent =
          com.google.common.io.Files.toString(new File(filePath), Charset.defaultCharset());
      Document document = new Document(fileContent);
      return edits
          .stream()
          .map(
              textEdit -> {
                ExtendedTextEdit result = new ExtendedTextEdit();
                result.setRange(textEdit.getRange());
                result.setNewText(textEdit.getNewText());
                try {
                  IRegion lineInformation =
                      document.getLineInformation(textEdit.getRange().getStart().getLine());
                  String lineText =
                      document.get(lineInformation.getOffset(), lineInformation.getLength());
                  result.setLineText(lineText);
                  result.setInLineStart(textEdit.getRange().getStart().getCharacter());
                  result.setInLineEnd(textEdit.getRange().getEnd().getCharacter());
                } catch (BadLocationException e) {
                  LOG.error("Can't read file line", e);
                }

                return result;
              })
          .collect(Collectors.toList());
    } catch (IOException e) {
      LOG.error("Can't read file", e);
    }
    return Collections.emptyList();
  }

  private String getFileContent(FileContentParameters params) {
    InitializedLanguageServer server =
        languageServerRegistry.getServer(params.getLanguagesServerId());
    if (server == null) {
      throw new JsonRpcException(-27000, "did not find language server");
    }
    LanguageServer originatingService = server.getServer();
    if (!(originatingService instanceof FileContentAccess)) {
      throw new JsonRpcException(-27000, "language server does not implement file access");
    }
    try {
      return ((FileContentAccess) originatingService)
          .getFileContent(params.getUri())
          .get(10, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private <P> void dtoToNothing(String name, Class<P> pClass, Consumer<P> consumer) {
    requestHandler
        .newConfiguration()
        .methodName("textDocument/" + name)
        .paramsAsDto(pClass)
        .noResult()
        .withConsumer(consumer);
  }

  private <P, R> void dtoToDtoList(
      String name, Class<P> pClass, Class<R> rClass, Function<P, List<R>> function) {
    requestHandler
        .newConfiguration()
        .methodName("textDocument/" + name)
        .paramsAsDto(pClass)
        .resultAsListOfDto(rClass)
        .withFunction(function);
  }

  private <P, R> void dtoToDto(
      String name, Class<P> pClass, Class<R> rClass, Function<P, R> function) {
    requestHandler
        .newConfiguration()
        .methodName("textDocument/" + name)
        .paramsAsDto(pClass)
        .resultAsDto(rClass)
        .withFunction(function);
  }

  private boolean truish(Boolean b) {
    return b != null && b;
  }

  private ExtendedLocation extendLocation(InitializedLanguageServer element, Location o) {
    if (LanguageServiceUtils.isProjectUri(o.getUri())) {
      o.setUri(Constants.CHE_WKSP_SCHEME + removePrefixUri(o.getUri()));
    }
    return new ExtendedLocation(element.getId(), o);
  }
}
