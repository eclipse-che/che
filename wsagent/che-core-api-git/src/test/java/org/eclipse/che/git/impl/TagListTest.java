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

import static org.eclipse.che.git.impl.GitTestUtil.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class TagListTest {

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
  public void testTagList(GitConnectionFactory connectionFactory) throws GitException, IOException {
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

    connection.tagCreate(TagCreateParams.create("first-tag"));
    connection.tagCreate(TagCreateParams.create("first-tag-other"));
    connection.tagCreate(TagCreateParams.create("second-tag"));

    assertTags(connection.tagList(null), "first-tag", "first-tag-other", "second-tag");
  }

  @Test(
    dataProvider = "GitConnectionFactory",
    dataProviderClass = org.eclipse.che.git.impl.GitConnectionFactoryProvider.class
  )
  public void testTagListPattern(GitConnectionFactory connectionFactory)
      throws GitException, IOException {
    GitConnection connection = connectToGitRepositoryWithContent(connectionFactory, repository);

    connection.tagCreate(TagCreateParams.create("first-tag"));
    connection.tagCreate(TagCreateParams.create("first-tag-other"));
    connection.tagCreate(TagCreateParams.create("second-tag"));

    assertTags(connection.tagList("first*"), "first-tag", "first-tag-other");
  }

  protected void assertTags(List<Tag> tagList, String... expNames) {
    assertEquals(tagList.size(), expNames.length);
    List<String> names = new ArrayList<>(tagList.size());
    for (Tag t : tagList) names.add(t.getName());
    for (String name : expNames)
      assertTrue(names.contains(name), "Expected tag " + name + " not found in result. ");
  }
}
