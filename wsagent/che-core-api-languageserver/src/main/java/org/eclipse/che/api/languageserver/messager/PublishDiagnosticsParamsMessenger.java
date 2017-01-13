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
package org.eclipse.che.api.languageserver.messager;

import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.impl.PublishDiagnosticsParamsImpl;

import com.google.gson.Gson;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import java.io.IOException;

@Singleton
public class PublishDiagnosticsParamsMessenger implements EventSubscriber<PublishDiagnosticsParams> {
    private final static Logger LOG = LoggerFactory.getLogger(PublishDiagnosticsParamsMessenger.class);

    private final EventService eventService;

    @Inject
    public PublishDiagnosticsParamsMessenger(EventService eventService) {
        this.eventService = eventService;
    }

    public void onEvent(final PublishDiagnosticsParams event) {
        try {
            if (event instanceof PublishDiagnosticsParamsImpl && event.getUri().startsWith("file:///projects")) {
                ((PublishDiagnosticsParamsImpl)event).setUri(event.getUri().substring(16));
            }
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel("languageserver/textDocument/publishDiagnostics");
            bm.setBody(new Gson().toJson(event));
            WSConnectionContext.sendMessage(bm);
        } catch (EncodeException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
