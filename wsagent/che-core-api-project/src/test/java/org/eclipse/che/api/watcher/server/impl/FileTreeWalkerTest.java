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
package org.eclipse.che.api.watcher.server.impl;

import static java.io.File.createTempFile;
import static java.lang.Thread.sleep;
import static org.apache.commons.io.FileUtils.write;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link FileTreeWalker} */
@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
@RunWith(MockitoJUnitRunner.class)
public class FileTreeWalkerTest {
  static final int FS_LATENCY_DELAY = 1_000;
  static final String TEST_FILE_NAME = "test-file-name";
  static final String TEST_FILE_CONTENT = "test-file-content";
  static final String TEST_FOLDER_NAME = "test-folder-name";

  @Rule public TemporaryFolder rootFolder = new TemporaryFolder();

  FileTreeWalker fileTreeWalker;

  Set<Consumer<Path>> directoryCreateConsumers = new HashSet<>();
  Set<Consumer<Path>> directoryUpdateConsumers = new HashSet<>();
  Set<Consumer<Path>> directoryDeleteConsumers = new HashSet<>();
  Set<PathMatcher> directoryExcludes = new HashSet<>();

  Set<Consumer<Path>> fileCreateConsumers = new HashSet<>();
  Set<Consumer<Path>> fileUpdateConsumers = new HashSet<>();
  Set<Consumer<Path>> fileDeleteConsumers = new HashSet<>();
  Set<PathMatcher> fileExcludes = new HashSet<>();

  @Mock Consumer<Path> fileCreatedConsumerMock;
  @Mock Consumer<Path> fileUpdateConsumerMock;
  @Mock Consumer<Path> fileDeleteConsumerMock;
  @Mock Consumer<Path> directoryCreatedConsumerMock;
  @Mock Consumer<Path> directoryUpdateConsumerMock;

  @Mock Consumer<Path> directoryDeleteConsumerMock;

  @Before
  public void setUp() throws Exception {
    fileTreeWalker =
        new FileTreeWalker(
            new DummyRootProvider(rootFolder.getRoot()),
            directoryUpdateConsumers,
            directoryCreateConsumers,
            directoryDeleteConsumers,
            directoryExcludes,
            fileUpdateConsumers,
            fileCreateConsumers,
            fileDeleteConsumers,
            fileExcludes);
  }

  @After
  public void tearDown() throws Exception {
    directoryUpdateConsumers.clear();
    directoryCreateConsumers.clear();
    directoryDeleteConsumers.clear();
    directoryExcludes.clear();
    fileUpdateConsumers.clear();
    fileCreateConsumers.clear();
    fileDeleteConsumers.clear();
    fileExcludes.clear();
  }

  @Test
  public void shouldRunFileCreatedConsumer() throws Exception {
    fileCreateConsumers.add(fileCreatedConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFile(TEST_FILE_NAME);

    fileTreeWalker.walk();
    verify(fileCreatedConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldRunFileUpdateConsumer() throws Exception {
    fileUpdateConsumers.add(fileUpdateConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFile(TEST_FILE_NAME);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    write(file, TEST_FILE_CONTENT);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    verify(fileUpdateConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldRunFileDeleteConsumer() throws Exception {
    fileDeleteConsumers.add(fileDeleteConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFile(TEST_FILE_NAME);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    file.delete();
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    verify(fileDeleteConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldRunDirectoryCreatedConsumer() throws Exception {
    directoryCreateConsumers.add(directoryCreatedConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFolder(TEST_FOLDER_NAME);

    fileTreeWalker.walk();
    verify(directoryCreatedConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldRunDirectoryUpdateConsumer() throws Exception {
    directoryUpdateConsumers.add(directoryUpdateConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFolder(TEST_FOLDER_NAME);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    createTempFile(TEST_FILE_NAME, "", file);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    verify(directoryUpdateConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldRunDirectoryDeleteConsumer() throws Exception {
    directoryDeleteConsumers.add(directoryDeleteConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFolder(TEST_FOLDER_NAME);
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    file.delete();
    sleep(FS_LATENCY_DELAY);
    fileTreeWalker.walk();

    verify(directoryDeleteConsumerMock).accept(file.toPath());
  }

  @Test
  public void shouldProperlySkipExcludedFile() throws Exception {
    fileExcludes.add(it -> it.getFileName().toString().equals(TEST_FILE_NAME));
    fileCreateConsumers.add(fileCreatedConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFile(TEST_FILE_NAME);

    fileTreeWalker.walk();
    verify(fileCreatedConsumerMock, never()).accept(file.toPath());
  }

  @Test
  public void shouldProperlySkipExcludedDirectory() throws Exception {
    directoryExcludes.add(it -> it.getFileName().toString().equals(TEST_FOLDER_NAME));
    directoryCreateConsumers.add(directoryCreatedConsumerMock);
    fileTreeWalker.initialize();

    File file = rootFolder.newFolder(TEST_FOLDER_NAME);

    fileTreeWalker.walk();
    verify(directoryCreatedConsumerMock, never()).accept(file.toPath());
  }

  @Test
  public void shouldNotRunDirectoryCreatedConsumerForAlreadyExistingDirectory() throws Exception {
    directoryCreateConsumers.add(directoryCreatedConsumerMock);
    File file = rootFolder.newFolder(TEST_FOLDER_NAME);

    fileTreeWalker.initialize();

    fileTreeWalker.walk();
    verify(directoryCreatedConsumerMock, never()).accept(file.toPath());
  }

  @Test
  public void shouldNotRunFileCreatedConsumerForAlreadyExistingFile() throws Exception {
    fileCreateConsumers.add(fileCreatedConsumerMock);
    File file = rootFolder.newFile(TEST_FILE_NAME);

    fileTreeWalker.initialize();

    fileTreeWalker.walk();
    verify(fileCreatedConsumerMock, never()).accept(file.toPath());
  }

  private static class DummyRootProvider extends RootDirPathProvider {

    public DummyRootProvider(File folder) {
      this.rootFile = folder;
    }
  }
}
