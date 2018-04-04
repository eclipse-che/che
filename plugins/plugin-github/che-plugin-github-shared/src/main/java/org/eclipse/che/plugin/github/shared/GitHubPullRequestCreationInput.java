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
