package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import java.util.List;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.JsonSerializable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;

import com.google.inject.Inject;

public class TextDocumentServiceClient {

    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext appContext;

    @Inject
    public TextDocumentServiceClient(DtoUnmarshallerFactory unmarshallerFactory, AppContext appContext,
            AsyncRequestFactory asyncRequestFactory) {
        this.appContext = appContext;
        this.unmarshallerFactory = unmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    public Promise<List<CompletionItemDTO>> completion(TextDocumentPositionParamsDTO position) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/textDocument/completion";
        Unmarshallable<List<CompletionItemDTO>> unmarshaller = unmarshallerFactory
                .newListUnmarshaller(CompletionItemDTO.class);
        return asyncRequestFactory.createPostRequest(requestUrl, null).header(ACCEPT, APPLICATION_JSON)
                .header(CONTENT_TYPE, APPLICATION_JSON).data(((JsonSerializable) position).toJson()).send(unmarshaller);
    }

}
