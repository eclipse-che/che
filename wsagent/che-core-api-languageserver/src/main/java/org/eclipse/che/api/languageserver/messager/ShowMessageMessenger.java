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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls;
import org.eclipse.lsp4j.MessageParams;
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

/**
 * {@link EventSubscriber} for incoming <code>window/showMessage</code> notifications.
 *
 * @author xcoulon
 */
@Singleton
public class ShowMessageMessenger implements EventSubscriber<MessageParams> {

    private final static Logger LOG = LoggerFactory.getLogger(ShowMessageMessenger.class);

    private final EventService eventService;

    @Inject
    public ShowMessageMessenger(EventService eventService) {
        this.eventService = eventService;
    }

    public void onEvent(final MessageParams event) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel("languageserver/window/showMessage");
            bm.setBody(new DtoServerImpls.MessageParamsDto(event).toJson());
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
