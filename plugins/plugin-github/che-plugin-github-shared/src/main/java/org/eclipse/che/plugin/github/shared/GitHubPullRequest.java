/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.github.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GitHubPullRequest {
  /**
   * Get pull request id.
   *
   * @return {@link String} id
   */
  String getId();

  void setId(String id);

  GitHubPullRequest withId(String id);

  /**
   * Get pull request URL.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(String url);

  GitHubPullRequest withUrl(String url);

  /**
   * Get pull request html URL.
   *
   * @return {@link String} html_url
   */
  String getHtmlUrl();

  void setHtmlUrl(String htmlUrl);

  GitHubPullRequest withHtmlUrl(String htmlUrl);

  /**
   * Get pull request number.
   *
   * @return {@link String} number
   */
  String getNumber();

  void setNumber(String number);

  GitHubPullRequest withNumber(String number);

  /**
   * Get pull request state.
   *
   * @return {@link String} state
   */
  String getState();

  void setState(String state);

  GitHubPullRequest withState(String state);

  /**
   * Get pull request head.
   *
   * @return {@link GitHubPullRequestHead} head
   */
  GitHubPullRequestHead getHead();

  void setHead(GitHubPullRequestHead head);

  GitHubPullRequest withHead(GitHubPullRequestHead head);

  /**
   * Tells if the pull request is merged.
   *
   * @return true iff the pull request is merged
   */
  boolean getMerged();

  void setMerged(boolean merged);

  GitHubPullRequest withMerged(boolean merged);

  /**
   * Tells which user merged the pull request (if it was).
   *
   * @return the user
   */
  GitHubUser getMergedBy();

  void setMergedBy(GitHubUser user);

  GitHubPullRequest withMergedBy(GitHubUser user);

  /**
   * Tells if the pull request is mergeable.
   *
   * @return true iff the merge can be done automatically
   */
  boolean getMergeable();

  void setMergeable(boolean mergeable);

  GitHubPullRequest withMergeable(boolean mergeable);

  /**
   * Get pull request body.
   *
   * @return {@link String} body
   */
  String getBody();

  void setBody(String body);

  GitHubPullRequest withBody(String body);

  /**
   * Get pull request title.
   *
   * @return {@link String} title
   */
  String getTitle();

  void setTitle(String title);

  GitHubPullRequest withTitle(String title);
}
