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

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEvent;
import org.eclipse.che.ide.extension.machine.client.outputspanel.OutputsContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class LogsOutputHandlerTest {

    private static final String CHANNEL  = "Channel";
    private static final String TAB_NAME = "tab_name";

    //constructor mocks
    @Mock
    private CommandConsoleFactory     consoleFactory;
    @Mock
    private OutputsContainerPresenter outputsContainerPresenter;
    @Mock
    private MessageBusProvider        messageBusProvider;
    @Mock
    private EventBus                  eventBus;

    //additional mocks
    @Mock
    private MessageBus             messageBus;
    @Mock
    private DefaultOutputConsole   console;
    @Mock
    private DependencyUpdatedEvent event;

    private LogsOutputHandler logsOutputHandler;

    @Before
    public void setUp() {
        when(messageBusProvider.getMachineMessageBus()).thenReturn(messageBus);
        when(consoleFactory.create(TAB_NAME)).thenReturn(console);
        when(event.getChannel()).thenReturn(CHANNEL);

        logsOutputHandler = new LogsOutputHandler(consoleFactory, outputsContainerPresenter, eventBus, messageBusProvider);
    }

    @Test
    public void dependencyUpdatedEventShouldBeAdded() {
        verify(eventBus).addHandler(DependencyUpdatedEvent.TYPE, logsOutputHandler);
    }

    @Test
    public void dependencyUpdatedEventShouldBeCaughtAndMessageBusShouldBeUnsubscribed() throws Exception {
        logsOutputHandler.subscribeToOutput(CHANNEL, TAB_NAME);

        logsOutputHandler.onDependencyUpdated(event);

        verify(event).getChannel();
        verify(messageBus).unsubscribe(eq(CHANNEL), Matchers.<MessageHandler>anyObject());
    }

    @Test
    public void handlerShouldBeSubscribedToOutput() throws Exception {
        logsOutputHandler.subscribeToOutput(CHANNEL, TAB_NAME);

        verify(consoleFactory).create(TAB_NAME);
        verify(outputsContainerPresenter).addConsole(console);
        verify(messageBus).subscribe(eq(CHANNEL), Matchers.<SubscriptionHandler<String>>anyObject());
    }
}