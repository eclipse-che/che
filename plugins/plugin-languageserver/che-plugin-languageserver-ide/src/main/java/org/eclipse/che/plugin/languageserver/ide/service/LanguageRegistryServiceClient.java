package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import java.util.List;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LanguageDescriptionDTO;

import com.google.inject.Inject;

/**
 * 
 * @author Sven Efftinge
 */
public class LanguageRegistryServiceClient {

    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext appContext;

    @Inject
    public LanguageRegistryServiceClient(
            final DtoUnmarshallerFactory unmarshallerFactory, 
            final AppContext appContext,
            final AsyncRequestFactory asyncRequestFactory) {
        this.unmarshallerFactory = unmarshallerFactory;
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    /**
     * @return all registered languages
     */
    public Promise<List<LanguageDescriptionDTO>> getRegisteredLanguages() {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/supportedLanguages";
        Unmarshallable<List<LanguageDescriptionDTO>> unmarshaller = unmarshallerFactory
                .newListUnmarshaller(LanguageDescriptionDTO.class);
        return asyncRequestFactory.createGetRequest(requestUrl).header(ACCEPT, APPLICATION_JSON).
                send(unmarshaller);
    }

}
