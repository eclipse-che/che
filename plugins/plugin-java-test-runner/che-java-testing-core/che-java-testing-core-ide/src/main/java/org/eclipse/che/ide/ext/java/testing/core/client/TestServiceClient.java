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
package org.eclipse.che.ide.ext.java.testing.core.client;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;

/**
 * Client for calling test services
 *
 * @author Mirage Abeysekara
 */
@Singleton
public class TestServiceClient {

    private final AppContext appContext;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;

    @Inject
    public TestServiceClient(AppContext appContext, AsyncRequestFactory asyncRequestFactory,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory) {

        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

    }

    public Promise<TestResult> getTestResult(String projectPath, String testFramework, Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        if (parameters != null) {
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(URL.encode(e.getKey())).append('=').append(URL.encode(e.getValue()));
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/java/testing/run/?projectPath=" + projectPath + "&testFramework=" + testFramework + "&"
                + sb.toString();
        return asyncRequestFactory.createGetRequest(url)
                .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                .send(dtoUnmarshallerFactory.newUnmarshaller(TestResult.class));
    }

}
