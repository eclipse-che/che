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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
public class CheTestWorkspace implements TestWorkspace {
  private static final Logger LOG = LoggerFactory.getLogger(CheTestWorkspace.class);

  private final String name;
  private final TestUser owner;
  private final AtomicReference<String> id = new AtomicReference<>();
  private final TestWorkspaceServiceClient testWorkspaceServiceClient;
  private CompletableFuture<Void> future;

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
    this.testWorkspaceServiceClient = testWorkspaceServiceClient;

    this.future =
        CompletableFuture.runAsync(
            () -> {
              Workspace ws;
              try {
                ws = testWorkspaceServiceClient.createWorkspace(name, memoryInGB, GB, template);
              } catch (Exception e) {
                String errorMessage = format("Workspace name='%s' creation failed.", name);
                LOG.error(errorMessage, e);

                try {
                  testWorkspaceServiceClient.delete(name, owner.getName());
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
                  testWorkspaceServiceClient.start(
                      id.updateAndGet((s) -> workspaceId), name, owner);
                } catch (Exception e) {
                  String errorMessage =
                      format("Workspace with name='%s' id='%s' start failed.", name, workspaceId);
                  LOG.error(errorMessage);

                  // try to store the logs of workspace which didn't start
                  Path pathToWorkspaceLogs =
                      get(workspaceLogsDir, "injecting_workspaces_which_did_not_start");
                  testWorkspaceLogsReader.store(workspaceId, pathToWorkspaceLogs, true);

                  try {
                    testWorkspaceServiceClient.delete(name, owner.getName());
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

  public CheTestWorkspace(
      String name, TestUser owner, TestWorkspaceServiceClient testWorkspaceServiceClient) {
    this.testWorkspaceServiceClient = testWorkspaceServiceClient;
    this.name = name;
    this.owner = owner;
  }

  @Override
  public void await() throws InterruptedException, ExecutionException {
    if (future == null) {
      return;
    }

    future.get();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  @Nullable
  public String getId() throws ExecutionException, InterruptedException {
    if (future == null) {
      try {
        Workspace wsConfig = testWorkspaceServiceClient.getByName(name, owner.getName());
        id.set(wsConfig.getId());
        return id.get();
      } catch (Exception e) {
        String errorMessage =
            format("Failed to obtain id of workspace name='%s' owner='%s'", name, owner.getName());

        LOG.warn(errorMessage, e);

        return null;
      }
    }

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
      testWorkspaceServiceClient.delete(name, owner.getName());
    } catch (Exception e) {
      throw new RuntimeException(format("Failed to remove workspace '%s'", this), e);
    }
  }
}
