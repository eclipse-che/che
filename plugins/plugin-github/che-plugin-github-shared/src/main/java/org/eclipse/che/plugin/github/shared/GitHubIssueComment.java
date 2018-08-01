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

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface GitHubIssueComment {
  /**
   * Get comment id.
   *
   * @return {@link String} id
   */
  String getId();

  void setId(String id);

  GitHubIssueComment withId(String id);

  /**
   * Get comment URL.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(String url);

  GitHubIssueComment withUrl(String url);

  /**
   * Get comment body.
   *
   * @return {@link String} body
   */
  String getBody();

  void setBody(String body);

  GitHubIssueComment withBody(String body);
}
