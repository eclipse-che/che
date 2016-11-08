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
package org.eclipse.che.ide.ext.java.client.dependenciesupdater;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Implementation of {@link JavaClasspathServiceClient}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class JavaClasspathServiceClientImpl implements JavaClasspathServiceClient {

    private final String                 baseHttpUrl;
    private final WsAgentStateController wsAgentStateController;

    @Inject
    protected JavaClasspathServiceClientImpl(WsAgentStateController wsAgentStateController) {
        this.wsAgentStateController = wsAgentStateController;
        this.baseHttpUrl = "/java/";
    }

    /** {@inheritDoc} */
    @Override
    public void updateDependencies(String projectPath, RequestCallback<ClassPathBuilderResult> callback) {
        final String requestUrl = baseHttpUrl + "/classpath/update?projectpath=" + projectPath;

        MessageBuilder builder = new MessageBuilder(GET, requestUrl);
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
