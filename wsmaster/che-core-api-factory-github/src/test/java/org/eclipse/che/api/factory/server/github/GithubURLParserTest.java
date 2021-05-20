/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
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

  @Mock private URLFetcher urlFetcher;
  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  /** Instance of component that will be tested. */
  @InjectMocks private GithubURLParser githubUrlParser;

  /** Check invalid url (not a github one) */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void invalidUrl() {
    githubUrlParser.parse("http://www.eclipse.org");
  }

  @BeforeMethod
  public void init() {
    lenient().when(urlFetcher.fetchSafely(any(String.class))).thenReturn("");
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

  /** Compare parsing */
  @Test(dataProvider = "parsingBadRepository")
  public void checkParsingBadRepositoryDoNotModifiesInitialInput(String url, String repository) {
    GithubUrl githubUrl = githubUrlParser.parse(url);
    assertEquals(githubUrl.getRepository(), repository);
  }

  @DataProvider(name = "UrlsProvider")
  public Object[][] urls() {
    return new Object[][] {
      {"https://github.com/eclipse/che"},
      {"https://github.com/eclipse/che123"},
      {"https://github.com/eclipse/che/"},
      {"https://github.com/eclipse/che/tree/4.2.x"},
      {"https://github.com/eclipse/che/tree/master/"},
      {"https://github.com/eclipse/che/tree/master/dashboard/"},
      {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git"},
      {"https://github.com/eclipse/che/tree/master/plugins/plugin-git/che-plugin-git-ext-git/"},
      {"https://github.com/eclipse/che/pull/11103"},
      {"https://github.com/eclipse/che.git"},
      {"https://github.com/eclipse/che.with.dots.git"},
      {"https://github.com/eclipse/che-with-hyphen"},
      {"https://github.com/eclipse/che-with-hyphen.git"}
    };
  }

  @DataProvider(name = "parsing")
  public Object[][] expectedParsing() {
    return new Object[][] {
      {"https://github.com/eclipse/che", "eclipse", "che", null, null},
      {"https://github.com/eclipse/che123", "eclipse", "che123", null, null},
      {"https://github.com/eclipse/che.git", "eclipse", "che", null, null},
      {"https://github.com/eclipse/che.with.dot.git", "eclipse", "che.with.dot", null, null},
      {"https://github.com/eclipse/-.git", "eclipse", "-", null, null},
      {"https://github.com/eclipse/-j.git", "eclipse", "-j", null, null},
      {"https://github.com/eclipse/-", "eclipse", "-", null, null},
      {"https://github.com/eclipse/che-with-hyphen", "eclipse", "che-with-hyphen", null, null},
      {"https://github.com/eclipse/che-with-hyphen.git", "eclipse", "che-with-hyphen", null, null},
      {"https://github.com/eclipse/che/", "eclipse", "che", null, null},
      {"https://github.com/eclipse/repositorygit", "eclipse", "repositorygit", null, null},
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

  @DataProvider(name = "parsingBadRepository")
  public Object[][] parsingBadRepository() {
    return new Object[][] {
      {"https://github.com/eclipse/che .git", "che .git"},
      {"https://github.com/eclipse/.git", ".git"},
      {"https://github.com/eclipse/myB@dR&pository.git", "myB@dR&pository.git"},
      {"https://github.com/eclipse/.", "."},
      {"https://github.com/eclipse/івапівап.git", "івапівап.git"},
      {"https://github.com/eclipse/ ", " "},
      {"https://github.com/eclipse/.", "."},
      {"https://github.com/eclipse/ .git", " .git"}
    };
  }

  /** Check Pull Request with data inside the repository */
  @Test
  public void checkPullRequestFromRepository() {

    String url = "https://github.com/eclipse/che/pull/11103";
    when(urlFetcher.fetchSafely(url))
        .thenReturn(
            "<div class=\"TableObject gh-header-meta\">\n"
                + "    <div class=\"TableObject-item\">\n"
                + "        <div class=\"State State--green\">\n"
                + "    <svg class=\"octicon octicon-git-pull-request\" viewBox=\"0 0 12 16\" version=\"1.1\" width=\"12\" height=\"16\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M11 11.28V5c-.03-.78-.34-1.47-.94-2.06C9.46 2.35 8.78 2.03 8 2H7V0L4 3l3 3V4h1c.27.02.48.11.69.31.21.2.3.42.31.69v6.28A1.993 1.993 0 0 0 10 15a1.993 1.993 0 0 0 1-3.72zm-1 2.92c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zM4 3c0-1.11-.89-2-2-2a1.993 1.993 0 0 0-1 3.72v6.56A1.993 1.993 0 0 0 2 15a1.993 1.993 0 0 0 1-3.72V4.72c.59-.34 1-.98 1-1.72zm-.8 10c0 .66-.55 1.2-1.2 1.2-.65 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2zM2 4.2C1.34 4.2.8 3.65.8 3c0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2z\"/></svg>\n"
                + "      Open\n"
                + "  </div>\n"
                + "\n"
                + "    </div>\n"
                + "    <div class=\"TableObject-item TableObject-item--primary\">\n"
                + "          <a class=\"author pull-header-username css-truncate css-truncate-target expandable\" data-hovercard-type=\"user\" data-hovercard-url=\"/hovercards?user_id=1651062\" data-octo-click=\"hovercard-link-click\" data-octo-dimensions=\"link_type:self\" href=\"/mshaposhnik\">mshaposhnik</a>\n"
                + "   wants to merge 1 commit into\n"
                + "\n"
                + "\n"
                + "\n"
                + "  <span title=\"eclipse/che:cleanup-e2e-theia\" class=\"commit-ref css-truncate user-select-contain expandable \"><span class=\"css-truncate-target\">master</span></span>\n"
                + "\n"
                + "from\n"
                + "\n"
                + "<span title=\"eclipse/che:cleanup-e2e-theia\" class=\"commit-ref css-truncate user-select-contain expandable head-ref\"><span class=\"css-truncate-target\">cleanup-e2e-theia</span></span>\n"
                + "\n"
                + "\n"
                + "  <relative-time datetime=\"2018-09-07T08:00:49Z\">Sep 7, 2018</relative-time>\n"
                + "\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</div>");
    GithubUrl githubUrl = githubUrlParser.parse(url);

    assertEquals(githubUrl.getUsername(), "eclipse");
    assertEquals(githubUrl.getRepository(), "che");
    assertEquals(githubUrl.getBranch(), "cleanup-e2e-theia");
  }

  /** Check Pull Request with data outside the repository (fork) */
  @Test
  public void checkPullRequestFromForkedRepository() {

    String url = "https://github.com/eclipse/che/pull/11103";
    when(urlFetcher.fetchSafely(url))
        .thenReturn(
            "<div class=\"TableObject gh-header-meta\">\n"
                + "    <div class=\"TableObject-item\">\n"
                + "        <div class=\"State State--green\">\n"
                + "          <svg class=\"octicon octicon-git-pull-request\" viewBox=\"0 0 12 16\" version=\"1.1\" width=\"12\" height=\"16\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M11 11.28V5c-.03-.78-.34-1.47-.94-2.06C9.46 2.35 8.78 2.03 8 2H7V0L4 3l3 3V4h1c.27.02.48.11.69.31.21.2.3.42.31.69v6.28A1.993 1.993 0 0 0 10 15a1.993 1.993 0 0 0 1-3.72zm-1 2.92c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zM4 3c0-1.11-.89-2-2-2a1.993 1.993 0 0 0-1 3.72v6.56A1.993 1.993 0 0 0 2 15a1.993 1.993 0 0 0 1-3.72V4.72c.59-.34 1-.98 1-1.72zm-.8 10c0 .66-.55 1.2-1.2 1.2-.65 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2zM2 4.2C1.34 4.2.8 3.65.8 3c0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2z\"/></svg>\n"
                + "          Open\n"
                + "        </div>\n"
                + "    </div>"
                + "    <div class=\"TableObject-item TableObject-item--primary\">\n"
                + "          <a class=\"author pull-header-username css-truncate css-truncate-target expandable\" data-hovercard-user-id=\"436777\" data-octo-click=\"hovercard-link-click\" data-octo-dimensions=\"link_type:self\" href=\"/benoitf\">benoitf</a>\n"
                + "  merged 1 commit into\n"
                + "from\n"
                + "\n"
                + "<span title=\"garagatyi/che:fixDeployment\" class=\"commit-ref css-truncate user-select-contain expandable head-ref\"><span class=\"css-truncate-target user\">garagatyi</span>:<span class=\"css-truncate-target\">fixDeployment</span></span>\n"
                + "\n");
    GithubUrl githubUrl = githubUrlParser.parse(url);

    assertEquals(githubUrl.getUsername(), "garagatyi");
    assertEquals(githubUrl.getRepository(), "che");
    assertEquals(githubUrl.getBranch(), "fixDeployment");
  }

  /** Check Pull Request is failing with Merged state */
  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*found Merged.*")
  public void checkPullRequestMergedState() {

    String url = "https://github.com/eclipse/che/pull/11103";
    when(urlFetcher.fetchSafely(url))
        .thenReturn(
            "\n"
                + "  <div class=\"TableObject gh-header-meta\">\n"
                + "    <div class=\"TableObject-item\">\n"
                + "        <div class=\"State State--purple\">\n"
                + "          <svg class=\"octicon octicon-git-merge\" viewBox=\"0 0 12 16\" version=\"1.1\" width=\"12\" height=\"16\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M10 7c-.73 0-1.38.41-1.73 1.02V8C7.22 7.98 6 7.64 5.14 6.98c-.75-.58-1.5-1.61-1.89-2.44A1.993 1.993 0 0 0 2 .99C.89.99 0 1.89 0 3a2 2 0 0 0 1 1.72v6.56c-.59.35-1 .99-1 1.72 0 1.11.89 2 2 2a1.993 1.993 0 0 0 1-3.72V7.67c.67.7 1.44 1.27 2.3 1.69.86.42 2.03.63 2.97.64v-.02c.36.61 1 1.02 1.73 1.02 1.11 0 2-.89 2-2 0-1.11-.89-2-2-2zm-6.8 6c0 .66-.55 1.2-1.2 1.2-.65 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2zM2 4.2C1.34 4.2.8 3.65.8 3c0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zm8 6c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2z\"/></svg>\n"
                + "          Merged\n"
                + "        </div>\n"
                + "    </div>\n"
                + "    <div class=\"TableObject-item TableObject-item--primary\">\n"
                + "          <a class=\"author pull-header-username css-truncate css-truncate-target expandable\" data-hovercard-user-id=\"436777\" data-octo-click=\"hovercard-link-click\" data-octo-dimensions=\"link_type:self\" href=\"/benoitf\">benoitf</a>\n"
                + "  merged 1 commit into\n"
                + "\n"
                + "\n"
                + "\n"
                + "  <span title=\"eclipse/che:master\" class=\"commit-ref css-truncate user-select-contain expandable \"><span class=\"css-truncate-target\">master</span></span>\n"
                + "\n"
                + "from\n"
                + "\n"
                + "<span title=\"eclipse/che:stack-theia\" class=\"commit-ref css-truncate user-select-contain expandable head-ref\"><span class=\"css-truncate-target\">stack-theia</span></span>\n"
                + "\n"
                + "\n"
                + "  <relative-time datetime=\"2018-09-06T09:29:33Z\">Sep 6, 2018</relative-time>\n"
                + "\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</div>\n");
    githubUrlParser.parse(url);
  }

  /** Check Pull Request is failing with Closed state */
  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = ".*found Closed.*")
  public void checkPullRequestClosedState() {

    String url = "https://github.com/eclipse/che/pull/11103";
    when(urlFetcher.fetchSafely(url))
        .thenReturn(
            "  <div class=\"TableObject gh-header-meta\">\n"
                + "    <div class=\"TableObject-item\">\n"
                + "        <div class=\"State State--red\">\n"
                + "          <svg class=\"octicon octicon-git-pull-request\" viewBox=\"0 0 12 16\" version=\"1.1\" width=\"12\" height=\"16\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M11 11.28V5c-.03-.78-.34-1.47-.94-2.06C9.46 2.35 8.78 2.03 8 2H7V0L4 3l3 3V4h1c.27.02.48.11.69.31.21.2.3.42.31.69v6.28A1.993 1.993 0 0 0 10 15a1.993 1.993 0 0 0 1-3.72zm-1 2.92c-.66 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2zM4 3c0-1.11-.89-2-2-2a1.993 1.993 0 0 0-1 3.72v6.56A1.993 1.993 0 0 0 2 15a1.993 1.993 0 0 0 1-3.72V4.72c.59-.34 1-.98 1-1.72zm-.8 10c0 .66-.55 1.2-1.2 1.2-.65 0-1.2-.55-1.2-1.2 0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2zM2 4.2C1.34 4.2.8 3.65.8 3c0-.65.55-1.2 1.2-1.2.65 0 1.2.55 1.2 1.2 0 .65-.55 1.2-1.2 1.2z\"/></svg>\n"
                + "          Closed\n"
                + "        </div>\n"
                + "    </div>\n"
                + "    <div class=\"TableObject-item TableObject-item--primary\">\n"
                + "          <a class=\"author pull-header-username css-truncate css-truncate-target expandable\" data-hovercard-user-id=\"436777\" data-octo-click=\"hovercard-link-click\" data-octo-dimensions=\"link_type:self\" href=\"/benoitf\">benoitf</a>\n"
                + "   wants to merge 1 commit into\n"
                + "\n"
                + "\n"
                + "\n"
                + "  <span title=\"eclipse/che:che6\" class=\"commit-ref css-truncate user-select-contain expandable base-ref\"><span class=\"css-truncate-target\">che6</span></span>\n"
                + "\n"
                + "  <div class=\"commit-ref-dropdown\">\n"
                + "    <details class=\"details-reset details-overlay select-menu js-select-menu commitish-suggester js-load-contents\" data-contents-url=\"/eclipse/che/pull/7923/show_partial?partial=pull_requests%2Fdescription_branches_dropdown\">\n"
                + "      <summary class=\"btn btn-sm select-menu-button branch\" title=\"Choose a base branch\" aria-haspopup=\"true\">\n"
                + "        <i>base:</i>\n"
                + "        <span class=\"js-select-button css-truncate css-truncate-target\" title=\"che6\">che6</span>\n"
                + "      </summary>\n"
                + "      <details-menu class=\"select-menu-modal position-absolute\" style=\"z-index: 90;\">\n"
                + "        <div class=\"js-select-menu-deferred-content\"></div>\n"
                + "        <div class=\"select-menu-loading-overlay anim-pulse\">\n"
                + "          <svg height=\"32\" class=\"octicon octicon-octoface\" viewBox=\"0 0 16 16\" version=\"1.1\" width=\"32\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M14.7 5.34c.13-.32.55-1.59-.13-3.31 0 0-1.05-.33-3.44 1.3-1-.28-2.07-.32-3.13-.32s-2.13.04-3.13.32c-2.39-1.64-3.44-1.3-3.44-1.3-.68 1.72-.26 2.99-.13 3.31C.49 6.21 0 7.33 0 8.69 0 13.84 3.33 15 7.98 15S16 13.84 16 8.69c0-1.36-.49-2.48-1.3-3.35zM8 14.02c-3.3 0-5.98-.15-5.98-3.35 0-.76.38-1.48 1.02-2.07 1.07-.98 2.9-.46 4.96-.46 2.07 0 3.88-.52 4.96.46.65.59 1.02 1.3 1.02 2.07 0 3.19-2.68 3.35-5.98 3.35zM5.49 9.01c-.66 0-1.2.8-1.2 1.78s.54 1.79 1.2 1.79c.66 0 1.2-.8 1.2-1.79s-.54-1.78-1.2-1.78zm5.02 0c-.66 0-1.2.79-1.2 1.78s.54 1.79 1.2 1.79c.66 0 1.2-.8 1.2-1.79s-.53-1.78-1.2-1.78z\"/></svg>\n"
                + "        </div>\n"
                + "      </details-menu>\n"
                + "    </details>\n"
                + "  </div>\n"
                + "\n"
                + "from\n"
                + "\n"
                + "<span title=\"eclipse/che:gwt-mockito\" class=\"commit-ref css-truncate user-select-contain expandable head-ref\"><span class=\"css-truncate-target\">gwt-mockito</span></span>\n"
                + "\n"
                + "\n"
                + "\n"
                + "    </div>");
    githubUrlParser.parse(url);
  }
}
