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
package org.eclipse.che.plugin.github.shared;

import com.google.common.base.Strings;

public class GitHubUrlUtils {

  /**
   * normalize git@ and https:git@ urls
   *
   * @param gitUrl
   * @return
   */
  public static String toHttpsIfNeed(String gitUrl) {
    if (Strings.isNullOrEmpty(gitUrl)) {
      return gitUrl;
    }
    String gitRepoUrl = gitUrl;

    if (gitUrl.startsWith("git@")) {
      // normalize git@ and https:git@ urls
      gitRepoUrl = gitUrl.replaceFirst("git@", "https://");
      gitRepoUrl = gitRepoUrl.replaceFirst(".com:", ".com/");
    }
    if (gitRepoUrl.endsWith(".git")) {
      gitRepoUrl = gitRepoUrl.substring(0, gitRepoUrl.lastIndexOf(".git"));
    }
    return gitRepoUrl;
  }

  public static String getBlobUrl(String rootGitRepoUrl, String ref, String path) {
    return getBlobUrl(rootGitRepoUrl, ref, path, 0, 0);
  }

  public static String getBlobUrl(
      String rootGitRepoUrl, String ref, String path, int lineStart, int lineEnd) {
    return rootGitRepoUrl
        + "/blob/"
        + ref
        + "/"
        + path
        + (lineStart > 0 ? "#L" + lineStart : "")
        + (lineEnd > 0 ? "-L" + lineEnd : "");
  }

  public static String getTreeUrl(String rootGitRepoUrl, String ref, String path) {
    return rootGitRepoUrl + "/tree/" + ref + "/" + path;
  }

  public static boolean isGitHubUrl(String rootGitRepoUrl) {
    if (Strings.isNullOrEmpty(rootGitRepoUrl)) {
      return false;
    }
    return rootGitRepoUrl.startsWith("git@") || rootGitRepoUrl.startsWith("https://github.com");
  }
}
