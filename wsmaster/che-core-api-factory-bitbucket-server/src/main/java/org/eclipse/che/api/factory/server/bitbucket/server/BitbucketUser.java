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
package org.eclipse.che.api.factory.server.bitbucket.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

@JsonIgnoreProperties(value = "links")
public class BitbucketUser {

  private String displayName;
  private String name;
  private long id;
  private String type;
  private boolean isActive;
  private String slug;
  private String emailAddress;

  public BitbucketUser(
      String displayName,
      String name,
      long id,
      String type,
      boolean isActive,
      String slug,
      String emailAddress) {
    this.displayName = displayName;
    this.name = name;
    this.id = id;
    this.type = type;
    this.isActive = isActive;
    this.slug = slug;
    this.emailAddress = emailAddress;
  }

  public BitbucketUser() {}

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BitbucketUser that = (BitbucketUser) o;
    return id == that.id
        && isActive == that.isActive
        && Objects.equals(displayName, that.displayName)
        && Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(slug, that.slug)
        && Objects.equals(emailAddress, that.emailAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, name, id, type, isActive, slug, emailAddress);
  }

  @Override
  public String toString() {
    return "BitbucketUser{"
        + "displayName='"
        + displayName
        + '\''
        + ", name='"
        + name
        + '\''
        + ", id="
        + id
        + ", type='"
        + type
        + '\''
        + ", isActive="
        + isActive
        + ", slug='"
        + slug
        + '\''
        + ", emailAddress='"
        + emailAddress
        + '\''
        + '}';
  }
}
