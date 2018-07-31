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
package org.eclipse.che.api.git;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link GitUrlUtils}.
 *
 * @author Igor Vinokur
 */
public class GitUrlUtilsTest {

  @DataProvider(name = "validGitSshUrlsProvider")
  public static Object[][] validGitSshUrls() {
    return new Object[][] {
      {"ssh://user@host.xz:port/path/to/repo.git"},
      {"ssh://user@host.xz/path/to/repo.git"},
      {"ssh://host.xz:port/path/to/repo.git"},
      {"ssh://host.xz/path/to/repo.git"},
      {"ssh://user@host.xz/path/to/repo.git"},
      {"ssh://host.xz/path/to/repo.git"},
      {"ssh://user@host.xz/~user/path/to/repo.git"},
      {"ssh://host.xz/~user/path/to/repo.git"},
      {"ssh://user@host.xz/~/path/to/repo.git"},
      {"ssh://host.xz/~/path/to/repo.git"},
      {"user@host.xz:/path/to/repo.git"},
      {"user@host.xz:path/to/repo.git"},
      {"git://host.xz/path/to/repo.git"},
      {"git://host.xz/~user/path/to/repo.git"},
      {"git@vcsProvider.com:user/test.git"},
      {"ssh@vcsProvider.com:user/test.git"}
    };
  }

  @DataProvider(name = "otherGitUrlsProvider")
  public static Object[][] otherGitUrls() {
    return new Object[][] {
      {"http://host.xz/path/to/repo.git"}, {"https://host.xz/path/to/repo.git"}
    };
  }

  @Test(dataProvider = "validGitSshUrls")
  public void shouldReturnTrueIfGivenUrlIsSsh(String url) throws Exception {
    assertTrue(GitUrlUtils.isSSH(url));
  }

  @Test(dataProvider = "otherGitUrls")
  public void shouldReturnFalseIfGivenUrlIsNotSsh(String url) throws Exception {
    assertFalse(GitUrlUtils.isSSH(url));
  }
}
