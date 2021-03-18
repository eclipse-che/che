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
package org.eclipse.che.api.factory.server.gitlab;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test of {@Link GitlabUrl} Note: The parser is also testing the {@code GitlabURLParser} object
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GitlabUrlTest {

  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  /** Parser used to create the url. */
  private GitlabUrlParser gitlabUrlParser;

  /** Setup objects/ */
  @BeforeClass
  protected void init() {
    when(devfileFilenamesProvider.getConfiguredDevfileFilenames())
        .thenReturn(Arrays.asList("devfile.yaml", "foo.bar"));
    gitlabUrlParser = new GitlabUrlParser("https://gitlab.net", devfileFilenamesProvider);
  }

  /** Check when there is devfile in the repository */
  @Test(dataProvider = "urlsProvider")
  public void checkDevfileLocation(String repoUrl, String fileUrl) {
    lenient()
        .when(devfileFilenamesProvider.getConfiguredDevfileFilenames())
        .thenReturn(Arrays.asList("devfile.yaml", "foo.bar"));

    GitlabUrl gitlabUrl = gitlabUrlParser.parse(repoUrl);
    assertEquals(gitlabUrl.devfileFileLocations().size(), 2);
    Iterator<DevfileLocation> iterator = gitlabUrl.devfileFileLocations().iterator();
    assertEquals(iterator.next().location(), fileUrl + "devfile.yaml");

    assertEquals(iterator.next().location(), fileUrl + "foo.bar");
  }

  @DataProvider
  public static Object[][] urlsProvider() {
    return new Object[][] {
      {"https://gitlab.net/eclipse/che.git", "https://gitlab.net/eclipse/che/-/raw/master/"},
      {
        "https://gitlab.net/eclipse/fooproj/che.git",
        "https://gitlab.net/eclipse/fooproj/che/-/raw/master/"
      },
      {
        "https://gitlab.net/eclipse/fooproj/-/tree/master/",
        "https://gitlab.net/eclipse/fooproj/-/raw/master/"
      },
      {
        "https://gitlab.net/eclipse/fooproj/che/-/tree/foobranch/",
        "https://gitlab.net/eclipse/fooproj/che/-/raw/foobranch/"
      },
      {
        "https://gitlab.net/eclipse/fooproj/che/-/tree/foobranch/subfolder",
        "https://gitlab.net/eclipse/fooproj/che/-/raw/foobranch/subfolder/"
      },
    };
  }

  /** Check the original repository */
  @Test(dataProvider = "repoProvider")
  public void checkRepositoryLocation(String rawUrl, String repoUrl) {
    GitlabUrl gitlabUrl = gitlabUrlParser.parse(rawUrl);
    assertEquals(gitlabUrl.repositoryLocation(), repoUrl);
  }

  @DataProvider
  public static Object[][] repoProvider() {
    return new Object[][] {
      {"https://gitlab.net/eclipse/che.git", "https://gitlab.net/eclipse/che.git"},
      {"https://gitlab.net/eclipse/foo/che.git", "https://gitlab.net/eclipse/foo/che.git"},
      {
        "https://gitlab.net/eclipse/fooproj/che/-/tree/master/",
        "https://gitlab.net/eclipse/fooproj/che.git"
      }
    };
  }
}
