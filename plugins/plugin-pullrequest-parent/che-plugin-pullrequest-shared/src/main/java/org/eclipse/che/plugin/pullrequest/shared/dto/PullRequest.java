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
