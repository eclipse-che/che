/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.testing.shared.TestingOutput;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.DefaultOutputConsole;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;

import static org.eclipse.che.api.testing.shared.Constants.TESTING_OUTPUT_CHANNEL_NAME;;

/**
 * Handler which receives messages from the Testing tools.
 *
 * @author David Festal
 */
@Singleton
public class TestingHandler {

    private final EventBus                eventBus;
    private final DtoFactory              factory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final CommandConsoleFactory   commandConsoleFactory;
    private final AppContext              appContext;

    @Inject
    public TestingHandler(EventBus eventBus,
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
                        handleTestingOutput(messageBus);
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });
    }

    private void handleTestingOutput(final MessageBus messageBus) {
        final DefaultOutputConsole outputConsole = (DefaultOutputConsole)commandConsoleFactory.create("Tests");
        try {
            messageBus.subscribe(TESTING_OUTPUT_CHANNEL_NAME, new MessageHandler() {
                @Override
                public void onMessage(String message) {
                    TestingOutput archetypeOutput = factory.createDtoFromJson(message, TestingOutput.class);
                    switch (archetypeOutput.getState()) {
                        case SESSION_START:
                            processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), outputConsole);
                            outputConsole.clearOutputsButtonClicked();
                        case DETAIL:
                            outputConsole.printText(archetypeOutput.getOutput());
                            break;
                        case SUCCESS:
                            outputConsole.printText(archetypeOutput.getOutput(), "green");
                            break;
                        case ERROR:
                            outputConsole.printText(archetypeOutput.getOutput(), "red");
                            break;
                        case FAILURE:
                            outputConsole.printText(archetypeOutput.getOutput(), "darkred");
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
