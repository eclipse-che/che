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
public interface HostUser {
  String getId();

  HostUser withId(String id);

  String getName();

  HostUser withName(String name);

  String getLogin();

  HostUser withLogin(String login);

  String getUrl();

  HostUser withUrl(String url);
}
