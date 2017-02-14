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
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessKillResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessSubscribeResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessFinishedEvent;
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

    private final OutputConsoleView       view;
    private final MachineResources        resources;
    private final CommandImpl             command;
    private final EventBus                eventBus;
    private final Machine                 machine;
    private final CommandManager          commandManager;
    private final ExecAgentCommandManager execAgentCommandManager;

    private int            pid;
    private boolean        finished;

    /** Wrap text or not */
    private boolean wrapText = false;

    /** Follow output when printing text */
    private boolean followOutput = true;

    private final List<ActionDelegate> actionDelegates = new ArrayList<>();

    @Inject
    public CommandOutputConsolePresenter(final OutputConsoleView view,
                                         MachineResources resources,
                                         CommandManager commandManager,
                                         MacroProcessor macroProcessor,
                                         EventBus eventBus,
                                         ExecAgentCommandManager execAgentCommandManager,
                                         @Assisted CommandImpl command,
                                         @Assisted Machine machine) {
        this.view = view;
        this.resources = resources;
        this.execAgentCommandManager = execAgentCommandManager;
        this.command = command;
        this.machine = machine;
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

        view.showCommandLine(command.getCommandLine());
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
    }

    @Override
    public void attachToProcess(MachineProcessDto process) {
    }

    @Override
    public Operation<ProcessStdErrEventDto> getStdErrOperation() {
        return new Operation<ProcessStdErrEventDto>() {
            @Override
            public void apply(ProcessStdErrEventDto event) throws OperationException {
                String text = event.getText();
                boolean carriageReturn = text.endsWith("\r");
                String color = "red";
                view.print(text, carriageReturn, color);

                for (ActionDelegate actionDelegate : actionDelegates) {
                    actionDelegate.onConsoleOutput(CommandOutputConsolePresenter.this);
                }
            }
        };
    }

    @Override
    public Operation<ProcessStdOutEventDto> getStdOutOperation() {
        return new Operation<ProcessStdOutEventDto>() {
            @Override
            public void apply(ProcessStdOutEventDto event) throws OperationException {
                String stdOutMessage = event.getText();
                boolean carriageReturn = stdOutMessage.endsWith("\r");
                view.print(stdOutMessage, carriageReturn);

                for (ActionDelegate actionDelegate : actionDelegates) {
                    actionDelegate.onConsoleOutput(CommandOutputConsolePresenter.this);
                }
            }
        };

    }

    @Override
    public Operation<ProcessStartedEventDto> getProcessStartedOperation() {
        return new Operation<ProcessStartedEventDto>() {
            @Override
            public void apply(ProcessStartedEventDto event) throws OperationException {
                finished = false;
                view.enableStopButton(true);
                view.toggleScrollToEndButton(true);

                pid = event.getPid();
            }
        };
    }

    @Override
    public Operation<ProcessDiedEventDto> getProcessDiedOperation() {
        return new Operation<ProcessDiedEventDto>() {
            @Override
            public void apply(ProcessDiedEventDto event) throws OperationException {
                finished = true;
                view.enableStopButton(false);
                view.toggleScrollToEndButton(false);

                eventBus.fireEvent(new ProcessFinishedEvent(pid));
            }
        };
    }

    @Override
    public Operation<ProcessSubscribeResponseDto> getProcessSubscribeOperation() {
        return new Operation<ProcessSubscribeResponseDto>() {
            @Override
            public void apply(ProcessSubscribeResponseDto process) throws OperationException {
                pid = process.getPid();
            }
        };
    }

    @Override
    public void printOutput(String output) {
        view.print(output.replaceAll("\\[STDOUT\\] ", ""), false);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void stop() {
        execAgentCommandManager.killProcess(machine.getId(), pid);
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
            execAgentCommandManager.killProcess(machine.getId(), pid).then(new Operation<ProcessKillResponseDto>() {
                @Override
                public void apply(ProcessKillResponseDto arg) throws OperationException {
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
