/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.watcher.server.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link FileWatcherOperation} */
@RunWith(MockitoJUnitRunner.class)
public class FileWatcherOperationTest {

  private static final int ID = 0;
  @Mock Consumer<String> create;
  @Mock Consumer<String> modify;
  @Mock Consumer<String> delete;

  FileWatcherOperation operation;

  @Before
  public void setUp() throws Exception {
    operation = new FileWatcherOperation(ID, create, modify, delete);
  }

  @Test
  public void shouldProperlyGetCreateEventKindConsumer() throws Exception {
    Optional<Consumer<String>> consumer = operation.get(ENTRY_CREATE);

    assertTrue(consumer.isPresent());
    if (consumer.isPresent()) {
      Consumer<String> actual = consumer.get();
      assertEquals(create, actual);
    }
  }

  @Test
  public void shouldProperlyGetModifyEventKindConsumer() throws Exception {
    Optional<Consumer<String>> consumer = operation.get(ENTRY_MODIFY);

    assertTrue(consumer.isPresent());
    if (consumer.isPresent()) {
      Consumer<String> actual = consumer.get();
      assertEquals(modify, actual);
    }
  }

  @Test
  public void shouldProperlyGetDeleteEventKindConsumer() throws Exception {
    Optional<Consumer<String>> consumer = operation.get(ENTRY_DELETE);

    assertTrue(consumer.isPresent());
    if (consumer.isPresent()) {
      Consumer<String> actual = consumer.get();
      assertEquals(delete, actual);
    }
  }
}
