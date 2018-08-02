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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class IsInsideWorkTreeTest {

  private File repository;
  private File regularDir;

  @BeforeMethod
  public void setUp() {
    repository = Files.createTempDir();
    regularDir = Files.createTempDir();
  }

  @AfterMethod
  public void cleanUp() {
    cleanupTestRepo(repository);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void shouldReturnTrueInsideWorkingTree(GitConnectionFactory connectionFactory)
      throws ServerException, IOException, UnauthorizedException, URISyntaxException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // add new dir into working tree
    addFile(connection.getWorkingDir().toPath().resolve("new_directory"), "a", "content of a");
    connection.add(AddParams.create(singletonList(".")));
    connection.commit(CommitParams.create("test"));

    // when
    boolean isInsideWorkingTree = connection.isInsideWorkTree();

    // then
    assertTrue(isInsideWorkingTree);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void shouldReturnFalseInsideDotGitDirectory(GitConnectionFactory connectionFactory)
      throws ServerException, IOException, UnauthorizedException, URISyntaxException {
    // given
    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);
    Path internalDir = connection.getWorkingDir().toPath().resolve(".git");

    // when
    GitConnection internalDirConnection = connectionFactory.getConnection(internalDir.toFile());
    boolean isInsideWorkingTree = internalDirConnection.isInsideWorkTree();

    // then
    assertFalse(isInsideWorkingTree);
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void shouldReturnFalseOutsideRepositoryDirectory(GitConnectionFactory connectionFactory)
      throws ServerException, IOException, UnauthorizedException, URISyntaxException {
    // given
    GitConnection externalDir = connectionFactory.getConnection(regularDir);

    // when
    boolean isInsideWorkingTree = externalDir.isInsideWorkTree();

    // then
    assertFalse(isInsideWorkingTree);
  }
}
