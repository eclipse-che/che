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
package org.eclipse.che.ide.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

import java.util.function.Consumer;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

/** Restores outputs of running processes on loading IDE. */
@Singleton
public class ProcessesOutputRestorer {

    private EventBus                eventBus;
    private ExecAgentCommandManager execAgentCommandManager;

    @Inject
    public ProcessesOutputRestorer(EventBus eventBus, AppContext appContext, ExecAgentCommandManager execAgentCommandManager) {
        this.eventBus = eventBus;
        this.execAgentCommandManager = execAgentCommandManager;

        eventBus.addHandler(ExecAgentServerRunningEvent.TYPE, event -> restoreLogs(event.getMachineName()));

        // in case workspace is already running
        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, event -> {
            final WorkspaceImpl workspace = appContext.getWorkspace();

            if (workspace.getStatus() == RUNNING) {
                final RuntimeImpl runtime = workspace.getRuntime();

                if (runtime != null) {
                    runtime.getMachines().values()
                           .forEach(m -> restoreLogs(m.getName()));
                }
            }
        });
    }

    private void restoreLogs(String machineName) {
        execAgentCommandManager.getProcesses(machineName, false)
                               .onSuccess(processes -> {
                                   Consumer<Integer> pidConsumer = pid -> execAgentCommandManager
                                           .getProcessLogs(machineName, pid, null, null, 50, 0)
                                           .onSuccess(logs -> logs.forEach(log -> {
                                               final String fixedLog = log.getText().replaceAll("\\[STDOUT\\] ", "");

                                               eventBus.fireEvent(new EnvironmentOutputEvent(fixedLog, machineName));
                                           }));

                                   processes.stream()
                                            .filter(it -> "CheWsAgent".equals(it.getName()))
                                            .map(DtoWithPid::getPid)
                                            .forEach(pidConsumer);
                               });
    }
}
