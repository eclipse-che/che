/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.search.server.impl;
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

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.che.api.search.SearcherTest.TEST_CONTENT;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Files;
import java.io.File;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.fs.server.impl.RootAwarePathTransformer;
import org.eclipse.che.api.search.SearcherTest.ContentBuilder;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.commons.lang.IoUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("Duplicates")
public class FSLuceneSearcherTest {

  File indexDirectory;
  File workspaceStorage;
  Set<PathMatcher> excludePatterns;
  LuceneSearcher searcher;
  RootAwarePathTransformer pathTransformer;
  ContentBuilder contentBuilder;

  @BeforeMethod
  public void setUp() throws Exception {
    indexDirectory = Files.createTempDir();
    workspaceStorage = Files.createTempDir();
    excludePatterns = new HashSet<>();
    pathTransformer = new RootAwarePathTransformer(workspaceStorage);
    searcher =
        new LuceneSearcher(excludePatterns, indexDirectory, workspaceStorage, pathTransformer);
    contentBuilder = new ContentBuilder(workspaceStorage.toPath());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IoUtil.deleteRecursive(indexDirectory);
    IoUtil.deleteRecursive(workspaceStorage);
  }

  @Test
  public void shouldBeAbleToInitializesIndexForExistedFiles() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[1]);

    // when
    searcher.initialize();
    searcher.getInitialIndexingLatch().await();

    // then
    List<String> paths = searcher.search(new QueryExpression().setText("think")).getFilePaths();
    assertEquals(newArrayList("/folder/zzz.txt"), paths);
  }

  @Test
  public void shouldBeAbleToExcludesFilesFromIndexWithFilter() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("yyy.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[2]);
    excludePatterns.add(
        it -> it.toFile().isFile() && "yyy.txt".equals(it.getFileName().toString()));
    searcher.add(contentBuilder.getCurrentFolder());

    // when
    List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
    // then
    assertEquals(newArrayList("/folder/xxx.txt", "/folder/zzz.txt"), paths);
  }
}
