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
package org.eclipse.che.plugin.github.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GitHubPullRequestCreationInput {
  /**
   * Get pull request title.
   *
   * @return {@link String} title
   */
  String getTitle();

  void setTitle(String title);

  GitHubPullRequestCreationInput withTitle(String title);

  /**
   * Get pull request head branch.
   *
   * @return {@link String} head
   */
  String getHead();

  void setHead(String head);

  GitHubPullRequestCreationInput withHead(String head);

  /**
   * Get pull request base branch.
   *
   * @return {@link String} base
   */
  String getBase();

  void setBase(String base);

  GitHubPullRequestCreationInput withBase(String base);

  /**
   * Get pull request body.
   *
   * @return {@link String} body
   */
  String getBody();

  void setBody(String body);

  GitHubPullRequestCreationInput withBody(String body);
}
