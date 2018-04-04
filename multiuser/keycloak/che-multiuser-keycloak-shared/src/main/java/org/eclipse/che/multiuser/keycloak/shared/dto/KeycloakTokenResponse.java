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
