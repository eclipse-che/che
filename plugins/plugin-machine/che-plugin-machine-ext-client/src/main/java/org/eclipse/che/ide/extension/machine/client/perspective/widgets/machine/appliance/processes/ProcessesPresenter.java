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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class defines methods which contains business logic to control machine's processes.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProcessesPresenter implements TabPresenter, ProcessesView.ActionDelegate {

    private final ProcessesView           view;
    private final ExecAgentCommandManager execAgentCommandManager;
    private final DtoFactory              dtoFactory;

    @Inject
    public ProcessesPresenter(ProcessesView view, ExecAgentCommandManager execAgentCommandManager, DtoFactory dtoFactory) {
        this.view = view;
        this.execAgentCommandManager = execAgentCommandManager;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
    }

    /**
     * Gets all process for current machine and adds them to special table on view.
     *
     * @param machineId
     *         machine identifier for which need get processes
     */
    public void showProcesses(@NotNull String workspaceId, @NotNull String machineId) {
        execAgentCommandManager.getProcesses(machineId, false).then(new Operation<List<GetProcessesResponseDto>>() {
            @Override
            public void apply(List<GetProcessesResponseDto> processes) throws OperationException {
                List<MachineProcessDto> machineProcesses = new ArrayList<>(processes.size());

                for (GetProcessesResponseDto process : processes) {
                    int pid = process.getPid();
                    boolean isAlive = process.isAlive();
                    String name = process.getName();
                    String commandLine = process.getCommandLine();
                    String type = process.getType();

                    MachineProcessDto machineProcess = dtoFactory.createDto(MachineProcessDto.class)
                                                                 .withName(name)
                                                                 .withPid(pid)
                                                                 .withCommandLine(commandLine)
                                                                 .withAlive(isAlive)
                                                                 .withType(type);

                    machineProcesses.add(machineProcess);
                }

                view.setProcesses(machineProcesses);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onProcessClicked(@NotNull MachineProcessDto descriptor) {
        //TODO need add implementation
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }
}
