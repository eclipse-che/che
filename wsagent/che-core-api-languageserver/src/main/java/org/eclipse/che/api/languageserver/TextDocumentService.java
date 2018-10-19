/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.emptyList;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.isStartWithProject;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.prefixProject;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removeUriScheme;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CommandDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.CompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.DocumentHighlightDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionItemDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedCompletionListDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.HoverDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LocationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.RenameResultDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SignatureHelpDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.TextEditDto;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextDocumentEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceEdit;
import org.eclipse.che.api.languageserver.shared.model.RenameResult;
import org.eclipse.che.api.languageserver.shared.model.SnippetParameters;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.api.languageserver.shared.util.CharStreamEditor;
import org.eclipse.che.api.languageserver.shared.util.CharStreamIterator;
import org.eclipse.che.api.languageserver.shared.util.LinearRangeComparator;
import org.eclipse.che.api.languageserver.util.LSOperation;
import org.eclipse.che.api.languageserver.util.LineReader;
import org.eclipse.che.api.languageserver.util.OperationUtil;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json RPC API for the textDoc
 *
 * <p>Dispatches onto the {@link LanguageServerInitializer}.
 *
 * @author Thomas Mäder
 */
@Singleton
public class TextDocumentService {
  private static final Logger LOG = LoggerFactory.getLogger(TextDocumentService.class);

  private final FindServer findServer;
  private final RequestHandlerConfigurator requestHandler;
  private FsManager fsManager;

  @Inject
  public TextDocumentService(
      FindServer findServer, RequestHandlerConfigurator requestHandler, FsManager fsManager) {
    this.findServer = findServer;
    this.requestHandler = requestHandler;
    this.fsManager = fsManager;
  }

  @PostConstruct
  public void configureMethods() {
    dtoToDtoList(
        "definition", TextDocumentPositionParams.class, LocationDto.class, this::definition);
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
    dtoToDtoList("references", ReferenceParams.class, LocationDto.class, this::references);
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
        "completion", CompletionParams.class, ExtendedCompletionListDto.class, this::completion);
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
        .paramsAsString()
        .resultAsString()
        .withFunction(this::getFileContent);

