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

import static org.eclipse.che.git.impl.GitTestUtil.*;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.TagCreateParams;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class TagCreateTest {

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
  public void testCreateTag(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);
    int beforeTagCount = connection.tagList(null).size();
    // when
    connection.tagCreate(TagCreateParams.create("v1").withMessage("first version"));
    // then
    int afterTagCount = connection.tagList(null).size();
    assertEquals(afterTagCount, beforeTagCount + 1);
  }
}
