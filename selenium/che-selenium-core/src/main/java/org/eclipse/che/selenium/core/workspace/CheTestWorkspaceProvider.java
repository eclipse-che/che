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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;

@Singleton
public class CheTestWorkspaceProvider extends AbstractTestWorkspaceProvider {

  @Inject
  CheTestWorkspaceProvider(
      @Named("che.workspace_pool_size") String poolSize,
      @Named("che.threads") int threads,
      @Named("workspace.default_memory_gb") int defaultMemoryGb,
      DefaultTestUser defaultUser,
      WorkspaceDtoDeserializer workspaceDtoDeserializer,
      TestWorkspaceServiceClient testWorkspaceServiceClient,
      TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory) {
    super(
        poolSize,
        threads,
        defaultMemoryGb,
        defaultUser,
        workspaceDtoDeserializer,
        testWorkspaceServiceClient,
        testWorkspaceServiceClientFactory);
  }

  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  protected void initializePool() {
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
                  new TestWorkspaceImpl(
                      name,
                      defaultUser,
                      defaultMemoryGb,
                      true,
                      workspaceDtoDeserializer.deserializeWorkspaceTemplate(
                          WorkspaceTemplate.DEFAULT),
                      testWorkspaceServiceClient);
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
}
