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
package org.eclipse.che.multiuser.keycloak.shared.dto;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@DTO
public interface KeycloakTokenResponse {

  @JsonFieldName("access_token")
  String getAccessToken();

  void setAccessToken(String accessToken);

  KeycloakTokenResponse withAccessToken(String accessToken);

  String getTokenType();

  void setTokenType(String tokenType);

  KeycloakTokenResponse withTokenType(String tokenType);

  String getScope();

  void setScope(String scope);

  KeycloakTokenResponse withScope(String scope);
}
