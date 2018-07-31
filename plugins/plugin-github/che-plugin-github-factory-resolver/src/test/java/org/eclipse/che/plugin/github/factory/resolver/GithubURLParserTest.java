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
package org.eclipse.che.plugin.github.factory.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Validate operations performed by the Github parser
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GithubURLParserTest {

  /** Instance of component that will be tested. */
  @InjectMocks private GithubURLParserImpl githubUrlParser;

  /** Check invalid url (not a github one) */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidUrl() {
    githubUrlParser.parse("http://www.eclipse.org");
  }

  /** Check URLs are valid with regexp */
  @Test(dataProvider = "UrlsProvider")
  public void checkRegexp(String url) {
    assertTrue(githubUrlParser.isValid(url), "url " + url + " is invalid");
  }

  /** Compare parsing */
  @Test(dataProvider = "parsing")
  public void checkParsing(
      String url, String username, String repository, String branch, String subfolder) {
    GithubUrl githubUrl = githubUrlParser.parse(url);

    assertEquals(githubUrl.getUsername(), username);
    assertEquals(githubUrl.getRepository(), repository);
    assertEquals(githubUrl.getBranch(), branch);
    assertEquals(githubUrl.getSubfolder(), subfolder);
  }

  @DataProvider(name = "UrlsProvider")
  public Object[][] urls() {
    return new Object[][] {
      {"https://github.com/eclipse/che"},
      {"https://github.com/eclipse/che/tree/4.2.x"},
      {"https://github.com/eclipse/che/tree/master/"},
      {"https://github.com/eclipse/che/tree/master/dashboard/"},
      {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git"},
      {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git/"}
    };
  }

  @DataProvider(name = "parsing")
  public Object[][] expectedParsing() {
    return new Object[][] {
      {"https://github.com/eclipse/che", "eclipse", "che", "master", null},
      {"https://github.com/eclipse/che/tree/4.2.x", "eclipse", "che", "4.2.x", null},
      {
        "https://github.com/eclipse/che/tree/master/dashboard/",
        "eclipse",
        "che",
        "master",
        "dashboard/"
      },
      {
        "https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git",
        "eclipse",
        "che",
        "master",
        "plugins/plugin-git/che-plugin-git-ext-git"
      }
    };
  }
}
