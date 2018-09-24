/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static java.lang.String.format;
import static java.nio.file.Paths.get;
import static org.eclipse.che.selenium.core.workspace.MemoryMeasure.GB;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
public class CheTestWorkspace implements TestWorkspace {
  private static final Logger LOG = LoggerFactory.getLogger(CheTestWorkspace.class);

  private final String name;
  private final CompletableFuture<Void> future;
  private final TestUser owner;
  private final AtomicReference<String> id;
  private final TestWorkspaceServiceClient workspaceServiceClient;

  public CheTestWorkspace(
      String name,
      TestUser owner,
      int memoryInGB,
      boolean startAfterCreation,
      WorkspaceConfigDto template,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceLogsReader testWorkspaceLogsReader,
      String workspaceLogsDir) {
    if (template == null) {
      throw new IllegalStateException("Workspace template cannot be null");
    }

    this.name = name;
    this.owner = owner;
    this.id = new AtomicReference<>();
    this.workspaceServiceClient = testWorkspaceServiceClient;

    this.future =
        CompletableFuture.runAsync(
            () -> {
              Workspace ws;
              try {
                ws = workspaceServiceClient.createWorkspace(name, memoryInGB, GB, template);
              } catch (Exception e) {
                String errorMessage = format("Workspace name='%s' creation failed.", name);
                LOG.error(errorMessage, e);

                try {
                  workspaceServiceClient.delete(name, owner.getName());
                } catch (Exception e1) {
                  LOG.warn("Failed to remove workspace name='{}' which creation is failed.", name);
                }

                throw new IllegalStateException(errorMessage, e);
              }

              long start = System.currentTimeMillis();
              String workspaceId = ws.getId();
              LOG.info(
                  format("Workspace with name='%s' id='%s' is starting...", name, workspaceId));
              if (startAfterCreation) {
                try {
                  workspaceServiceClient.start(id.updateAndGet((s) -> workspaceId), name, owner);
                } catch (Exception e) {
                  String errorMessage =
                      format("Workspace with name='%s' id='%s' start failed.", name, workspaceId);
                  LOG.error(errorMessage);

                  // try to store the logs of workspace which didn't start
                  Path pathToWorkspaceLogs =
                      get(workspaceLogsDir, "injecting_workspaces_which_did_not_start");
                  testWorkspaceLogsReader.store(workspaceId, pathToWorkspaceLogs, true);

                  try {
                    workspaceServiceClient.delete(name, owner.getName());
                  } catch (Exception e1) {
                    LOG.warn(
                        "Failed to remove workspace with name='{}' id='{}' which start is failed.",
                        name,
                        workspaceId);
                  }

                  throw new IllegalStateException(errorMessage, e);
                }

                LOG.info(
                    "Workspace name='{}' id='{}' started in {} sec.",
                    name,
                    ws.getId(),
                    (System.currentTimeMillis() - start) / 1000);
              }
            });
  }

  @Override
  public void await() throws InterruptedException, ExecutionException {
    future.get();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() throws ExecutionException, InterruptedException {
    return future.thenApply(aVoid -> id.get()).get();
  }

  @Override
  public TestUser getOwner() {
    return owner;
  }

  @PreDestroy
  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  public void delete() {
    try {
      workspaceServiceClient.delete(name, owner.getName());
    } catch (Exception e) {
      throw new RuntimeException(format("Failed to remove workspace '%s'", this), e);
    }
  }
}
