/*******************************************************************************
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.plugin.composer.shared.dto.ComposerOutput;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_OUTPUT;
import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_SUBSCRIBE;

/**
 * A mechanism for handling all messages from the Composer and applying
 * registered consumers.
 * 
 * @author Kaloyan Raev
 */
@Singleton
public class ComposerJsonRpcHandler {
    private static final String WS_AGENT_ENDPOINT = "ws-agent";

    private RequestHandlerConfigurator configurator;

    private Set<Consumer<ComposerOutput>> composerOutputConsumers = new HashSet<>();

    private boolean isSubscribed = false;

    @Inject
    public ComposerJsonRpcHandler(RequestHandlerConfigurator configurator) {
        this.configurator = configurator;

        handleComposerMessages();
    }

    @Inject
    private void subscribe(RequestTransmitter requestTransmitter) {
        if (isSubscribed) {
            return;
        }

        requestTransmitter.newRequest()
                          .endpointId(WS_AGENT_ENDPOINT)
                          .methodName(COMPOSER_CHANNEL_SUBSCRIBE)
                          .noParams()
                          .sendAndSkipResult();

        isSubscribed = true;
    }

    /**
     * Adds consumer for the event with {@link ComposerOutput}.
     *
     * @param consumer
     *            new consumer
     */
    public void addComposerOutputHandler(Consumer<ComposerOutput> consumer) {
        composerOutputConsumers.add(consumer);
    }

    private void handleComposerMessages() {
        configurator.newConfiguration()
                    .methodName(COMPOSER_CHANNEL_OUTPUT)
                    .paramsAsDto(ComposerOutput.class)
                    .noResult()
                    .withConsumer(archetypeOutput -> composerOutputConsumers.forEach(it -> it.accept(archetypeOutput)));
    }
}
