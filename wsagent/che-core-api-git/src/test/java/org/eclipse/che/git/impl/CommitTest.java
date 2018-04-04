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

import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class CommitTest {
  private File repository;
  private String CONTENT = "git repository content\n";

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
  public void testSimpleCommit(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    // add new File
    addFile(connection, "DONTREADME", "secret");
    // add changes
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));

    // when
    CommitParams commitParams =
        CommitParams.create("Commit message").withAmend(false).withAll(false);
    Revision revision = connection.commit(commitParams);

    // then
    assertEquals(revision.getMessage(), commitParams.getMessage());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testCommitWithAddAll(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));

    // when
    // change existing README
    addFile(connection, "README.txt", "not secret");

    // then
    CommitParams commitParams =
        CommitParams.create("Other commit message").withAmend(false).withAll(true);
    Revision revision = connection.commit(commitParams);
    assertEquals(revision.getMessage(), commitParams.getMessage());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testAmendCommit(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));
    int beforeCommitsCount = connection.log(LogParams.create()).getCommits().size();

    // when
    // change existing README
    addFile(connection, "README.txt", "some new content");
    CommitParams commitParams = CommitParams.create("Amend commit").withAmend(true).withAll(true);

    // then
    Revision revision = connection.commit(commitParams);
    int afterCommitsCount = connection.log(LogParams.create()).getCommits().size();
    assertEquals(revision.getMessage(), commitParams.getMessage());
    assertEquals(beforeCommitsCount, afterCommitsCount);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testChangeMessageOfLastCommit(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addFile(connection, "NewFile.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("NewFile.txt")));
    connection.commit(CommitParams.create("First commit"));
    int beforeCommitsCount = connection.log(LogParams.create()).getCommits().size();

    // when
    CommitParams commitParams = CommitParams.create("Changed message").withAmend(true);
    connection.commit(commitParams);

    // then
    int afterCommitsCount = connection.log(LogParams.create()).getCommits().size();
    assertEquals(beforeCommitsCount, afterCommitsCount);
    assertEquals(
        connection.log(LogParams.create()).getCommits().get(0).getMessage(),
        commitParams.getMessage());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testChangeMessageOfLastCommitWithSpecifiedPath(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addFile(connection, "NewFile.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("NewFile.txt")));
    connection.commit(CommitParams.create("First commit"));
    int beforeCommitsCount = connection.log(LogParams.create()).getCommits().size();

    // when
    CommitParams commitParams =
        CommitParams.create("Changed message")
            .withFiles(singletonList("NewFile.txt"))
            .withAmend(true);
    connection.commit(commitParams);

    // then
    int afterCommitsCount = connection.log(LogParams.create()).getCommits().size();
    assertEquals(beforeCommitsCount, afterCommitsCount);
    assertEquals(
        connection.log(LogParams.create()).getCommits().get(0).getMessage(),
        commitParams.getMessage());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testCommitSeparateFiles(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addFile(connection, "File1.txt", CONTENT);
    addFile(connection, "File2.txt", CONTENT);
    connection.add(AddParams.create(asList("File1.txt", "File2.txt")));

    // when
    connection.commit(CommitParams.create("commit").withFiles(singletonList("File1.txt")));

    // then
    assertTrue(connection.status(emptyList()).getAdded().contains("File2.txt"));
    assertTrue(connection.status(emptyList()).getAdded().size() == 1);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp = "No changes added to commit"
  )
  public void testCommitWithNotStagedChanges(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    // Prepare unstaged deletion
    addFile(connection, "FileToDelete.txt", "content");
    connection.add(AddParams.create(ImmutableList.of("FileToDelete.txt")));
    connection.commit(CommitParams.create("File to delete"));
    new File(connection.getWorkingDir().getAbsolutePath(), "FileToDelete.txt").delete();
    // Prepare unstaged new file
    addFile(connection, "newFile", "content");
    // Prepare unstaged editing
    write(new File(connection.getWorkingDir(), "README.txt").toPath(), "new content".getBytes());

    // when
    connection.commit(CommitParams.create("test commit"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp = "Nothing to commit, working directory clean"
  )
  public void testCommitWithCleanIndex(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

    // when
    connection.commit(CommitParams.create("test commit"));
  }
}
