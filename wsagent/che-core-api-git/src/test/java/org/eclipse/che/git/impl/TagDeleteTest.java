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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.che.api.git.GitConnection;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.TagCreateParams;
import org.eclipse.che.api.git.shared.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Eugene Voevodin */
public class TagDeleteTest {

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
      dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class)
  public void testDeleteTag(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    // given
    // create tags
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

    connection.tagCreate(TagCreateParams.create("first-tag"));
    connection.tagCreate(TagCreateParams.create("second-tag"));

    List<Tag> tags = connection.tagList(null);
    assertTrue(tagExists(tags, "first-tag"));
    assertTrue(tagExists(tags, "second-tag"));
    // when
    // delete first-tag
    connection.tagDelete("first-tag");
    // then
    // check not exists more
    tags = connection.tagList(null);
    assertFalse(tagExists(tags, "first-tag"));
    assertTrue(tagExists(tags, "second-tag"));
  }

  private boolean tagExists(List<Tag> list, String name) {
    for (Tag tag : list) {
      if (tag.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
