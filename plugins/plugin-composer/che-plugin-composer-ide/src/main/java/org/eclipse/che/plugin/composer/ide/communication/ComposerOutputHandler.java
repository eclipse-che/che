/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.ide.communication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.plugin.composer.shared.dto.ComposerOutput;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_CHANNEL_NAME;

/**
 * Handler which receives messages from the Composer tool.
 *
 * @author Kaloyan Raev
 */
@Singleton
public class ComposerOutputHandler {

    private final EventBus                eventBus;
    private final DtoFactory              factory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;
    private final AppContext              appContext;

    @Inject
    public ComposerOutputHandler(EventBus eventBus,
                                 DtoFactory factory,
                                 WsAgentStateController wsAgentStateController,
                                 ProcessesPanelPresenter processesPanelPresenter,
                                 CommandConsoleFactory commandConsoleFactory,
                                 AppContext appContext) {
        this.eventBus = eventBus;
        this.factory = factory;
        this.processesPanelPresenter = processesPanelPresenter;
        this.commandConsoleFactory = commandConsoleFactory;
        this.appContext = appContext;

        handleOperations(factory, wsAgentStateController);
    }

    private void handleOperations(final DtoFactory factory, final WsAgentStateController wsAgentStateController) {
        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
                    @Override
                    public void apply(MessageBus messageBus) throws OperationException {
                        handleComposerOutput(messageBus);
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });
    }

    private void handleComposerOutput(final MessageBus messageBus)  {
        final DefaultOutputConsole outputConsole = (DefaultOutputConsole) commandConsoleFactory.create("Composer");

        try {
            messageBus.subscribe(COMPOSER_CHANNEL_NAME, new MessageHandler() {
                @Override
                public void onMessage(String message) {
                    Log.info(getClass(), message);
                    ComposerOutput archetypeOutput = factory.createDtoFromJson(message, ComposerOutput.class);
                    processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), outputConsole);
                    switch (archetypeOutput.getState()) {
                        case START:
                            outputConsole.clearOutputsButtonClicked();
                            outputConsole.printText(archetypeOutput.getOutput(),"green");
                            break;
                        case IN_PROGRESS:
                            outputConsole.printText(archetypeOutput.getOutput());
                            break;
                        case DONE:
                            outputConsole.printText(archetypeOutput.getOutput(),"green");
                            break;
                        case ERROR:
                            outputConsole.printText(archetypeOutput.getOutput(),"red");
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

}
