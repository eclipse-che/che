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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CheTestWorkspaceProvider implements TestWorkspaceProvider {
  private static final Logger LOG = LoggerFactory.getLogger(CheTestWorkspaceProvider.class);
  private static final String AUTO = "auto";

  private final DefaultTestUser defaultUser;
  private final int defaultMemoryGb;
  private final int poolSize;
  private final TestWorkspaceServiceClient testWorkspaceServiceClient;
  private final TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory;
  private final TestWorkspaceLogsReader testWorkspaceLogsReader;
  private final String workspaceLogsDir;
  private final WorkspaceDtoDeserializer workspaceDtoDeserializer;

  private ScheduledExecutorService executor;
  private ArrayBlockingQueue<TestWorkspace> testWorkspaceQueue;

  @Inject
  CheTestWorkspaceProvider(
      @Named("che.workspace_pool_size") String poolSize,
      @Named("che.threads") int threads,
      @Named("workspace.default_memory_gb") int defaultMemoryGb,
      @Named("tests.workspacelogs_dir") String workspaceLogsDir,
      DefaultTestUser defaultUser,
      WorkspaceDtoDeserializer workspaceDtoDeserializer,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory,
      TestWorkspaceLogsReader testWorkspaceLogsReader) {
    this.defaultMemoryGb = defaultMemoryGb;
    this.defaultUser = defaultUser;
    this.workspaceDtoDeserializer = workspaceDtoDeserializer;
    this.testWorkspaceServiceClient = testWorkspaceServiceClient;
    this.testWorkspaceServiceClientFactory = testWorkspaceServiceClientFactory;
    this.testWorkspaceLogsReader = testWorkspaceLogsReader;
    this.workspaceLogsDir = workspaceLogsDir;

    if (poolSize.equals(AUTO)) {
      this.poolSize = (threads - 1) / 2 + 1;
    } else {
      this.poolSize = Integer.parseInt(poolSize);
    }

    if (this.poolSize > 0) {
      initializePool();
    }
  }

  public TestWorkspace getWorkspace(String workspaceName, TestUser owner) {
    return new CheTestWorkspace(
        workspaceName, owner, testWorkspaceServiceClientFactory.create(owner));
  }

  @Override
  public TestWorkspace createWorkspace(
      TestUser owner, int memoryGB, WorkspaceTemplate template, boolean startAfterCreation)
      throws Exception {
    if (poolSize > 0 && hasDefaultValues(owner, memoryGB, template, startAfterCreation)) {
      return doGetWorkspaceFromPool();
    }

    return createWorkspace(
        generateName(),
        owner,
        memoryGB,
        startAfterCreation,
        workspaceDtoDeserializer.deserializeWorkspaceTemplate(template));
  }

  public TestWorkspace createWorkspace(
      String name,
      TestUser owner,
      int memoryGB,
      boolean startAfterCreation,
      WorkspaceConfigDto config) {
    return new CheTestWorkspace(
        name,
        owner,
        memoryGB,
        startAfterCreation,
        config,
        testWorkspaceServiceClientFactory.create(owner),
        testWorkspaceLogsReader,
        workspaceLogsDir);
  }

  @Override
  public void shutdown() {
    if (executor == null) {
      return;
    }

    boolean isInterrupted = false;
    if (!executor.isShutdown()) {
      executor.shutdown();
      try {
        LOG.info("Shutdown workspace threads pool, wait 30s to stop normally");
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
          executor.shutdownNow();
          LOG.info("Interrupt workspace threads pool, wait 60s to stop");
          if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            LOG.error("Couldn't shutdown workspace threads pool");
          }
        }
      } catch (InterruptedException x) {
        isInterrupted = true;
        if (!executor.isShutdown()) {
          LOG.warn("Unable to terminate executor service");
        }
      }
      LOG.info("Workspace threads pool is terminated");
    }

    LOG.info("Destroy remained workspaces: {}.", extractWorkspaceInfo());
    testWorkspaceQueue.parallelStream().forEach(TestWorkspace::delete);

    if (isInterrupted) {
      Thread.currentThread().interrupt();
    }
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private void initializePool() {
    LOG.info("Initialize workspace pool with {} entries.", poolSize);
    testWorkspaceQueue = new ArrayBlockingQueue<>(poolSize);
    executor =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("WorkspaceInitializer-%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .build());

    executor.scheduleWithFixedDelay(
        () -> {
          while (testWorkspaceQueue.remainingCapacity() != 0) {
            String name = generateName();
            TestWorkspace testWorkspace;
            try {
              testWorkspace =
                  new CheTestWorkspace(
                      name,
                      defaultUser,
                      defaultMemoryGb,
                      true,
                      workspaceDtoDeserializer.deserializeWorkspaceTemplate(
                          WorkspaceTemplate.DEFAULT),
                      testWorkspaceServiceClient,
                      testWorkspaceLogsReader,
                      workspaceLogsDir);
            } catch (Exception e) {
              // scheduled executor service doesn't log any exceptions, so log possible exception
              // here
              LOG.error(e.getLocalizedMessage(), e);
              throw e;
            }
            try {
              if (!testWorkspaceQueue.offer(testWorkspace)) {
                LOG.warn("Workspace {} can't be added into the pool and will be destroyed.", name);
                testWorkspace.delete();
              }
            } catch (Exception e) {
              LOG.warn(
                  "Workspace {} can't be added into the pool and will be destroyed because of: {}",
                  name,
                  e.getMessage());
              testWorkspace.delete();
            }
          }
        },
        0,
        100,
        TimeUnit.MILLISECONDS);
  }

  private boolean hasDefaultValues(
      TestUser testUser, int memoryGB, WorkspaceTemplate template, boolean startAfterCreation) {
    return memoryGB == defaultMemoryGb
        && WorkspaceTemplate.DEFAULT == template
        && testUser.getEmail().equals(defaultUser.getEmail())
        && startAfterCreation;
  }

  private TestWorkspace doGetWorkspaceFromPool() throws Exception {
    try {
      // insure workspace is running
      TestWorkspace testWorkspace = testWorkspaceQueue.take();
      WorkspaceStatus testWorkspaceStatus =
          testWorkspaceServiceClient.getById(testWorkspace.getId()).getStatus();

      if (testWorkspaceStatus != WorkspaceStatus.RUNNING) {
        testWorkspaceServiceClient.start(
            testWorkspace.getId(), testWorkspace.getName(), testWorkspace.getOwner());
      }

      return testWorkspace;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Retrieving a new workspace has been interrupted.", e);
    }
  }

  private List<String> extractWorkspaceInfo() {
    return testWorkspaceQueue
        .stream()
        .map(
            s -> {
              try {
                return s.getName();
              } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Error of getting name of workspace.", e);
              }
            })
        .collect(Collectors.toList());
  }

  private String generateName() {
    return NameGenerator.generate("workspace", 6);
  }
}
