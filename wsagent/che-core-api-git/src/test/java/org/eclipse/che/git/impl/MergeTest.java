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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CheckoutParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.shared.MergeResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class MergeTest {

  private String branchName = "MergeTestBranch";

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
  public void testMergeNoChanges(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    connection.branchCreate(branchName, null);
    // when
    MergeResult mergeResult = connection.merge(branchName);
    // then
    assertEquals(mergeResult.getMergeStatus(), MergeResult.MergeStatus.ALREADY_UP_TO_DATE);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testMerge(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    connection.checkout(CheckoutParams.create(branchName).withCreateNew(true));
    File file = addFile(connection, "t-merge", "aaa\n");

    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file in new branch"));
    connection.checkout(CheckoutParams.create("master"));
    // when
    MergeResult mergeResult = connection.merge(branchName);
    // then
    assertEquals(mergeResult.getMergeStatus(), MergeResult.MergeStatus.FAST_FORWARD);
    assertTrue(file.exists());
    assertEquals(Files.toString(file, Charsets.UTF_8), "aaa\n");
    assertEquals(
        connection.log(LogParams.create()).getCommits().get(0).getMessage(),
        "add file in new branch");
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testMergeConflict(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    connection.checkout(CheckoutParams.create(branchName).withCreateNew(true));
    addFile(connection, "t-merge-conflict", "aaa\n");
    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file in new branch"));

    connection.checkout(CheckoutParams.create("master"));
    addFile(connection, "t-merge-conflict", "bbb\n");
    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file in new branch"));
    // when
    MergeResult mergeResult = connection.merge(branchName);
    // then
    List<String> conflicts = mergeResult.getConflicts();
    assertEquals(conflicts.size(), 1);
    assertEquals(conflicts.get(0), "t-merge-conflict");

    assertEquals(mergeResult.getMergeStatus(), MergeResult.MergeStatus.CONFLICTING);

    String expContent =
        "<<<<<<< HEAD\n" //
            + "bbb\n" //
            + "=======\n" //
            + "aaa\n" //
            + ">>>>>>> MergeTestBranch\n";
    String actual =
        Files.toString(new File(connection.getWorkingDir(), "t-merge-conflict"), Charsets.UTF_8);
    assertEquals(actual, expContent);
  }

  //        TODO Uncomment as soon as IDEX-1776 is fixed
  //    @Test(dataProvider = "GitConnectionFactory", dataProviderClass =
  // org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  //    public void testFailed(GitConnectionFactory connectionFactory) throws GitException,
  // IOException {
  //        //given
  //        GitConnection connection = connectToGitRepositoryWithContent(connectionFactory,
  // repository);
  //
  //
  // connection.checkout(newDto(CheckoutRequest.class).withName(branchName).withCreateNew(true));
  //        addFile(connection, "t-merge-failed", "aaa\n");
  //        connection.add(newDto(AddRequest.class).withFilepattern(new
  // ArrayList<>(Arrays.asList("."))));
  //        connection.commit(newDto(CommitRequest.class).withMessage("add file in new branch"));
  //
  //        connection.checkout(newDto(CheckoutRequest.class).withName("master"));
  //        addFile(connection, "t-merge-failed", "bbb\n");
  //        //when
  //        MergeResult mergeResult =
  // connection.merge(newDto(MergeRequest.class).withCommit(branchName));
  //        //then
  //        assertEquals(mergeResult.getMergeStatus(), MergeResult.MergeStatus.FAILED);
  //        assertEquals(mergeResult.getFailed().size(), 1);
  //        assertEquals(mergeResult.getFailed().get(0), "t-merge-failed");
  //    }
}
