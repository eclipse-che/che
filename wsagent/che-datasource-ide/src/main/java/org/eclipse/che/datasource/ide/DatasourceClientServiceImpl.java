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
package org.eclipse.che.datasource.ide;

import com.google.gwt.http.client.RequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.datasource.ide.inject.DatasourceGinModule;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.datasource.shared.ServicePaths;
import org.eclipse.che.datasource.shared.TextDTO;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

/**
 * Implementation (REST) for the datasource server services client interface.
 */
@Singleton
public class DatasourceClientServiceImpl implements DatasourceClientService {

    private final String              restServiceContext;
    private final DtoFactory          dtoFactory;
    private final AsyncRequestFactory asyncRequestFactory;
    private final AppContext          appContext;

    /**
     * @param restContext rest context
     */
    @Inject
    public DatasourceClientServiceImpl(final @Named(DatasourceGinModule.DATASOURCE_CONTEXT_NAME) String restContext,
                                          final DtoFactory dtoFactory,
                                          final AppContext appContext,
                                          final AsyncRequestFactory asyncRequestFactory) {
        this.appContext = appContext;
        restServiceContext = restContext;
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
    }



    @Override
    public void getAvailableDrivers(AsyncRequestCallback<String> asyncRequestCallback) throws RequestException {
        String url = formatUrl(ServicePaths.DATABASE_TYPES_PATH, "", null);
//        String url = appContext.getDevMachine().getWsAgentBaseUrl() +"/databasetypes";
        Log.error(DatasourceClientServiceImpl.class, "URL is : "+url);
        final AsyncRequest getRequest = asyncRequestFactory.createGetRequest(url, false);
        getRequest.send(asyncRequestCallback);
    }

    @Override
    public String getRestServiceContext() {
        return restServiceContext;
    }


    @Override
    public void testDatabaseConnectivity(final @NotNull DatabaseConfigurationDTO configuration,
                                         final @NotNull AsyncRequestCallback<String> asyncRequestCallback) throws RequestException {
        String url = formatUrl(ServicePaths.TEST_DATABASE_CONNECTIVITY_PATH, "", null);
        final AsyncRequest postRequest = asyncRequestFactory.createPostRequest(url, configuration, false);
        postRequest.send(asyncRequestCallback);
    }


    @Override
    public void encryptText(final String textToEncrypt,
                            final AsyncRequestCallback<String> asyncRequestCallback) throws RequestException {
        TextDTO textDTO = dtoFactory.createDto(TextDTO.class).withValue(textToEncrypt);
        String url = formatUrl( ServicePaths.ENCRYPT_TEXT_PATH, "", null);
        AsyncRequest postRequest = asyncRequestFactory.createPostRequest(url, textDTO, false);
        postRequest.send(asyncRequestCallback);
    }

    /**
     * Builds the target REST service url.
     *
     * @param root the root of the service
     * @param service the rest service
     * @param param the parameters
     * @return the url
     */
    private String formatUrl(final String root, final String service, final String param) {
        StringBuilder sb = new StringBuilder(appContext.getDevMachine().getWsAgentBaseUrl());
//        if (restServiceContext != null && !restServiceContext.isEmpty()) {
//            sb.append("/")
//              .append(restServiceContext);
//        }
        if (root != null && !root.isEmpty()) {
            sb.append("/")
              .append(root);
        }
        if (service != null && !service.isEmpty()) {
            sb.append("/")
              .append(service);
        }

        if (param != null) {
            sb.append('/')
              .append(param);
        }
        Log.debug(DatasourceClientServiceImpl.class, "Create REST URL : " + sb.toString());
        return sb.toString();
    }
}
