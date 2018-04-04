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
