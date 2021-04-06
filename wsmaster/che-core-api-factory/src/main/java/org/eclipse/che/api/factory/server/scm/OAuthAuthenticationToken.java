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
package org.eclipse.che.api.factory.server.scm;

import java.util.Objects;
import org.eclipse.che.commons.env.EnvironmentContext;

public class OAuthAuthenticationToken extends ScmAuthenticationToken {

  public OAuthAuthenticationToken(String scmProviderUrl, String scmUserName, String token) {
    super(
        scmProviderUrl,
        EnvironmentContext.getCurrent().getSubject().getUserId(),
        scmUserName,
        null,
        null,
        null,
        token);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OAuthAuthenticationToken that = (OAuthAuthenticationToken) o;
    return Objects.equals(scmProviderUrl, that.scmProviderUrl)
        && Objects.equals(scmUserName, that.scmUserName)
        && Objects.equals(scmUserId, that.scmUserId)
        && Objects.equals(scmTokenName, that.scmTokenName)
        && Objects.equals(scmTokenId, that.scmTokenId)
        && Objects.equals(token, that.token)
        && Objects.equals(cheUserId, that.cheUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scmProviderUrl, scmUserName, scmUserId, scmTokenName, scmTokenId, token, cheUserId);
  }

  @Override
  public String toString() {
    return "OauthAuthenticationToken{"
        + "scmProviderUrl="
        + scmProviderUrl
        + ", scmUserName='"
        + scmUserName
        + '\''
        + ", scmUserId='"
        + scmUserId
        + '\''
        + ", scmTokenName='"
        + scmTokenName
        + '\''
        + ", scmTokenId='"
        + scmTokenId
        + '\''
        + ", token='"
        + token
        + '\''
        + ", cheUserId='"
        + cheUserId
        + '\''
        + '}';
  }
}
