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

/**
 * Personal access token that can be used to authorise scm operations like api calls, git clone or
 * git push.
 */
public class PersonalAccessToken {

  private final String scmProviderUrl;
  private final String scmUserName;
  private final String scmUserId;
  private final String scmTokenName;
  private final String scmTokenId;
  private final String token;
  private final String cheUserId;

  public PersonalAccessToken(
      String scmProviderUrl,
      String cheUserId,
      String scmUserName,
      String scmUserId,
      String scmTokenName,
      String scmTokenId,
      String token) {
    this.scmProviderUrl = scmProviderUrl;
    this.scmUserName = scmUserName;
    this.scmUserId = scmUserId;
    this.scmTokenName = scmTokenName;
    this.scmTokenId = scmTokenId;
    this.token = token;
    this.cheUserId = cheUserId;
  }

  public PersonalAccessToken(String scmProviderUrl, String scmUserName, String token) {
    this(
        scmProviderUrl,
        EnvironmentContext.getCurrent().getSubject().getUserId(),
        scmUserName,
        null,
        null,
        null,
        token);
  }

  public String getScmProviderUrl() {
    return scmProviderUrl;
  }

  public String getScmTokenName() {
    return scmTokenName;
  }

  public String getScmTokenId() {
    return scmTokenId;
  }

  public String getScmUserName() {
    return scmUserName;
  }

  public String getScmUserId() {
    return scmUserId;
  }

  public String getToken() {
    return token;
  }

  public String getCheUserId() {
    return cheUserId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PersonalAccessToken that = (PersonalAccessToken) o;
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
    return "PersonalAccessToken{"
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
