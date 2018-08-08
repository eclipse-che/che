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
import org.eclipse.che.api.git.params.FetchParams;
import org.eclipse.che.api.git.params.LogParams;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class FetchTest {

  private File fetchTestRepo;
  private File repository;

  @BeforeMethod
  public void setUp() {
    repository = Files.createTempDir();
    fetchTestRepo = Files.createTempDir();
  }

  @AfterMethod
  public void cleanUp() {
    cleanupTestRepo(repository);
    cleanupTestRepo(fetchTestRepo);
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = GitConnectionFactoryProvider.class)
  public void testSimpleFetch(GitConnectionFactory connectionFactory)
      throws ServerException, IOException, UnauthorizedException, URISyntaxException {

    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    GitConnection fetchConnection =
        connectionFactory.getConnection(fetchTestRepo.getAbsolutePath());

    addFile(connection, "README", "readme content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("fetch test"));
    // clone default repo into fetchRepo
    fetchConnection.clone(
        CloneParams.create(connection.getWorkingDir().getAbsolutePath())
            .withWorkingDir(fetchConnection.getWorkingDir().getAbsolutePath()));

    // add new File into defaultRepository
    addFile(connection, "newfile1", "newfile1 content");
    // add file to index and make commit
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("fetch test"));

    // when
    fetchConnection.fetch(FetchParams.create(repository.getAbsolutePath()));

    // then
    // make merge with FETCH_HEAD
    fetchConnection.merge("FETCH_HEAD");
    assertTrue(new File(fetchTestRepo, "newfile1").exists());
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = GitConnectionFactoryProvider.class)
  public void testFetchBranch(GitConnectionFactory connectionFactory)
      throws ServerException, IOException, UnauthorizedException, URISyntaxException {

    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    GitConnection fetchConnection =
        connectionFactory.getConnection(fetchTestRepo.getAbsolutePath());

    addFile(connection, "README", "readme content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("fetch test"));
    // clone default repo into fetchRepo
    fetchConnection.clone(CloneParams.create(repository.getAbsolutePath()));

    // add new File into defaultRepository
    addFile(connection, "newfile1", "newfile1 content");
    // add file to index and make commit
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("fetch test"));

    String branchName = "branch";
    connection.checkout(CheckoutParams.create(branchName).withCreateNew(true));
    addFile(connection, "otherfile1", "otherfile1 content");
    addFile(connection, "otherfile2", "otherfile2 content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("fetch branch test"));

    // when
    fetchConnection.fetch(FetchParams.create(repository.getAbsolutePath()));
    // then
    // make merge with FETCH_HEAD
    fetchConnection.merge("FETCH_HEAD");
    assertTrue(new File(fetchTestRepo, "otherfile1").exists());
    assertTrue(new File(fetchTestRepo, "otherfile2").exists());
    assertEquals(
        fetchConnection.log(LogParams.create()).getCommits().get(0).getMessage(),
        "fetch branch test");
  }

  @Test(
      dataProvider = "GitConnectionFactory",
      dataProviderClass = GitConnectionFactoryProvider.class,
      expectedExceptions = GitException.class,
      expectedExceptionsMessageRegExp =
          "No remote repository specified.  "
              + "Please, specify either a URL or a remote name from which new revisions should be fetched in request.")
  public void testWhenThereAreNoAnyRemotes(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // when
    connection.fetch(FetchParams.create(null));
  }
}
