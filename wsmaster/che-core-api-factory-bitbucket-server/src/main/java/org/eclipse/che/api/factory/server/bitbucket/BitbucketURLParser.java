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
package org.eclipse.che.api.factory.server.bitbucket;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Parser of String Bitbucket URLs and provide {@link BitbucketUrl} objects.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class BitbucketURLParser {

  private final URLFetcher urlFetcher;
  private final DevfileFilenamesProvider devfileFilenamesProvider;
  private static final String bitbucketUrlPatternTemplate =
      "^(?<host>%s)/scm/(?<project>[^/]++)/(?<repo>[^.]++).git(\\?at=)?(?<branch>[\\w\\d-_]*)";
  private Pattern bitbucketUrlPattern;

  @Inject
  public BitbucketURLParser(
      @Nullable @Named("bitbucket.server.endpoint") String bitbucketEndpoint,
      URLFetcher urlFetcher,
      DevfileFilenamesProvider devfileFilenamesProvider) {
    if (bitbucketEndpoint != null) {
      String trimmedEndpoint =
          bitbucketEndpoint.endsWith("/")
              ? bitbucketEndpoint.substring(0, bitbucketEndpoint.length() - 1)
              : bitbucketEndpoint;
      this.bitbucketUrlPattern =
          Pattern.compile(format(bitbucketUrlPatternTemplate, trimmedEndpoint));
    }
    this.urlFetcher = urlFetcher;
    this.devfileFilenamesProvider = devfileFilenamesProvider;
  }

  public boolean isValid(@NotNull String url) {
    return bitbucketUrlPattern != null && bitbucketUrlPattern.matcher(url).matches();
  }

  /**
   * Parses url-s like
   * https://bitbucket.apps.cluster-cb82.cb82.example.opentlc.com/scm/test/test1.git into
   * BitbucketUrl objects.
   */
  public BitbucketUrl parse(String url) {

    Matcher matcher = bitbucketUrlPattern.matcher(url);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format(
              "The given url %s is not a valid Bitbucket server URL. Check either URL or server configuration.",
              url));
    }

    String host = matcher.group("host");
    String project = matcher.group("project");
    String repoName = matcher.group("repo");
    String branch = matcher.group("branch");

    return new BitbucketUrl()
        .withHostName(host)
        .withProject(project)
        .withRepository(repoName)
        .withBranch(branch)
        .withFactoryFilename(".factory.json")
        .withDevfileFilenames(devfileFilenamesProvider.getConfiguredDevfileFilenames());
  }
}
