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
package org.eclipse.che.ide.ext.java.client.dependenciesupdater;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.ExtServerStateHandler;
import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEvent;
import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEventHandler;
import org.eclipse.che.ide.extension.machine.client.outputspanel.OutputsContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.StringUnmarshallerWS;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The class contains business logic which allows output logs from different channels into different consoles.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class LogsOutputHandler implements DependencyUpdatedEventHandler, ExtServerStateHandler {

    private final CommandConsoleFactory          consoleFactory;
    private final OutputsContainerPresenter      outputsContainerPresenter;
    private final Map<String, ChannelParameters> channelParameters;
    private final MessageBusProvider             messageBusProvider;

    private MessageBus                  messageBus;
    private SubscriptionHandler<String> outputHandler;

    @Inject
    public LogsOutputHandler(CommandConsoleFactory consoleFactory,
                             OutputsContainerPresenter outputsContainerPresenter,
                             EventBus eventBus,
                             MessageBusProvider messageBusProvider) {
        this.consoleFactory = consoleFactory;
        this.outputsContainerPresenter = outputsContainerPresenter;
        this.messageBus = messageBusProvider.getMachineMessageBus();
        this.channelParameters = new HashMap<>();
        this.messageBusProvider = messageBusProvider;

        eventBus.addHandler(DependencyUpdatedEvent.TYPE, this);
        eventBus.addHandler(ExtServerStateEvent.TYPE, this);
    }

    @Override
    public void onExtServerStarted(ExtServerStateEvent event) {
        messageBus = messageBusProvider.getMachineMessageBus();
    }

    @Override
    public void onExtServerStopped(ExtServerStateEvent event) {
    }

    @Override
    public void onDependencyUpdated(DependencyUpdatedEvent event) {
        String updatedChannel = event.getChannel();

        channelParameters.get(updatedChannel).outputEnded();

        flushBuffer(updatedChannel);

        try {
            if (outputHandler == null) {
                return;
            }

            messageBus.unsubscribe(updatedChannel, outputHandler);
        } catch (WebSocketException exception) {
            Log.error(getClass(), exception);
        }
    }

    /**
     * Subscribes to web socket via special channel to get logs.
     *
     * @param channel
     *         channel id which will be used to subscribe to output
     * @param tabName
     *         name of tab in which output will be printed
     */
    public void subscribeToOutput(final String channel, String tabName) {
        DefaultOutputConsole console = (DefaultOutputConsole)consoleFactory.create(tabName);
        outputsContainerPresenter.addConsole(console);

        channelParameters.put(channel, ChannelParameters.of(console, new LinkedList<String>()));

        outputHandler = new SubscriptionHandler<String>(new StringUnmarshallerWS()) {
            @Override
            protected void onMessageReceived(String logs) {
                List<String> lines = StringUtils.split(logs, "\n");

                channelParameters.get(channel).addAllLines(lines);

                if (channelParameters.get(channel).isOutputEnded()) {
                    flushBuffer(channel);

                    return;
                }

                printOutput(channel);
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                Log.error(getClass(), exception);
            }
        };

        try {
            messageBus.subscribe(channel, outputHandler);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void flushBuffer(String channel) {
        LinkedList<String> output = channelParameters.get(channel).getOutput();

        DefaultOutputConsole console = channelParameters.get(channel).getConsole();

        while (!output.isEmpty()) {
            String toOutput = output.pop();

            console.printText(toOutput, toOutput.contains("KB"));
        }
    }

    private void printOutput(final String channel) {
        final LinkedList<String> output = channelParameters.get(channel).getOutput();

        final DefaultOutputConsole console = channelParameters.get(channel).getConsole();

        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (output.isEmpty()) {
                    return false;
                }

                String toOutput = output.pop();

                console.printText(toOutput, toOutput.contains("KB"));

                return !channelParameters.get(channel).isOutputEnded();
            }
        }, 100);
    }
}
