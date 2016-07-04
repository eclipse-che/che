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
package org.eclipse.che.api.vfs.impl.file.event;

import com.google.common.annotations.Beta;

import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;

import javax.inject.Singleton;
import javax.websocket.EncodeException;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Designed to broadcast high level events over web sockets to a client-side listener.
 * WS listener must beforehand know corresponding WE channel name and event DTO class,
 * because basically all this class does is broadcasting a DTO to a specific channel.
 * Nothing more.
 * <p>
 *     Note: Proper DTO object and web socket channel name are assumed to be stored in
 *     {@link HiEvent} instance passed to this broadcaster. Relevance of DTO and ws
 *     chanel data is the responsibility of {@link HiEvent} instance creator.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
public class HiEventClientBroadcaster implements HiEventBroadcaster {
    private static final Logger LOG = getLogger(HiEventClientBroadcaster.class);

    @Override
    public void broadcast(HiEvent event) {
        final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
        bm.setChannel(event.getChannel());
        bm.setBody(DtoFactory.getInstance().toJson(event.getDto()));

        try {
            WSConnectionContext.sendMessage(bm);
            LOG.debug("Sending message over websocket connection: {}", bm);
        } catch (EncodeException | IOException e) {
            LOG.error("Can't send a VFS notification over web socket. Event: {}", event, e);
        }
    }
}
