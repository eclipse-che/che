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
package org.eclipse.che.plugin.languageserver.ide.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionList;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.dto.JsonSerializable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.plugin.languageserver.ide.editor.PublishDiagnosticsProcessor;
import org.eclipse.che.plugin.languageserver.ide.editor.ShowMessageProcessor;
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
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;


/**
 * @author Anatolii Bazko
 */
@Singleton
public class TextDocumentServiceClient {

    private final DtoUnmarshallerFactory      unmarshallerFactory;
    private final AsyncRequestFactory         asyncRequestFactory;
    private final AppContext                  appContext;
    private final NotificationManager         notificationManager;
    private final PublishDiagnosticsProcessor publishDiagnosticsProcessor;
    private final ShowMessageProcessor        showMessageProcessor;

    @Inject
    public TextDocumentServiceClient(
            final DtoUnmarshallerFactory unmarshallerFactory,
            final NotificationManager notificationManager,
            final AppContext appContext,
            final AsyncRequestFactory asyncRequestFactory,
            final WsAgentStateController wsAgentStateController,
            final PublishDiagnosticsProcessor publishDiagnosticsProcessor,
            final ShowMessageProcessor showMessageProcessor) {
        this.unmarshallerFactory = unmarshallerFactory;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.publishDiagnosticsProcessor = publishDiagnosticsProcessor;
        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus messageBus) throws OperationException {
                subscribeToPublishDiagnostics(messageBus);
                subscribeToShowMessages(messageBus);
            }
        });
        this.showMessageProcessor = showMessageProcessor;
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#completion(io.typefox.lsapi.TextDocumentPositionParams)}
     *
     * @param position
     * @return
     */
    public Promise<ExtendedCompletionList> completion(TextDocumentPositionParams position) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/completion";
        Unmarshallable<ExtendedCompletionList> unmarshaller = unmarshallerFactory.newUnmarshaller(ExtendedCompletionList.class);
        return asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)position).toJson()).send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#resolveCompletionItem(CompletionItem)}
     *
     * @param completionItem
     * @return
     */
    public Promise<ExtendedCompletionItem> resolveCompletionItem(CompletionItem completionItem) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/completionItem/resolve";
        Unmarshallable<ExtendedCompletionItem> unmarshaller = unmarshallerFactory.newUnmarshaller(ExtendedCompletionItem.class);
        return asyncRequestFactory.createPostRequest(requestUrl, completionItem)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON).send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#documentSymbol(io.typefox.lsapi.DocumentSymbolParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/documentSymbol";
        Unmarshallable<List<SymbolInformation>> unmarshaller = unmarshallerFactory.newListUnmarshaller(SymbolInformation.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#references(io.typefox.lsapi.ReferenceParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<Location>> references(ReferenceParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/references";
        Unmarshallable<List<Location>> unmarshaller = unmarshallerFactory.newListUnmarshaller(Location.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#references(io.typefox.lsapi.ReferenceParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<Location>> definition(TextDocumentPositionParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/definition";
        Unmarshallable<List<Location>> unmarshaller = unmarshallerFactory.newListUnmarshaller(Location.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#hover(io.typefox.lsapi.TextDocumentPositionParams)}
     *
     * @param params
     * @return
     */
    public Promise<Hover> hover(TextDocumentPositionParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/hover";
        Unmarshallable<Hover> unmarshaller = unmarshallerFactory.newUnmarshaller(Hover.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#signatureHelp(io.typefox.lsapi.TextDocumentPositionParams)}
     *
     * @param params
     * @return
     */
    public Promise<SignatureHelp> signatureHelp(TextDocumentPositionParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/signatureHelp";
        Unmarshallable<SignatureHelp> unmarshaller = unmarshallerFactory.newUnmarshaller(SignatureHelp.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#formatting(io.typefox.lsapi.DocumentFormattingParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<TextEdit>> formatting(DocumentFormattingParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/formatting";
        Unmarshallable<List<TextEdit>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEdit.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#formatting(io.typefox.lsapi.DocumentRangeFormattingParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/rangeFormatting";
        Unmarshallable<List<TextEdit>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEdit.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#formatting(io.typefox.lsapi.DocumentFormattingParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/onTypeFormatting";
        Unmarshallable<List<TextEdit>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEdit.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#didChange(io.typefox.lsapi.DidChangeTextDocumentParams)}
     *
     * @param change
     * @return
     */
    public void didChange(DidChangeTextDocumentParams change) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/didChange";
        asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)change).toJson()).send();
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#didOpen(io.typefox.lsapi.DidOpenTextDocumentParams)}
     *
     * @param openEvent
     * @return
     */
    public void didOpen(DidOpenTextDocumentParams openEvent) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/didOpen";
        asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)openEvent).toJson()).send();
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#didClose(io.typefox.lsapi.DidCloseTextDocumentParams)}
     *
     * @param closeEvent
     * @return
     */
    public void didClose(DidCloseTextDocumentParams closeEvent) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/didClose";
        asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)closeEvent).toJson()).send();
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#didSave(io.typefox.lsapi.DidSaveTextDocumentParams)}
     *
     * @param saveEvent
     * @return
     */
    public void didSave(DidSaveTextDocumentParams saveEvent) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/didSave";
        asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)saveEvent).toJson()).send();
    }


    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#documentHighlight(io.typefox.lsapi.TextDocumentPositionParams
     * position)}
     *
     * @param position
     * @return a {@link Promise} of an array of {@link DocumentHighlightDTO} which will be computed by the language server.
     */
    public Promise<DocumentHighlight> documentHighlight(TextDocumentPositionParams position) {
        final String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/documentHighlight";
        final Unmarshallable<DocumentHighlight> unmarshaller = unmarshallerFactory.newUnmarshaller(DocumentHighlight.class);
        return asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)position).toJson()).send(unmarshaller);
    }



    public Promise<List<Command>> codeAction(CodeActionParams params) {
        final String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/codeAction";
        final Unmarshallable<List<Command>> unmarshaller = unmarshallerFactory.newListUnmarshaller(Command.class);
        return asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)params).toJson()).send(unmarshaller);
    }

    /**
     * Subscribes to websocket for 'textDocument/publishDiagnostics' notifications.
     */
    private void subscribeToPublishDiagnostics(final MessageBus messageBus) {
        org.eclipse.che.ide.websocket.rest.Unmarshallable<PublishDiagnosticsParams> unmarshaller =
                unmarshallerFactory.newWSUnmarshaller(PublishDiagnosticsParams.class);
        try {
            messageBus.subscribe("languageserver/textDocument/publishDiagnostics",
                                 new SubscriptionHandler<PublishDiagnosticsParams>(unmarshaller) {
                                     @Override
                                     protected void onMessageReceived(PublishDiagnosticsParams statusEvent) {
                                         publishDiagnosticsProcessor.processDiagnostics(statusEvent);
                                     }

                                     @Override
                                     protected void onErrorReceived(Throwable exception) {
                                         notificationManager.notify(exception.getMessage(), StatusNotification.Status.FAIL,
                                                                    StatusNotification.DisplayMode.NOT_EMERGE_MODE);
                                     }
                                 });
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    /**
     * Subscribes to websocket for 'window/showMessage' notifications.
     */
    private void subscribeToShowMessages(final MessageBus messageBus) {
        final org.eclipse.che.ide.websocket.rest.Unmarshallable<ShowMessageRequestParams> unmarshaller =
                unmarshallerFactory.newWSUnmarshaller(ShowMessageRequestParams.class);
        try {
            messageBus.subscribe("languageserver/window/showMessage",
                                 new SubscriptionHandler<ShowMessageRequestParams>(unmarshaller) {
                                     @Override
                                     protected void onMessageReceived(ShowMessageRequestParams ShowMessageRequestParams) {
                                         showMessageProcessor.processNotification(ShowMessageRequestParams);
                                     }

                                     @Override
                                     protected void onErrorReceived(Throwable exception) {
                                         notificationManager.notify(exception.getMessage(), StatusNotification.Status.FAIL,
                                                                    StatusNotification.DisplayMode.NOT_EMERGE_MODE);
                                     }
                                 });
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }
}
