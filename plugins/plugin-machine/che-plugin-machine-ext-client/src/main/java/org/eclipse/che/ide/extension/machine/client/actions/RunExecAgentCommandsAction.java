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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventWithPidDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventWithPidDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentEventManager;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class RunExecAgentCommandsAction extends AbstractPerspectiveAction {

    private final DialogFactory           dialogFactory;
    private final ExecAgentCommandManager jsonRpcExecAgentCommandManager;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final AppContext              appContext;
    private final ExecAgentEventManager   execAgentEventManager;
    private final CommandConsoleFactory   commandConsoleFactory;

    @Inject
    public RunExecAgentCommandsAction(DialogFactory dialogFactory,
                                      ExecAgentCommandManager execAgentCommandManager,
                                      ExecAgentEventManager execAgentEventManager,
                                      CommandConsoleFactory commandConsoleFactory,
                                      ProcessesPanelPresenter processesPanelPresenter,
                                      AppContext appContext) {

        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID), "Exec Agent command", null, null, null);
        this.execAgentEventManager = execAgentEventManager;
        this.commandConsoleFactory = commandConsoleFactory;

        this.dialogFactory = dialogFactory;
        this.jsonRpcExecAgentCommandManager = execAgentCommandManager;
        this.processesPanelPresenter = processesPanelPresenter;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dialogFactory.createInputDialog("Exec agent command line", "Enter command line", new InputCallback() {
            @Override
            public void accepted(String value) {
                jsonRpcExecAgentCommandManager.startProcess("Test command", value, "Custom").then(new Operation<ProcessStartResponseDto>() {
                    @Override
                    public void apply(ProcessStartResponseDto arg) throws OperationException {
                        final String id = appContext.getDevMachine().getId();
                        final DefaultOutputConsole console = (DefaultOutputConsole)commandConsoleFactory.create("Exec Agent Console");
                        processesPanelPresenter.addCommandOutput(id, console);

                        final int pid = arg.getPid();

                        execAgentEventManager.registerProcessStdOutOperation(pid, new Operation<ProcessStdOutEventWithPidDto>() {
                            @Override
                            public void apply(ProcessStdOutEventWithPidDto arg) throws OperationException {
                                final String text = arg.getText();
                                console.printText(text);
                            }
                        });

                        execAgentEventManager.registerProcessStdErrOperation(pid, new Operation<ProcessStdErrEventWithPidDto>() {
                            @Override
                            public void apply(ProcessStdErrEventWithPidDto arg) throws OperationException {
                                final String text = arg.getText();
                                console.printText(text, "red");
                            }
                        });

                        execAgentEventManager.registerProcessStartedOperation(pid, new Operation<ProcessStartedEventWithPidDto>() {
                            @Override
                            public void apply(ProcessStartedEventWithPidDto arg) throws OperationException {
                                final Integer nativePid = arg.getNativePid();
                                final String commandLine = arg.getCommandLine();
                                final String time = arg.getTime();

                                console.printText("Process start:", "green");
                                console.printText("Command line: " + commandLine, "green");
                                console.printText("Native pid: " + nativePid, "green");
                                console.printText("Time: " + time, "green");

                            }
                        });

                        execAgentEventManager.registerProcessDiedOperation(pid, new Operation<ProcessDiedEventWithPidDto>() {
                            @Override
                            public void apply(ProcessDiedEventWithPidDto arg) throws OperationException {
                                final Integer nativePid = arg.getNativePid();
                                final String commandLine = arg.getCommandLine();
                                final String time = arg.getTime();

                                console.printText("Process finish:", "green");
                                console.printText("Command line: " + commandLine, "green");
                                console.printText("Native pid: " + nativePid, "green");
                                console.printText("Time: " + time, "green");
                            }
                        });
                    }
                });


            }
        }, null).show();
    }
}
