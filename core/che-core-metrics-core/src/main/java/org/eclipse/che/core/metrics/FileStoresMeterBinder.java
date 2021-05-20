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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.function.ToDoubleFunction;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Bind disk usage metrics for for every {@link java.nio.file.FileStore}. */
@Singleton
public class FileStoresMeterBinder implements MeterBinder {

  private static final Logger LOG = LoggerFactory.getLogger(FileStoresMeterBinder.class);

  @Override
  public void bindTo(MeterRegistry registry) {
    for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
      LOG.debug(
          "Add gauge metric for {}, isReadOnly {}, type {}",
          fileStore.name(),
          fileStore.isReadOnly(),
          fileStore.type());
      Iterable<Tag> tagsWithPath = Tags.concat(Tags.empty(), "path", fileStore.toString());

      Gauge.builder("disk.free", fileStore, exceptionToNonWrapper(FileStore::getUnallocatedSpace))
          .tags(tagsWithPath)
          .description("Unallocated space for file storage")
          .baseUnit("bytes")
          .strongReference(true)
          .register(registry);
      Gauge.builder("disk.total", fileStore, exceptionToNonWrapper(FileStore::getTotalSpace))
          .tags(tagsWithPath)
          .description("Total space for file storage")
          .baseUnit("bytes")
          .strongReference(true)
          .register(registry);
      Gauge.builder("disk.usable", fileStore, exceptionToNonWrapper(FileStore::getUsableSpace))
          .tags(tagsWithPath)
          .description("Usable space for file storage")
          .baseUnit("bytes")
          .strongReference(true)
          .register(registry);
    }
  }

  static <T> ToDoubleFunction<T> exceptionToNonWrapper(
      ThrowingToDoubleFunction<T> throwingConsumer) {

    return i -> {
      try {
        return throwingConsumer.applyAsDouble(i);
      } catch (Exception ex) {
        return Double.NaN;
      }
    };
  }

  @FunctionalInterface
  interface ThrowingToDoubleFunction<T> {
    double applyAsDouble(T t) throws IOException;
  }
}
