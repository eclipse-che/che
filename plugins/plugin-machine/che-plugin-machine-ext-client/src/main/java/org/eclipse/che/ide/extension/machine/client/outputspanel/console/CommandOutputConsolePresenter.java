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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.machine.CommandOutputMessageUnmarshaller;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessFinishedEvent;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

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
    private final AsyncRequestFactory    asyncRequestFactory;
    private final CommandImpl            command;
    private final EventBus               eventBus;
    private final Machine                machine;
    private final CommandManager         commandManager;

    private MessageBus     messageBus;
    private int            pid;
    private String         outputChannel;
    private MessageHandler outputHandler;
    private boolean        finished;

    /** Wrap text or not */
    private boolean wrapText = false;

    /** Follow output when printing text */
    private boolean followOutput = true;

    private final List<ActionDelegate> actionDelegates = new ArrayList<>();

    @Inject
    public CommandOutputConsolePresenter(final OutputConsoleView view,
                                         DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                         final MessageBusProvider messageBusProvider,
                                         MachineServiceClient machineServiceClient,
                                         MachineResources resources,
                                         CommandManager commandManager,
                                         MacroProcessor macroProcessor,
                                         EventBus eventBus,
                                         AsyncRequestFactory asyncRequestFactory,
                                         @Assisted CommandImpl command,
                                         @Assisted Machine machine) {
        this.view = view;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.machineServiceClient = machineServiceClient;
        this.resources = resources;
        this.asyncRequestFactory = asyncRequestFactory;
        this.command = command;
        this.machine = machine;
        this.messageBus = messageBusProvider.getMessageBus();
        this.eventBus = eventBus;
        this.commandManager = commandManager;

        view.setDelegate(this);

        final String previewUrl = command.getAttributes().get(PREVIEW_URL_ATTR);
        if (!isNullOrEmpty(previewUrl)) {
            macroProcessor.expandMacros(previewUrl).then(new Operation<String>() {
                @Override
                public void apply(String arg) throws OperationException {
                    view.showPreviewUrl(arg);
                }
            });
        } else {
            view.hidePreview();
        }
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public CommandImpl getCommand() {
        return command;
    }

    @Override
    public String getTitle() {
        return command.getName();
    }

    @Override
    public SVGResource getTitleIcon() {
        return resources.output();
    }

    @Override
    public void listenToOutput(String wsChannel) {
        view.enableStopButton(true);
        view.toggleScrollToEndButton(true);

        outputChannel = wsChannel;
        outputHandler = new SubscriptionHandler<String>(new CommandOutputMessageUnmarshaller(machine.getConfig().getName())) {
            @Override
            protected void onMessageReceived(String result) {
                view.print(result, result.endsWith("\r"));

                for (ActionDelegate actionDelegate : actionDelegates) {
                    actionDelegate.onConsoleOutput(CommandOutputConsolePresenter.this);
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                wsUnsubscribe(outputChannel, this);
            }
        };

        wsSubscribe(outputChannel, outputHandler);
    }

    @Override
    public void attachToProcess(MachineProcessDto process) {
        this.pid = process.getPid();
        view.showCommandLine(process.getCommandLine());
        //try to restore previous log of the process
        final Link link = process.getLink(Constants.LINK_REL_GET_PROCESS_LOGS);
        if (link != null) {
            asyncRequestFactory.createGetRequest(link.getHref()).send(new StringUnmarshaller()).then(
                    new Operation<String>() {
                        @Override
                        public void apply(String arg) throws OperationException {
                            view.print(arg.replaceAll("\\[STDOUT\\] ", ""), false);//logs comes from server side with ""[STDOUT] " in start
                                                                                   //so we will remove it in this brutal way
                            handelProcessEvents(); //start handel  incoming events
                        }
                    }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Log.error(getClass(), arg);
                    //if logs not found will handel incoming events any way
                    handelProcessEvents();
                }
            });
        } else {
            handelProcessEvents();
        }
    }

    private void handelProcessEvents() {
        final Unmarshallable<MachineProcessEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineProcessEvent.class);
        final String processStateChannel = "machine:process:" + machine.getId();
        final MessageHandler handler = new SubscriptionHandler<MachineProcessEvent>(unmarshaller) {
            @Override
            protected void onMessageReceived(MachineProcessEvent result) {
                final int processId = result.getProcessId();
                if (pid != processId) {
                    return;
                }
                switch (result.getEventType()) {
                    case STOPPED:
                        finished = true;
                        view.enableStopButton(false);
                        eventBus.fireEvent(new ProcessFinishedEvent(processId));
                        break;
                    case ERROR:
                        finished = true;
                        view.enableStopButton(false);
                        eventBus.fireEvent(new ProcessFinishedEvent(processId));
                        wsUnsubscribe(processStateChannel, this);
                        wsUnsubscribe(outputChannel, outputHandler);
                        String error = result.getError();
                        if (error == null) {
                            return;
                        }
                        view.print(error, false);
                        break;
                }
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                finished = true;
                view.enableStopButton(false);
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
        return finished;
    }

    @Override
    public void stop() {
        machineServiceClient.stopProcess(machine.getWorkspaceId(),
                                         machine.getId(),
                                         pid);
    }

    @Override
    public void close() {
        actionDelegates.clear();
    }

    @Override
    public void addActionDelegate(ActionDelegate actionDelegate) {
        actionDelegates.add(actionDelegate);
    }

    @Override
    public void reRunProcessButtonClicked() {
        if (isFinished()) {
            commandManager.executeCommand(command, machine);
        } else {
            machineServiceClient.stopProcess(machine.getWorkspaceId(),
                                             machine.getId(),
                                             pid).then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    commandManager.executeCommand(command, machine);
                }
            });
        }
    }

    @Override
    public void stopProcessButtonClicked() {
        stop();
    }

    @Override
    public void clearOutputsButtonClicked() {
        view.clearConsole();
    }

    @Override
    public void downloadOutputsButtonClicked() {
        for (ActionDelegate actionDelegate : actionDelegates) {
            actionDelegate.onDownloadOutput(this);
        }
    }

    @Override
    public void wrapTextButtonClicked() {
        wrapText = !wrapText;
        view.wrapText(wrapText);
        view.toggleWrapTextButton(wrapText);
    }

    @Override
    public void scrollToBottomButtonClicked() {
        followOutput = !followOutput;

        view.toggleScrollToEndButton(followOutput);
        view.enableAutoScroll(followOutput);
    }

    @Override
    public void onOutputScrolled(boolean bottomReached) {
        followOutput = bottomReached;
        view.toggleScrollToEndButton(bottomReached);
    }

    /**
     * Returns the console text.
     *
     * @return
     *          console text
     */
    public String getText() {
        return view.getText();
    }

}
