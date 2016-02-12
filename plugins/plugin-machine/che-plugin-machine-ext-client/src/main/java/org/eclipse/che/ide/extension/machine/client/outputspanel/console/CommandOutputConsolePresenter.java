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
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.gwt.client.OutputMessageUnmarshaller;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.processes.event.ProcessFinishedEvent;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.vectomatic.dom.svg.ui.SVGResource;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter.PREVIEW_URL_ATTR;

/**
 * Console for command output.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandOutputConsolePresenter implements CommandOutputConsole, OutputConsoleView.ActionDelegate {

    private final OutputConsoleView      view;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final MachineServiceClient   machineServiceClient;
    private final MachineResources       resources;
    private final CommandConfiguration   commandConfiguration;
    private final EventBus               eventBus;
    private final String                 machineId;

    private MessageBus     messageBus;
    private int            pid;
    private String         outputChannel;
    private MessageHandler outputHandler;
    private boolean        isFinished;

    @Inject
    public CommandOutputConsolePresenter(OutputConsoleView view,
                                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                         final MessageBusProvider messageBusProvider,
                                         MachineServiceClient machineServiceClient,
                                         MachineResources resources,
                                         CommandManager commandManager,
                                         EventBus eventBus,
                                         @Assisted CommandConfiguration commandConfiguration,
                                         @Assisted String machineId) {
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.machineServiceClient = machineServiceClient;
        this.resources = resources;
        this.commandConfiguration = commandConfiguration;
        this.machineId = machineId;
        this.messageBus = messageBusProvider.getMessageBus();
        this.eventBus = eventBus;

        view.setDelegate(this);

        final String previewUrl = commandConfiguration.getAttributes().get(PREVIEW_URL_ATTR);
        if (!isNullOrEmpty(previewUrl)) {
            view.printPreviewUrl(commandManager.substituteProperties(previewUrl));
        } else {
            view.hidePreview();
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public CommandConfiguration getCommand() {
        return commandConfiguration;
    }

    @Override
    public String getTitle() {
        return commandConfiguration.getName();
    }

    @Override
    public SVGResource getTitleIcon() {
        return resources.output();
    }

    @Override
    public void listenToOutput(String wsChannel) {
        outputChannel = wsChannel;
        outputHandler = new SubscriptionHandler<String>(new OutputMessageUnmarshaller()) {
            @Override
            protected void onMessageReceived(String result) {
                view.print(result, result.endsWith("\r"));
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                wsUnsubscribe(outputChannel, this);
            }
        };

        wsSubscribe(outputChannel, outputHandler);
    }

    @Override
    public void attachToProcess(final MachineProcessDto process) {
        this.pid = process.getPid();

        view.printCommandLine(process.getCommandLine());

        final Unmarshallable<MachineProcessEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineProcessEvent.class);
        final String processStateChannel = "machine:process:" + machineId;
        final MessageHandler handler = new SubscriptionHandler<MachineProcessEvent>(unmarshaller) {
            @Override
            protected void onMessageReceived(MachineProcessEvent result) {
                final int processId = result.getProcessId();

                if (pid != processId) {
                    return;
                }

                switch (result.getEventType()) {
                    case STOPPED:
                        isFinished = true;
                        eventBus.fireEvent(new ProcessFinishedEvent());
                    case ERROR:
                        isFinished = true;

                        eventBus.fireEvent(new ProcessFinishedEvent());

                        wsUnsubscribe(processStateChannel, this);
                        wsUnsubscribe(outputChannel, outputHandler);

                        String error = result.getError();
                        if (error == null) {
                            return;
                        }
                        view.print(error, false);
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                isFinished = true;
                wsUnsubscribe(processStateChannel, this);
                wsUnsubscribe(outputChannel, outputHandler);
            }
        };

        wsSubscribe(processStateChannel, handler);
    }

    private void wsSubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBus.subscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    private void wsUnsubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBus.unsubscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void onClose() {
        machineServiceClient.stopProcess(machineId, pid);
    }

}
