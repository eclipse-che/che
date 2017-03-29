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
package org.eclipse.che.ide.workspace;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;

import java.util.function.Consumer;

/**
 * Restores machine logs using exec agent API
 */
@Singleton
public class MachineLogsRestorer {
    private final EventBus                eventBus;
    private final ExecAgentCommandManager execAgentCommandManager;

    @Inject
    MachineLogsRestorer(EventBus eventBus, ExecAgentCommandManager execAgentCommandManager) {
        this.eventBus = eventBus;
        this.execAgentCommandManager = execAgentCommandManager;
    }

    public void restore(Machine machine) {
        if (machine == null) {
            return;
        }

        String machineId = machine.getId();

        execAgentCommandManager.getProcesses(machineId, false)
                               .then(processes -> {
                                   Consumer<Integer> pidConsumer = pid -> execAgentCommandManager
                                           .getProcessLogs(machineId, pid, null, null, 50, 0)
                                           .then(logs -> {
                                               logs.forEach(log -> {
                                                   String fixedLog = log.getText().replaceAll("\\[STDOUT\\] ", "");
                                                   String machineName = machine.getConfig().getName();
                                                   eventBus.fireEvent(new EnvironmentOutputEvent(fixedLog, machineName));
                                               });
                                           });

                                   processes.stream()
                                            .filter(it -> "CheWsAgent".equals(it.getName()))
                                            .map(DtoWithPid::getPid)
                                            .forEach(pidConsumer);
                               });
    }

}
