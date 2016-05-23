package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import java.util.List;

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
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DidChangeTextDocumentParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.PublishDiagnosticsParamsDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import com.google.inject.Inject;

public class TextDocumentServiceClient {

    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext appContext;
    private final NotificationManager notificationManager;
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
                .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable) position).toJson()).send(unmarshaller);
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
                .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable) change).toJson()).send();
    }
    
    /**
     * Subscribes to websocket for 'textDocument/publishDiagnostics' notifications. 
     */
    private void subscribeToPublishDiagnostics(final MessageBus messageBus) {
        org.eclipse.che.ide.websocket.rest.Unmarshallable<PublishDiagnosticsParamsDTO> unmarshaller = unmarshallerFactory.newWSUnmarshaller(PublishDiagnosticsParamsDTO.class);
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
