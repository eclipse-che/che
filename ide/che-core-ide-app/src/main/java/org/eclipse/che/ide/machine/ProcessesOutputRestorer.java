/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.machine;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.function.Consumer;
import org.eclipse.che.agent.exec.shared.dto.DtoWithPid;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.processes.panel.EnvironmentOutputEvent;

/** Restores outputs of running processes on loading IDE. */
@Singleton
public class ProcessesOutputRestorer {

  private EventBus eventBus;
  private ExecAgentCommandManager execAgentCommandManager;

  @Inject
  public ProcessesOutputRestorer(
      EventBus eventBus, AppContext appContext, ExecAgentCommandManager execAgentCommandManager) {
    this.eventBus = eventBus;
    this.execAgentCommandManager = execAgentCommandManager;

    eventBus.addHandler(
        ExecAgentServerRunningEvent.TYPE, event -> restoreLogs(event.getMachineName()));

    // in case workspace is already running
    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event -> {
          final WorkspaceImpl workspace = appContext.getWorkspace();

          if (workspace.getStatus() == RUNNING) {
            final RuntimeImpl runtime = workspace.getRuntime();

            if (runtime != null) {
              runtime.getMachines().values().forEach(m -> restoreLogs(m.getName()));
            }
          }
        });
  }

  private void restoreLogs(String machineName) {
    execAgentCommandManager
        .getProcesses(machineName, false)
        .onSuccess(
            processes -> {
              Consumer<Integer> pidConsumer =
                  pid ->
                      execAgentCommandManager
                          .getProcessLogs(machineName, pid, null, null, 50, 0)
                          .onSuccess(
                              logs ->
                                  logs.forEach(
                                      log -> {
                                        final String fixedLog =
                                            log.getText().replaceAll("\\[STDOUT\\] ", "");

                                        eventBus.fireEvent(
                                            new EnvironmentOutputEvent(fixedLog, machineName));
                                      }));

              processes
                  .stream()
                  .filter(it -> "CheWsAgent".equals(it.getName()))
                  .map(DtoWithPid::getPid)
                  .forEach(pidConsumer);
            });
  }
}
