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
package org.eclipse.che.api.project.server.importer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Importer output line consumer that perform broadcasting consumed output through the json rpc protocol to the specific method.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
public class ProjectImportOutputWSLineConsumer extends BaseProjectImportOutputLineConsumer {

    public static final String IMPORT_OUTPUT_CHANNEL = "importProject:output";

    private static final Logger LOG = LoggerFactory.getLogger(ProjectImportOutputWSLineConsumer.class);

    private final AtomicInteger lineCounter;

    public ProjectImportOutputWSLineConsumer(String projectName, int delayBetweenMessages) {
        super(projectName, delayBetweenMessages);

        lineCounter = new AtomicInteger(1);
    }

    @Override
    protected void sendOutputLine(String outputLine) {
        sendMessage(outputLine);
    }

    protected void sendMessage(String outputLine) {
        doSendMessage(IMPORT_OUTPUT_CHANNEL, createMessageObject(outputLine));
    }

    protected JsonObject createMessageObject(String message) {
        JsonObject jso = new JsonObject();
        jso.addProperty("num", lineCounter.getAndIncrement());
        jso.addProperty("line", message);
        jso.addProperty("project", projectName);

        return jso;
    }

    protected void doSendMessage(String channelId, JsonElement messageBody) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel(channelId);
            bm.setBody(messageBody.toString());

            WSConnectionContext.sendMessage(bm);
        } catch (Exception e) {
            LOG.error("A problem occurred while sending websocket message", e);
        }
    }
}
