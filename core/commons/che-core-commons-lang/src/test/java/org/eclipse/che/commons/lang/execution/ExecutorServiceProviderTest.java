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
package org.eclipse.che.commons.lang.execution;

import static org.testng.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExecutorServiceProviderTest {
  @Test
  public void shouldProvideExecutorService() {
    // given
    ExecutorServiceProvider executorServiceProvider = new ExecutorServiceProvider(10, 10, 10);
    // when
    ExecutorService executorService = executorServiceProvider.get();
    // then
    Assert.assertNotNull(executorService);
  }

  @Test
  public void shouldProvideExecutorServiceWithSynchronousQueue() {
    // given
    ExecutorServiceProvider executorServiceProvider = new ExecutorServiceProvider(10, 10, 0);

    // when
    ExecutorService executorService = executorServiceProvider.get();

    // then
    assertTrue(executorService instanceof ThreadPoolExecutor);
    assertTrue(((ThreadPoolExecutor) executorService).getQueue() instanceof SynchronousQueue);
  }

  @Test
  public void shouldProvideExecutorServiceWithLinkedBlockingQueue() {
    // given
    ExecutorServiceProvider executorServiceProvider = new ExecutorServiceProvider(10, 10, 10);

    // when
    ExecutorService executorService = executorServiceProvider.get();

    // then
    assertTrue(executorService instanceof ThreadPoolExecutor);
    assertTrue(((ThreadPoolExecutor) executorService).getQueue() instanceof LinkedBlockingQueue);
  }

  @Test
  public void shouldProvideExecutorServiceWithCorePoolSize() {
    // given
    ExecutorServiceProvider executorServiceProvider =
        new ExecutorServiceProvider(100500, 100501, 10);

    // when
    ExecutorService executorService = executorServiceProvider.get();

    // then
    assertTrue(executorService instanceof ThreadPoolExecutor);
    assertEquals(((ThreadPoolExecutor) executorService).getCorePoolSize(), 100500);
  }

  @Test
  public void shouldProvideExecutorServiceWithMaxPoolSize() {
    // given
    ExecutorServiceProvider executorServiceProvider = new ExecutorServiceProvider(10, 28, 10);

    // when
    ExecutorService executorService = executorServiceProvider.get();

    // then
    assertTrue(executorService instanceof ThreadPoolExecutor);
    assertEquals(((ThreadPoolExecutor) executorService).getMaximumPoolSize(), 28);
  }
}
