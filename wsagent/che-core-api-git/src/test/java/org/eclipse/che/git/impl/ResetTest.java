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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.git.impl.GitTestUtil.CONTENT;
import static org.eclipse.che.git.impl.GitTestUtil.addFile;
import static org.eclipse.che.git.impl.GitTestUtil.cleanupTestRepo;
import static org.eclipse.che.git.impl.GitTestUtil.connectToGitRepositoryWithContent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.params.AddParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.params.LogParams;
import org.eclipse.che.api.git.params.ResetParams;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class ResetTest {

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
  public void testResetHard(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    File aaa = addFile(connection, "aaa", "aaa\n");
    FileOutputStream fos = new FileOutputStream(new File(connection.getWorkingDir(), "README.txt"));
    fos.write("MODIFIED\n".getBytes());
    fos.flush();
    fos.close();
    String initMessage = connection.log(LogParams.create()).getCommits().get(0).getMessage();
    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file"));
    // when
    connection.reset(ResetParams.create("HEAD^", ResetRequest.ResetType.HARD));
    // then
    assertEquals(connection.log(LogParams.create()).getCommits().get(0).getMessage(), initMessage);
    assertFalse(aaa.exists());
    assertTrue(connection.status(emptyList()).isClean());
    assertEquals(
        CONTENT,
        Files.toString(new File(connection.getWorkingDir(), "README.txt"), Charsets.UTF_8));
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testResetSoft(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    File aaa = addFile(connection, "aaa", "aaa\n");
    FileOutputStream fos = new FileOutputStream(new File(connection.getWorkingDir(), "README.txt"));
    fos.write("MODIFIED\n".getBytes());
    fos.flush();
    fos.close();
    String initMessage = connection.log(LogParams.create()).getCommits().get(0).getMessage();
    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file"));
    // when
    connection.reset(ResetParams.create("HEAD^", ResetRequest.ResetType.SOFT));
    // then
    assertEquals(connection.log(LogParams.create()).getCommits().get(0).getMessage(), initMessage);
    assertTrue(aaa.exists());
    assertEquals(connection.status(emptyList()).getAdded().get(0), "aaa");
    assertEquals(connection.status(emptyList()).getChanged().get(0), "README.txt");
    assertEquals(
        Files.toString(new File(connection.getWorkingDir(), "README.txt"), Charsets.UTF_8),
        "MODIFIED\n");
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testResetMixed(GitConnectionFactory connectionFactory) throws Exception {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    File aaa = addFile(connection, "aaa", "aaa\n");
    FileOutputStream fos = new FileOutputStream(new File(connection.getWorkingDir(), "README.txt"));
    fos.write("MODIFIED\n".getBytes());
    fos.flush();
    fos.close();
    String initMessage = connection.log(LogParams.create()).getCommits().get(0).getMessage();
    connection.add(AddParams.create(new ArrayList<>(singletonList("."))));
    connection.commit(CommitParams.create("add file"));
    // when
    ResetRequest resetRequest = newDto(ResetRequest.class).withCommit("HEAD^");
    connection.reset(ResetParams.create("HEAD^", ResetRequest.ResetType.MIXED));
    // then
    assertEquals(connection.log(LogParams.create()).getCommits().get(0).getMessage(), initMessage);
    assertTrue(aaa.exists());
    assertEquals(connection.status(emptyList()).getUntracked().get(0), "aaa");
    assertEquals(connection.status(emptyList()).getModified().get(0), "README.txt");
    assertEquals(
        Files.toString(new File(connection.getWorkingDir(), "README.txt"), Charsets.UTF_8),
        "MODIFIED\n");
  }
}
