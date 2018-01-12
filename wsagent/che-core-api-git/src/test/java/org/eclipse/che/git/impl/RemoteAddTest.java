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

import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.PullParams;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.shared.Branch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class RemoteAddTest {

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
  public void testSimpleRemoteAdd(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    int beforeCount = connection.remoteList(null, false).size();
    // when
    connection.remoteAdd(RemoteAddParams.create("origin", "some.url"));
    // then
    int afterCount = connection.remoteList(null, false).size();
    assertEquals(afterCount, beforeCount + 1);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testAddNotAllBranchesTracked(GitConnectionFactory connectionFactory)
      throws GitException, URISyntaxException, IOException, UnauthorizedException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    connection.branchCreate("b1", null);
    connection.branchCreate("b2", null);
    connection.branchCreate("b3", null);

    GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    connection2.init(false);
    // when
    // add remote tracked only to b1 and b3 branches.
    RemoteAddParams params =
        RemoteAddParams.create("origin", connection.getWorkingDir().getAbsolutePath())
            .withBranches(ImmutableList.of("b1", "b3"));
    connection2.remoteAdd(params);
    // then
    // make pull
    connection2.pull(PullParams.create("origin"));

    assertTrue(
        Sets.symmetricDifference(
                Sets.newHashSet(connection2.branchList(LIST_REMOTE)),
                Sets.newHashSet(
                    newDto(Branch.class)
                        .withName("refs/remotes/origin/b1")
                        .withDisplayName("origin/b1")
                        .withActive(false)
                        .withRemote(true),
                    newDto(Branch.class)
                        .withName("refs/remotes/origin/b3")
                        .withDisplayName("origin/b3")
                        .withActive(false)
                        .withRemote(true)))
            .isEmpty());
  }
}
