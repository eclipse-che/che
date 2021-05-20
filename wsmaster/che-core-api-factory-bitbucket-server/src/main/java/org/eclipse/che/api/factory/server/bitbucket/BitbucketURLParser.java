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

import static java.lang.String.format;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.StringUtils;

/**
 * Parser of String Bitbucket URLs and provide {@link BitbucketUrl} objects.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class BitbucketURLParser {

  private final DevfileFilenamesProvider devfileFilenamesProvider;
  private static final String bitbucketUrlPatternTemplate =
      "^(?<host>%s)/scm/(?<project>[^/]++)/(?<repo>[^.]++).git(\\?at=)?(?<branch>[\\w\\d-_]*)";
  private final List<Pattern> bitbucketUrlPatterns = new ArrayList<>();

  @Inject
  public BitbucketURLParser(
      @Nullable @Named("che.integration.bitbucket.server_endpoints") String bitbucketEndpoints,
      DevfileFilenamesProvider devfileFilenamesProvider) {
    this.devfileFilenamesProvider = devfileFilenamesProvider;
    if (bitbucketEndpoints != null) {
      for (String bitbucketEndpoint : Splitter.on(",").split(bitbucketEndpoints)) {
        String trimmedEndpoint = StringUtils.trimEnd(bitbucketEndpoint, '/');
        this.bitbucketUrlPatterns.add(
            Pattern.compile(format(bitbucketUrlPatternTemplate, trimmedEndpoint)));
      }
    }
  }

  public boolean isValid(@NotNull String url) {
    return !bitbucketUrlPatterns.isEmpty()
        && bitbucketUrlPatterns.stream().anyMatch(pattern -> pattern.matcher(url).matches());
  }

  /**
   * Parses url-s like
   * https://bitbucket.apps.cluster-cb82.cb82.example.opentlc.com/scm/test/test1.git into
   * BitbucketUrl objects.
   */
  public BitbucketUrl parse(String url) {

    if (bitbucketUrlPatterns.isEmpty()) {
      throw new UnsupportedOperationException(
          "The Bitbucket integration is not configured properly and cannot be used at this moment."
              + "Please refer to docs to check the Bitbucket integration instructions");
    }

    Matcher matcher =
        bitbucketUrlPatterns
            .stream()
            .map(pattern -> pattern.matcher(url))
            .filter(Matcher::matches)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        format(
                            "The given url %s is not a valid Bitbucket server URL. Check either URL or server configuration.",
                            url)));
    String host = matcher.group("host");
    String project = matcher.group("project");
    String repoName = matcher.group("repo");
    String branch = matcher.group("branch");

    return new BitbucketUrl()
        .withHostName(host)
        .withProject(project)
        .withRepository(repoName)
        .withBranch(branch)
        .withDevfileFilenames(devfileFilenamesProvider.getConfiguredDevfileFilenames());
  }
}
