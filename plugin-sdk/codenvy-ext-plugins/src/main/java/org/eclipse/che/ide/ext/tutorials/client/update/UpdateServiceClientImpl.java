/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client.update;

import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;

import static com.google.gwt.http.client.RequestBuilder.POST;

/**
 * @author Artem Zatsarynnyy
 * @author Valeriy Svydenko
 */
@Singleton
public class UpdateServiceClientImpl implements UpdateServiceClient {
    private final String     updateServicePath;
    private final MessageBus wsMessageBus;

    @Inject
    public UpdateServiceClientImpl(MessageBus wsMessageBus) {
        this.updateServicePath = "/runner-sdk";
        this.wsMessageBus = wsMessageBus;
    }

    @Override
    public void update(@NotNull Runner runner, @NotNull RequestCallback<Void> callback)
            throws WebSocketException {
        final String url = updateServicePath + "/update/" + runner.getProcessId();
        MessageBuilder messageBuilder = new MessageBuilder(POST, url);
        Message message = messageBuilder.build();
        wsMessageBus.send(message, callback);
    }
}