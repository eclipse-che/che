/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import org.eclipse.che.security.oauth.shared.User;

/** Represents GitHub user. */
public class GitHubUser implements User {
  private String name;
  private String company;
  private String email;

  @Override
  public final String getId() {
    return email;
  }

  @Override
  public final void setId(String id) {
    // JSON response from Github API contains key 'id' but it has different purpose.
    // Ignore calls of this method. Email address is used as user identifier.
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "GitHubUser{"
        + "id='"
        + getId()
        + '\''
        + ", name='"
        + name
        + '\''
        + ", company='"
        + company
        + '\''
        + ", email='"
        + email
        + '\''
        + '}';
  }
}
