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

import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.shared.Remote;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class RemoteListTest {

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
  public void testRemoteList(GitConnectionFactory connectionFactory)
      throws ServerException, URISyntaxException, UnauthorizedException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    GitConnection connection2 = connectionFactory.getConnection(remoteRepo.getAbsolutePath());
    connection2.clone(
        CloneParams.create(connection.getWorkingDir().getAbsolutePath())
            .withWorkingDir(connection2.getWorkingDir().getAbsolutePath()));
    assertEquals(connection2.remoteList(null, false).size(), 1);
    // create new remote
    connection2.remoteAdd(RemoteAddParams.create("newremote", "newremote.url"));
    assertEquals(connection2.remoteList(null, false).size(), 2);
    // when
    List<Remote> one = connection2.remoteList("newremote", false);
    // then
    assertEquals(one.get(0).getUrl(), "newremote.url");
    assertEquals(one.size(), 1);
  }
}
