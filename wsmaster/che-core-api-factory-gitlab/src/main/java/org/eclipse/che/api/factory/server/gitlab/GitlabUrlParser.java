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

import static java.lang.String.format;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.StringUtils;

/**
 * Parser of String Gitlab URLs and provide {@link GitlabUrl} objects.
 *
 * @author Max Shaposhnyk
 */
public class GitlabUrlParser {

  private final DevfileFilenamesProvider devfileFilenamesProvider;
  private static final List<String> gitlabUrlPatternTemplates =
      List.of(
          "^(?<host>%s)/(?<user>[^/]++)/(?<project>[^.]++).git",
          "^(?<host>%s)/(?<user>[^/]++)/(?<project>[^/]++)/(?<repository>[^.]++).git",
          "^(?<host>%s)/(?<user>[^/]++)/(?<project>[^/]++)(/)?(?<repository>[^/]++)?/-/tree/(?<branch>[^/]++)(/)?(?<subfolder>[^/]++)?");
  private final List<Pattern> gitlabUrlPatterns = new ArrayList<>();

  @Inject
  public GitlabUrlParser(
      @Nullable @Named("che.integration.gitlab.server_endpoints") String bitbucketEndpoints,
      DevfileFilenamesProvider devfileFilenamesProvider) {
    this.devfileFilenamesProvider = devfileFilenamesProvider;
    if (bitbucketEndpoints != null) {
      for (String bitbucketEndpoint : Splitter.on(",").split(bitbucketEndpoints)) {
        String trimmedEndpoint = StringUtils.trimEnd(bitbucketEndpoint, '/');
        for (String gitlabUrlPatternTemplate : gitlabUrlPatternTemplates) {
          this.gitlabUrlPatterns.add(
              Pattern.compile(format(gitlabUrlPatternTemplate, trimmedEndpoint)));
        }
      }
    }
  }

  public boolean isValid(@NotNull String url) {
    return !gitlabUrlPatterns.isEmpty()
        && gitlabUrlPatterns.stream().anyMatch(pattern -> pattern.matcher(url).matches());
  }

  /**
   * Parses url-s like https://gitlab.apps.cluster-327a.327a.example.opentlc.com/root/proj1.git into
   * {@link GitlabUrl} objects.
   */
  public GitlabUrl parse(String url) {

    if (gitlabUrlPatterns.isEmpty()) {
      throw new UnsupportedOperationException(
          "The gitlab integration is not configured properly and cannot be used at this moment."
              + "Please refer to docs to check the Gitlab integration instructions");
    }

    Matcher matcher =
        gitlabUrlPatterns
            .stream()
            .map(pattern -> pattern.matcher(url))
            .filter(Matcher::matches)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        format(
                            "The given url %s is not a valid Gitlab server URL. Check either URL or server configuration.",
                            url)));
    String host = matcher.group("host");
    String userName = matcher.group("user");
    String project = matcher.group("project");
    String repository = null;
    String branch = null;
    String subfolder = null;
    try {
      repository = matcher.group("repository");
    } catch (IllegalArgumentException e) {
      // ok no such group
    }
    try {
      branch = matcher.group("branch");
    } catch (IllegalArgumentException e) {
      // ok no such group
    }
    try {
      subfolder = matcher.group("subfolder");
    } catch (IllegalArgumentException e) {
      // ok no such group
    }

    return new GitlabUrl()
        .withHostName(host)
        .withUsername(userName)
        .withProject(project)
        .withRepository(repository)
        .withBranch(branch)
        .withSubfolder(subfolder)
        .withDevfileFilenames(devfileFilenamesProvider.getConfiguredDevfileFilenames());
  }
}
