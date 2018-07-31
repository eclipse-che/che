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
package org.eclipse.che.api.fs.server.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks free disk space in mounted partition where projects are located with desired interval. By
 * default, cut off is set to 200 megabytes. If calculated size less than cut off limit, then
 * websocket message is fired.
 *
 * @author Vlad Zhukovskyi
 * @since 6.9.0
 */
@Singleton
public class FreeDiskSpaceChecker {
  private FreeDiskSpaceCheckerCommunication checkerCommunication;
  private RootDirPathProvider rootDirPathProvider;
  private static final Logger LOG = LoggerFactory.getLogger(FreeDiskSpaceChecker.class);
  private static final int CHECK_PERIOD_SEC = 60;
  private static final long BOUNDARY_CUTOFF_BYTES = 209_715_200; // 200MB

  private ScheduledExecutorService execs =
      Executors.newSingleThreadScheduledExecutor(
          new ThreadFactoryBuilder()
              .setNameFormat("FreeDiskSpaceChecker")
              .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
              .setDaemon(true)
              .build());

  @Inject
  public FreeDiskSpaceChecker(
      FreeDiskSpaceCheckerCommunication checkerCommunication,
      RootDirPathProvider rootDirPathProvider) {
    this.checkerCommunication = checkerCommunication;
    this.rootDirPathProvider = rootDirPathProvider;
  }

  @PostConstruct
  private void onStart() {
    execs.scheduleAtFixedRate(
        this::checkFreeDiskSpace, CHECK_PERIOD_SEC, CHECK_PERIOD_SEC, TimeUnit.SECONDS);
    LOG.debug("Free disk space checker initialized");
  }

  @PreDestroy
  private void onStop() {
    execs.shutdown();
    try {
      execs.awaitTermination(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.warn("Failed to terminate free disk space checker");
    }

    LOG.debug("Free disk space checker is stopped");
  }

  private void checkFreeDiskSpace() {
    LOG.debug("Checking free disk space");
    Path root = Paths.get(rootDirPathProvider.get());

    try {
      FileStore store = Files.getFileStore(root);
      long usableSpace = store.getUsableSpace();

      if (usableSpace < BOUNDARY_CUTOFF_BYTES) {
        checkerCommunication.broadcastLowDiskSpaceMessage();
      }

      LOG.debug(
          "Checking free disk space finished. Usable space: {}, boundary cutoff: {}.",
          usableSpace,
          BOUNDARY_CUTOFF_BYTES);
    } catch (IOException ioe) {
      LOG.warn("Failed to load file store for the {}", root);
    }
  }

  public static class FreeDiskSpaceCheckerModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(FreeDiskSpaceChecker.class).asEagerSingleton();
    }
  }
}
