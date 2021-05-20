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
package org.eclipse.che.api.user.server.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

/**
 * Describes JPA implementation of user's preferences.
 *
 * @author Anton Korneta
 * @author Yevhenii Voevodin
 */
@Entity(name = "Preference")
@Table(name = "preference")
public class PreferenceEntity {

  @Id
  @Column(name = "userid")
  private String userId;

  @ElementCollection
  @CollectionTable(
      name = "preference_preferences",
      joinColumns = @JoinColumn(name = "preference_userid"))
  @MapKeyColumn(name = "name")
  @Column(name = "value", columnDefinition = "TEXT")
  private Map<String, String> preferences;

  public PreferenceEntity() {}

  public PreferenceEntity(String userId, Map<String, String> preferences) {
    this.userId = userId;
    this.preferences = preferences;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Map<String, String> getPreferences() {
    if (preferences == null) {
      preferences = new HashMap<>();
    }
    return preferences;
  }

  public void setPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PreferenceEntity)) return false;

    final PreferenceEntity other = (PreferenceEntity) obj;

    return Objects.equals(userId, other.userId) && getPreferences().equals(other.getPreferences());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(userId);
    hash = 31 * hash + getPreferences().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "PreferenceEntity{" + "userId='" + userId + '\'' + ", preferences=" + preferences + '}';
  }
}
