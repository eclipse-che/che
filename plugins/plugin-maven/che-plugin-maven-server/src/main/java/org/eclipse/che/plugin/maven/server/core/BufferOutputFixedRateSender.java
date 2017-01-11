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
package org.eclipse.che.plugin.maven.server.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The class contains business logic which allows send messages via web socket after defined period of time. To create the consumer
 * we have to know channel to connect to web socket and define period of time after which messages will be sent via web socket.
 *
 * @author Dmitry Shnurenko
 */
public class BufferOutputFixedRateSender extends ListLineConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(BufferOutputFixedRateSender.class);

    private final ScheduledExecutorService executor;

    private final String channel;

    public BufferOutputFixedRateSender(String channel, long delay) {
        this.channel = channel;
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(BufferOutputFixedRateSender.class.getSimpleName() + "-%d")
                                                                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                                                .setDaemon(true)
                                                                .build();

        executor = Executors.newScheduledThreadPool(1, threadFactory);

        executor.scheduleAtFixedRate(this::sendMessage, 1_000, delay, MILLISECONDS);
    }

    private synchronized void sendMessage() {
        final ChannelBroadcastMessage message = new ChannelBroadcastMessage();
        message.setChannel(channel);

        String text = getText();

        if (text.isEmpty()) {
            return;
        }

        message.setBody(text);
        sendMessageToWS(message);

        lines.clear();
    }

    private void sendMessageToWS(final ChannelBroadcastMessage message) {
        try {
            WSConnectionContext.sendMessage(message);
        } catch (Exception exception) {
            LOG.error(getClass() + " A problem occurred while sending websocket message", exception);
        }
    }

    @Override
    public synchronized void writeLine(String line) {
        lines.add(line);
    }

    @Override
    public synchronized void close() {
        while (!getText().isEmpty()) {
            sendMessage();

            try {
                Thread.sleep(1_000);
            } catch (InterruptedException exception) {
                executor.shutdown();

                LOG.error(getClass() + " A problem occurred while closing sender", exception);
            }
        }

        executor.shutdown();
    }
}
