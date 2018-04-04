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
package org.eclipse.che.git.impl;

import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.DiffPage;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.DiffParams;
import org.eclipse.che.api.git.params.RmParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.DiffType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class DiffTest {
  private File repository;

  @BeforeMethod
  public void setUp() {
    repository = Files.createTempDir();
  }

  @AfterMethod
  public void cleanUp() {
    cleanupTestRepo(repository);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameStatus(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);
    addFile(connection, "Untracked", "Content\n");

    // when
    List<String> diff =
        readDiff(DiffParams.create().withType(DiffType.NAME_STATUS).withRenameLimit(0), connection);
    // then
    assertEquals(diff.size(), 2);
    assertTrue(diff.contains("M\taaa"));
    assertTrue(diff.contains("U\tUntracked"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameStatusWithCommits(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);
    // change README.txt
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.rm(RmParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("testDiffNameStatusWithCommits"));

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_STATUS)
                .withRenameLimit(0)
                .withCommitA("HEAD^")
                .withCommitB("HEAD"),
            connection);

    // then
    assertEquals(diff.size(), 2);
    assertTrue(diff.contains("D\tREADME.txt"));
    assertTrue(diff.contains("A\taaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameStatusWithFileFilterAndCommits(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    connection.add(AddParams.create(singletonList("aaa")));
    connection.rm(RmParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("testDiffNameStatusWithCommits"));

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(singletonList("aaa"))
                .withType(DiffType.NAME_STATUS)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD^1")
                .withCommitB("HEAD"),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("A\taaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnly(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyWithCommits(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    connection.add(AddParams.create(singletonList("aaa")));
    connection.rm(RmParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("testDiffNameStatusWithCommits"));

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD^1")
                .withCommitB("HEAD"),
            connection);

    // then
    assertEquals(diff.size(), 2);
    assertTrue(diff.contains("README.txt"));
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyCached(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    connection.add(AddParams.create(singletonList("aaa")));
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD")
                .withCached(true),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyCachedNoCommit(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    connection.add(AddParams.create(singletonList("aaa")));
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA(null)
                .withCached(true),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyWorkingTree(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(null)
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD")
                .withCached(false),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyWithFileFilter(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(singletonList("aaa"))
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyNotMatchingWithFileFilter(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(singletonList("anotherFile"))
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0),
            connection);
    // then
    assertEquals(diff.size(), 0);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffNameOnlyWithFileFilterAndCommits(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    connection.add(AddParams.create(singletonList("aaa")));
    connection.rm(RmParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("testDiffNameStatusWithCommits"));

    // when
    List<String> diff =
        readDiff(
            DiffParams.create()
                .withFileFilter(singletonList("aaa"))
                .withType(DiffType.NAME_ONLY)
                .withNoRenames(false)
                .withRenameLimit(0)
                .withCommitA("HEAD^1")
                .withCommitB("HEAD"),
            connection);

    // then
    assertEquals(diff.size(), 1);
    assertTrue(diff.contains("aaa"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffRaw(GitConnectionFactory connectionFactory) throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    // when
    DiffParams params =
        DiffParams.create()
            .withFileFilter(null)
            .withType(DiffType.RAW)
            .withNoRenames(false)
            .withRenameLimit(0);
    DiffPage diffPage = connection.diff(params);

    // then
    diffPage.writeTo(System.out);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testDiffRawWithCommits(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    makeCommitInMaster(connection);

    connection.add(AddParams.create(singletonList("aaa")));
    connection.rm(RmParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("testDiffNameStatusWithCommits"));

    // when
    DiffParams params =
        DiffParams.create()
            .withFileFilter(null)
            .withType(DiffType.RAW)
            .withNoRenames(false)
            .withRenameLimit(0)
            .withCommitA("HEAD^1")
            .withCommitB("HEAD");
    DiffPage diffPage = connection.diff(params);

    // then
    diffPage.writeTo(System.out);
  }

  private List<String> readDiff(DiffParams params, GitConnection connection)
      throws GitException, IOException {
    DiffPage diffPage = connection.diff(params);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    diffPage.writeTo(out);
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));

    String line;
    List<String> diff = new ArrayList<>();
    while ((line = reader.readLine()) != null) diff.add(line);

    return diff;
  }

  private void makeCommitInMaster(GitConnection connection) throws GitException, IOException {
    // create branch "master"
    addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
    connection.add(AddParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));

    // make some changes
    addFile(connection, "aaa", "AAA\n");
    connection.add(AddParams.create(singletonList(".")));
    addFile(connection, "aaa", "BBB\n");
  }
}
