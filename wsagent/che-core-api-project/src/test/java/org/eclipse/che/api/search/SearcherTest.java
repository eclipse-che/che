package org.eclipse.che.api.search;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.impl.RootAwarePathTransformer;
import org.eclipse.che.api.search.server.OffsetData;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.api.search.server.SearchResult;
import org.eclipse.che.api.search.server.Searcher;
import org.eclipse.che.api.search.server.impl.LuceneSearcher;
import org.eclipse.che.api.search.server.impl.SearchResultEntry;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("Duplicates")
public class SearcherTest {
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
  Searcher searcher;
  RootAwarePathTransformer pathTransformer;
  ContentBuilder contentBuilder;

  @BeforeMethod
  public void setUp() throws Exception {
    indexDirectory = Files.createTempDir(); // Paths.get("/tmp/indexdir").toFile();
    IoUtil.deleteRecursive(indexDirectory);
    workspaceStorage = Files.createTempDir();
    excludePatterns = Collections.emptySet();
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
  public void shouldBeAbleToFindSingleFile() throws Exception {

    // given
    contentBuilder.createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[1]);
    // when
    searcher.add(contentBuilder.getLastUpdatedFile());
    // then
    assertFind("should", "/aaa/aaa.txt");
  }

  @Test
  public void shouldBeAbleToFindTwoFilesAddedAsSingleDirectory() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[1]);
    // when
    searcher.add(contentBuilder.getCurrentFolder());
    // then
    assertFind("be", "/folder/xxx.txt");
    assertFind("should", "/folder/zzz.txt");
  }

  @Test
  public void shouldBeAbleToUpdateSingleFile() throws Exception {
    // given
    contentBuilder.createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getLastUpdatedFile());
    assertEmptyResult("should");
    // when
    contentBuilder.createFile("aaa.txt", TEST_CONTENT[1]);
    searcher.add(contentBuilder.getLastUpdatedFile());
    // then
    assertFind("should", "/aaa/aaa.txt");
  }

  @Test
  public void shouldBeAbleToDeleteSingleFile() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[1]);
    searcher.add(contentBuilder.getCurrentFolder());
    contentBuilder
        .takeWorkspceRoot()
        .createFolder("aaa")
        .createFile("aaa.txt1", TEST_CONTENT[3])
        .createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    assertFind("be", "/folder/xxx.txt", "/aaa/aaa.txt");

    // when
    contentBuilder.deleteFileInCurrentFolder("aaa.txt");
    searcher.delete(contentBuilder.getLastUpdatedFile());
    // then
    assertFind("be", "/folder/xxx.txt");
  }

  @Test
  public void shouldBeAbleToFindNumberWithComaInText() throws Exception {
    // given
    contentBuilder
        .createFolder("aaa")
        .createFile("aaa.txt1", TEST_CONTENT[3])
        .createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind("1961,", "/aaa/aaa.txt1");
  }

  @Test
  public void shouldBeAbleToFindTwoWordsInText() throws Exception {
    // given
    contentBuilder
        .createFolder("aaa")
        .createFile("aaa.txt1", TEST_CONTENT[3])
        .createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind("was generally", "/aaa/aaa.txt1");
  }

  @Test
  public void shouldBeAbleToDeleteFolder() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("zzz.txt", TEST_CONTENT[1]);
    searcher.add(contentBuilder.getCurrentFolder());
    contentBuilder
        .takeWorkspceRoot()
        .createFolder("aaa")
        .createFile("aaa.txt1", TEST_CONTENT[3])
        .createFile("aaa.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    assertFind("be", "/folder/xxx.txt", "/aaa/aaa.txt");
    assertFind("generally", "/aaa/aaa.txt1");

    // when
    searcher.delete(contentBuilder.getCurrentFolder());
    contentBuilder.deleteCurrentFolder();
    // then
    assertFind("be", "/folder/xxx.txt");
    assertEmptyResult("generally");
  }

  @Test
  public void shouldBeAbleToSearchByWordFragment() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[0])
        .createFile("yyy.txt", TEST_CONTENT[1])
        .createFile("zzz.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind("*stone*", "/folder/xxx.txt");
  }

  @Test
  public void shouldBeAbleToSearchByTextTermAndFileName() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("yyy.txt", TEST_CONTENT[1])
        .createFile("zzz.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind(new QueryExpression().setText("be").setName("xxx.txt"), "/folder/xxx.txt");
  }

  @Test
  public void shouldBeAbleToSearchByFullTextPatternAndFileName() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("yyy.txt", TEST_CONTENT[1])
        .createFile("zzz.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind(new QueryExpression().setText("*be*").setName("xxx.txt"), "/folder/xxx.txt");
  }

  @Test
  public void shouldBeAbleToSearchWithPositions() throws Exception {
    // given
    contentBuilder
        .createFolder("folder")
        .createFile("xxx.txt", TEST_CONTENT[2])
        .createFile("yyy.txt", TEST_CONTENT[1])
        .createFile("zzz.txt", TEST_CONTENT[4]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when

    // then
    assertFind(
        new QueryExpression().setText("*to*").setIncludePositions(true),
        new SearchResultEntry(
            "/folder/xxx.txt",
            ImmutableList.of(
                new OffsetData("To", 0, 2, 0, 1.0f, 0, TEST_CONTENT[2]),
                new OffsetData("to", 13, 15, 0, 1.0f, 0, TEST_CONTENT[2]))),
        new SearchResultEntry(
            "/folder/zzz.txt",
            ImmutableList.of(new OffsetData("to", 5, 7, 2, 1.0f, 0, TEST_CONTENT[4]))));
  }

  @DataProvider
  public Object[][] searchByName() {
    return new Object[][] {
      {"sameName.txt", "sameName.txt"},
      {"notCaseSensitive.txt", "notcasesensitive.txt"},
      {"fullName.txt", "full*"},
      {"file name.txt", "file name"},
      {"prefixFileName.txt", "prefixF*"},
      {"name.with.dot.txt", "name.With.Dot.txt"},
    };
  }

  @Test(dataProvider = "searchByName")
  public void shouldSearchFileByName(String fileName, String searchedFileName) throws Exception {
    // given
    contentBuilder
        .createFolder("parent")
        .createFolder("child")
        .createFile(NameGenerator.generate(null, 10), TEST_CONTENT[3])
        .createFile(fileName, TEST_CONTENT[2])
        .createFile(NameGenerator.generate(null, 10), TEST_CONTENT[1]);
    searcher.add(contentBuilder.getCurrentFolder());
    contentBuilder
        .takeWorkspceRoot()
        .createFolder("folder2")
        .createFile(NameGenerator.generate(null, 10), TEST_CONTENT[2])
        .createFile(NameGenerator.generate(null, 10), TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind(new QueryExpression().setName(searchedFileName), "/parent/child/" + fileName);
  }

  @Test
  public void shouldSearchByTextAndPath() throws Exception {
    // given
    contentBuilder.createFolder("folder2").createFile("zzz.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    contentBuilder
        .takeWorkspceRoot()
        .createFolder("folder1")
        .createFolder("a")
        .createFolder("B")
        .createFile("xxx.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind(new QueryExpression().setText("be").setPath("/folder1"), "/folder1/a/B/xxx.txt");
  }

  @Test
  public void shouldSearchByTextAndPathAndFileName() throws Exception {
    contentBuilder
        .createFolder("folder1")
        .createFolder("a")
        .createFolder("b")
        .createFile("yyy.txt", TEST_CONTENT[2])
        .createFile("xxx.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());

    contentBuilder
        .takeWorkspceRoot()
        .createFolder("folder2")
        .createFolder("a")
        .createFolder("b")
        .createFile("zzz.txt", TEST_CONTENT[2]);
    searcher.add(contentBuilder.getCurrentFolder());
    // when
    // then
    assertFind(
        new QueryExpression().setText("be").setPath("/folder1").setName("xxx.txt"),
        "/folder1/a/b/xxx.txt");
  }

  @Test
  public void shouldLimitsNumberOfSearchResultsWhenMaxItemIsSet() throws Exception {

    // given
    for (int i = 0; i < 125; i++) {
      contentBuilder.createFile(
          String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
    }
    searcher.add(contentBuilder.getCurrentFolder());

    // when
    SearchResult result = searcher.search(new QueryExpression().setText("mission").setMaxItems(5));
    // then
    assertEquals(25, result.getTotalHits());
    assertEquals(5, result.getFilePaths().size());
  }

  @Test
  public void shouldBeAbleToGeneratesQueryExpressionForRetrievingNextPageOfResults()
      throws Exception {
    // given
    for (int i = 0; i < 125; i++) {
      contentBuilder.createFile(
          String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
    }
    searcher.add(contentBuilder.getCurrentFolder());

    SearchResult result =
        searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(7));

    assertEquals(result.getTotalHits(), 25);

    Optional<QueryExpression> optionalNextPageQueryExpression = result.getNextPageQueryExpression();
    assertTrue(optionalNextPageQueryExpression.isPresent());
    QueryExpression nextPageQueryExpression = optionalNextPageQueryExpression.get();
    assertEquals("spaceflight", nextPageQueryExpression.getText());
    assertEquals(7, nextPageQueryExpression.getSkipCount());
    assertEquals(7, nextPageQueryExpression.getMaxItems());
  }

  @Test
  public void shouldBeAbleToRetrievesSearchResultWithPages() throws Exception {
    for (int i = 0; i < 125; i++) {
      contentBuilder.createFile(
          String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
    }
    searcher.add(contentBuilder.getCurrentFolder());

    SearchResult firstPage =
        searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(8));
    assertEquals(firstPage.getFilePaths().size(), 8);

    QueryExpression nextPageQueryExpression = firstPage.getNextPageQueryExpression().get();
    nextPageQueryExpression.setMaxItems(100);

    SearchResult lastPage = searcher.search(nextPageQueryExpression);
    assertEquals(lastPage.getFilePaths().size(), 17);

    assertTrue(Collections.disjoint(firstPage.getFilePaths(), lastPage.getFilePaths()));
  }

  public void assertFind(QueryExpression query, SearchResultEntry... expectedResults)
      throws ServerException {
    SearchResult result = searcher.search(query);
    assertEquals(result.getResults(), Arrays.asList(expectedResults));
  }

  public void assertFind(QueryExpression query, String... expectedPaths) throws ServerException {
    List<String> paths = searcher.search(query).getFilePaths();
    assertEquals(paths, Arrays.asList(expectedPaths));
  }

  public void assertFind(String text, String... expectedPaths) throws ServerException {
    assertFind(new QueryExpression().setText(text), expectedPaths);
  }

  public void assertEmptyResult(QueryExpression query) throws ServerException {
    List<String> paths = searcher.search(query).getFilePaths();
    assertTrue(paths.isEmpty());
  }

  public void assertEmptyResult(String text) throws ServerException {
    assertEmptyResult(new QueryExpression().setText(text));
  }

  public static class ContentBuilder {
    private final Path workspaceRoot;
    private Path root;
    private Path lastUpdatedFile;

    public ContentBuilder(Path root) {
      this.workspaceRoot = root;
      this.root = root;
    }

    public ContentBuilder createFolder(String name) throws IOException {
      this.root = Paths.get(this.root.toString(), name);
      java.nio.file.Files.createDirectories(this.root);
      return this;
    }

    public ContentBuilder createFile(String name, String content) throws IOException {
      this.lastUpdatedFile = Paths.get(this.root.toString(), name);
      Files.write(content.getBytes(), lastUpdatedFile.toFile());
      return this;
    }

    public Path getCurrentFolder() {
      return this.root;
    }

    public ContentBuilder takeParent() {
      this.root = this.root.getParent();
      return this;
    }

    public ContentBuilder takeWorkspceRoot() {
      this.root = workspaceRoot;
      return this;
    }

    public Path getLastUpdatedFile() {
      return lastUpdatedFile;
    }

    public ContentBuilder deleteCurrentFolder() {
      IoUtil.deleteRecursive(this.root.toFile());
      this.root = this.root.getParent();
      return this;
    }

    public ContentBuilder deleteFileInCurrentFolder(String name) {
      this.lastUpdatedFile = Paths.get(this.root.toString(), name);
      this.lastUpdatedFile.toFile().delete();
      return this;
    }
  }
}
