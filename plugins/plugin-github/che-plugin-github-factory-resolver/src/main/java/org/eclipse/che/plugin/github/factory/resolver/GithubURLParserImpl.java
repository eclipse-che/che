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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;

/**
 * Parser of String Github URLs and provide {@link GithubUrl} objects.
 *
 * @author Florent Benoit
 */
public class GithubURLParserImpl implements GithubURLParser {

  /**
   * Regexp to find repository details (repository name, project name and branch and subfolder)
   * Examples of valid URLs are in the test class.
   */
  protected static final Pattern GITHUB_PATTERN =
      Pattern.compile(
          "^(?:http)(?:s)?(?:\\:\\/\\/)github.com/(?<repoUser>[^/]++)/(?<repoName>[^/]++)(?:/tree/(?<branchName>[^/]++)(?:/(?<subFolder>.*))?)?$");

  @Override
  public boolean isValid(@NotNull String url) {
    return GITHUB_PATTERN.matcher(url).matches();
  }

  @Override
  public GithubUrl parse(String url) {
    // Apply github url to the regexp
    Matcher matcher = GITHUB_PATTERN.matcher(url);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format(
              "The given github url %s is not a valid URL github url. It should start with https://github.com/<user>/<repo>",
              url));
    }

    return new GithubUrl()
        .withUsername(matcher.group("repoUser"))
        .withRepository(matcher.group("repoName"))
        .withBranch(matcher.group("branchName"))
        .withSubfolder(matcher.group("subFolder"))
        .withDockerfileFilename(".factory.dockerfile")
        .withFactoryFilename(".factory.json");
  }
}
