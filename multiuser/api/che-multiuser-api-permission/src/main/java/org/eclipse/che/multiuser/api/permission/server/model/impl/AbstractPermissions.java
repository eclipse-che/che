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
package org.eclipse.che.multiuser.api.permission.server.model.impl;

import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Represents user's permissions to access to some resources
 *
 * @author Sergii Leschenko
 */
@MappedSuperclass
public abstract class AbstractPermissions implements Permissions {

  @Id
  @GeneratedValue
  @Column(name = "id")
  protected String id;

  @Column(name = "user_id")
  protected String userId;

  @OneToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private UserImpl user;

  @Transient private String userIdHolder;

  public AbstractPermissions() {}

  public AbstractPermissions(Permissions permissions) {
    this(permissions.getUserId());
  }

  public AbstractPermissions(String userId) {
    this.userIdHolder = userId;
    this.userId = userId;
  }

  /** Returns used id */
  @Override
  public String getUserId() {
    return userIdHolder;
  }

  public void setUserId(String userId) {
    this.userIdHolder = userId;
  }

  /** Returns instance id */
  @Override
  public abstract String getInstanceId();

  /** Returns domain id */
  @Override
  public abstract String getDomainId();

  /** List of actions which user can perform for particular instance */
  @Override
  public abstract List<String> getActions();

  @PreUpdate
  @PrePersist
  private void prePersist() {
    if ("*".equals(userIdHolder)) {
      userId = null;
    } else {
      userId = userIdHolder;
    }
  }

  @PostLoad
  private void postLoad() {
    if (userId == null) {
      userIdHolder = "*";
    } else {
      userIdHolder = userId;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AbstractPermissions)) return false;
    final AbstractPermissions other = (AbstractPermissions) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(getUserId(), other.getUserId())
        && Objects.equals(getInstanceId(), other.getInstanceId())
        && Objects.equals(getDomainId(), other.getDomainId())
        && Objects.equals(getActions(), other.getActions());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(getUserId());
    hash = 31 * hash + Objects.hashCode(getInstanceId());
    hash = 31 * hash + Objects.hashCode(getDomainId());
    hash = 31 * hash + Objects.hashCode(getActions());
    return hash;
  }

  @Override
  public String toString() {
    return "AbstractPermissions{" + "id='" + id + '\'' + ", user=" + user + '}';
  }
}
