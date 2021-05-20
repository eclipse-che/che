/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang.execution;

import static org.testng.Assert.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.testng.annotations.Test;

public class ExecutorServiceBuilderTest {
  ThreadFactory threadFactory =
      new ThreadFactoryBuilder()
          .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
          .setNameFormat(ExecutorServiceBuilderTest.class + "-%d")
          .setDaemon(true)
          .build();

  @Test
  public void testBuild() {

    ExecutorService executorService =
        new ExecutorServiceBuilder()
            .corePoolSize(6)
            .maxPoolSize(12)
            .queueCapacity(5000)
            .threadFactory(threadFactory)
            .build();

    assertTrue(executorService instanceof ThreadPoolExecutor);

    ThreadPoolExecutor threadPoolExecutorService = (ThreadPoolExecutor) executorService;
    assertEquals(threadPoolExecutorService.getCorePoolSize(), 6);
    assertEquals(threadPoolExecutorService.getMaximumPoolSize(), 12);
    assertEquals(threadPoolExecutorService.getThreadFactory(), threadFactory);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "maxPoolSize must be greater than corePoolSize")
  public void testSetMaxLowerThenCore() {
    new ExecutorServiceBuilder().corePoolSize(6).maxPoolSize(1).build();
  }
}
