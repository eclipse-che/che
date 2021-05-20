/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GitlabUrlParserTest {

  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  /** Instance of component that will be tested. */
  private GitlabUrlParser gitlabUrlParser;

  @BeforeClass
  public void setUp() {
    gitlabUrlParser =
        new GitlabUrlParser("https://gitlab1.com,https://gitlab.foo.xxx", devfileFilenamesProvider);
  }

  /** Check URLs are valid with regexp */
  @Test(dataProvider = "UrlsProvider")
  public void checkRegexp(String url) {
    assertTrue(gitlabUrlParser.isValid(url), "url " + url + " is invalid");
  }

  /** Compare parsing */
  @Test(dataProvider = "parsing")
  public void checkParsing(
      String url, String user, String project, String repository, String branch, String subfolder) {
    GitlabUrl gitlabUrl = gitlabUrlParser.parse(url);

    assertEquals(gitlabUrl.getUsername(), user);
    assertEquals(gitlabUrl.getProject(), project);
    assertEquals(gitlabUrl.getRepository(), repository);
    assertEquals(gitlabUrl.getBranch(), branch);
    assertEquals(gitlabUrl.getSubfolder(), subfolder);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "The given url https://github.com/org/repo is not a valid Gitlab server URL. Check either URL or server configuration.")
  public void shouldThrowExceptionWhenURLDintMatchAnyConfiguredServer() {
    gitlabUrlParser.parse("https://github.com/org/repo");
  }

  @DataProvider(name = "UrlsProvider")
  public Object[][] urls() {
    return new Object[][] {
      {"https://gitlab1.com/user/project/test1.git"},
      {"https://gitlab1.com/user/project1.git"},
      {"https://gitlab.foo.xxx/scm/project/test1.git"},
      {"https://gitlab1.com/user/project/"},
      {"https://gitlab1.com/user/project/repo/"},
      {"https://gitlab1.com/user/project/-/tree/master/"},
      {"https://gitlab1.com/user/project/repo/-/tree/master/subfolder"}
    };
  }

  @DataProvider(name = "parsing")
  public Object[][] expectedParsing() {
    return new Object[][] {
      {"https://gitlab1.com/user/project1.git", "user", "project1", null, null, null},
      {"https://gitlab1.com/user/project/test1.git", "user", "project", "test1", null, null},
      {"https://gitlab1.com/user/project/", "user", "project", null, null, null},
      {"https://gitlab1.com/user/project/repo/", "user", "project", "repo", null, null},
      {"https://gitlab1.com/user/project/-/tree/master/", "user", "project", null, "master", null},
      {
        "https://gitlab1.com/user/project/repo/-/tree/foo/subfolder",
        "user",
        "project",
        "repo",
        "foo",
        "subfolder"
      }
    };
  }
}
