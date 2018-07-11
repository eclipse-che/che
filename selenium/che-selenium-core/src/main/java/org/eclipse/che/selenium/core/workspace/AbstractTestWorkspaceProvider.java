/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TestWorkspaceProvider} implementation containing workspace pool.
 *
 * @author Anatolii Bazko
 */
public abstract class AbstractTestWorkspaceProvider implements TestWorkspaceProvider {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractTestWorkspaceProvider.class);

  private static final String AUTO = "auto";
  private final TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory;

  protected final int poolSize;
  protected final DefaultTestUser defaultUser;
  protected final int defaultMemoryGb;
  protected final TestWorkspaceServiceClient testWorkspaceServiceClient;
  protected final WorkspaceDtoDeserializer workspaceDtoDeserializer;
  protected ArrayBlockingQueue<TestWorkspace> testWorkspaceQueue;
  protected ScheduledExecutorService executor;

  protected AbstractTestWorkspaceProvider(
      String poolSize,
      int threads,
      int defaultMemoryGb,
      DefaultTestUser defaultUser,
      WorkspaceDtoDeserializer workspaceDtoDeserializer,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory) {
    this.defaultUser = defaultUser;
    this.defaultMemoryGb = defaultMemoryGb;
    this.testWorkspaceServiceClient = testWorkspaceServiceClient;
    this.testWorkspaceServiceClientFactory = testWorkspaceServiceClientFactory;
    this.workspaceDtoDeserializer = workspaceDtoDeserializer;

    if (poolSize.equals(AUTO)) {
      this.poolSize = (threads - 1) / 2 + 1;
    } else {
      this.poolSize = Integer.parseInt(poolSize);
    }

    if (this.poolSize > 0) {
      initializePool();
    }
  }

  @Override
  public TestWorkspace createWorkspace(
      TestUser owner, int memoryGB, String template, boolean startAfterCreation) throws Exception {
    if (poolSize > 0 && hasDefaultValues(owner, memoryGB, template, startAfterCreation)) {
      return doGetWorkspaceFromPool();
    }

    return new TestWorkspaceImpl(
        generateName(),
        owner,
        memoryGB,
        startAfterCreation,
        workspaceDtoDeserializer.deserializeWorkspaceTemplate(template),
        testWorkspaceServiceClientFactory.create(owner));
  }

  private boolean hasDefaultValues(
      TestUser testUser, int memoryGB, String template, boolean startAfterCreation) {
    return memoryGB == defaultMemoryGb
        && WorkspaceTemplate.DEFAULT.equals(template)
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

  protected abstract void initializePool();

  protected String generateName() {
    return NameGenerator.generate("workspace", 6);
  }
}
