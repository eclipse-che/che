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
package org.eclipse.che.core.metrics;

import static org.testng.Assert.*;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Collection;
import org.testng.annotations.Test;

public class FileStoresMeterTest {

  @Test
  public void shouldBindFileStores() {
    MeterRegistry registry = new SimpleMeterRegistry();

    new FileStoresMeterBinder().bindTo(registry);
    Collection<Gauge> df = registry.get("disk.free").gauges();

    assertNotNull(df);
    assertTrue(df.size() > 0);
    assertEquals(df.size(), registry.get("disk.total").gauges().size());
    assertEquals(df.size(), registry.get("disk.usable").gauges().size());
  }
}
