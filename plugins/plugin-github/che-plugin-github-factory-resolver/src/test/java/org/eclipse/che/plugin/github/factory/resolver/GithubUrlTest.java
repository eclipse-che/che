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
package org.eclipse.che.plugin.github.factory.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test of {@Link GithubUrl} Note: The parser is also testing the object
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GithubUrlTest {

  /** Parser used to create the url. */
  @InjectMocks private GithubURLParser githubUrlParser;

  /** Instance of the url created */
  private GithubUrl githubUrl;

  /** Setup objects/ */
  @BeforeClass
  protected void init() {
    this.githubUrl = this.githubUrlParser.parse("https://github.com/eclipse/che");
    assertNotNull(this.githubUrl);
  }

  /** Check when there is devfile in the repository */
  @Test
  public void checkDevfileLocation() {
    assertEquals(
        githubUrl.devfileFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/devfile.yaml");
  }

  /** Check when there is .factory.json file in the repository */
  @Test
  public void checkFactoryJsonFileLocation() {
    assertEquals(
        githubUrl.factoryFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/.factory.json");
  }

  /** Check the original repository */
  @Test
  public void checkRepositoryLocation() {
    assertEquals(githubUrl.repositoryLocation(), "https://github.com/eclipse/che");
  }
}
