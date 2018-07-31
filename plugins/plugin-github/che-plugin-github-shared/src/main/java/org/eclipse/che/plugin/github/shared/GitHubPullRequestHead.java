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
public interface GitHubPullRequestHead {
  /**
   * Get pull request head label.
   *
   * @return {@link String} label
   */
  String getLabel();

  void setLabel(String label);

  GitHubPullRequestHead withLabel(String label);

  /**
   * Get pull request head ref.
   *
   * @return {@link String} ref
   */
  String getRef();

  void setRef(String ref);

  GitHubPullRequestHead withRef(String ref);

  /**
   * Get pull request head sha.
   *
   * @return {@link String} sha
   */
  String getSha();

  void setSha(String sha);

  GitHubPullRequestHead withSha(String sha);
}
