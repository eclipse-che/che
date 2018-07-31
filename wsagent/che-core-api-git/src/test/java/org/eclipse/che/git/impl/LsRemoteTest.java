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

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToInitializedGitRepository;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.RemoteReference;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class LsRemoteTest {

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
  public void testShouldBeAbleToGetResultFromPublicRepo(GitConnectionFactory connectionFactory)
      throws GitException, IOException, UnauthorizedException {

    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    // when
    Set<RemoteReference> remoteReferenceSet =
        new HashSet<>(connection.lsRemote("https://github.com/codenvy/everrest.git"));

    // then
    assertTrue(
        remoteReferenceSet.contains(
            newDto(RemoteReference.class)
                .withCommitId("259e24c83c8a122af858c8306c3286586404ef3f")
                .withReferenceName("refs/tags/1.1.9")));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class,
    expectedExceptions = UnauthorizedException.class,
    expectedExceptionsMessageRegExp =
        "fatal: Authentication failed for 'https://bitbucket.org/exoinvitemain/privater.git/'\n"
  )
  public void
      testShouldThrowUnauthorizedExceptionIfUserTryGetInfoAboutPrivateRepoAndUserIsUnauthorized(
          GitConnectionFactory connectionFactory)
          throws GitException, UnauthorizedException, IOException {

    GitConnection connection = connectToInitializedGitRepository(connectionFactory, repository);

    connection.lsRemote("https://bitbucket.org/exoinvitemain/privater.git");
  }
}
