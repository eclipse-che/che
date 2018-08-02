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
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.PushParams;
import org.eclipse.che.api.git.shared.PushResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class PushTest {

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
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testSimplePush(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    GitConnection remoteConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    remoteConnection.clone(
        CloneParams.create(connection.getWorkingDir().getAbsolutePath())
            .withWorkingDir(remoteConnection.getWorkingDir().getAbsolutePath()));
    addFile(remoteConnection, "newfile", "content");
    remoteConnection.add(AddParams.create(singletonList(".")));
    remoteConnection.commit(CommitParams.create("Fake commit"));
    // when
    remoteConnection.push(
        PushParams.create("origin")
            .withRefSpec(singletonList("refs/heads/master:refs/heads/test"))
            .withTimeout(-1));
    // then
    // check branches in origin repository
    assertEquals(connection.branchList(null).size(), 1);
    // checkout test branch
    connection.checkout(CheckoutParams.create("test"));
    assertTrue(new File(connection.getWorkingDir(), "newfile").exists());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testPushRemote(GitConnectionFactory connectionFactory)
      throws GitException, IOException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    GitConnection remoteConnection =
        connectToInitializedGitRepository(connectionFactory, remoteRepo);
    addFile(connection, "README", "README");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Init commit."));
    // make push
    int branchesBefore = remoteConnection.branchList(null).size();
    // when
    connection.push(
        PushParams.create(remoteRepo.getAbsolutePath())
            .withRefSpec(singletonList("refs/heads/master:refs/heads/test"))
            .withTimeout(-1));
    // then
    int branchesAfter = remoteConnection.branchList(null).size();
    assertEquals(branchesAfter - 1, branchesBefore);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp =
        "No remote repository specified.  "
            + "Please, specify either a URL or a remote name from which new revisions should be fetched in request."
  )
  public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // when
    connection.push(PushParams.create(null));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testPushWhenLocalRepositoryIsNotSynchronisedWithRemote(
      GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection remoteConnection =
        connectToGitRepositoryWithContent(connectionFactory, repository);
    GitConnection localConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    localConnection.clone(CloneParams.create(remoteConnection.getWorkingDir().getAbsolutePath()));
    addFile(remoteConnection, "newfile", "content");
    remoteConnection.add(AddParams.create(singletonList(".")));
    remoteConnection.commit(CommitParams.create("Fake commit"));

    // when
    String errorMessage = "";
    try {
      localConnection.push(PushParams.create("origin").withTimeout(-1));
    } catch (GitException exception) {
      errorMessage = exception.getMessage();
    }

    // then
    assertTrue(errorMessage.contains("master -> master"));
    assertTrue(errorMessage.contains(remoteConnection.getWorkingDir().getAbsolutePath()));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testPushWhenLocalRepositoryIsUpToDate(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    GitConnection remoteConnection =
        connectToGitRepositoryWithContent(connectionFactory, repository);
    GitConnection localConnection = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    localConnection.clone(CloneParams.create(remoteConnection.getWorkingDir().getAbsolutePath()));

    // when
    PushResponse pushResponse =
        localConnection.push(
            PushParams.create("origin")
                .withRefSpec(singletonList("refs/heads/master:refs/heads/master"))
                .withTimeout(-1));

    // then
    assertEquals(pushResponse.getCommandOutput(), "Everything up-to-date");
  }
}
