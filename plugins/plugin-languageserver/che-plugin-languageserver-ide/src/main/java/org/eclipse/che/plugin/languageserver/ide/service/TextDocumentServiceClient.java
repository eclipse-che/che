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
package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionList;
import org.eclipse.che.api.languageserver.shared.model.RenameResult;
import org.eclipse.che.api.languageserver.shared.model.SnippetParameters;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
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
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.services.TextDocumentService;

@Singleton
public class TextDocumentServiceClient {

  private final RequestTransmitter requestTransmitter;

  @Inject
  public TextDocumentServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  /**
   * GWT client implementation of {@link TextDocumentService#completion(TextDocumentPositionParams)}
   *
   * @param position
   * @return
   */
  public Promise<ExtendedCompletionList> completion(TextDocumentPositionParams position) {
    return transmitDtoAndReceiveDto(
        position, "textDocument/completion", ExtendedCompletionList.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#resolveCompletionItem(CompletionItem)}
   *
   * @param completionItem
   * @return
   */
  public Promise<ExtendedCompletionItem> resolveCompletionItem(
      ExtendedCompletionItem completionItem) {
    return transmitDtoAndReceiveDto(
        completionItem, "textDocument/completionItem/resolve", ExtendedCompletionItem.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#documentSymbol(DocumentSymbolParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
    return transmitDtoAndReceiveDtoList(
        params, "textDocument/documentSymbol", SymbolInformation.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#references(ReferenceParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<Location>> references(ReferenceParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/references", Location.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#references(ReferenceParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<Location>> definition(TextDocumentPositionParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/definition", Location.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#hover(TextDocumentPositionParams)}
   *
   * @param params
   * @return
   */
  public Promise<Hover> hover(TextDocumentPositionParams params) {
    return transmitDtoAndReceiveDto(params, "textDocument/hover", Hover.class);
  }

  /**
   * GWT client implementation of {@link
   * TextDocumentService#signatureHelp(TextDocumentPositionParams)}
   *
   * @param params
   * @return
   */
  public Promise<SignatureHelp> signatureHelp(TextDocumentPositionParams params) {
    return transmitDtoAndReceiveDto(params, "textDocument/signatureHelp", SignatureHelp.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#formatting(DocumentFormattingParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<TextEdit>> formatting(DocumentFormattingParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/formatting", TextEdit.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#formatting(DocumentFormattingParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/rangeFormatting", TextEdit.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#formatting(DocumentFormattingParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/onTypeFormatting", TextEdit.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#didChange(DidChangeTextDocumentParams)}
   *
   * @param params
   * @return
   */
  public void didChange(DidChangeTextDocumentParams params) {
    transmitDtoAndReceiveNothing(params, "textDocument/didChange");
  }

  /**
   * GWT client implementation of {@link TextDocumentService#didOpen(DidOpenTextDocumentParams)}
   *
   * @param params
   * @return
   */
  public void didOpen(DidOpenTextDocumentParams params) {
    transmitDtoAndReceiveNothing(params, "textDocument/didOpen");
  }

  /**
   * GWT client implementation of {@link TextDocumentService#didClose(DidCloseTextDocumentParams)}
   *
   * @param params
   * @return
   */
  public void didClose(DidCloseTextDocumentParams params) {
    transmitDtoAndReceiveNothing(params, "textDocument/didClose");
  }

  /**
   * GWT client implementation of {@link TextDocumentService#didSave(DidSaveTextDocumentParams)}
   *
   * @param params
   * @return
   */
  public void didSave(DidSaveTextDocumentParams params) {
    transmitDtoAndReceiveNothing(params, "textDocument/didSave");
  }

  /**
   * GWT client implementation of {@link
   * TextDocumentService#documentHighlight(TextDocumentPositionParams position)}
   *
   * @param params
   * @return a {@link Promise} of an array of {@link DocumentHighlight} which will be computed by
   *     the language server.
   */
  public Promise<List<DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
    return transmitDtoAndReceiveDtoList(
        params, "textDocument/documentHighlight", DocumentHighlight.class);
  }

  /**
   * GWT client implementation of {@link TextDocumentService#rename(RenameParams)}
   *
   * @param params
   * @return a {@link Promise} of a rename result object which contains all workspace edits.
   */
  public Promise<RenameResult> rename(RenameParams params) {
    return transmitDtoAndReceiveDto(params, "textDocument/rename", RenameResult.class);
  }

  public Promise<List<Command>> codeAction(CodeActionParams params) {
    return transmitDtoAndReceiveDtoList(params, "textDocument/codeAction", Command.class);
  }

  private <T> Promise<T> transmitDtoAndReceiveDto(
      Object jsonSerializable, String name, Class<T> resultDtoClass) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(name)
                .paramsAsDto(jsonSerializable)
                .sendAndReceiveResultAsDto(resultDtoClass)
                .onSuccess(resolve::apply)
                .onFailure(error -> reject.apply(getPromiseError(error))));
  }

  private <T> Promise<List<T>> transmitDtoAndReceiveDtoList(
      Object jsonSerializable, String name, Class<T> resultDtoClass) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(name)
                .paramsAsDto(jsonSerializable)
                .sendAndReceiveResultAsListOfDto(resultDtoClass)
                .onSuccess(resolve::apply)
                .onFailure(error -> reject.apply(getPromiseError(error))));
  }

  private void transmitDtoAndReceiveNothing(Object jsonSerializable, String name) {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(name)
        .paramsAsDto(jsonSerializable)
        .sendAndSkipResult();
  }

  private PromiseError getPromiseError(JsonRpcError jsonRpcError) {
    return new PromiseError() {
      @Override
      public String getMessage() {
        return jsonRpcError.getMessage();
      }

      @Override
      public Throwable getCause() {
        return new JsonRpcException(jsonRpcError.getCode(), jsonRpcError.getMessage());
      }
    };
  }

  public Promise<List<SnippetResult>> getSnippets(SnippetParameters params) {
    return Promises.create(
        (resolve, reject) -> {
          requestTransmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName("textDocument/snippets")
              .paramsAsDto(params)
              .sendAndReceiveResultAsListOfDto(SnippetResult.class)
              .onSuccess(resolve::apply)
              .onFailure(error -> reject.apply(getPromiseError(error)));
        });
  }

  public Promise<String> getFileContent(String uri) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName("textDocument/fileContent")
                .paramsAsString(uri)
                .sendAndReceiveResultAsString()
                .onSuccess(resolve::apply)
                .onFailure(error -> reject.apply(getPromiseError(error))));
  }
}
