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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.CONTENT;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.eclipse.che.git.impl.GitTestUtil.deleteFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LsFilesParams;
import org.eclipse.che.api.git.shared.AddRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Eugene Voevodin
 * @author Sergii Kabnashniuk.
 * @author Mykola Morhun
 */
public class AddTest {

  public File repository;

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
  public void testSimpleAdd(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "testAdd", org.eclipse.che.git.impl.GitTestUtil.CONTENT);

    // when
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));

    // then
    // check added files
    List<String> files = connection.listFiles(LsFilesParams.create());
    assertEquals(files.size(), 1);
    assertTrue(files.contains("testAdd"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testAddUpdate(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("README.txt")));
    connection.commit(CommitParams.create("Initial add"));

    // when
    // modify README.txt
    addFile(connection, "README.txt", "SOME NEW CONTENT");
    List<String> listFiles = connection.listFiles(LsFilesParams.create().withModified(true));
    // then
    // modified but not added to stage
    assertTrue(listFiles.contains("README.txt"));

    // when
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN).withUpdate(true));
    // then
    listFiles = connection.listFiles(LsFilesParams.create().withStaged(true));
    // added to stage
    assertEquals(listFiles.size(), 1);
    assertTrue(listFiles.get(0).contains("README.txt"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testAddDeletedFile(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", CONTENT);
    addFile(connection, "CHANGELOG.txt", "WE'VE FIXED ALL BUGS");
    connection.add(AddParams.create(ImmutableList.of("README.txt", "CHANGELOG.txt")));
    connection.commit(CommitParams.create("Initial add"));

    // when
    // remove file from disk
    deleteFile(connection, "CHANGELOG.txt");
    // add all files to index
    connection.add(AddParams.create(AddRequest.DEFAULT_PATTERN));

    // then
    // the deleted file is added to index, so it becomes removed for git
    List<String> stagedDeletedFiles = connection.status(emptyList()).getRemoved();
    assertEquals(stagedDeletedFiles.size(), 1);
    assertEquals(stagedDeletedFiles.get(0), "CHANGELOG.txt");
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void shouldAddToIndexOnlySpecifiedFile(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "README.txt", CONTENT);
    connection.add(AddParams.create(ImmutableList.of("README.txt", "CHANGELOG.txt")));
    connection.commit(CommitParams.create("Initial add"));

    // when
    deleteFile(connection, "README.txt");
    addFile(connection, "CHANGELOG.txt", "WE'VE FIXED ALL BUGS");
    connection.add(AddParams.create(singletonList("CHANGELOG.txt")));

    // then
    List<String> notStagedDeletedFiles = connection.status(emptyList()).getMissing();
    assertEquals(notStagedDeletedFiles.size(), 1);
    assertEquals(notStagedDeletedFiles.get(0), "README.txt");

    List<String> stagedFiles = connection.status(emptyList()).getAdded();
    assertEquals(stagedFiles.size(), 1);
    assertEquals(stagedFiles.get(0), "CHANGELOG.txt");
  }
}
