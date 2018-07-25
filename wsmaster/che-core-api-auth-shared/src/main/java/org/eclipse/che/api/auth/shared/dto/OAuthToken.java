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
package org.eclipse.che.api.auth.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * OAuth token.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface OAuthToken {
  /** Get OAuth token */
  String getToken();

  /** Set OAuth token */
  void setToken(String token);

  OAuthToken withToken(String token);

  /** Get OAuth scope */
  String getScope();

  /** Set OAuth scope */
  void setScope(String scope);

  OAuthToken withScope(String scope);
}
