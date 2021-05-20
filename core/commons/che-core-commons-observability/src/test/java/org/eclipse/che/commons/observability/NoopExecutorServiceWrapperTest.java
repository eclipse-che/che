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
package org.eclipse.che.commons.observability;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.eclipse.che.commons.schedule.executor.CronExecutorService;
import org.eclipse.che.commons.schedule.executor.CronThreadPoolExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NoopExecutorServiceWrapperTest {
  NoopExecutorServiceWrapper noopExecutorServiceWrapper = new NoopExecutorServiceWrapper();

  @Test
  public void testWrapExecutorService() {
    // given
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    // when
    ExecutorService result =
        noopExecutorServiceWrapper.wrap(
            executorService, NoopExecutorServiceWrapper.class.getName(), "key", "value");
    // then
    Assert.assertSame(result, executorService);
  }

  @Test
  public void testWrapScheduledExecutorService() {
    // given
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    // when
    ScheduledExecutorService result =
        noopExecutorServiceWrapper.wrap(
            executorService, NoopExecutorServiceWrapper.class.getName(), "key", "value");
    // then
    Assert.assertSame(result, executorService);
  }

  @Test
  public void testWrapCronExecutorService() {
    // given
    CronExecutorService executorService = new CronThreadPoolExecutor(1);
    // when
    CronExecutorService result =
        noopExecutorServiceWrapper.wrap(
            executorService, NoopExecutorServiceWrapper.class.getName(), "key", "value");
    // then
    Assert.assertSame(result, executorService);
  }
}
