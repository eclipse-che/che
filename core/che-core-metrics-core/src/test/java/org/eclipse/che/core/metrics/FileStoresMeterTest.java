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
package org.eclipse.che.core.metrics;

import static org.testng.Assert.*;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class FileStoresMeterTest {

  private static final Logger LOG = LoggerFactory.getLogger(FileStoresMeterTest.class);

  @Test
  public void shouldBindFileStores() {
    MeterRegistry registry = new SimpleMeterRegistry();

    new FileStoresMeterBinder().bindTo(registry);
    Collection<Gauge> dd = registry.get("disk.free").gauges();

    assertNotNull(dd);
    assertEquals(dd.size(), getSize());
    assertTrue(registry.get("disk.free").gauge().value() >= 0);
    assertTrue(registry.get("disk.total").gauge().value() >= 0);
    assertTrue(registry.get("disk.usable").gauge().value() >= 0);
  }

  private int getSize() {
    int i = 0;
    Iterator<FileStore> it = FileSystems.getDefault().getFileStores().iterator();
    while (it.hasNext()) {
      LOG.debug("Found {} meter ", it.next().name());
      i++;
    }
    return i;
  }
}
