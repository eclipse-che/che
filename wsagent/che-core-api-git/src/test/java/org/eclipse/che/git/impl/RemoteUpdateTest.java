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
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.RemoteAddParams;
import org.eclipse.che.api.git.params.RemoteUpdateParams;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class RemoteUpdateTest {

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
  public void testUpdateBranches(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addInitialRemote(connection);
    // when
    // change branch1 to branch2
    RemoteUpdateParams request =
        RemoteUpdateParams.create("newRemote").withBranches(singletonList("branch2"));
    connection.remoteUpdate(request);
    // then
    assertEquals(
        parseAllConfig(connection).get("remote.newRemote.fetch").get(0),
        "+refs/heads/branch2:refs/remotes/newRemote/branch2");
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testAddUrl(GitConnectionFactory connectionFactory) throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addInitialRemote(connection);
    // when
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withAddUrl(singletonList("new.com")));
    // then
    assertTrue(parseAllConfig(connection).get("remote.newRemote.url").contains("new.com"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testAddPushUrl(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addInitialRemote(connection);
    // when
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withAddPushUrl(singletonList("pushurl1")));
    // then
    assertTrue(parseAllConfig(connection).get("remote.newRemote.pushurl").contains("pushurl1"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testDeleteUrl(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    // add url
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addInitialRemote(connection);
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withAddUrl(singletonList("newUrl")));
    // when
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withRemoveUrl(singletonList("newUrl")));
    // then
    assertFalse(parseAllConfig(connection).containsKey("remote.newRemote.newUrl"));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = GitConnectionFactoryProvider.class
  )
  public void testDeletePushUrl(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    addInitialRemote(connection);
    // add push url
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withAddPushUrl(singletonList("pushurl")));

    // when
    connection.remoteUpdate(
        RemoteUpdateParams.create("newRemote").withRemovePushUrl(singletonList("pushurl")));
    // then
    assertNull(parseAllConfig(connection).get("remote.newRemote.pushurl"));
  }

  private Map<String, List<String>> parseAllConfig(GitConnection connection) throws GitException {
    Map<String, List<String>> config = new HashMap<>();
    List<String> lines = connection.getConfig().getList();
    for (String outLine : lines) {
      String[] pair = outLine.split("=");
      List<String> list = config.get(pair[0]);
      if (list == null) {
        list = new LinkedList<>();
      }
      if (pair.length == 2) {
        list.add(pair[1]);
      }
      config.put(pair[0], list);
    }
    return config;
  }

  private void addInitialRemote(GitConnection connection) throws GitException {
    RemoteAddParams params =
        RemoteAddParams.create("newRemote", "newRemote.url").withBranches(singletonList("branch1"));
    connection.remoteAdd(params);
  }
}
