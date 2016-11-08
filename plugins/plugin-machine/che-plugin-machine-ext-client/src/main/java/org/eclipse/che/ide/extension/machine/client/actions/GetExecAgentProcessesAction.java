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

import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class GetExecAgentProcessesAction extends AbstractPerspectiveAction {

    private final ExecAgentCommandManager jsonRpcExecAgentCommandManager;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final AppContext              appContext;
    private final CommandConsoleFactory   commandConsoleFactory;

    @Inject
    public GetExecAgentProcessesAction(ExecAgentCommandManager execAgentCommandManager,
                                       CommandConsoleFactory commandConsoleFactory,
                                       ProcessesPanelPresenter processesPanelPresenter,
                                       AppContext appContext) {

        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID), "Exec Agent processes", null, null, null);
        this.commandConsoleFactory = commandConsoleFactory;

        this.jsonRpcExecAgentCommandManager = execAgentCommandManager;
        this.processesPanelPresenter = processesPanelPresenter;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String id = appContext.getDevMachine().getId();
        final DefaultOutputConsole console = (DefaultOutputConsole)commandConsoleFactory.create("Exec Agent processes list");
        processesPanelPresenter.addCommandOutput(id, console);

        final boolean all = true;
        jsonRpcExecAgentCommandManager.getProcesses(all).then(new Operation<List<GetProcessesResponseDto>>() {
            @Override
            public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                for (GetProcessesResponseDto dto : arg) {
                    final Integer pid = dto.getPid();
                    final String name = dto.getName();
                    final String commandLine = dto.getCommandLine();
                    final String type = dto.getType();
                    final Boolean alive = dto.isAlive();

                    console.printText("Process name: " + name);
                    console.printText("Process type: " + type);
                    console.printText("Command line: " + commandLine);
                    console.printText("Process pid: " + pid);
                    console.printText("Process is alive: " + alive, alive ? "green" : "red");
                }
            }
        });
    }
}
