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
package org.eclipse.che.ide.ext.java.testing.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
//import org.eclipse.che.api.machine.gwt.client.WsAgentStateController;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

@Singleton
public class TestServiceClient {
//    private final AsyncRequestFactory asyncRequestFactory;
//    private final LoaderFactory loaderFactory;
//    private final String extPath;
//    private final String wsID;
    private final WsAgentStateController wsAgentStateController;
//    private final AppContext appContext;

    @Inject
    public TestServiceClient(WsAgentStateController wsAgentStateController) {
//        this.asyncRequestFactory = asyncRequestFactory;
//        this.loaderFactory = loaderFactory;
        this.wsAgentStateController = wsAgentStateController;
//        this.appContext =  appContext;
        // extPath gets the relative path of Che app from the @Named DI in constructor
        // appContext is a Che class that provides access to workspace
//        helloPath = extPath + "/testing/" + appContext.getWorkspace().getId();
//        this.extPath = appContext.getDevMachine().getWsAgentBaseUrl();
//        this.wsID = appContext.getWorkspace().getId();
//        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/jdt/"
//                + appContext.getWorkspaceId() + "/reconcile/?projectpath=" + projectPath + "&fqn=" + fqn;


    }
//
//    // Invoked by our TestAction class
//    // Invokes the request to the server
//    public Promise<String> computeProposals(String projectPath, String fqn) {
//        String url = extPath + "/testing/" + wsID + "/run/?projectpath=" + projectPath + "&fqn=" + fqn;
//
//        return asyncRequestFactory.createGetRequest(url)
//                .loader(loaderFactory.newLoader("Loading your response..."))
//                .send(new StringUnmarshaller());
//    }

    public void runTest(String workspaceId, ProjectConfigDto project, String fqn,
                        RequestCallback<TestResult> callback) {
        String url = "/testing/" + workspaceId + "/run/?projectpath=" + project.getPath() + "&fqn=" + fqn;
//        asyncRequestFactory.createGetRequest(url)
//                .loader(loaderFactory.newLoader("Loading test results...")).send(callback);
        updateDependencies(url,callback);
    }


    public void updateDependencies(String url, RequestCallback<TestResult> callback) {

        MessageBuilder builder = new MessageBuilder(GET, url);
        builder.header(ACCEPT, APPLICATION_JSON);
        Message message = builder.build();
        sendMessageToWS(message, callback);
    }


    private void sendMessageToWS(final @NotNull Message message, final @NotNull RequestCallback<?> callback) {
        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus arg) throws OperationException {
                try {
                    arg.send(message, callback);
                } catch (WebSocketException e) {
                    throw new OperationException(e.getMessage(), e);
                }
            }
        });
    }
}
