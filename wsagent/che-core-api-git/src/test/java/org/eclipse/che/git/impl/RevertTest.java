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
package org.eclipse.che.git.impl;

import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.LogPage;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.RevertResult;
import org.eclipse.che.api.git.shared.RevertResult.RevertStatus;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RevertTest {
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
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void testRevertCommit(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    // first commit
    File file = addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.commit(CommitParams.create("add test-revert file"));
    // second commit
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("one more bla\n");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    Revision revision = connection.commit(CommitParams.create("add one more bla\n"));

    // when
    RevertResult result = connection.revert(revision.getId());

    // then
    assertFalse(result.getNewHead().isEmpty());
    assertTrue(result.getConflicts().isEmpty());
    assertEquals(result.getRevertedCommits().get(0), revision.getId());
    assertEquals(connection.log(LogParams.create()).getCommits().size(), 3);
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void ShouldSetAuthorToRevertCommit(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    // first commit
    addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    String revision = connection.commit(CommitParams.create("add test-revert file")).getId();

    // when
    RevertResult result = connection.revert(revision);
    LogPage log = connection.log(LogParams.create());

    // then
    assertEquals(log.getCommits().get(0).getAuthor().getName(), "test_name");
    assertEquals(log.getCommits().get(0).getAuthor().getEmail(), "test@email");
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void testConflictsIfDirtyWorkTree(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    // first commit
    File file = addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.commit(CommitParams.create("add test-revert file"));
    // second commit
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("one more bla\n");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    Revision revision = connection.commit(CommitParams.create("add one more bla"));
    // fill the file with new content
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("add smth one the same line");
    }

    // when
    RevertResult result = connection.revert(revision.getId());

    // then
    assertNull(result.getNewHead());
    assertTrue(result.getRevertedCommits().isEmpty());

    Map<String, RevertStatus> conflictsMap = new HashMap<>();
    conflictsMap.put("test-revert", RevertStatus.DIRTY_WORKTREE);
    assertEquals(result.getConflicts(), conflictsMap);
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void testConflictsIfDirtyIndex(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    // first commit
    File file = addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.commit(CommitParams.create("add test-revert file"));
    // second commit
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("one more bla\n");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    Revision revision = connection.commit(CommitParams.create("add one more bla"));
    // dirty index
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("add smth one the same line");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));

    // when
    RevertResult result = connection.revert(revision.getId());

    // then
    assertNull(result.getNewHead());
    assertTrue(result.getRevertedCommits().isEmpty());

    Map<String, RevertStatus> conflictsMap = new HashMap<>();
    conflictsMap.put("test-revert", RevertStatus.DIRTY_INDEX);
    assertEquals(result.getConflicts(), conflictsMap);
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void testConflictsIfSameLineWasChangedInLaterCommits(
      GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    // first commit
    File file = addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.commit(CommitParams.create("add test-revert file"));
    // second commit
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("one more bla\n");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    Revision revision = connection.commit(CommitParams.create("add one more bla"));
    // third commit changing the same line
    try (PrintWriter pw = new PrintWriter(file)) {
      pw.append("add smth one the same line");
    }
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    connection.commit(CommitParams.create("change the same line"));

    // when
    // revert second commit
    RevertResult result = connection.revert(revision.getId());

    // then
    assertNull(result.getNewHead());
    assertTrue(result.getRevertedCommits().isEmpty());

    Map<String, RevertStatus> conflictsMap = new HashMap<>();
    conflictsMap.put("test-revert", RevertStatus.FAILED);
    assertEquals(result.getConflicts(), conflictsMap);
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
      expectedExceptions = GitException.class,
      expectedExceptionsMessageRegExp =
          "Cannot revert commit .* because it has 0 parents, only commits with exactly one parent are supported")
  public void testExceptionOnRevertFirstCommit(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "test-revert", "blabla\n");
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));
    Revision revision = connection.commit(CommitParams.create("add test-revert file"));
    // when
    connection.revert(revision.getId());
  }
}
