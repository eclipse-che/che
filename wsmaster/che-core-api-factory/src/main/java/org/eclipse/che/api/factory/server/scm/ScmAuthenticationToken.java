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

public class ScmAuthenticationToken {

  protected final String scmProviderUrl;
  protected final String scmUserName;
  protected final String scmUserId;
  protected final String scmTokenName;
  protected final String scmTokenId;
  protected final String token;
  protected final String cheUserId;

  public ScmAuthenticationToken(
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
}
