/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.git.impl;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.shared.Branch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Eugene Voevodin
 * @author Mihail Kuznyetsov
 */
public class BranchDeleteTest {

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
  public void testSimpleDelete(GitConnectionFactory connectionFactory)
      throws GitException, IOException, UnauthorizedException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
    connection.add(AddParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));
    connection.branchCreate("newbranch", null);

    assertTrue(
        Sets.symmetricDifference(
                Sets.newHashSet(connection.branchList(LIST_LOCAL)),
                Sets.newHashSet(
                    newDto(Branch.class)
                        .withName("refs/heads/master")
                        .withDisplayName("master")
                        .withActive(true)
                        .withRemote(false),
                    newDto(Branch.class)
                        .withName("refs/heads/newbranch")
                        .withDisplayName("newbranch")
                        .withActive(false)
                        .withRemote(false)))
            .isEmpty());
    // when
    connection.branchDelete("newbranch", false);
    // then
    assertTrue(
        Sets.symmetricDifference(
                Sets.newHashSet(connection.branchList(LIST_LOCAL)),
                Sets.newHashSet(
                    newDto(Branch.class)
                        .withName("refs/heads/master")
                        .withDisplayName("master")
                        .withActive(true)
                        .withRemote(false)))
            .isEmpty());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void shouldDeleteNotFullyMergedBranchWithForce(GitConnectionFactory connectionFactory)
      throws GitException, IOException, UnauthorizedException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
    connection.add(AddParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));
    // create new branch and make a commit
    connection.checkout(CheckoutParams.create("newbranch").withCreateNew(true));
    addFile(connection, "newfile", "new file content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("second commit"));
    connection.checkout(CheckoutParams.create("master"));

    // when
    connection.branchDelete("newbranch", true);

    // then
    assertTrue(
        Sets.symmetricDifference(
                Sets.newHashSet(connection.branchList(LIST_LOCAL)),
                Sets.newHashSet(
                    newDto(Branch.class)
                        .withName("refs/heads/master")
                        .withDisplayName("master")
                        .withActive(true)
                        .withRemote(false)))
            .isEmpty());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class
  )
  public void shouldThrowExceptionOnDeletingNotFullyMergedBranchWithoutForce(
      GitConnectionFactory connectionFactory)
      throws GitException, IOException, UnauthorizedException, NoSuchFieldException,
          IllegalAccessException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
    connection.add(AddParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));
    // create new branch and make a commit
    connection.checkout(CheckoutParams.create("newbranch").withCreateNew(true));
    addFile(connection, "newfile", "new file content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("second commit"));
    connection.checkout(CheckoutParams.create("master"));

    connection.branchDelete("newbranch", false);
  }
}
