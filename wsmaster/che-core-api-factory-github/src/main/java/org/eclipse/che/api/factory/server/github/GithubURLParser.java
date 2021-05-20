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
package org.eclipse.che.api.factory.server.github;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;

/**
 * Parser of String Github URLs and provide {@link GithubUrl} objects.
 *
 * @author Florent Benoit
 */
@Singleton
public class GithubURLParser {

  /** Fetcher to grab PR data */
  private final URLFetcher urlFetcher;

  private final DevfileFilenamesProvider devfileFilenamesProvider;

  @Inject
  public GithubURLParser(URLFetcher urlFetcher, DevfileFilenamesProvider devfileFilenamesProvider) {
    this.urlFetcher = urlFetcher;
    this.devfileFilenamesProvider = devfileFilenamesProvider;
  }

  /**
   * Regexp to find repository details (repository name, project name and branch and subfolder)
   * Examples of valid URLs are in the test class.
   */
  protected static final Pattern GITHUB_PATTERN =
      Pattern.compile(
          "^(?:http)(?:s)?(?:\\:\\/\\/)github.com/(?<repoUser>[^/]++)/(?<repoName>[^/]++)((/)|(?:/tree/(?<branchName>[^/]++)(?:/(?<subFolder>.*))?)|(/pull/(?<pullRequestId>[^/]++)))?$");

  /** Regexp to find repository and branch name from PR link */
  protected static final Pattern PR_DATA_PATTERN =
      Pattern.compile(
          ".*<div class=\"State[\\s|\\S]+(?<prState>Closed|Open|Merged)[\\s|\\S]+<\\/div>[\\s|\\S]+into[\\s]+(from[\\s]*)*<span title=\"(?<prRepoUser>[^\\\\/]+)\\/(?<prRepoName>[^\\:]+):(?<prBranch>[^\\\"]+).*",
          Pattern.DOTALL);

  public boolean isValid(@NotNull String url) {
    return GITHUB_PATTERN.matcher(url).matches();
  }

  public GithubUrl parse(String url) {
    // Apply github url to the regexp
    Matcher matcher = GITHUB_PATTERN.matcher(url);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format(
              "The given github url %s is not a valid URL github url. It should start with https://github.com/<user>/<repo>",
              url));
    }

    String repoUser = matcher.group("repoUser");
    String repoName = matcher.group("repoName");
    if (repoName.matches("^[\\w-][\\w.-]*?\\.git$")) {
      repoName = repoName.substring(0, repoName.length() - 4);
    }
    String branchName = matcher.group("branchName");

    String pullRequestId = matcher.group("pullRequestId");
    if (pullRequestId != null) {
      // there is a Pull Request ID, analyze content to extract repository and branch to use
      String prData = this.urlFetcher.fetchSafely(url);
      Matcher prMatcher = PR_DATA_PATTERN.matcher(prData);
      if (prMatcher.matches()) {
        String prState = prMatcher.group("prState");
        if (!"open".equalsIgnoreCase(prState)) {
          throw new IllegalArgumentException(
              String.format(
                  "The given Pull Request url %s is not Opened, (found %s), thus it can't be opened as branch may have been removed.",
                  url, prState));
        }
        repoUser = prMatcher.group("prRepoUser");
        repoName = prMatcher.group("prRepoName");
        branchName = prMatcher.group("prBranch");
      } else {
        throw new IllegalArgumentException(
            String.format(
                "The given Pull Request github url %s is not a valid Pull Request URL github url. Unable to extract the data",
                url));
      }
    }

    return new GithubUrl()
        .withUsername(repoUser)
        .withRepository(repoName)
        .withBranch(branchName)
        .withSubfolder(matcher.group("subFolder"))
        .withDevfileFilenames(devfileFilenamesProvider.getConfiguredDevfileFilenames());
  }
}
