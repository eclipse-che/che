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

import static java.util.Collections.singletonList;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertEquals;

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
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.shared.ShowFileContentResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Igor Vinokur */
public class ShowFileContentTest {

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
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testShowFileContentFromHead(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    // create new repository
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "newFile", "new file content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Test commit"));
    // when
    final ShowFileContentResponse response = connection.showFileContent("newFile", "HEAD");
    // then
    assertEquals("new file content", response.getContent());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testShowFileContentFromBranch(GitConnectionFactory connectionFactory)
      throws IOException, ServerException, URISyntaxException, UnauthorizedException {
    // given
    // create new repository
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "newFile", "new file content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Test commit"));
    connection.branchCreate("new-branch", null);
    // when
    final ShowFileContentResponse response = connection.showFileContent("newFile", "new-branch");
    // then
    assertEquals("new file content", response.getContent());
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class,
    expectedExceptions = GitException.class,
    expectedExceptionsMessageRegExp = "fatal: Path 'dummyFile' does not exist in 'HEAD'\n"
  )
  public void testShowContentOfNotExistFile(GitConnectionFactory connectionFactory)
      throws Exception {
    // given
    // create new repository
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    addFile(connection, "newfile", "new file content");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("Test commit"));
    // when
    connection.showFileContent("dummyFile", "HEAD");
  }
}
