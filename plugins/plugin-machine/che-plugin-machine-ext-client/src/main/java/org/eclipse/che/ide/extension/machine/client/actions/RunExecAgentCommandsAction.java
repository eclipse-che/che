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

import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessDiedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStartedEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdErrEventDto;
import org.eclipse.che.api.machine.shared.dto.execagent.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
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
    private final CommandConsoleFactory   commandConsoleFactory;

    @Inject
    public RunExecAgentCommandsAction(DialogFactory dialogFactory,
                                      ExecAgentCommandManager execAgentCommandManager,
                                      CommandConsoleFactory commandConsoleFactory,
                                      ProcessesPanelPresenter processesPanelPresenter,
                                      AppContext appContext) {

        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID), "Exec Agent command", null, null, null);
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
        final InputCallback yesCallback = new InputCallback() {
            @Override
            public void accepted(String value) {
                final String id = appContext.getDevMachine().getId();
                final DefaultOutputConsole console = (DefaultOutputConsole)commandConsoleFactory.create("Exec Agent Console");
                processesPanelPresenter.addCommandOutput(id, console);

                final CommandImpl command = new CommandImpl("Test command", value, "Custom");
                jsonRpcExecAgentCommandManager.startProcess(command)
                                              .thenIfProcessStartedEvent(new ProcessStartedEventOperation(console))
                                              .thenIfProcessDiedEvent(new ProcessDiedEventOperation(console))
                                              .thenIfProcessStdOutEvent(new ProcessStdOutEventOperation(console))
                                              .thenIfProcessStdErrEvent(new ProcessStdErrEventOperation(console));
            }
        };

        dialogFactory.createInputDialog("Exec agent command line", "Enter command line", yesCallback, null).show();
    }

    private static class ProcessStartedEventOperation implements Operation<ProcessStartedEventDto> {
        private final DefaultOutputConsole console;

        public ProcessStartedEventOperation(DefaultOutputConsole console) {
            this.console = console;
        }

        @Override
        public void apply(ProcessStartedEventDto arg) throws OperationException {
            final Integer nativePid = arg.getNativePid();
            final String commandLine = arg.getCommandLine();
            final String time = arg.getTime();

            console.printText("Process start:", "green");
            console.printText("Command line: " + commandLine, "green");
            console.printText("Native pid: " + nativePid, "green");
            console.printText("Time: " + time, "green");
        }
    }

    private static class ProcessDiedEventOperation implements Operation<ProcessDiedEventDto> {
        private final DefaultOutputConsole console;

        public ProcessDiedEventOperation(DefaultOutputConsole console) {
            this.console = console;
        }

        @Override
        public void apply(ProcessDiedEventDto arg) throws OperationException {
            final Integer nativePid = arg.getNativePid();
            final String commandLine = arg.getCommandLine();
            final String time = arg.getTime();

            console.printText("Process finish:", "green");
            console.printText("Command line: " + commandLine, "green");
            console.printText("Native pid: " + nativePid, "green");
            console.printText("Time: " + time, "green");
        }
    }

    private static class ProcessStdOutEventOperation implements Operation<ProcessStdOutEventDto> {
        private final DefaultOutputConsole console;

        public ProcessStdOutEventOperation(DefaultOutputConsole console) {
            this.console = console;
        }

        @Override
        public void apply(ProcessStdOutEventDto arg) throws OperationException {
            final String text = arg.getText();
            console.printText(text);
        }
    }

    private static class ProcessStdErrEventOperation implements Operation<ProcessStdErrEventDto> {
        private final DefaultOutputConsole console;

        public ProcessStdErrEventOperation(DefaultOutputConsole console) {
            this.console = console;
        }

        @Override
        public void apply(ProcessStdErrEventDto arg) throws OperationException {
            final String text = arg.getText();
            console.printText(text, "red");
        }
    }
}
