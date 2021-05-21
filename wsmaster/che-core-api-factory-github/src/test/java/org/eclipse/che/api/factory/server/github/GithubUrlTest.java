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
package org.eclipse.che.api.factory.server.github;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  /** Parser used to create the url. */
  @InjectMocks private GithubURLParser githubUrlParser;

  /** Instance of the url created */
  private GithubUrl githubUrl;

  /** Setup objects/ */
  @BeforeClass
  protected void init() {
    when(devfileFilenamesProvider.getConfiguredDevfileFilenames())
        .thenReturn(Arrays.asList("devfile.yaml", "foo.bar"));
    this.githubUrl = this.githubUrlParser.parse("https://github.com/eclipse/che");
    assertNotNull(this.githubUrl);
  }

  /** Check when there is devfile in the repository */
  @Test
  public void checkDevfileLocation() {
    lenient()
        .when(devfileFilenamesProvider.getConfiguredDevfileFilenames())
        .thenReturn(Arrays.asList("devfile.yaml", "foo.bar"));

    assertEquals(githubUrl.devfileFileLocations().size(), 2);
    Iterator<DevfileLocation> iterator = githubUrl.devfileFileLocations().iterator();
    assertEquals(
        iterator.next().location(),
        "https://raw.githubusercontent.com/eclipse/che/HEAD/devfile.yaml");

    assertEquals(
        iterator.next().location(), "https://raw.githubusercontent.com/eclipse/che/HEAD/foo.bar");
  }

  /** Check the original repository */
  @Test
  public void checkRepositoryLocation() {
    assertEquals(githubUrl.repositoryLocation(), "https://github.com/eclipse/che.git");
  }
}
