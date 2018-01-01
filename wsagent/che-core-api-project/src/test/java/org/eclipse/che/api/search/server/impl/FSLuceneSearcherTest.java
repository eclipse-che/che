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
import static org.testng.Assert.assertEquals;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.fs.server.impl.RootAwarePathTransformer;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.commons.lang.IoUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// import org.eclipse.che.api.search.server.impl.LuceneSearcher;
// import org.eclipse.che.api.vfs.ArchiverFactory;
// import org.eclipse.che.api.vfs.VirtualFile;
// import org.eclipse.che.api.vfs.VirtualFileSystem;
// import org.eclipse.che.api.vfs.impl.memory.MemoryVirtualFileSystem;
// import org.eclipse.che.api.vfs.search.QueryExpression;
// import org.eclipse.che.api.vfs.search.SearchResult;

@SuppressWarnings("Duplicates")
public class FSLuceneSearcherTest {
  private static final String[] TEST_CONTENT = {
    "Apollo set several major human spaceflight milestones",
    "Maybe you should think twice",
    "To be or not to be beeeee lambergeeene",
    "In early 1961, direct ascent was generally the mission mode in favor at NASA",
    "Time to think"
  };

  File indexDirectory;
  File workspaceStorage;
  Set<PathMatcher> excludePatterns;
  LuceneSearcher searcher;
  RootAwarePathTransformer pathTransformer;

  @BeforeMethod
  public void setUp() throws Exception {
    indexDirectory = Files.createTempDir();
    workspaceStorage = Files.createTempDir();
    excludePatterns = Collections.emptySet();
    //    File targetDir =
    //        new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath())
    //            .getParentFile();
    //    indexDirectory = new File(targetDir, NameGenerator.generate("index-", 4));
    //    assertTrue(indexDirectory.mkdir());

    //    filter = mock(VirtualFileFilter.class);
    //    when(filter.accept(any(VirtualFile.class))).thenReturn(false);
    //
    //    closeCallback = mock(AbstractLuceneSearcherProvider.CloseCallback.class);
    pathTransformer = new RootAwarePathTransformer(workspaceStorage);
    searcher =
        new LuceneSearcher(excludePatterns, indexDirectory, workspaceStorage, pathTransformer);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IoUtil.deleteRecursive(indexDirectory);
    IoUtil.deleteRecursive(workspaceStorage);
  }

  @Test
  public void initializesIndexForExistedFiles() throws Exception {
    // given

    File someFolder = new File(workspaceStorage, "folder");
    someFolder.mkdir();
    createFile(someFolder, "xxx.txt", TEST_CONTENT[2]);
    createFile(someFolder, "zzz.txt", TEST_CONTENT[1]);
    // when
    searcher.initialize();
    searcher.getInitialIndexingLatch().await();

    // then

    List<String> paths = searcher.search(new QueryExpression().setText("think")).getFilePaths();
    assertEquals(newArrayList("/folder/zzz.txt"), paths);
  }

  //
  //  @Test
  //  public void excludesFilesFromIndexWithFilter() throws Exception {
  //    VirtualFileSystem virtualFileSystem = virtualFileSystem();
  //    VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
  //    folder.createFile("xxx.txt", TEST_CONTENT[2]);
  //    folder.createFile("yyy.txt", TEST_CONTENT[2]);
  //    folder.createFile("zzz.txt", TEST_CONTENT[2]);
  //
  //    when(filter.accept(withName("yyy.txt"))).thenReturn(true);
  //    searcher.init(virtualFileSystem);
  //
  //    List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
  //    assertEquals(newArrayList("/folder/xxx.txt", "/folder/zzz.txt"), paths);
  //  }

  private Path createFile(File folder, String name, String content) throws IOException {
    Path file = Paths.get(folder.getPath(), name);
    Files.write(content.getBytes(), file.toFile());
    return file;
  }
}
