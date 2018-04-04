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
package org.eclipse.che.plugin.maven.server.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class MavenExecutorService {

  private static final Logger LOG = LoggerFactory.getLogger(MavenExecutorService.class);

  private final ExecutorService service;

  public MavenExecutorService() {
    ThreadFactory threadFactory =
        new ThreadFactoryBuilder()
            .setNameFormat("Maven Executor - %d")
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .build();
    service = Executors.newFixedThreadPool(1, threadFactory);
  }

  public void submit(Runnable task) {
    service.execute(task);
  }

  @PreDestroy
  public void shutdown() throws InterruptedException {
    // Tell threads to finish off.
    service.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
        service.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!service.awaitTermination(60, TimeUnit.SECONDS)) LOG.warn("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      service.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
