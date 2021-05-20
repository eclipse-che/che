/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabOauthTokenInfo {

  private long resource_owner_id;
  private String[] scope;
  private long expires_in;
  private long created_at;

  public long getResource_owner_id() {
    return resource_owner_id;
  }

  public void setResource_owner_id(long resource_owner_id) {
    this.resource_owner_id = resource_owner_id;
  }

  public String[] getScope() {
    return scope;
  }

  public void setScope(String[] scope) {
    this.scope = scope;
  }

  public long getExpires_in() {
    return expires_in;
  }

  public void setExpires_in(long expires_in) {
    this.expires_in = expires_in;
  }

  public long getCreated_at() {
    return created_at;
  }

  public void setCreated_at(long created_at) {
    this.created_at = created_at;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GitlabOauthTokenInfo info = (GitlabOauthTokenInfo) o;
    return resource_owner_id == info.resource_owner_id
        && expires_in == info.expires_in
        && created_at == info.created_at
        && Arrays.equals(scope, info.scope);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(resource_owner_id, expires_in, created_at);
    result = 31 * result + Arrays.hashCode(scope);
    return result;
  }

  @Override
  public String toString() {
    return "GitlabOauthTokenInfo{"
        + "resource_owner_id="
        + resource_owner_id
        + ", scope="
        + Arrays.toString(scope)
        + ", expires_in="
        + expires_in
        + ", created_at="
        + created_at
        + '}';
  }
}
