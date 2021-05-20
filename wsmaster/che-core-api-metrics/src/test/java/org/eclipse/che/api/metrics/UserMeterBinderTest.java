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
package org.eclipse.che.api.metrics;

import static java.lang.Double.NaN;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class UserMeterBinderTest {

  @Mock private UserManager userManager;

  private MeterRegistry registry;

  @BeforeMethod
  public void setUp() {
    registry = new SimpleMeterRegistry();
    UserMeterBinder meterBinder = new UserMeterBinder(userManager);
    meterBinder.bindTo(registry);
  }

  @Test
  public void shouldCollectUserCount() throws Exception {
    when(userManager.getTotalCount()).thenReturn(5L);

    assertEquals(registry.find("che.user.total").gauge().value(), 5.0);
  }

  @Test
  public void shouldCollectNaNUserCountIfExceptionOccursInManager() throws Exception {
    doThrow(ServerException.class).when(userManager).getTotalCount();

    assertEquals(registry.find("che.user.total").gauge().value(), NaN);
  }
}
