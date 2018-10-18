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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link FileWatcherByPathMatcher} */
@RunWith(MockitoJUnitRunner.class)
public class FileWatcherByPathMatcherTest {

  @Rule public TemporaryFolder rootFolder = new TemporaryFolder();

  @Spy Set<PathMatcher> directoryExcludes = new HashSet<>();
  @Spy Set<PathMatcher> fileExcludes = new HashSet<>();
  @Mock FileWatcherByPathValue fileWatcherByPathValue;
  @Mock PathTransformer pathTransformer;
  @Mock RootDirPathProvider pathProvider;

  @InjectMocks FileWatcherByPathMatcher fileWatcherByPathMatcher;

  @Mock Consumer<String> create;
  @Mock Consumer<String> modify;
  @Mock Consumer<String> delete;

  @Test
  public void shouldRegisterExistingFiles() throws Exception {
    File pom = rootFolder.newFile("pom.xml");
    when(pathProvider.get()).thenReturn(rootFolder.getRoot().toPath().toString());

    PathMatcher pathMatcher = path -> pom.getPath().equals(path.toString());
    fileWatcherByPathMatcher.watch(pathMatcher, create, modify, delete);

    verify(fileWatcherByPathValue).watch(pom.toPath(), create, modify, delete);
  }
}
