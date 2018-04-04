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
  @InjectMocks private GithubURLParserImpl githubUrlParser;

  /** Instance of the url created */
  private GithubUrl githubUrl;

  /** Setup objects/ */
  @BeforeClass
  protected void init() {
    this.githubUrl = this.githubUrlParser.parse("https://github.com/eclipse/che");
    assertNotNull(this.githubUrl);
  }

  /** Check when there is .codenvy.dockerfile in the repository */
  @Test
  public void checkDockerfileLocation() {
    assertEquals(
        githubUrl.dockerFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/.factory.dockerfile");
  }

  /** Check when there is .codenvy.json file in the repository */
  @Test
  public void checkCodenvyFactoryJsonFileLocation() {
    assertEquals(
        githubUrl.factoryJsonFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/.factory.json");
  }

  /** Check the original repository */
  @Test
  public void checkRepositoryLocation() {
    assertEquals(githubUrl.repositoryLocation(), "https://github.com/eclipse/che");
  }
}
