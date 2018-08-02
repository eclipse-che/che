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
package org.eclipse.che.plugin.pullrequest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PullRequest {
  String getId();

  PullRequest withId(String id);

  int getVersion();

  PullRequest withVersion(int version);

  String getTitle();

  PullRequest withTitle(String title);

  String getUrl();

  PullRequest withUrl(String url);

  String getHtmlUrl();

  PullRequest withHtmlUrl(String htmlUrl);

  String getNumber();

  PullRequest withNumber(String number);

  String getState();

  PullRequest withState(String state);

  String getHeadRef();

  PullRequest withHeadRef(String head);

  String getDescription();

  PullRequest withDescription(String description);
}
