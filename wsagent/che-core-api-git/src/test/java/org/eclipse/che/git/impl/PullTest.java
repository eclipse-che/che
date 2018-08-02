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

import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.params.PullParams;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class PullTest {

  public static final String UNKNOWN = "UNKNOWN";
  private File repository;
  private File remoteRepo;

  @BeforeMethod
  public void setUp() {
    repository = Files.createTempDir();
    remoteRepo = Files.createTempDir();
  }

  @AfterMethod
  public void cleanUp() {
    cleanupTestRepo(repository);
    cleanupTestRepo(remoteRepo);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testSimplePull(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    // create new repository clone of default
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    connection2.clone(CloneParams.create(connection.getWorkingDir().getAbsolutePath()));
    addFile(connection, "newfile1", "new file1 content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Test commit"));
    // when
    connection2.pull(PullParams.create("origin").withTimeout(-1));
    // then
    assertTrue(new File(remoteRepo.getAbsolutePath(), "newfile1").exists());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testPullWithRebase(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection remote = connectToInitializedGitRepository(connectionFactory, remoteRepo);
    addFile(remote, "file", "file content");
    remote.add(AddParams.create(singletonList(".")));
    remote.commit(CommitParams.create("First commit common"));
    GitConnection local = connectionFactory.getConnection(repository.getAbsolutePath());
    local.clone(CloneParams.create(remote.getWorkingDir().getAbsolutePath()));
    addFile(local, "local file", "file content");
    local.add(AddParams.create(singletonList(".")));
    local.commit(CommitParams.create("Second commit local"));
    addFile(remote, "remote file", "file content");
    remote.add(AddParams.create(singletonList(".")));
    remote.commit(CommitParams.create("Second commit remote"));

    // when
    local.pull(PullParams.create("origin").withRebase(true));
    List<Revision> commits = local.log(LogParams.create()).getCommits();

    // then
    assertTrue("Second commit local".equals(commits.get(0).getMessage()));
    assertTrue("Second commit remote".equals(commits.get(1).getMessage()));
    assertTrue("First commit common".equals(commits.get(2).getMessage()));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testPullWithRefSpec(GitConnectionFactory connectionFactory)
      throws ServerException, URISyntaxException, IOException, UnauthorizedException {
    // given
    // create new repository clone of default
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());

    connection2.clone(CloneParams.create(connection.getWorkingDir().getAbsolutePath()));
    // add new branch
    connection.checkout(CheckoutParams.create("b1").withCreateNew(true));
    addFile(connection, "newfile1", "new file1 content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Test commit"));
    int branchesBefore = connection2.branchList(null).size();
    // when
    connection2.pull(
        PullParams.create("origin").withRefSpec("refs/heads/b1:refs/heads/b1").withTimeout(-1));
    int branchesAfter = connection2.branchList(null).size();
    assertEquals(branchesAfter, branchesBefore + 1);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testPullRemote(GitConnectionFactory connectionFactory)
      throws GitException, IOException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    String branchName = "remoteBranch";
    connection.checkout(CheckoutParams.create(branchName).withCreateNew(true));
    addFile(connection, "remoteFile", "");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("remote test"));

    GitConnection connection2 = connectToGitRepositoryWithContent(connectionFactory, remoteRepo);

    // when
    PullParams params =
        PullParams.create(connection.getWorkingDir().getAbsolutePath())
            .withRefSpec("refs/heads/remoteBranch:refs/heads/remoteBranch");
    connection2.pull(params);
    // then
    assertTrue(new File(remoteRepo.getAbsolutePath(), "remoteFile").exists());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp =
        "No remote repository specified.  "
            + "Please, specify either a URL or a remote name from "
            + "which new revisions should be fetched in request."
  )
  public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // when
    connection.pull(PullParams.create(null));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp =
        "No remote repository specified.  "
            + "Please, specify either a URL or a remote name from "
            + "which new revisions should be fetched in request."
  )
  public void testWhenThereAreNoAnyRemotesBehindTheProxy(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    System.setProperty("http.proxyUser", "user1");
    System.setProperty("http.proxyPassword", "paswd1");
    System.setProperty("https.proxyUser", "user2");
    System.setProperty("https.proxyPassword", "paswd2");

    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // when
    connection.pull(PullParams.create(null));
  }

  @AfterMethod
  public void tearDown() throws Exception {
    System.clearProperty("http.proxyUser");
    System.clearProperty("http.proxyPassword");
    System.clearProperty("https.proxyUser");
    System.clearProperty("https.proxyPassword");
  }
}
