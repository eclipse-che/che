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
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_LOCAL;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.Revision;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class BranchCreateTest {

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
  public void testSimpleBranchCreate(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", org.eclipse.che.git.impl.GitTestUtil.CONTENT);
    connection.add(AddParams.create(singletonList("README.txt")));
    connection.commit(CommitParams.create("Initial addd"));

    int beforeCountOfBranches = connection.branchList(LIST_LOCAL).size();

    // when
    connection.branchCreate("new-branch", null);

    // then
    int afterCountOfBranches = connection.branchList(LIST_LOCAL).size();
    assertEquals(afterCountOfBranches, beforeCountOfBranches + 1);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testBranchCreateWithStartPoint(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "newfile1", "file 1 content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Commit message"));

    // change content
    addFile(connection, "newfile1", "new file 1 content");
    connection.commit(CommitParams.create("Commit message").withAll(true));

    // get list of master branch commits
    List<Revision> revCommitList = connection.log(LogParams.create()).getCommits();
    int beforeCheckoutCommitsCount = revCommitList.size();

    // when
    // create new branch to 2nd commit
    Branch branch = connection.branchCreate("new-branch", revCommitList.get(1).getId());
    // then
    connection.checkout(CheckoutParams.create(branch.getDisplayName()));

    int afterCheckoutCommitsCount = connection.log(LogParams.create()).getCommits().size();
    assertEquals(afterCheckoutCommitsCount, beforeCheckoutCommitsCount - 1);
  }
}
