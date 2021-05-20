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
package org.eclipse.che.api.factory.server.bitbucket;

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
public class BitbucketURLParserTest {

  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  /** Instance of component that will be tested. */
  private BitbucketURLParser bitbucketURLParser;

  @BeforeClass
  public void setUp() {
    bitbucketURLParser =
        new BitbucketURLParser(
            "https://bitbucket.2mcl.com,https://bbkt.com", devfileFilenamesProvider);
  }

  /** Check URLs are valid with regexp */
  @Test(dataProvider = "UrlsProvider")
  public void checkRegexp(String url) {
    assertTrue(bitbucketURLParser.isValid(url), "url " + url + " is invalid");
  }

  /** Compare parsing */
  @Test(dataProvider = "parsing")
  public void checkParsing(String url, String project, String repository, String branch) {
    BitbucketUrl bitbucketUrl = bitbucketURLParser.parse(url);

    assertEquals(bitbucketUrl.getProject(), project);
    assertEquals(bitbucketUrl.getRepository(), repository);
    assertEquals(bitbucketUrl.getBranch(), branch);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "The given url https://github.com/org/repo is not a valid Bitbucket server URL. Check either URL or server configuration.")
  public void shouldThrowExceptionWhenURLDintMatchAnyConfiguredServer() {
    bitbucketURLParser.parse("https://github.com/org/repo");
  }

  @DataProvider(name = "UrlsProvider")
  public Object[][] urls() {
    return new Object[][] {
      {"https://bitbucket.2mcl.com/scm/project/test1.git"},
      {"https://bitbucket.2mcl.com/scm/project/test1.git?at=branch"},
      {"https://bbkt.com/scm/project/test1.git"},
    };
  }

  @DataProvider(name = "parsing")
  public Object[][] expectedParsing() {
    return new Object[][] {
      {"https://bitbucket.2mcl.com/scm/project/test1.git", "project", "test1", null},
      {"https://bitbucket.2mcl.com/scm/project/test1.git?at=branch", "project", "test1", "branch"},
      {"https://bbkt.com/scm/project/test1.git?at=branch", "project", "test1", "branch"}
    };
  }
}
