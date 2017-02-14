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
package org.eclipse.che.api.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

/**
 * Message consumer that sends messages to specified websocket channel
 *
 * @author Alexander Garagatyi
 */
public class WebsocketMessageConsumer<T> extends AbstractMessageConsumer<T> {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final LineConsumer messageSender;

    public WebsocketMessageConsumer(String channel) {
        this.messageSender = new WebsocketLineConsumer(channel);
    }

    @Override
    public void consume(T message) throws IOException {
        messageSender.writeLine(GSON.toJson(message));
    }
}
