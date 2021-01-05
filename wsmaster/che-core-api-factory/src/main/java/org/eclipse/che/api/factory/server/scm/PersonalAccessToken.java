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

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Personal access token that can be used to authorise scm operations like api calls, git clone or
 * git push.
 */
public class PersonalAccessToken {

  private final URL scmProviderUrl;
  private final String scmUserName;
  private final String scmUserId;
  private final String token;
  private final String cheUserId;

  public PersonalAccessToken(
      String scmProviderUrl, String cheUserId, String scmUserName, String scmUserId, String token) {
    try {
      this.scmProviderUrl = new URL(scmProviderUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.scmUserName = scmUserName;
    this.scmUserId = scmUserId;
    this.token = token;
    this.cheUserId = cheUserId;
  }

  public PersonalAccessToken(String scmProviderUrl, String scmUserName, String token) {
    this(
        scmProviderUrl,
        EnvironmentContext.getCurrent().getSubject().getUserId(),
        scmUserName,
        null,
        token);
  }

  public String getScmProviderUrl() {
    return scmProviderUrl.toString();
  }

  public String getScmProviderHost() {
    return scmProviderUrl.getHost();
  }

  public String getScmProviderProtocol() {
    return scmProviderUrl.getProtocol();
  }

  public URL getScmProviderURL() {
    return scmProviderUrl;
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