    requestHandler
        .newConfiguration()
        .methodName("textDocument/snippets")
        .paramsAsDto(SnippetParameters.class)
        .resultAsListOfDto(SnippetResult.class)
        .withFunction(this::getSnippets);
  }

  private List<CommandDto> codeAction(CodeActionParams params) {
    TextDocumentIdentifier textDocument = params.getTextDocument();
    String wsPath = textDocument.getUri();
    String uri = prefixURI(wsPath);
    textDocument.setUri(uri);
    List<CommandDto> result = new ArrayList<>();
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);
    LSOperation<ExtendedLanguageServer, List<Either<Command, CodeAction>>> op =
        new LSOperation<ExtendedLanguageServer, List<Either<Command, CodeAction>>>() {

          @Override
          public boolean canDo(ExtendedLanguageServer server) {
            return truish(server.getCapabilities().getCodeActionProvider());
          }

          @Override
          public CompletableFuture<List<Either<Command, CodeAction>>> start(
              ExtendedLanguageServer element) {
            return element.getTextDocumentService().codeAction(params);
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element, List<Either<Command, CodeAction>> res) {
            for (Either<Command, CodeAction> cmd : res) {
              if (cmd.isLeft()) {
                result.add(new CommandDto(cmd.getLeft()));
              } else {
                // see https://github.com/eclipse/che/issues/11140
                LOG.warn("Ignoring code action: {}", cmd.getRight());
              }
            }
            return false;
          }
        };
    OperationUtil.doInParallel(servers, op, 10000);
    return result;
  }

  private ExtendedCompletionListDto completion(CompletionParams textDocumentPositionParams) {
    TextDocumentIdentifier textDocument = textDocumentPositionParams.getTextDocument();
    String wsPath = textDocument.getUri();
    String uri = prefixURI(wsPath);
    textDocument.setUri(uri);
    textDocumentPositionParams.setUri(prefixURI(textDocumentPositionParams.getUri()));

    ExtendedCompletionListDto[] result = new ExtendedCompletionListDto[1];
    result[0] = new ExtendedCompletionListDto();
    result[0].setInComplete(true);
    result[0].setItems(newLinkedList());

    LSOperation<ExtendedLanguageServer, Either<List<CompletionItem>, CompletionList>> lsOperation =
        new LSOperation<ExtendedLanguageServer, Either<List<CompletionItem>, CompletionList>>() {
          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return element.getCapabilities().getCompletionProvider() != null;
          }

          @Override
          public CompletableFuture<Either<List<CompletionItem>, CompletionList>> start(
              ExtendedLanguageServer element) {
            return element.getTextDocumentService().completion(textDocumentPositionParams);
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element, Either<List<CompletionItem>, CompletionList> r) {
            List<ExtendedCompletionItem> items = newLinkedList();

            List<CompletionItem> itemList;
            if (r.isRight()) {
              result[0].setInComplete(result[0].isInComplete() && r.getRight().isIncomplete());
              itemList = r.getRight().getItems();
            } else {
              itemList = r.getLeft();
            }

            for (CompletionItem item : itemList) {
              ExtendedCompletionItem exItem = new ExtendedCompletionItemDto();
              exItem.setItem(new CompletionItemDto(item));
              exItem.setLanguageServerId(element.getId());
              items.add(exItem);
            }

            result[0].getItems().addAll(items);
            return false;
          }
        };

    Set<ExtendedLanguageServer> languageServers = findServer.byPath(wsPath);
    OperationUtil.doInSequence(languageServers, lsOperation, 10000);

    return result[0];
  }

  private List<SymbolInformationDto> documentSymbol(DocumentSymbolParams documentSymbolParams) {
    String wsPath = documentSymbolParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    documentSymbolParams.getTextDocument().setUri(uri);
    List<SymbolInformationDto> result = new ArrayList<>();
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);

    OperationUtil.doInParallel(
        servers,
        new LSOperation<ExtendedLanguageServer, List<Either<SymbolInformation, DocumentSymbol>>>() {

          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return truish(element.getCapabilities().getDocumentSymbolProvider());
          }

          @Override
          public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> start(
              ExtendedLanguageServer element) {
            return element.getTextDocumentService().documentSymbol(documentSymbolParams);
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element,
              List<Either<SymbolInformation, DocumentSymbol>> locations) {
            locations.forEach(
                o -> {
                  // minimal fix for https://github.com/eclipse/che/issues/11139 when updating
                  // lsp4j
                  if (o.isLeft()) {
                    SymbolInformation si = o.getLeft();
                    si.getLocation().setUri(removePrefixUri(si.getLocation().getUri()));
                    result.add(new SymbolInformationDto(si));
                  } else {
                    result.addAll(convertDocumentSymbol(o.getRight()));
                  }
                });
            return true;
          }

          private Collection<? extends SymbolInformationDto> convertDocumentSymbol(
              DocumentSymbol symbol) {
            ArrayList<SymbolInformationDto> result = new ArrayList<>();
            result.add(
                new SymbolInformationDto(
                    new SymbolInformation(
                        symbol.getName(), symbol.getKind(), new Location(uri, symbol.getRange()))));
            for (DocumentSymbol child : symbol.getChildren()) {
              result.addAll(convertDocumentSymbol(child));
            }
            return result;
          }
        },
        10000);
    return result;
  }

  private List<LocationDto> references(ReferenceParams referenceParams) {
    String wsPath = referenceParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    referenceParams.getTextDocument().setUri(uri);
    List<LocationDto> result = new ArrayList<>();
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);
    OperationUtil.doInParallel(
        servers,
        new LSOperation<ExtendedLanguageServer, List<? extends Location>>() {

          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return truish(element.getCapabilities().getReferencesProvider());
          }

          @Override
          public CompletableFuture<List<? extends Location>> start(ExtendedLanguageServer element) {
            return element.getTextDocumentService().references(referenceParams);
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element, List<? extends Location> locations) {
            locations.forEach(
                o -> {
                  o.setUri(removePrefixUri(o.getUri()));
                  result.add(new LocationDto(o));
                });
            return true;
          }
        },
        30000);
    return result;
  }

  private List<LocationDto> definition(TextDocumentPositionParams textDocumentPositionParams) {
    String wsPath = textDocumentPositionParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    textDocumentPositionParams.getTextDocument().setUri(uri);
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);
    List<LocationDto> result = new ArrayList<>();
    OperationUtil.doInParallel(
        servers,
        new LSOperation<ExtendedLanguageServer, List<? extends Location>>() {

          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return truish(element.getCapabilities().getDefinitionProvider());
          }

          @Override
          public CompletableFuture<List<? extends Location>> start(ExtendedLanguageServer element) {
            return element.getTextDocumentService().definition(textDocumentPositionParams);
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element, List<? extends Location> locations) {
            locations.forEach(
                o -> {
                  o.setUri(removePrefixUri(o.getUri()));
                  result.add(new LocationDto(o));
                });
            return true;
          }
        },
        30000);
    return result;
  }

  private ExtendedCompletionItemDto completionItemResolve(ExtendedCompletionItem unresolved) {
    try {
      ExtendedLanguageServer languageServer = findServer.byId(unresolved.getLanguageServerId());

      if (languageServer == null) {
        return new ExtendedCompletionItemDto(unresolved);
      } else {
        ExtendedCompletionItem res = new ExtendedCompletionItem();
        res.setItem(
            languageServer
                .getTextDocumentService()
                .resolveCompletionItem(unresolved.getItem())
                .get());
        res.setLanguageServerId(unresolved.getLanguageServerId());
        return new ExtendedCompletionItemDto(res);
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private HoverDto hover(TextDocumentPositionParams positionParams) {
    String wsPath = positionParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    positionParams.getTextDocument().setUri(uri);
    positionParams.setUri(prefixURI(positionParams.getUri()));
    Hover result = new Hover();
    StringBuilder content = new StringBuilder();

    Set<ExtendedLanguageServer> servers = findServer.byPath(uri);
    OperationUtil.doInParallel(
        servers,
        new LSOperation<ExtendedLanguageServer, Hover>() {

          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return truish(element.getCapabilities().getHoverProvider());
          }

          @Override
          public CompletableFuture<Hover> start(ExtendedLanguageServer element) {
            return element.getTextDocumentService().hover(positionParams);
          }

          @Override
          public boolean handleResult(ExtendedLanguageServer element, Hover hover) {
            if (hover != null) {
              Either<List<Either<String, MarkedString>>, MarkupContent> contents =
                  hover.getContents();
              if (contents.isLeft()) {
                for (Either<String, MarkedString> part : contents.getLeft()) {
                  if (content.length() > 0) {
                    content.append("\n\n");
                  }
                  if (part.isLeft()) {
                    content.append(part.getLeft());
                  } else {
                    // we don't handle the "language" in the IDE anyway.
                    content.append(part.getRight().getValue());
                  }
                }
              } else {
                MarkupContent markup = contents.getRight();
                if (MarkupKind.MARKDOWN.equals(markup.getKind())
                    || MarkupKind.PLAINTEXT.equals(markup.getKind())) {
                  if (content.length() > 0) {
                    content.append("\n\n");
                  }
                  content.append(markup.getValue());
                } else {
                  LOG.warn("Unknown markup type: {}", markup.getKind());
                }
              }
            }
            return true;
          }
        },
        10000);
    MarkupContent markupContent = new MarkupContent();
    markupContent.setKind(MarkupKind.MARKDOWN);
    markupContent.setValue(content.toString());
    result.setContents(markupContent);
    return new HoverDto(result);
  }

  private SignatureHelpDto signatureHelp(TextDocumentPositionParams positionParams) {
    String wsPath = positionParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    positionParams.getTextDocument().setUri(uri);
    positionParams.setUri(prefixURI(positionParams.getUri()));
    SignatureHelpDto[] result = new SignatureHelpDto[1];
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);
    LSOperation<ExtendedLanguageServer, SignatureHelp> op =
        new LSOperation<ExtendedLanguageServer, SignatureHelp>() {

          @Override
          public boolean canDo(ExtendedLanguageServer element) {
            return element.getCapabilities().getSignatureHelpProvider() != null;
          }

          @Override
          public CompletableFuture<SignatureHelp> start(ExtendedLanguageServer element) {
            return element.getTextDocumentService().signatureHelp(positionParams);
          }

          @Override
          public boolean handleResult(ExtendedLanguageServer element, SignatureHelp res) {
            if (res != null && !res.getSignatures().isEmpty()) {
              result[0] = new SignatureHelpDto(res);
              return true;
            }
            return false;
          }
        };
    OperationUtil.doInSequence(servers, op, 10000);
    return result[0];
  }

  private List<TextEditDto> formatting(DocumentFormattingParams documentFormattingParams) {
    try {
      String wsPath = documentFormattingParams.getTextDocument().getUri();
      String uri = prefixURI(wsPath);
      documentFormattingParams.getTextDocument().setUri(uri);
      Optional<ExtendedLanguageServer> serverOptional =
          findServer
              .byPath(wsPath)
              .stream()
              .filter(s -> truish(s.getCapabilities().getDocumentFormattingProvider()))
              .findFirst();
      if (serverOptional.isPresent()) {
        return serverOptional
            .get()
            .getTextDocumentService()
            .formatting(documentFormattingParams)
            .get(5000, TimeUnit.MILLISECONDS)
            .stream()
            .map(TextEditDto::new)
            .collect(Collectors.toList());
      } else {
        return emptyList();
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<TextEditDto> rangeFormatting(
      DocumentRangeFormattingParams documentRangeFormattingParams) {
    try {
      String wsPath = documentRangeFormattingParams.getTextDocument().getUri();
      String uri = prefixURI(wsPath);
      documentRangeFormattingParams.getTextDocument().setUri(uri);
      Optional<ExtendedLanguageServer> serverOptional =
          findServer
              .byPath(wsPath)
              .stream()
              .filter(s -> truish(s.getCapabilities().getDocumentRangeFormattingProvider()))
              .findFirst();
      if (serverOptional.isPresent()) {
        return serverOptional
            .get()
            .getTextDocumentService()
            .rangeFormatting(documentRangeFormattingParams)
            .get()
            .stream()
            .map(TextEditDto::new)
            .collect(Collectors.toList());
      } else {
        return emptyList();
      }

    } catch (InterruptedException | ExecutionException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private List<TextEditDto> onTypeFormatting(
      DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
    try {
      String wsPath = documentOnTypeFormattingParams.getTextDocument().getUri();
      String uri = prefixURI(wsPath);
      documentOnTypeFormattingParams.getTextDocument().setUri(uri);
      Optional<ExtendedLanguageServer> serverOptional =
          findServer
              .byPath(wsPath)
              .stream()
              .filter(it -> it.getCapabilities().getDocumentOnTypeFormattingProvider() != null)
              .findFirst();
      if (serverOptional.isPresent()) {
        return serverOptional
            .get()
            .getTextDocumentService()
            .onTypeFormatting(documentOnTypeFormattingParams)
            .get()
            .stream()
            .map(TextEditDto::new)
            .collect(Collectors.toList());
      } else {
        return emptyList();
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    String wsPath = didChangeTextDocumentParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    didChangeTextDocumentParams.getTextDocument().setUri(uri);
    didChangeTextDocumentParams.setUri(prefixURI(didChangeTextDocumentParams.getUri()));
    findServer
        .byPath(wsPath)
        .forEach(server -> server.getTextDocumentService().didChange(didChangeTextDocumentParams));
  }

  private void didOpen(DidOpenTextDocumentParams openTextDocumentParams) {
    String wsPath = openTextDocumentParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    openTextDocumentParams.getTextDocument().setUri(uri);
    findServer
        .byPath(wsPath)
        .forEach(server -> server.getTextDocumentService().didOpen(openTextDocumentParams));
  }

  private void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
    String wsPath = didCloseTextDocumentParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    didCloseTextDocumentParams.getTextDocument().setUri(uri);
    findServer
        .byPath(wsPath)
        .forEach(server -> server.getTextDocumentService().didClose(didCloseTextDocumentParams));
  }

  private void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
    String wsPath = didSaveTextDocumentParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    didSaveTextDocumentParams.getTextDocument().setUri(uri);
    findServer
        .byPath(wsPath)
        .forEach(server -> server.getTextDocumentService().didSave(didSaveTextDocumentParams));
  }

  private DocumentHighlightDto documentHighlight(
      TextDocumentPositionParams textDocumentPositionParams) {
    String wsPath = textDocumentPositionParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    textDocumentPositionParams.getTextDocument().setUri(uri);
    @SuppressWarnings("unchecked")
    List<DocumentHighlightDto>[] result = new List[1];
    LSOperation<ExtendedLanguageServer, List<DocumentHighlightDto>> op =
        new LSOperation<ExtendedLanguageServer, List<DocumentHighlightDto>>() {

          @Override
          public boolean canDo(ExtendedLanguageServer servers) {
            return true;
          }

          @Override
          public CompletableFuture<List<DocumentHighlightDto>> start(
              ExtendedLanguageServer element) {
            return CompletableFuture.supplyAsync(
                () -> {
                  List<DocumentHighlightDto> res = new ArrayList<>();
                  LSOperation<ExtendedLanguageServer, List<? extends DocumentHighlight>> op2 =
                      new LSOperation<ExtendedLanguageServer, List<? extends DocumentHighlight>>() {

                        @Override
                        public boolean canDo(ExtendedLanguageServer lsProxy) {
                          return truish(lsProxy.getCapabilities().getDocumentHighlightProvider());
                        }

                        @Override
                        public CompletableFuture<List<? extends DocumentHighlight>> start(
                            ExtendedLanguageServer element) {
                          return element
                              .getTextDocumentService()
                              .documentHighlight(textDocumentPositionParams);
                        }

                        @Override
                        public boolean handleResult(
                            ExtendedLanguageServer element,
                            List<? extends DocumentHighlight> result) {

                          return false;
                        }
                      };
                  OperationUtil.doInParallel(Collections.singleton(element), op2, 10000);

                  return res;
                });
          }

          @Override
          public boolean handleResult(
              ExtendedLanguageServer element, List<DocumentHighlightDto> list) {
            result[0] = list;
            return !list.isEmpty();
          }
        };
    OperationUtil.doInSequence(findServer.byPath(wsPath), op, 10000);

    if (!result[0].isEmpty()) {
      return result[0].get(0);
    }
    return null;
  }

  private RenameResultDto rename(RenameParams renameParams) {
    String wsPath = renameParams.getTextDocument().getUri();
    String uri = prefixURI(wsPath);
    renameParams.getTextDocument().setUri(uri);
    Map<String, ExtendedWorkspaceEdit> edits = new ConcurrentHashMap<>();
    Set<ExtendedLanguageServer> servers = findServer.byPath(wsPath);
    LSOperation<ExtendedLanguageServer, WorkspaceEdit> op =
        new LSOperation<ExtendedLanguageServer, WorkspaceEdit>() {
          @Override
          public boolean canDo(ExtendedLanguageServer server) {
            Boolean renameProvider = server.getCapabilities().getRenameProvider();
            return renameProvider != null && renameProvider;
          }

          @Override
          public CompletableFuture<WorkspaceEdit> start(ExtendedLanguageServer element) {
            return element.getTextDocumentService().rename(renameParams);
          }

          @Override
          public boolean handleResult(ExtendedLanguageServer element, WorkspaceEdit result) {

            addRenameResult(edits, element.getId(), result);
            return true;
          }
        };
    OperationUtil.doInParallel(servers, op, TimeUnit.SECONDS.toMillis(30));
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

  private static List<ExtendedTextEdit> convertToExtendedEdit(
      List<TextEdit> edits, String filePath) {
    // for some reason C# LS sends ws related path,
    if (!isStartWithProject(filePath)) {
      filePath = prefixProject(filePath);
    }
    try (FileReader reader = new FileReader(filePath)) {
      CharStreamIterator charStreamIter =
          new CharStreamIterator(CharStreamEditor.forReader(reader));

      return convertToExtendedEdits(edits, charStreamIter);
    } catch (IOException e) {
      LOG.error("Can't read file", e);
      return Collections.emptyList();
    }
  }

  @VisibleForTesting
  static List<ExtendedTextEdit> convertToExtendedEdits(
      List<TextEdit> edits, CharStreamIterator charStreamIter) {
    // don't manipulate the original collection
    edits = new ArrayList<>(edits);
    edits.sort(CharStreamEditor.COMPARATOR);

    List<ExtendedTextEdit> result = new ArrayList<>(edits.size());
    Iterator<TextEdit> editIterator = edits.iterator();
    if (editIterator.hasNext()) {
      TextEdit edit = editIterator.next();
      while (edit != null) {
        int currentLine = edit.getRange().getStart().getLine();
        Position lineStart = new Position(edit.getRange().getStart().getLine(), 0);
        Position nextLineStart = new Position(edit.getRange().getStart().getLine() + 1, 0);
        charStreamIter.advanceTo(lineStart, CharStreamIterator.NULL_CONSUMER);
        StringBuilder lineText = new StringBuilder();
        charStreamIter.advanceTo(
            nextLineStart,
            new BiConsumer<Integer, Integer>() {

              @Override
              public void accept(Integer t, Integer u) {
                if (t != '\r' && t != '\n') {
                  lineText.append((char) t.intValue());
                }
              }
            });
        while (edit != null && edit.getRange().getStart().getLine() == currentLine) {
          result.add(doConvert(edit, lineText));
          if (editIterator.hasNext()) {
            edit = editIterator.next();
          } else {
            edit = null;
          }
        }
      }
    }
    return result;
  }

  private static ExtendedTextEdit doConvert(TextEdit edit, StringBuilder currentLine) {
    ExtendedTextEdit extendedEdit = new ExtendedTextEdit();
    extendedEdit.setRange(edit.getRange());
    extendedEdit.setNewText(edit.getNewText());

    extendedEdit.setLineText(currentLine.toString());
    extendedEdit.setInLineStart(edit.getRange().getStart().getCharacter());
    if (edit.getRange().getEnd().getLine() == edit.getRange().getStart().getLine()) {
      extendedEdit.setInLineEnd(edit.getRange().getEnd().getCharacter());
    } else {
      extendedEdit.setInLineEnd(Math.max(0, currentLine.length() - 1));
    }
    return extendedEdit;
  }

  private String getFileContent(String uri) {
    try {
      uri = prefixURI(uri);
      Optional<ExtendedLanguageServer> serverOptional =
          findServer
              .byPath(uri)
              .stream()
              .filter(
                  s -> {
                    return s.getServer() instanceof FileContentAccess;
                  })
              .findFirst();
      if (serverOptional.isPresent()) {
        return ((FileContentAccess) serverOptional.get().getServer())
            .getFileContent(uri)
            .get(5000, TimeUnit.MILLISECONDS);
      } else {
        return null;
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  List<SnippetResult> getSnippets(SnippetParameters params) {
    try {
      String uri = params.getUri();
      if (LanguageServiceUtils.isWorkspaceUri(uri)) {
        uri = LanguageServiceUtils.workspaceURIToFileURI(uri);
      }
      Reader content = null;

      if (LanguageServiceUtils.isProjectUri(uri)) {
        String path = LanguageServiceUtils.removePrefixUri(uri);
        String wsPath = absolutize(path);

        if (fsManager.existsAsFile(wsPath)) {
          content = new InputStreamReader(new BufferedInputStream(fsManager.read(wsPath)));
        }
      } else {
        String fileContent = getFileContent(uri);
        if (fileContent != null) {
          content = new StringReader(fileContent);
        }
      }

      if (content != null) {
        ArrayList<LinearRange> ranges = new ArrayList<>(params.getRanges());
        try {
          List<SnippetResult> result = new ArrayList<>();
          Collections.sort(ranges, LinearRangeComparator.INSTANCE);
          LineReader lineReader = new LineReader(content);
          for (LinearRange range : ranges) {
            lineReader.readTo(range.getOffset());
            String snippet = lineReader.getCurrentLine();
            int offsetInLine = range.getOffset() - lineReader.getCurrentLineStartOffset();
            int lengthInLine = Math.min(snippet.length() - offsetInLine, range.getLength());
            LinearRange rangeInLine = new LinearRange(offsetInLine, lengthInLine);
            result.add(
                new SnippetResult(range, snippet, lineReader.getCurrentLineIndex(), rangeInLine));
          }
          return result;
        } finally {
          content.close();
        }
      } else {
        LOG.error("did not find file " + params.getUri());
        throw new JsonRpcException(-27000, "File not found for edit: " + params.getUri());
      }
    } catch (ServerException | NotFoundException | IOException | ConflictException e) {
      LOG.error("error editing file", e);
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
}
