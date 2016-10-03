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
package org.eclipse.che.plugin.languageserver.ide.service;

import io.typefox.lsapi.CompletionItem;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidCloseTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidOpenTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DidSaveTextDocumentParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentOnTypeFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentRangeFormattingParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.DocumentSymbolParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.HoverDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.PublishDiagnosticsParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.ReferenceParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.SignatureHelpDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.SymbolInformationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextEditDTO;
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

    @Inject
    public TextDocumentServiceClient(
            final DtoUnmarshallerFactory unmarshallerFactory,
            final NotificationManager notificationManager,
            final AppContext appContext,
            final AsyncRequestFactory asyncRequestFactory,
            final WsAgentStateController wsAgentStateController,
            final PublishDiagnosticsProcessor publishDiagnosticsProcessor) {
        this.unmarshallerFactory = unmarshallerFactory;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.publishDiagnosticsProcessor = publishDiagnosticsProcessor;
        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus arg) throws OperationException {
                subscribeToPublishDiagnostics(arg);
            }
        });
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#completion(io.typefox.lsapi.TextDocumentPositionParams)}
     *
     * @param position
     * @return
     */
    public Promise<List<CompletionItemDTO>> completion(TextDocumentPositionParamsDTO position) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/completion";
        Unmarshallable<List<CompletionItemDTO>> unmarshaller = unmarshallerFactory
                .newListUnmarshaller(CompletionItemDTO.class);
        return asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)position).toJson()).send(unmarshaller);
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.TextDocumentService#resolveCompletionItem(CompletionItem)}
     *
     * @param completionItem
     * @return
     */
    public Promise<CompletionItemDTO> resolveCompletionItem(CompletionItemDTO completionItem) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/completionItem/resolve";
        Unmarshallable<CompletionItemDTO> unmarshaller = unmarshallerFactory.newUnmarshaller(CompletionItemDTO.class);
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
    public Promise<List<SymbolInformationDTO>> documentSymbol(DocumentSymbolParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/documentSymbol";
        Unmarshallable<List<SymbolInformationDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(SymbolInformationDTO.class);
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
    public Promise<List<LocationDTO>> references(ReferenceParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/references";
        Unmarshallable<List<LocationDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(LocationDTO.class);
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
    public Promise<List<LocationDTO>> definition(TextDocumentPositionParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/definition";
        Unmarshallable<List<LocationDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(LocationDTO.class);
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
    public Promise<HoverDTO> hover(TextDocumentPositionParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/hover";
        Unmarshallable<HoverDTO> unmarshaller = unmarshallerFactory.newUnmarshaller(HoverDTO.class);
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
    public Promise<SignatureHelpDTO> signatureHelp(TextDocumentPositionParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/signatureHelp";
        Unmarshallable<SignatureHelpDTO> unmarshaller = unmarshallerFactory.newUnmarshaller(SignatureHelpDTO.class);
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
    public Promise<List<TextEditDTO>> formatting(DocumentFormattingParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/formatting";
        Unmarshallable<List<TextEditDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEditDTO.class);
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
    public Promise<List<TextEditDTO>> rangeFormatting(DocumentRangeFormattingParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/rangeFormatting";
        Unmarshallable<List<TextEditDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEditDTO.class);
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
    public Promise<List<TextEditDTO>> onTypeFormatting(DocumentOnTypeFormattingParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/onTypeFormatting";
        Unmarshallable<List<TextEditDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(TextEditDTO.class);
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
    public void didChange(DidChangeTextDocumentParamsDTO change) {
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
    public void didOpen(DidOpenTextDocumentParamsDTO openEvent) {
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
    public void didClose(DidCloseTextDocumentParamsDTO closeEvent) {
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
    public void didSave(DidSaveTextDocumentParamsDTO saveEvent) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/didSave";
        asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable)saveEvent).toJson()).send();
    }

    /**
     * Subscribes to websocket for 'textDocument/publishDiagnostics' notifications. 
     */
    private void subscribeToPublishDiagnostics(final MessageBus messageBus) {
        org.eclipse.che.ide.websocket.rest.Unmarshallable<PublishDiagnosticsParamsDTO> unmarshaller =
                unmarshallerFactory.newWSUnmarshaller(PublishDiagnosticsParamsDTO.class);
        try {
            messageBus.subscribe("languageserver/textDocument/publishDiagnostics",
                                 new SubscriptionHandler<PublishDiagnosticsParamsDTO>(unmarshaller) {
                                     @Override
                                     protected void onMessageReceived(PublishDiagnosticsParamsDTO statusEvent) {
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

}
